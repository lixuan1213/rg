import { createContext, useContext, useState, type ReactNode } from 'react';

interface AuthState {
  role: 'none' | 'user' | 'admin';
  userId: string | null;
  userName: string | null;
  loginAsUser: (carId: string, name: string) => void;
  loginAsAdmin: () => void;
  logout: () => void;
}

const AuthContext = createContext<AuthState>({
  role: 'none', userId: null, userName: null,
  loginAsUser: () => {}, loginAsAdmin: () => {}, logout: () => {},
});

export function AuthProvider({ children }: { children: ReactNode }) {
  const [role, setRole] = useState<'none' | 'user' | 'admin'>('none');
  const [userId, setUserId] = useState<string | null>(null);
  const [userName, setUserName] = useState<string | null>(null);

  const loginAsUser = (carId: string, name: string) => {
    setRole('user'); setUserId(carId); setUserName(name);
  };
  const loginAsAdmin = () => {
    setRole('admin'); setUserId(null); setUserName(null);
  };
  const logout = () => {
    setRole('none'); setUserId(null); setUserName(null);
  };

  return (
    <AuthContext.Provider value={{ role, userId, userName, loginAsUser, loginAsAdmin, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
