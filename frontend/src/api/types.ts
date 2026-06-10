export type ChargingMode = 'FAST' | 'SLOW';
export type CarState = 'WAITING' | 'QUEUED' | 'CHARGING' | 'PENDING_UNPLUG' | 'COMPLETED' | 'CANCELLED';
export type PileWorkingState = 'OFF' | 'IDLE' | 'CHARGING' | 'WAITING_UNPLUG' | 'FAULT';
export type SchedulingStrategy = 'TIME_ORDER' | 'PRIORITY';
export type TimePeriod = 'PEAK' | 'NORMAL' | 'VALLEY';

export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface ResultResponse {
  result: number;
  message: string;
}

export interface EndChargingResponse {
  result: number;
  message: string;
  billId: number | null;
  chargePileNum: string | null;
  chargeAmount: number | null;
  chargeDuration: number | null;
  totalFee: number | null;
}

export interface ChargingRequestDto {
  carId: string;
  requestAmount: number;
  requestMode: ChargingMode;
}

export interface ChargingRequestResponse {
  carPosition: number;
  carState: CarState;
  queueNum: number | null;
  requestTime: string;
}

export interface CarStateResponse {
  carNumberBeforePosition: number;
  carState: CarState;
  queueNum: number | null;
  requestTime: string;
}

export interface ChargingStateResponse {
  carId: string;
  chargePileNum: string | null;
  requestMode: ChargingMode;
  requestAmount: number;
  chargedAmount: number;
  carState: CarState;
  startTime: string | null;
  elapsedSeconds: number | null;
  estimatedRemainingMinutes: number | null;
  reminderMessage: string | null;
}

export interface QueueStateResponse {
  carId: string;
  carCapacity: number;
  requestAmount: number;
  waitTime: number;
}

export interface BillResponse {
  carId: string;
  date: string;
  billId: number;
  chargePileNum: string;
  chargeAmount: number;
  chargeDuration: number;
  startTime: string;
  endTime: string;
  totalChargeFee: number;
  totalServiceFee: number;
  totalFee: number;
}

export interface BillDetailResponse {
  carId: string;
  date: string;
  billId: number;
  chargePileNum: string;
  chargeAmount: number;
  chargeDuration: number;
  startTime: string;
  endTime: string;
  chargeFee: number;
  serviceFee: number;
  subtotalFee: number;
}

export interface PileStateResponse {
  pileId: string;
  workingState: PileWorkingState;
  totalChargeNum: number;
  totalChargeTime: number;
  totalCapacity: number;
}

export interface ModifyAmountRequest {
  carId: string;
  amount: number;
}

export interface ModifyModeRequest {
  carId: string;
  mode: ChargingMode;
}

export interface StartChargingRequest {
  carId: string;
  chargePileNum: string;
}

export interface EndChargingRequest {
  carId: string;
  chargingPileNum: string;
}

export interface BillingRuleDto {
  timePeriod: TimePeriod;
  startHour: number;
  endHour: number;
  electricityPrice: number;
  serviceFeeRate: number;
}

export interface SetParametersRequest {
  priceRules: BillingRuleDto[];
  schedulingStrategy: SchedulingStrategy | null;
}

export interface CreateAccountRequest {
  carId: string;
  userName: string;
  carCapacity: number;
}

export interface SetPasswordRequest {
  carId: string;
  password: string;
}
