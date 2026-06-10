import client from './client';
import type {
  ApiResponse,
  ChargingRequestDto,
  ChargingRequestResponse,
  CarStateResponse,
  ChargingStateResponse,
  QueueStateResponse,
  BillResponse,
  BillDetailResponse,
  ModifyAmountRequest,
  ModifyModeRequest,
  StartChargingRequest,
  EndChargingRequest,
  ResultResponse,
  EndChargingResponse,
  ChargingMode,
} from './types';

export function submitChargingRequest(data: ChargingRequestDto) {
  return client.post<ApiResponse<ChargingRequestResponse>>('/api/charging/request', data);
}

export function modifyAmount(data: ModifyAmountRequest) {
  return client.put<ApiResponse<ResultResponse>>('/api/charging/amount', data);
}

export function modifyMode(data: ModifyModeRequest) {
  return client.put<ApiResponse<ResultResponse>>('/api/charging/mode', data);
}

export function queryCarState(carId: string) {
  return client.get<ApiResponse<CarStateResponse>>('/api/charging/car-state', { params: { carId } });
}

export function startCharging(data: StartChargingRequest) {
  return client.post<ApiResponse<ResultResponse>>('/api/charging/start', data);
}

export function queryChargingState(carId: string) {
  return client.get<ApiResponse<ChargingStateResponse>>('/api/charging/state', { params: { carId } });
}

export function endCharging(data: EndChargingRequest) {
  return client.post<ApiResponse<EndChargingResponse>>('/api/charging/end', data);
}

export function requestBill(carId: string, date: string) {
  return client.get<ApiResponse<BillResponse[]>>('/api/charging/bill', {
    params: { carId, date },
  });
}

export function requestDetailedList(billId: number) {
  return client.get<ApiResponse<BillDetailResponse>>(`/api/charging/bill/${billId}`);
}

export function queryQueueState(mode: ChargingMode) {
  return client.get<ApiResponse<QueueStateResponse[]>>('/api/charging/queue', {
    params: { mode },
  });
}
