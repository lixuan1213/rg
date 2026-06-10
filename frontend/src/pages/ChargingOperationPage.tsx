import { useState } from 'react';
import {
  Card, Form, Input, InputNumber, Select, Button, Descriptions, Tag, Space, Divider, message, Spin, Alert,
} from 'antd';
import type { ChargingMode, CarState, ChargingStateResponse } from '../api/types';
import {
  submitChargingRequest,
  startCharging,
  endCharging,
  queryChargingState,
} from '../api/chargingApi';

const carStateLabel: Record<CarState, string> = {
  WAITING: '等待中',
  QUEUED: '已排队',
  CHARGING: '充电中',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
};
const carStateColor: Record<CarState, string> = {
  WAITING: 'default',
  QUEUED: 'warning',
  CHARGING: 'processing',
  COMPLETED: 'success',
  CANCELLED: 'error',
};
const modeLabel: Record<ChargingMode, string> = { FAST: '快充', SLOW: '慢充' };

export default function ChargingOperationPage() {
  const [state, setState] = useState<ChargingStateResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [activeCarId, setActiveCarId] = useState<string | null>(null);
  const [submitForm] = Form.useForm();
  const [queryForm] = Form.useForm();

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
      } else {
        message.error('开始充电失败');
      }
    } catch { /* handled */ }
    setLoading(false);
  };

  const handleEnd = async () => {
    if (!state?.chargePileNum || !activeCarId) return;
    setLoading(true);
    try {
      const res = await endCharging({ carId: activeCarId, chargingPileNum: state.chargePileNum });
      if (res.data.data?.result === 0) {
        message.success('结束充电，账单已生成');
        await fetchState(activeCarId);
      } else {
        message.error('结束充电失败');
      }
    } catch { /* handled */ }
    setLoading(false);
  };

  const canStart = state?.carState === 'QUEUED' && !!state?.chargePileNum;
  const canEnd = state?.carState === 'CHARGING' && !!state?.chargePileNum;

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

            <Divider plain>操作</Divider>

            <Space size="middle">
              <Button
                type="primary"
                size="large"
                disabled={!canStart}
                onClick={handleStart}
              >
                开始充电
              </Button>
              <Button
                danger
                type="primary"
                size="large"
                disabled={!canEnd}
                onClick={handleEnd}
              >
                结束充电
              </Button>
            </Space>

            {!canStart && !canEnd && state && (
              <Alert
                style={{ marginTop: 12 }}
                message={
                  state.carState === 'WAITING'
                    ? '车辆正在等候区，待有空闲充电桩时将自动分配'
                    : state.carState === 'COMPLETED'
                    ? '充电已完成'
                    : '当前状态无法操作'
                }
                type="info"
                showIcon
              />
            )}
          </>
        )}
      </Card>
    </Spin>
  );
}
