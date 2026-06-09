import { useEffect, useState, useCallback } from 'react';
import { Card, Col, Row, Statistic, Table, Tag, Spin, Input, Descriptions, Divider, message } from 'antd';
import {
  ThunderboltOutlined,
  ClockCircleOutlined,
  DashboardOutlined,
  CarOutlined,
} from '@ant-design/icons';
import { queryAllPileStates } from '../api/pileApi';
import { queryQueueState, queryChargingState, queryCarState } from '../api/chargingApi';
import type {
  PileStateResponse,
  QueueStateResponse,
  PileWorkingState,
  CarState,
} from '../api/types';

const stateColorMap: Record<PileWorkingState, string> = {
  OFF: 'default', IDLE: 'success', CHARGING: 'processing', FAULT: 'error',
};
const stateLabelMap: Record<PileWorkingState, string> = {
  OFF: '关机', IDLE: '空闲', CHARGING: '充电中', FAULT: '故障',
};
const carStateLabel: Record<CarState, string> = {
  WAITING: '等待中', QUEUED: '已排队', CHARGING: '充电中', COMPLETED: '已完成', CANCELLED: '已取消',
};
const carStateColor: Record<CarState, string> = {
  WAITING: 'default', QUEUED: 'warning', CHARGING: 'processing', COMPLETED: 'success', CANCELLED: 'error',
};

const PILE_POWER: Record<string, number> = { FAST: 60, SLOW: 7 };

interface QueueDetail {
  carId: string;
  carCapacity: number;
  requestAmount: number;
  waitTime: number;
  pileId: string | null;
  aheadCount: number;
  estimatedWait: number | null;
  queueNum: number | null;
}

