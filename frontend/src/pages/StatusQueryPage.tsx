import { useState, useEffect } from 'react';
import { Card, Input, Button, Descriptions, Tag, Space, Spin } from 'antd';
import { useAuth } from '../components/AuthContext';
import { queryCarState, queryChargingState } from '../api/chargingApi';
import type { CarState, ChargingStateResponse } from '../api/types';

const carStateLabel: Record<CarState, string> = {
  WAITING: '等待中', QUEUED: '已排队', CHARGING: '充电中', PENDING_UNPLUG: '待拔枪', COMPLETED: '已完成', CANCELLED: '已取消',
};
const carStateColor: Record<CarState, string> = {
  WAITING: 'default', QUEUED: 'warning', CHARGING: 'processing', PENDING_UNPLUG: 'orange', COMPLETED: 'success', CANCELLED: 'error',
};

export default function StatusQueryPage() {
  const { userId } = useAuth();
  const [carId, setCarId] = useState(userId || '');
  const [carState, setCarState] = useState<{ carState: CarState; queueNum: number | null; carNumberBeforePosition: number; requestTime: string } | null>(null);
  const [chargeState, setChargeState] = useState<ChargingStateResponse | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => { if (userId) setCarId(userId); }, [userId]);

  const handleQueryCarState = async () => {
    if (!carId.trim()) return;
    setLoading(true);
    try { setCarState((await queryCarState(carId.trim())).data.data); } catch { setCarState(null); }
    setLoading(false);
  };

  const handleQueryChargeState = async () => {
    if (!carId.trim()) return;
    setLoading(true);
    try { setChargeState((await queryChargingState(carId.trim())).data.data); } catch { setChargeState(null); }
    setLoading(false);
  };

  return (
    <Spin spinning={loading}>
      <Card title="车辆排队状态">
        <Space>
          <Input placeholder="车辆ID" value={carId} onChange={e => setCarId(e.target.value)} style={{ width: 140 }} readOnly={!!userId} />
          <Button type="primary" onClick={handleQueryCarState}>查询</Button>
        </Space>
        {carState && (
          <Descriptions bordered style={{ marginTop: 12 }} column={2} size="small">
            <Descriptions.Item label="状态"><Tag color={carStateColor[carState.carState]}>{carStateLabel[carState.carState]}</Tag></Descriptions.Item>
            <Descriptions.Item label="排队号">{carState.queueNum ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="前方车辆">{carState.carNumberBeforePosition}</Descriptions.Item>
            <Descriptions.Item label="请求时间">{carState.requestTime}</Descriptions.Item>
          </Descriptions>
        )}
      </Card>

      <Card title="充电详细状态" style={{ marginTop: 16 }}>
        <Space>
          <Input placeholder="车辆ID" value={carId} onChange={e => setCarId(e.target.value)} style={{ width: 140 }} readOnly={!!userId} />
          <Button type="primary" onClick={handleQueryChargeState}>查询</Button>
        </Space>
        {chargeState && (
          <Descriptions bordered style={{ marginTop: 12 }} column={2} size="small">
            <Descriptions.Item label="状态"><Tag color={carStateColor[chargeState.carState]}>{carStateLabel[chargeState.carState]}</Tag></Descriptions.Item>
            <Descriptions.Item label="分配桩">{chargeState.chargePileNum ?? '未分配'}</Descriptions.Item>
            <Descriptions.Item label="请求/已充(kWh)">{chargeState.requestAmount} / {chargeState.chargedAmount.toFixed(1)}</Descriptions.Item>
            <Descriptions.Item label="预计剩余(分)">{chargeState.estimatedRemainingMinutes ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="开始时间">{chargeState.startTime ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="已过(秒)">{chargeState.elapsedSeconds ?? '-'}</Descriptions.Item>
            {chargeState.reminderMessage && (
              <Descriptions.Item label="提示" span={2}><Tag color="orange">{chargeState.reminderMessage}</Tag></Descriptions.Item>
            )}
          </Descriptions>
        )}
      </Card>
    </Spin>
  );
}
