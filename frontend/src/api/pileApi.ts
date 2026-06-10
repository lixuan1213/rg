import client from './client';
import type {
  ApiResponse,
  PileStateResponse,
  SetParametersRequest,
  ResultResponse,
} from './types';

export function powerOn(pileId: string) {
  return client.post<ApiResponse<ResultResponse>>(`/api/pile/${pileId}/power-on`);
}

export function powerOff(pileId: string) {
  return client.post<ApiResponse<ResultResponse>>(`/api/pile/${pileId}/power-off`);
}

export function startChargingPile(pileId: string) {
  return client.post<ApiResponse<ResultResponse>>(`/api/pile/${pileId}/start`);
}

export function setParameters(data: SetParametersRequest) {
  return client.put<ApiResponse<ResultResponse>>('/api/pile/parameters', data);
}

export function queryPileState(pileId: string) {
  return client.get<ApiResponse<PileStateResponse>>(`/api/pile/${pileId}/state`);
}

export function queryAllPileStates() {
  return client.get<ApiResponse<PileStateResponse[]>>('/api/pile/state/all');
}

export function reportFault(pileId: string) {
  return client.post<ApiResponse<ResultResponse>>(`/api/pile/${pileId}/fault`);
}

export function recoverFault(pileId: string) {
  return client.post<ApiResponse<ResultResponse>>(`/api/pile/${pileId}/recover`);
}
