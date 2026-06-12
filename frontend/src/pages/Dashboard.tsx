import { useEffect, useState, useCallback } from 'react';
import { Card, Col, Row, Statistic, Table, Tag, Spin } from 'antd';
import {
  ThunderboltOutlined, ClockCircleOutlined, DashboardOutlined, CarOutlined,
} from '@ant-design/icons';
import { queryAllPileStates } from '../api/pileApi';
import { queryQueueState, queryChargingState, queryCarState } from '../api/chargingApi';
import type {
  PileStateResponse, QueueStateResponse, PileWorkingState,
} from '../api/types';

const stateColorMap: Record<PileWorkingState, string> = {
  OFF: 'default', IDLE: 'success', CHARGING: 'processing', WAITING_UNPLUG: 'warning', FAULT: 'error',
};
const stateLabelMap: Record<PileWorkingState, string> = {
  OFF: '关机', IDLE: '空闲', CHARGING: '充电中', WAITING_UNPLUG: '待拔枪', FAULT: '故障',
};

const PILE_POWER: Record<string, number> = { FAST: 60, SLOW: 7 };

interface QueueDetail {
  carId: string; carCapacity: number; requestAmount: number; waitTime: number;
  pileId: string | null; aheadCount: number; estimatedWait: number | null; queueNum: number | null;
}

