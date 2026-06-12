import client from './client';
import type {
  ApiResponse,
  CreateAccountRequest,
  SetPasswordRequest,
  ResultResponse,
  LoginRequest,
  LoginResponse,
  AccountSummaryResponse,
} from './types';

export function createAccount(data: CreateAccountRequest) {
  return client.post<ApiResponse<ResultResponse>>('/api/account/create', data);
}

export function setPassword(data: SetPasswordRequest) {
  return client.post<ApiResponse<ResultResponse>>('/api/account/set-pwd', data);
}

export function adminLogin(data: LoginRequest) {
  return client.post<ApiResponse<ResultResponse>>('/api/account/adminlogin', data);
}

export function userLogin(data: LoginRequest) {
  return client.post<ApiResponse<LoginResponse>>('/api/account/login', data);
}

export function getAccounts() {
  return client.get<ApiResponse<AccountSummaryResponse[]>>('/api/account/getaccounts');
}
