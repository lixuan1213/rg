import client from './client';
import type {
  ApiResponse,
  CreateAccountRequest,
  SetPasswordRequest,
  ResultResponse,
} from './types';

export function createAccount(data: CreateAccountRequest) {
  return client.post<ApiResponse<ResultResponse>>('/api/account/create', data);
}

export function setPassword(data: SetPasswordRequest) {
  return client.post<ApiResponse<ResultResponse>>('/api/account/set-pwd', data);
}
