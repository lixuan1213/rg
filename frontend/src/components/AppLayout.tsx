import { useState, useMemo } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { Layout, Menu, Typography } from 'antd';
import {
  DashboardOutlined,
  ThunderboltOutlined,
  ClusterOutlined,
  FileTextOutlined,
  SearchOutlined,
  LogoutOutlined,
} from '@ant-design/icons';
import { useAuth } from './AuthContext';

const { Header, Sider, Content } = Layout;

export default function AppLayout() {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { role, userId, logout } = useAuth();

  const selectedKey = '/' + location.pathname.split('/')[1] || '/';

  const items = useMemo(() => {
    if (role === 'admin') {
      return [
        { key: '/admin', icon: <DashboardOutlined />, label: '管理总览' },
        { key: '/bills', icon: <FileTextOutlined />, label: '账单查询' },
        { key: '/pile-management', icon: <ClusterOutlined />, label: '充电桩管理' },
        { type: 'divider' as const },
        { key: 'logout', icon: <LogoutOutlined />, label: '退出登录', danger: true },
      ];
    }
    return [
      { key: '/', icon: <DashboardOutlined />, label: '系统总览' },
      { key: '/charging-operation', icon: <ThunderboltOutlined />, label: '充电操作' },
      { key: '/status-query', icon: <SearchOutlined />, label: '状态查询' },
      { key: '/my-bills', icon: <FileTextOutlined />, label: '我的账单' },
      { type: 'divider' as const },
      { key: 'logout', icon: <LogoutOutlined />, label: `退出 (${userId})`, danger: true },
    ];
  }, [role, userId]);

  const handleMenuClick = ({ key }: { key: string }) => {
    if (key === 'logout') {
      logout();
      navigate('/login');
    } else {
      navigate(key);
    }
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider collapsible collapsed={collapsed} onCollapse={setCollapsed} theme="dark">
        <div style={{ height: 48, margin: 16, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <Typography.Text style={{ color: '#fff', fontSize: collapsed ? 14 : 18, fontWeight: 600, whiteSpace: 'nowrap' }}>
            {collapsed ? '充电' : '充电桩调度计费系统'}
          </Typography.Text>
        </div>
        <Menu
          theme="dark" mode="inline"
          selectedKeys={[selectedKey || items[0]?.key || '/']}
          items={items}
          onClick={handleMenuClick}
        />
      </Sider>
      <Layout>
        <Header style={{ background: '#fff', padding: '0 24px', display: 'flex', alignItems: 'center' }}>
          <Typography.Title level={4} style={{ margin: 0 }}>
            {items.find(i => i.key === selectedKey)?.label || '系统总览'}
          </Typography.Title>
        </Header>
        <Content style={{ margin: 16, padding: 24, background: '#fff', borderRadius: 8, overflow: 'auto' }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
}
