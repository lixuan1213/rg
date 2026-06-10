import { useState } from 'react';
import {
  Card, Form, Input, InputNumber, Select, Button, Descriptions, Tag, Space, Divider, message, Spin, Alert, Modal,
} from 'antd';
import type { ChargingMode, CarState, ChargingStateResponse } from '../api/types';
import {
  submitChargingRequest,
  startCharging,
  endCharging,
  queryChargingState,
  modifyAmount,
  modifyMode,
} from '../api/chargingApi';

const carStateLabel: Record<CarState, string> = {
  WAITING: '等待中', QUEUED: '已排队', CHARGING: '充电中', PENDING_UNPLUG: '待拔枪', COMPLETED: '已完成', CANCELLED: '已取消',
};
const carStateColor: Record<CarState, string> = {
  WAITING: 'default', QUEUED: 'warning', CHARGING: 'processing', PENDING_UNPLUG: 'orange', COMPLETED: 'success', CANCELLED: 'error',
};
const modeLabel: Record<ChargingMode, string> = { FAST: '快充', SLOW: '慢充' };

export default function ChargingOperationPage() {
  const [state, setState] = useState<ChargingStateResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [activeCarId, setActiveCarId] = useState<string | null>(null);
  const [submitForm] = Form.useForm();
  const [queryForm] = Form.useForm();
  const [amountModalOpen, setAmountModalOpen] = useState(false);
  const [amountForm] = Form.useForm();
  const [modeModalOpen, setModeModalOpen] = useState(false);
  const [modeForm] = Form.useForm();

  const fetchState = async (carId: string) => {
    try {
      const res = await queryChargingState(carId);
      setState(res.data.data);
      setActiveCarId(carId);
    } catch {
      setState(null);
    }
  };

  const handleSubmitRequest = async (values: { carId: string; requestAmount: number; requestMode: ChargingMode }) => {
    setLoading(true);
    try {
      await submitChargingRequest(values);
      message.success('充电请求提交成功');
      queryForm.setFieldsValue({ carId: values.carId });
      await fetchState(values.carId);
    } catch { /* handled */ }
    setLoading(false);
  };

  const handleQuery = async (values: { carId: string }) => {
    setLoading(true);
    await fetchState(values.carId);
    setLoading(false);
  };

  const handleStart = async () => {
    if (!state?.chargePileNum || !activeCarId) return;
    setLoading(true);
    try {
      const res = await startCharging({ carId: activeCarId, chargePileNum: state.chargePileNum });
      if (res.data.data?.result === 0) {
        message.success('开始充电');
        await fetchState(activeCarId);
      } else { message.error('开始充电失败'); }
    } catch { /* handled */ }
    setLoading(false);
  };

  const handleEnd = async () => {
    if (!state?.chargePileNum || !activeCarId) return;
    setLoading(true);
    try {
      const res = await endCharging({ carId: activeCarId, chargingPileNum: state.chargePileNum });
      if (res.data.data?.result === 0) {
        message.success(`结束充电，账单号 #${res.data.data?.billId}`);
        await fetchState(activeCarId);
      } else { message.error('结束充电失败'); }
    } catch { /* handled */ }
    setLoading(false);
  };

  const handleModifyAmount = async (values: { amount: number }) => {
    if (!activeCarId) return;
    setLoading(true);
    try {
      const res = await modifyAmount({ carId: activeCarId, amount: values.amount });
      if (res.data.data?.result === 0) {
        message.success('电量修改成功');
        setAmountModalOpen(false);
        await fetchState(activeCarId);
      } else { message.error('修改失败'); }
    } catch { /* handled */ }
    setLoading(false);
  };

  const handleModifyMode = async (values: { mode: ChargingMode }) => {
    if (!activeCarId) return;
    setLoading(true);
    try {
      const res = await modifyMode({ carId: activeCarId, mode: values.mode });
      if (res.data.data?.result === 0) {
        message.success('模式修改成功，已重新调度');
        setModeModalOpen(false);
        await fetchState(activeCarId);
      } else { message.error('修改失败'); }
    } catch { /* handled */ }
    setLoading(false);
  };

  const canStart = state?.carState === 'QUEUED' && !!state?.chargePileNum;
  const canEnd = state && (state.carState === 'CHARGING' || state.carState === 'PENDING_UNPLUG') && !!state?.chargePileNum;
  const canModify = state && (state.carState === 'WAITING' || state.carState === 'QUEUED');

  return (
    <Spin spinning={loading}>
      <Card title="提交充电请求">
        <Form layout="inline" form={submitForm} onFinish={handleSubmitRequest}>
          <Form.Item name="carId" label="车辆ID" rules={[{ required: true }]}>
            <Input placeholder="如 CAR001" style={{ width: 120 }} />
          </Form.Item>
          <Form.Item name="requestAmount" label="请求电量(kWh)" rules={[{ required: true, type: 'number', min: 0.1 }]}>
            <InputNumber step={0.1} min={0.1} style={{ width: 120 }} />
          </Form.Item>
          <Form.Item name="requestMode" label="模式" rules={[{ required: true }]}>
            <Select style={{ width: 100 }} options={[{ label: '快充', value: 'FAST' }, { label: '慢充', value: 'SLOW' }]} />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit">提交</Button>
          </Form.Item>
        </Form>
      </Card>

      <Divider />

      <Card title="查询或选择车辆操作">
        <Form layout="inline" form={queryForm} onFinish={handleQuery}>
          <Form.Item name="carId" label="车辆ID" rules={[{ required: true }]}>
            <Input placeholder="输入 carId 查询" style={{ width: 140 }} />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit">查询</Button>
          </Form.Item>
        </Form>

        {state && (
          <>
            <Descriptions bordered style={{ marginTop: 16 }} column={4} size="small">
              <Descriptions.Item label="车辆ID">{state.carId}</Descriptions.Item>
              <Descriptions.Item label="模式">{modeLabel[state.requestMode]}</Descriptions.Item>
              <Descriptions.Item label="状态">
                <Tag color={carStateColor[state.carState]}>{carStateLabel[state.carState]}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="分配桩">
                {state.chargePileNum ? <Tag color="blue">{state.chargePileNum}</Tag> : <Tag>未分配</Tag>}
              </Descriptions.Item>
              <Descriptions.Item label="请求电量(kWh)">{state.requestAmount}</Descriptions.Item>
              <Descriptions.Item label="已充电(kWh)">{state.chargedAmount.toFixed(1)}</Descriptions.Item>
              <Descriptions.Item label="开始时间">{state.startTime ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="预计剩余(分)">{state.estimatedRemainingMinutes ?? '-'}</Descriptions.Item>
            </Descriptions>

            {state.reminderMessage && (
              <Alert type="warning" message={state.reminderMessage} showIcon style={{ marginTop: 8 }} />
            )}

            <Divider plain>操作</Divider>

            <Space size="middle" wrap>
              <Button type="primary" size="large" disabled={!canStart} onClick={handleStart}>
                开始充电
              </Button>
              <Button danger type="primary" size="large" disabled={!canEnd} onClick={handleEnd}>
                {state.carState === 'PENDING_UNPLUG' ? '拔枪结束' : '结束充电'}
              </Button>
              <Button disabled={!canModify} onClick={() => { amountForm.setFieldsValue({ amount: state.requestAmount }); setAmountModalOpen(true); }}>
                修改电量
              </Button>
              <Button disabled={!canModify} onClick={() => { modeForm.setFieldsValue({ mode: state.requestMode }); setModeModalOpen(true); }}>
                修改模式
              </Button>
            </Space>

            {!canStart && !canEnd && state && (
              <Alert style={{ marginTop: 12 }}
                message={state.carState === 'WAITING' ? '车辆正在等候区，待有空闲充电桩时将自动分配'
                  : state.carState === 'COMPLETED' ? '充电已完成，车位已释放'
                  : state.carState === 'PENDING_UNPLUG' ? '充电已满，请拔枪结束'
                  : '当前状态无法操作'}
                type="info" showIcon />
            )}
          </>
        )}
      </Card>

      <Modal title="修改请求电量" open={amountModalOpen} onCancel={() => setAmountModalOpen(false)} footer={null}>
        <Form form={amountForm} layout="vertical" onFinish={handleModifyAmount}>
          <Form.Item name="amount" label="新电量(kWh)" rules={[{ required: true, type: 'number', min: 0.1 }]}>
            <InputNumber step={0.1} min={0.1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item><Button type="primary" htmlType="submit">确认修改</Button></Form.Item>
        </Form>
      </Modal>

      <Modal title="修改充电模式" open={modeModalOpen} onCancel={() => setModeModalOpen(false)} footer={null}>
        <Form form={modeForm} layout="vertical" onFinish={handleModifyMode}>
          <Form.Item name="mode" label="新模式" rules={[{ required: true }]}>
            <Select options={[{ label: '快充', value: 'FAST' }, { label: '慢充', value: 'SLOW' }]} />
          </Form.Item>
          <Form.Item><Button type="primary" htmlType="submit">确认修改</Button></Form.Item>
        </Form>
      </Modal>
    </Spin>
  );
}