export default function Dashboard() {
  const [piles, setPiles] = useState<PileStateResponse[]>([]);
  const [fastDetail, setFastDetail] = useState<QueueDetail[]>([]);
  const [slowDetail, setSlowDetail] = useState<QueueDetail[]>([]);
  const [loading, setLoading] = useState(false);
  const [lookupCarId, setLookupCarId] = useState('');
  const [lookupResult, setLookupResult] = useState<{
    carState: CarState;
    queueNum: number | null;
    carNumberBeforePosition: number;
    requestTime: string;
  } | null>(null);
  const [lookupLoading, setLookupLoading] = useState(false);

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const [pileRes, fastQRes, slowQRes] = await Promise.all([
        queryAllPileStates(),
        queryQueueState('FAST'),
        queryQueueState('SLOW'),
      ]);
      setPiles(pileRes.data.data);

      const enrichQueue = async (queue: QueueStateResponse[], mode: 'FAST' | 'SLOW') => {
        if (queue.length === 0) return [] as QueueDetail[];
        const power = PILE_POWER[mode] ?? 60;
        const details = await Promise.all(
          queue.map(async (car, idx) => {
            try {
              const [stRes, csRes] = await Promise.all([
                queryChargingState(car.carId),
                queryCarState(car.carId),
              ]);
              const st = stRes.data.data;
              const cs = csRes.data.data;
              let estimatedWait: number | null = null;
              if (cs.carNumberBeforePosition > 0) {
                estimatedWait = 0;
                for (let j = idx - cs.carNumberBeforePosition; j < idx; j++) {
                  if (j >= 0 && j < queue.length) estimatedWait += queue[j].requestAmount / power * 60;
                }
                estimatedWait = Math.ceil(estimatedWait);
              }
              return {
                carId: car.carId,
                carCapacity: car.carCapacity,
                requestAmount: car.requestAmount,
                waitTime: car.waitTime,
                pileId: st.chargePileNum ?? null,
                aheadCount: cs.carNumberBeforePosition,
                estimatedWait,
                queueNum: cs.queueNum,
              };
            } catch {
              return {
                carId: car.carId, carCapacity: car.carCapacity, requestAmount: car.requestAmount,
                waitTime: car.waitTime, pileId: null, aheadCount: idx, estimatedWait: null, queueNum: null,
              };
            }
          })
        );
        return details;
      };

      const [fd, sd] = await Promise.all([
        enrichQueue(fastQRes.data.data, 'FAST'),
        enrichQueue(slowQRes.data.data, 'SLOW'),
      ]);
      setFastDetail(fd);
      setSlowDetail(sd);
    } catch { /* handled */ }
    setLoading(false);
  }, []);

  useEffect(() => {
    fetchData();
    const timer = setInterval(fetchData, 10000);
    return () => clearInterval(timer);
  }, [fetchData]);

  const handleLookup = async () => {
    if (!lookupCarId.trim()) return;
    setLookupLoading(true);
    try {
      setLookupResult((await queryCarState(lookupCarId.trim())).data.data);
    } catch { setLookupResult(null); message.error('未找到该车辆'); }
    setLookupLoading(false);
  };

  const allQueueDetails = [...fastDetail, ...slowDetail];

  const pileQueueMap = new Map<string, QueueDetail[]>();
  for (const d of allQueueDetails) {
    if (d.pileId) {
      const list = pileQueueMap.get(d.pileId) || [];
      list.push(d);
      pileQueueMap.set(d.pileId, list);
    }
  }

  const totalCharged = piles.reduce((s, p) => s + p.totalCapacity, 0);
  const totalTime = piles.reduce((s, p) => s + p.totalChargeTime, 0);

  const pileColumns = [
    { title: '桩编号', dataIndex: 'pileId', key: 'pileId', width: 90 },
    { title: '状态', dataIndex: 'workingState', key: 'workingState',
      render: (s: PileWorkingState) => <Tag color={stateColorMap[s]}>{stateLabelMap[s]}</Tag> },
    { title: '模式', key: 'mode', width: 60,
      render: (_: unknown, r: PileStateResponse) => r.pileId?.startsWith('F') ? '快充' : '慢充' },
    { title: '排队', key: 'queued', width: 50,
      render: (_: unknown, r: PileStateResponse) => pileQueueMap.get(r.pileId)?.length ?? 0 },
    { title: '充电次数', dataIndex: 'totalChargeNum', key: 'totalChargeNum', width: 80 },
    { title: '时长(分)', dataIndex: 'totalChargeTime', key: 'totalChargeTime', width: 80 },
    { title: '电量(kWh)', dataIndex: 'totalCapacity', key: 'totalCapacity', width: 80, render: (v: number) => v.toFixed(1) },
  ];

  const queueSubColumns = [
    { title: '排队号', dataIndex: 'queueNum', key: 'queueNum', width: 60 },
    { title: '车辆ID', dataIndex: 'carId', key: 'carId', width: 80 },
    { title: '容量(kWh)', dataIndex: 'carCapacity', key: 'carCapacity', width: 80 },
    { title: '请求(kWh)', dataIndex: 'requestAmount', key: 'requestAmount', width: 80 },
    { title: '已等(分)', dataIndex: 'waitTime', key: 'waitTime', width: 70 },
    { title: '前方', dataIndex: 'aheadCount', key: 'aheadCount', width: 50 },
    { title: '预计(分)', dataIndex: 'estimatedWait', key: 'estimatedWait', width: 70, render: (v: number | null) => v ?? '-' },
  ];

  return (
    <Spin spinning={loading}>
      <Row gutter={[16, 16]}>
        <Col xs={12} sm={6}><Card><Statistic title="充电桩" value={piles.length} prefix={<DashboardOutlined />} /></Card></Col>
        <Col xs={12} sm={6}><Card><Statistic title="排队车辆" value={allQueueDetails.length} prefix={<CarOutlined />} /></Card></Col>
        <Col xs={12} sm={6}><Card><Statistic title="总充电量(kWh)" value={totalCharged.toFixed(1)} prefix={<ThunderboltOutlined />} /></Card></Col>
        <Col xs={12} sm={6}><Card><Statistic title="总时长(分)" value={totalTime} prefix={<ClockCircleOutlined />} /></Card></Col>
      </Row>

      <Card title="充电桩状态" style={{ marginTop: 16 }}>
        <Table
          dataSource={piles}
          rowKey="pileId"
          pagination={false}
          size="small"
          columns={pileColumns}
          expandable={{
            expandedRowRender: (record) => {
              const qCars = pileQueueMap.get(record.pileId);
              if (!qCars || qCars.length === 0) {
                return <Tag>暂无排队车辆</Tag>;
              }
              const sorted = [...qCars].sort((a, b) => (a.queueNum ?? 0) - (b.queueNum ?? 0));
              return (
                <div>
                  <Tag color="blue" style={{ marginBottom: 8 }}>
                    充电中: {record.workingState === 'CHARGING' ? '有车正在充电' : '无'}
                  </Tag>
                  <Table
                    dataSource={sorted}
                    rowKey="carId"
                    pagination={false}
                    size="small"
                    columns={queueSubColumns}
                    title={() => <strong>排队车辆（按排队号排序）</strong>}
                    scroll={{ x: 500 }}
                  />
                </div>
              );
            },
            rowExpandable: () => true,
          }}
        />
      </Card>

      <Divider />

      <Card title="等候区（未分配桩的车辆查询）" extra={<Tag>输入carId查找</Tag>}>
        <Input.Search
          placeholder="输入车辆ID"
          value={lookupCarId}
          onChange={e => setLookupCarId(e.target.value)}
          onSearch={handleLookup}
          enterButton="查询"
          loading={lookupLoading}
        />
        {lookupResult && (
          <Descriptions bordered style={{ marginTop: 16 }} column={4} size="small">
            <Descriptions.Item label="状态">
              <Tag color={carStateColor[lookupResult.carState]}>{carStateLabel[lookupResult.carState]}</Tag>
            </Descriptions.Item>
            <Descriptions.Item label="等候位置">
              {lookupResult.carState === 'WAITING' ? `第 ${lookupResult.carNumberBeforePosition + 1} 位` : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="排队号">{lookupResult.queueNum ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="请求时间">{lookupResult.requestTime}</Descriptions.Item>
          </Descriptions>
        )}
      </Card>
    </Spin>
  );
}
