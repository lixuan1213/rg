import { useEffect, useState } from 'react';
import {
  Card, Table, Button, Tag, Row, Col, Modal, Form, InputNumber, Select, Space, message, Spin,
} from 'antd';
import {
  queryAllPileStates,
  powerOn,
  powerOff,
  startChargingPile,
  reportFault,
  recoverFault,
  setParameters,
} from '../api/pileApi';
import type {
  PileStateResponse,
  PileWorkingState,
  BillingRuleDto,
  SchedulingStrategy,
  TimePeriod,
} from '../api/types';

const stateColorMap: Record<PileWorkingState, string> = {
  OFF: 'default', IDLE: 'success', CHARGING: 'processing', WAITING_UNPLUG: 'warning', FAULT: 'error',
};
const stateLabelMap: Record<PileWorkingState, string> = {
  OFF: '关机', IDLE: '空闲', CHARGING: '充电中', WAITING_UNPLUG: '待拔枪', FAULT: '故障',
};

export default function PileManagementPage() {
  const [piles, setPiles] = useState<PileStateResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [paramModalOpen, setParamModalOpen] = useState(false);
  const [actionLoading, setActionLoading] = useState<string | null>(null);

  const fetchPiles = async () => {
    setLoading(true);
    try {
      const res = await queryAllPileStates();
      setPiles(res.data.data);
    } catch { /* handled */ }
    setLoading(false);
  };

  useEffect(() => {
    fetchPiles();
  }, []);

  const doAction = async (pileId: string, action: () => Promise<unknown>, label: string) => {
    setActionLoading(pileId);
    try {
      await action();
      message.success(`${label}成功`);
      fetchPiles();
    } catch { /* handled */ }
    setActionLoading(null);
  };

  const handleSetParams = async (values: {
    priceRules: { timePeriod: TimePeriod; startHour: number; endHour: number; electricityPrice: number; serviceFeeRate: number }[];
    schedulingStrategy: SchedulingStrategy | null;
  }) => {
    try {
      await setParameters(values as { priceRules: BillingRuleDto[]; schedulingStrategy: SchedulingStrategy | null });
      message.success('设置成功');
      setParamModalOpen(false);
    } catch { /* handled */ }
  };

  const columns = [
    { title: '桩编号', dataIndex: 'pileId', key: 'pileId' },
    {
      title: '工作状态',
      dataIndex: 'workingState',
      key: 'workingState',
      render: (s: PileWorkingState) => <Tag color={stateColorMap[s]}>{stateLabelMap[s]}</Tag>,
    },
    { title: '累计充电次数', dataIndex: 'totalChargeNum', key: 'totalChargeNum' },
    { title: '累计时长(分)', dataIndex: 'totalChargeTime', key: 'totalChargeTime' },
    {
      title: '累计容量(kWh)',
      dataIndex: 'totalCapacity',
      key: 'totalCapacity',
      render: (v: number) => v.toFixed(1),
    },
    {
      title: '操作',
      key: 'actions',
      render: (_: unknown, record: PileStateResponse) => {
        const { pileId, workingState } = record;
        const btnLoading = actionLoading === pileId;
        return (
          <Space wrap>
            <Button
              size="small"
              onClick={() => doAction(pileId, () => powerOn(pileId), '开机')}
              loading={btnLoading}
              disabled={workingState !== 'OFF'}
            >
              开机
            </Button>
            <Button
              size="small"
              onClick={() => doAction(pileId, () => powerOff(pileId), '关机')}
              loading={btnLoading}
              disabled={workingState === 'OFF' || workingState === 'FAULT'}
            >
              关机
            </Button>
            <Button
              size="small"
              type="primary"
              onClick={() => doAction(pileId, () => startChargingPile(pileId), '启动')}
              loading={btnLoading}
              disabled={workingState !== 'IDLE'}
            >
              启动
            </Button>
            <Button
              size="small"
              danger
              onClick={() => doAction(pileId, () => reportFault(pileId), '上报故障')}
              loading={btnLoading}
              disabled={workingState === 'OFF' || workingState === 'FAULT'}
            >
              故障
            </Button>
            <Button
              size="small"
              onClick={() => doAction(pileId, () => recoverFault(pileId), '恢复')}
              loading={btnLoading}
              disabled={workingState !== 'FAULT'}
            >
              恢复
            </Button>
          </Space>
        );
      },
    },
  ];

  return (
    <Spin spinning={loading}>
      <Card
        title="充电桩管理"
        extra={
          <Button type="primary" onClick={() => setParamModalOpen(true)}>
            调度策略设置
          </Button>
        }
      >
        <Table dataSource={piles} rowKey="pileId" columns={columns} pagination={false} />
      </Card>

      <Modal
        title="调度策略设置"
        open={paramModalOpen}
        onCancel={() => setParamModalOpen(false)}
        footer={null}
        width={700}
      >
        <Form
          layout="vertical"
          onFinish={handleSetParams}
          initialValues={{
            schedulingStrategy: 'TIME_ORDER' as SchedulingStrategy,
            priceRules: [
              { timePeriod: 'PEAK' as TimePeriod, startHour: 8, endHour: 22, electricityPrice: 1.5, serviceFeeRate: 0.1 },
              { timePeriod: 'VALLEY' as TimePeriod, startHour: 22, endHour: 8, electricityPrice: 0.7, serviceFeeRate: 0.1 },
            ],
          }}
        >
          <Form.Item name="schedulingStrategy" label="调度策略">
            <Select
              options={[
                { label: '时间优先 (先到先服务)', value: 'TIME_ORDER' },
                { label: '优先级优先', value: 'PRIORITY' },
              ]}
            />
          </Form.Item>

          <Form.List name="priceRules">
            {(fields, { add, remove }) => (
              <>
                {fields.map(({ key, name, ...restField }) => (
                  <Card
                    key={key}
                    size="small"
                    title={`计费规则 ${name + 1}`}
                    style={{ marginBottom: 8 }}
                    extra={<Button size="small" danger onClick={() => remove(name)}>删除</Button>}
                  >
                    <Row gutter={8}>
                      <Col span={8}>
                        <Form.Item {...restField} name={[name, 'timePeriod']} label="时段类型" rules={[{ required: true }]}>
                          <Select
                            options={[
                              { label: '高峰', value: 'PEAK' },
                              { label: '平段', value: 'NORMAL' },
                              { label: '低谷', value: 'VALLEY' },
                            ]}
                          />
                        </Form.Item>
                      </Col>
                      <Col span={4}>
                        <Form.Item {...restField} name={[name, 'startHour']} label="开始小时" rules={[{ required: true }]}>
                          <InputNumber min={0} max={23} style={{ width: '100%' }} />
                        </Form.Item>
                      </Col>
                      <Col span={4}>
                        <Form.Item {...restField} name={[name, 'endHour']} label="结束小时" rules={[{ required: true }]}>
                          <InputNumber min={0} max={24} style={{ width: '100%' }} />
                        </Form.Item>
                      </Col>
                      <Col span={4}>
                        <Form.Item {...restField} name={[name, 'electricityPrice']} label={`电价(元/kWh)`} rules={[{ required: true }]}>
                          <InputNumber min={0} step={0.1} style={{ width: '100%' }} />
                        </Form.Item>
                      </Col>
                      <Col span={4}>
                        <Form.Item {...restField} name={[name, 'serviceFeeRate']} label="服务费率" rules={[{ required: true }]}>
                          <InputNumber min={0} max={1} step={0.01} style={{ width: '100%' }} />
                        </Form.Item>
                      </Col>
                    </Row>
                  </Card>
                ))}
                <Button type="dashed" onClick={() => add()} block>
                  + 添加计费规则
                </Button>
              </>
            )}
          </Form.List>

          <Form.Item style={{ marginTop: 16 }}>
            <Button type="primary" htmlType="submit">保存设置</Button>
          </Form.Item>
        </Form>
      </Modal>
    </Spin>
  );
}
