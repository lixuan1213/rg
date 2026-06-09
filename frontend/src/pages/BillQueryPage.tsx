import { useState } from 'react';
import { Card, Form, Input, Button, Table, Tag, Spin } from 'antd';
import { requestBill } from '../api/chargingApi';
import type { BillResponse } from '../api/types';
import dayjs from 'dayjs';

export default function BillQueryPage() {
  const [bills, setBills] = useState<BillResponse[]>([]);
  const [loading, setLoading] = useState(false);

  const handleQueryBills = async (values: { carId: string }) => {
    setLoading(true);
    try {
      const res = await requestBill(values.carId, dayjs().format('YYYY-MM-DD'));
      setBills(res.data.data);
    } catch { /* handled */ }
    setLoading(false);
  };

  return (
    <Spin spinning={loading}>
      <Card title="查询账单（默认当天）">
        <Form layout="inline" onFinish={handleQueryBills}>
          <Form.Item name="carId" label="车辆ID" rules={[{ required: true }]}>
            <Input placeholder="CAR001" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit">查询</Button>
          </Form.Item>
        </Form>
      </Card>

      {bills.length > 0 && (
        <Card title={`账单列表（${dayjs().format('YYYY-MM-DD')}）`} style={{ marginTop: 16 }}>
          <Table
            dataSource={bills}
            rowKey="billId"
            columns={[
              { title: '账单号', dataIndex: 'billId', key: 'billId' },
              { title: '充电桩', dataIndex: 'chargePileNum', key: 'chargePileNum' },
              { title: '充电量(kWh)', dataIndex: 'chargeAmount', key: 'chargeAmount', render: (v: number) => v.toFixed(1) },
              { title: '时长(分)', dataIndex: 'chargeDuration', key: 'chargeDuration' },
              { title: '电费(元)', dataIndex: 'totalChargeFee', key: 'totalChargeFee', render: (v: number) => v.toFixed(2) },
              { title: '服务费(元)', dataIndex: 'totalServiceFee', key: 'totalServiceFee', render: (v: number) => v.toFixed(2) },
              { title: '总费用(元)', dataIndex: 'totalFee', key: 'totalFee', render: (v: number) => <Tag color="red">{v.toFixed(2)}</Tag> },
              { title: '开始时间', dataIndex: 'startTime', key: 'startTime', render: (v: string) => dayjs(v).format('HH:mm:ss') },
              { title: '结束时间', dataIndex: 'endTime', key: 'endTime', render: (v: string) => dayjs(v).format('HH:mm:ss') },
            ]}
          />
        </Card>
      )}
    </Spin>
  );
}
