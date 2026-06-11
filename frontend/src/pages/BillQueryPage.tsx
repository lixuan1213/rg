import { useState } from 'react';
import { Card, Form, Input, Button, Table, Tag, Spin, Modal, Descriptions, Row, Col, message } from 'antd';
import { requestBill, requestDetailedList } from '../api/chargingApi';
import type { BillResponse, BillDetailResponse } from '../api/types';
import dayjs from 'dayjs';

export default function BillQueryPage() {
  const [bills, setBills] = useState<BillResponse[]>([]);
  const [detail, setDetail] = useState<BillDetailResponse | null>(null);
  const [detailOpen, setDetailOpen] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleQueryBills = async (values: { carId: string }) => {
    setLoading(true);
    try {
      const res = await requestBill(values.carId, dayjs().format('YYYY-MM-DD'));
      setBills(res.data.data);
    } catch { /* handled */ }
    setLoading(false);
  };

  const handleQueryById = async (values: { billId: string }) => {
    const id = Number(values.billId);
    if (isNaN(id) || id <= 0) {
      message.error('请输入有效的账单号');
      return;
    }
    setLoading(true);
    try {
      const res = await requestDetailedList(id);
      setDetail(res.data.data);
      setDetailOpen(true);
    } catch { /* handled */ }
    setLoading(false);
  };

  const columns = [
    { title: '账单号', dataIndex: 'billId', key: 'billId' },
    { title: '充电桩', dataIndex: 'chargePileNum', key: 'chargePileNum' },
    { title: '充电量(kWh)', dataIndex: 'chargeAmount', key: 'chargeAmount', render: (v: number) => v.toFixed(1) },
    { title: '时长(分)', dataIndex: 'chargeDuration', key: 'chargeDuration' },
    { title: '电费(元)', dataIndex: 'totalChargeFee', key: 'totalChargeFee', render: (v: number) => v.toFixed(2) },
    { title: '服务费(元)', dataIndex: 'totalServiceFee', key: 'totalServiceFee', render: (v: number) => v.toFixed(2) },
    { title: '总费用(元)', dataIndex: 'totalFee', key: 'totalFee', render: (v: number) => <Tag color="red">{v.toFixed(2)}</Tag> },
    { title: '开始', dataIndex: 'startTime', key: 'startTime', render: (v: string) => dayjs(v).format('HH:mm:ss') },
    { title: '结束', dataIndex: 'endTime', key: 'endTime', render: (v: string) => dayjs(v).format('HH:mm:ss') },
  ];

  return (
    <Spin spinning={loading}>
      <Row gutter={16}>
        <Col span={14}>
          <Card title="按车辆查询当天账单">
            <Form layout="inline" onFinish={handleQueryBills}>
              <Form.Item name="carId" label="车辆ID" rules={[{ required: true }]}>
                <Input placeholder="CAR001" />
              </Form.Item>
              <Form.Item>
                <Button type="primary" htmlType="submit">查询</Button>
              </Form.Item>
            </Form>
          </Card>
        </Col>
        <Col span={10}>
          <Card title="按账单号查询">
            <Form layout="inline" onFinish={handleQueryById}>
              <Form.Item name="billId" label="账单号" rules={[{ required: true }]}>
                <Input placeholder="如 1" style={{ width: 100 }} />
              </Form.Item>
              <Form.Item>
                <Button htmlType="submit">查看</Button>
              </Form.Item>
            </Form>
          </Card>
        </Col>
      </Row>

      {bills.length > 0 && (
        <Card title={`账单列表（${dayjs().format('YYYY-MM-DD')}）`} style={{ marginTop: 16 }}>
          <Table dataSource={bills} rowKey="billId" columns={columns} />
        </Card>
      )}

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
