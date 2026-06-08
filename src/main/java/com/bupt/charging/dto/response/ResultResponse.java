package com.bupt.charging.dto.response;

public class ResultResponse {

    private final int result;

    public ResultResponse(int result) {
        this.result = result;
    }

    public static ResultResponse success() {
        return new ResultResponse(0);
    }

    public static ResultResponse fail() {
        return new ResultResponse(1);
    }

    public int getResult() { return result; }
}
