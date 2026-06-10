import { useState } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { Layout, Menu, Typography } from 'antd';
import {
  DashboardOutlined,
  ThunderboltOutlined,
  ClusterOutlined,
  FileTextOutlined,
  UserOutlined,
} from '@ant-design/icons';

const { Header, Sider, Content } = Layout;

const menuItems = [
  { key: '/', icon: <DashboardOutlined />, label: '系统总览' },
  { key: '/charging-operation', icon: <ThunderboltOutlined />, label: '充电操作' },
  { key: '/pile-management', icon: <ClusterOutlined />, label: '充电桩管理' },
  { key: '/bills', icon: <FileTextOutlined />, label: '账单查询' },
  { key: '/account', icon: <UserOutlined />, label: '账户管理' },
];

export default function AppLayout() {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  const selectedKey = '/' + location.pathname.split('/')[1] || '/';

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider
        collapsible
        collapsed={collapsed}
        onCollapse={setCollapsed}
        theme="dark"
      >
        <div style={{ height: 48, margin: 16, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <Typography.Text style={{ color: '#fff', fontSize: collapsed ? 14 : 18, fontWeight: 600, whiteSpace: 'nowrap' }}>
            {collapsed ? '充电' : '充电桩调度计费系统'}
          </Typography.Text>
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[menuItems.find(i => i.key === selectedKey) ? selectedKey : '/']}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
        />
      </Sider>
      <Layout>
        <Header style={{ background: '#fff', padding: '0 24px', display: 'flex', alignItems: 'center' }}>
          <Typography.Title level={4} style={{ margin: 0 }}>
            {menuItems.find(i => i.key === selectedKey)?.label || '系统总览'}
          </Typography.Title>
        </Header>
        <Content style={{ margin: 16, padding: 24, background: '#fff', borderRadius: 8, overflow: 'auto' }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
}
