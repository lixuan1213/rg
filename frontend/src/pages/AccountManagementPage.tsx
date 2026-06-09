import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Form, Input, InputNumber, Button, message, Spin } from 'antd';
import { createAccount, setPassword } from '../api/accountApi';

export default function AccountManagementPage() {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (values: {
    carId: string;
    userName: string;
    carCapacity: number;
    password: string;
  }) => {
    setLoading(true);
    try {
      await createAccount({
        carId: values.carId,
        userName: values.userName,
        carCapacity: values.carCapacity,
      });
      await setPassword({ carId: values.carId, password: values.password });
      message.success('账户创建并注册成功');
      setTimeout(() => navigate('/charging-operation'), 800);
    } catch { /* handled */ }
    setLoading(false);
  };

  return (
    <Spin spinning={loading}>
      <Card title="创建并注册车辆账户">
        <Form layout="vertical" onFinish={handleSubmit} style={{ maxWidth: 400 }}>
          <Form.Item name="carId" label="车辆ID" rules={[{ required: true, message: '请输入车辆ID' }]}>
            <Input placeholder="如 CAR001" />
          </Form.Item>
          <Form.Item name="userName" label="用户名" rules={[{ required: true, message: '请输入用户名' }]}>
            <Input placeholder="用户姓名" />
          </Form.Item>
          <Form.Item
            name="carCapacity"
            label="电池容量(kWh)"
            rules={[{ required: true, type: 'number', min: 1, message: '请输入电池容量' }]}
          >
            <InputNumber style={{ width: '100%' }} min={1} step={1} placeholder="电池容量" />
          </Form.Item>
          <Form.Item
            name="password"
            label="设置密码"
            rules={[{ required: true, min: 6, message: '密码至少6位' }]}
          >
            <Input.Password placeholder="至少6位" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit">创建并注册</Button>
          </Form.Item>
        </Form>
      </Card>
    </Spin>
  );
}
