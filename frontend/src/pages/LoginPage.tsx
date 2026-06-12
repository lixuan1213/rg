import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Form, Input, InputNumber, Button, Tabs, message, Spin, Typography } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { adminLogin, userLogin, createAccount, setPassword } from '../api/accountApi';
import { useAuth } from '../components/AuthContext';

export default function LoginPage() {
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('user');
  const navigate = useNavigate();
  const { loginAsUser, loginAsAdmin } = useAuth();

  const handleUserLogin = async (values: { userName: string; password: string }) => {
    setLoading(true);
    try {
      const res = await userLogin(values);
      if (res.data.data) {
        loginAsUser(res.data.data.carId, res.data.data.userName);
        message.success(`欢迎，${res.data.data.userName}`);
        navigate('/');
      }
    } catch { /* handled */ }
    setLoading(false);
  };

  const handleAdminLogin = async (values: { userName: string; password: string }) => {
    setLoading(true);
    try {
      const res = await adminLogin(values);
      if (res.data.data?.result === 0) {
        loginAsAdmin();
        message.success('管理员登录成功');
        navigate('/admin');
      } else {
        message.error('管理员用户名或密码错误');
      }
    } catch { /* handled */ }
    setLoading(false);
  };

  const handleRegister = async (values: { carId: string; userName: string; carCapacity: number; password: string }) => {
    setLoading(true);
    try {
      await createAccount({ carId: values.carId, userName: values.userName, carCapacity: values.carCapacity });
      await setPassword({ carId: values.carId, password: values.password });
      message.success('注册成功');
      setActiveTab('user');
    } catch { /* handled */ }
    setLoading(false);
  };

  return (
    <div style={{
      display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh',
      background: 'url(/bg.jpg) center/cover no-repeat',
      position: 'relative',
    }}>
      <div style={{
        position: 'absolute', inset: 0,
        background: 'rgba(0,0,0,0.5)',
      }} />
      <Card style={{ width: 440, boxShadow: '0 2px 12px rgba(0,0,0,0.1)', position: 'relative', zIndex: 1 }}>
        <Typography.Title level={2} style={{ textAlign: 'center', marginBottom: 8 }}>
          充电桩调度计费系统
        </Typography.Title>
        <Spin spinning={loading}>
          <Tabs centered activeKey={activeTab} onChange={setActiveTab} items={[
            {
              key: 'user', label: '用户登录',
              children: (
                <Form onFinish={handleUserLogin} size="large">
                  <Form.Item name="userName" rules={[{ required: true, message: '请输入车辆ID' }]}>
                    <Input prefix={<UserOutlined />} placeholder="车辆ID" />
                  </Form.Item>
                  <Form.Item name="password" rules={[{ required: true, message: '请输入密码' }]}>
                    <Input.Password prefix={<LockOutlined />} placeholder="密码" />
                  </Form.Item>
                  <Form.Item><Button type="primary" htmlType="submit" block>登录</Button></Form.Item>
                </Form>
              ),
            },
            {
              key: 'register', label: '注册账号',
              children: (
                <Form onFinish={handleRegister} size="large">
                  <Form.Item name="carId" rules={[{ required: true, message: '请输入车辆ID' }]}>
                    <Input placeholder="车辆ID" />
                  </Form.Item>
                  <Form.Item name="userName" rules={[{ required: true, message: '请输入用户名' }]}>
                    <Input placeholder="用户名" />
                  </Form.Item>
                  <Form.Item name="carCapacity" rules={[{ required: true, type: 'number', min: 1, message: '请输入电池容量' }]}>
                    <InputNumber placeholder="电池容量(kWh)" style={{ width: '100%' }} min={1} />
                  </Form.Item>
                  <Form.Item name="password" rules={[{ required: true, min: 6, message: '密码至少6位' }]}>
                    <Input.Password prefix={<LockOutlined />} placeholder="密码" />
                  </Form.Item>
                  <Form.Item><Button type="primary" htmlType="submit" block>注册</Button></Form.Item>
                </Form>
              ),
            },
            {
              key: 'admin', label: '管理员登录',
              children: (
                <Form onFinish={handleAdminLogin} size="large">
                  <Form.Item name="userName" rules={[{ required: true, message: '请输入管理员用户名' }]}>
                    <Input prefix={<UserOutlined />} placeholder="管理员用户名" />
                  </Form.Item>
                  <Form.Item name="password" rules={[{ required: true, message: '请输入密码' }]}>
                    <Input.Password prefix={<LockOutlined />} placeholder="密码" />
                  </Form.Item>
                  <Form.Item><Button type="primary" htmlType="submit" block>管理员登录</Button></Form.Item>
                </Form>
              ),
            },
          ]} />
        </Spin>
      </Card>
    </div>
  );
}
