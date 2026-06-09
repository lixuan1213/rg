package com.bupt.charging.dto;

import com.bupt.charging.dto.response.EndChargingResponse;
import com.bupt.charging.dto.response.ResultResponse;

public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;

    public ApiResponse() {}

    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "success", data);
    }

    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(1, message, null);
    }

    public static ApiResponse<ResultResponse> ofResult(ResultResponse result) {
        if (result.getResult() == 0) {
            return new ApiResponse<>(0, "success", result);
        }
        return new ApiResponse<>(1, result.getMessage(), result);
    }

    public static ApiResponse<EndChargingResponse> ofEndCharging(EndChargingResponse response) {
        if (response.getResult() == 0) {
            return new ApiResponse<>(0, "success", response);
        }
        return new ApiResponse<>(1, response.getMessage(), response);
    }

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
