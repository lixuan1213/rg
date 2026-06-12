import { useState, useEffect } from 'react';
import { Card, Table, Tag, Spin, Modal, Descriptions, Button, Form, Input, message, Divider } from 'antd';
import { useAuth } from '../components/AuthContext';
import { requestBill, requestDetailedList } from '../api/chargingApi';
import type { BillResponse, BillDetailResponse } from '../api/types';
import dayjs from 'dayjs';

export default function MyBillsPage() {
  const { userId } = useAuth();
  const [bills, setBills] = useState<BillResponse[]>([]);
  const [detail, setDetail] = useState<BillDetailResponse | null>(null);
  const [detailOpen, setDetailOpen] = useState(false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (userId) fetchBills();
  }, [userId]);

  const fetchBills = async () => {
    if (!userId) return;
    setLoading(true);
    try { setBills((await requestBill(userId, dayjs().format('YYYY-MM-DD'))).data.data); } catch {}
    setLoading(false);
  };

  const handleQueryById = async (values: { billId: string }) => {
    const id = Number(values.billId);
    if (isNaN(id) || id <= 0) { message.error('请输入有效的账单号'); return; }
    setLoading(true);
    try {
      const res = await requestDetailedList(id);
      const d = res.data.data;
      if (d && d.carId !== userId) {
        message.error('无权查看其他用户的账单');
        setLoading(false);
        return;
      }
      setDetail(d);
      setDetailOpen(true);
    } catch {}
    setLoading(false);
  };

  const columns = [
    { title: '账单号', dataIndex: 'billId' },
    { title: '充电桩', dataIndex: 'chargePileNum' },
    { title: '电量(kWh)', dataIndex: 'chargeAmount', render: (v: number) => v.toFixed(1) },
    { title: '时长(分)', dataIndex: 'chargeDuration' },
    { title: '电费(元)', dataIndex: 'totalChargeFee', render: (v: number) => v.toFixed(2) },
    { title: '服务费(元)', dataIndex: 'totalServiceFee', render: (v: number) => v.toFixed(2) },
    { title: '总费(元)', dataIndex: 'totalFee', render: (v: number) => <Tag color="red">{v.toFixed(2)}</Tag> },
    { title: '开始', dataIndex: 'startTime', render: (v: string) => dayjs(v).format('HH:mm:ss') },
    { title: '结束', dataIndex: 'endTime', render: (v: string) => dayjs(v).format('HH:mm:ss') },
  ];

  return (
    <Spin spinning={loading}>
      <Card title={`我的账单 (${dayjs().format('YYYY-MM-DD')})`}>
        {bills.length > 0 ? (
          <Table dataSource={bills} rowKey="billId" size="small" columns={columns} />
        ) : (
          <Tag>当天暂无账单</Tag>
        )}
      </Card>

      <Divider />

      <Card title="按账单号查询详情">
        <Form layout="inline" onFinish={handleQueryById}>
          <Form.Item name="billId" label="账单号" rules={[{ required: true }]}>
            <Input placeholder="如 1" style={{ width: 100 }} />
          </Form.Item>
          <Form.Item>
            <Button htmlType="submit">查看</Button>
          </Form.Item>
        </Form>
      </Card>

      <Modal title={`账单详情 #${detail?.billId ?? ''}`} open={detailOpen} onCancel={() => setDetailOpen(false)} footer={null} width={600}>
        {detail && (
          <Descriptions bordered column={2} size="small">
            <Descriptions.Item label="车辆ID">{detail.carId}</Descriptions.Item>
            <Descriptions.Item label="充电桩">{detail.chargePileNum}</Descriptions.Item>
            <Descriptions.Item label="日期">{detail.date}</Descriptions.Item>
            <Descriptions.Item label="充电量(kWh)">{detail.chargeAmount.toFixed(1)}</Descriptions.Item>
            <Descriptions.Item label="时长(分钟)">{detail.chargeDuration}</Descriptions.Item>
            <Descriptions.Item label="电费(元)">{detail.chargeFee.toFixed(2)}</Descriptions.Item>
            <Descriptions.Item label="服务费(元)">{detail.serviceFee.toFixed(2)}</Descriptions.Item>
            <Descriptions.Item label="总费用(元)"><Tag color="red">{detail.subtotalFee.toFixed(2)}</Tag></Descriptions.Item>
            <Descriptions.Item label="开始时间">{dayjs(detail.startTime).format('HH:mm:ss')}</Descriptions.Item>
            <Descriptions.Item label="结束时间">{dayjs(detail.endTime).format('HH:mm:ss')}</Descriptions.Item>
          </Descriptions>
        )}
      </Modal>
    </Spin>
  );
}