export default function Dashboard() {
  const [piles, setPiles] = useState<PileStateResponse[]>([]);
  const [fastDetail, setFastDetail] = useState<QueueDetail[]>([]);
  const [slowDetail, setSlowDetail] = useState<QueueDetail[]>([]);
  const [loading, setLoading] = useState(false);

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const [pileRes, fastQRes, slowQRes] = await Promise.all([
        queryAllPileStates(), queryQueueState('FAST'), queryQueueState('SLOW'),
      ]);
      setPiles(pileRes.data.data);
      const enrichQueue = async (queue: QueueStateResponse[], mode: 'FAST' | 'SLOW') => {
        if (queue.length === 0) return [] as QueueDetail[];
        const power = PILE_POWER[mode] ?? 60;
        return Promise.all(queue.map(async (car, idx) => {
          try {
            const [st, cs] = await Promise.all([queryChargingState(car.carId), queryCarState(car.carId)]);
            let ew: number | null = null;
            if (cs.data.data.carNumberBeforePosition > 0) {
              ew = 0;
              for (let j = idx - cs.data.data.carNumberBeforePosition; j < idx; j++) {
                if (j >= 0 && j < queue.length) ew += queue[j].requestAmount / power * 60;
              }
              ew = Math.ceil(ew);
            }
            return { carId: car.carId, carCapacity: car.carCapacity, requestAmount: car.requestAmount,
              waitTime: car.waitTime, pileId: st.data.data.chargePileNum ?? null,
              aheadCount: cs.data.data.carNumberBeforePosition, estimatedWait: ew, queueNum: cs.data.data.queueNum };
          } catch {
            return { carId: car.carId, carCapacity: car.carCapacity, requestAmount: car.requestAmount,
              waitTime: car.waitTime, pileId: null, aheadCount: idx, estimatedWait: null, queueNum: null };
          }
        }));
      };
      const [fd, sd] = await Promise.all([enrichQueue(fastQRes.data.data, 'FAST'), enrichQueue(slowQRes.data.data, 'SLOW')]);
      setFastDetail(fd); setSlowDetail(sd);
    } catch { /* handled */ }
    setLoading(false);
  }, []);

  useEffect(() => { fetchData(); const t = setInterval(fetchData, 10000); return () => clearInterval(t); }, [fetchData]);

  const allQueueDetails = [...fastDetail, ...slowDetail];
  const pileQueueMap = new Map<string, QueueDetail[]>();
  for (const d of allQueueDetails) { if (d.pileId) { const l = pileQueueMap.get(d.pileId) || []; l.push(d); pileQueueMap.set(d.pileId, l); } }

  const queueColumns = [
    { title: '车辆', dataIndex: 'carId', key: 'carId', width: 80 },
    { title: '容量', dataIndex: 'carCapacity', key: 'carCapacity', width: 60 },
    { title: '请求', dataIndex: 'requestAmount', key: 'requestAmount', width: 60 },
    { title: '已等', dataIndex: 'waitTime', key: 'waitTime', width: 50 },
    { title: '桩', dataIndex: 'pileId', key: 'pileId', width: 60, render: (v: string | null) => v ?? '-' },
    { title: '前方', dataIndex: 'aheadCount', key: 'aheadCount', width: 45 },
    { title: '预计', dataIndex: 'estimatedWait', key: 'estimatedWait', width: 50, render: (v: number | null) => v ?? '-' },
  ];

  const pileColumns = [
    { title: '桩', dataIndex: 'pileId', key: 'pileId', width: 60 },
    { title: '状态', dataIndex: 'workingState', key: 'workingState', render: (s: PileWorkingState) => <Tag color={stateColorMap[s]}>{stateLabelMap[s]}</Tag> },
    { title: '模式', key: 'mode', width: 50, render: (_: unknown, r: PileStateResponse) => r.pileId?.startsWith('F') ? '快' : '慢' },
    { title: '排队', key: 'queued', width: 45, render: (_: unknown, r: PileStateResponse) => pileQueueMap.get(r.pileId)?.length ?? 0 },
    { title: '次数', dataIndex: 'totalChargeNum', key: 'n', width: 50 },
    { title: '时长', dataIndex: 'totalChargeTime', key: 't', width: 55 },
    { title: '电量', dataIndex: 'totalCapacity', key: 'c', width: 55, render: (v: number) => v.toFixed(1) },
  ];

  return (
    <Spin spinning={loading}>
      <Row gutter={[16, 16]}>
        <Col xs={12} sm={6}><Card><Statistic title="充电桩" value={piles.length} prefix={<DashboardOutlined />} /></Card></Col>
        <Col xs={12} sm={6}><Card><Statistic title="排队车辆" value={allQueueDetails.length} prefix={<CarOutlined />} /></Card></Col>
        <Col xs={12} sm={6}><Card><Statistic title="总充电量" value={piles.reduce((s,p)=>s+p.totalCapacity,0).toFixed(1)} suffix="kWh" prefix={<ThunderboltOutlined />} /></Card></Col>
        <Col xs={12} sm={6}><Card><Statistic title="总时长" value={piles.reduce((s,p)=>s+p.totalChargeTime,0)} suffix="分" prefix={<ClockCircleOutlined />} /></Card></Col>
      </Row>

      <Card title="充电桩状态" style={{ marginTop: 16 }}>
        <Table dataSource={piles} rowKey="pileId" pagination={false} size="small" columns={pileColumns}
          expandable={{
            expandedRowRender: (r) => {
              const cs = pileQueueMap.get(r.pileId);
              if (!cs?.length) return <Tag>无排队车辆</Tag>;
              return <Table dataSource={[...cs].sort((a,b)=>(a.queueNum??0)-(b.queueNum??0))} rowKey="carId" pagination={false} size="small"
                columns={[{title:'排队号',dataIndex:'queueNum',width:60},{title:'车辆ID',dataIndex:'carId',width:80},{title:'请求',dataIndex:'requestAmount',width:60},{title:'已等',dataIndex:'waitTime',width:50},{title:'预计',dataIndex:'estimatedWait',width:50,render:(v:number|null)=>v??'-'}]}
                title={()=><strong>排队车辆</strong>} scroll={{x:300}} />;
            }
          }}
        />
      </Card>

      <Row gutter={16} style={{ marginTop: 16 }}>
        <Col xs={24} md={12}>
          <Card title={`快充排队 (${fastDetail.length}辆)`} extra={<Tag color="blue">FAST</Tag>}>
            <Table dataSource={fastDetail} rowKey="carId" pagination={false} size="small" scroll={{x:500}} columns={queueColumns} />
          </Card>
        </Col>
        <Col xs={24} md={12}>
          <Card title={`慢充排队 (${slowDetail.length}辆)`} extra={<Tag color="green">SLOW</Tag>}>
            <Table dataSource={slowDetail} rowKey="carId" pagination={false} size="small" scroll={{x:500}} columns={queueColumns} />
          </Card>
        </Col>
      </Row>
    </Spin>
  );
}
