package com.screenlocker.secure.retrofit;

/**
 * @author : Muhammad Nadeem
 * Created at: 2/11/2020
 */
public class ErrorResponse {
    private long requestId;

    public ErrorResponse(long requestId) {
        this.requestId = requestId;
    }

    public long getRequestId() {
        return requestId;
    }
}
