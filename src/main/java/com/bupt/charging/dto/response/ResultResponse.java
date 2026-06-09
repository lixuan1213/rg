package com.bupt.charging.dto.response;

public class ResultResponse {

    private final int result;
    private final String message;

    public ResultResponse(int result, String message) {
        this.result = result;
        this.message = message;
    }

    public static ResultResponse success() {
        return new ResultResponse(0, "操作成功");
    }

    public static ResultResponse success(String message) {
        return new ResultResponse(0, message);
    }

    public static ResultResponse fail() {
        return new ResultResponse(1, "操作失败");
    }

    public static ResultResponse fail(String message) {
        return new ResultResponse(1, message);
    }

    public int getResult() { return result; }
    public String getMessage() { return message; }
}
