package com.example.okhttp.model;

import lombok.Builder;
import lombok.Data;

import java.util.concurrent.TimeUnit;

@Data
@Builder
public class ConnectionPoolOption {
    public final static TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLISECONDS;
    public final static int DEFAULT_MAX_CONNECTION_COUNT = 200;
    public final static long DEFAULT_KEEP_ALIVE_DURATION = 1000;
    public final static int DEFAULT_RETRY_COUNT = 0;

    private int maxConnectionCount;
    private long keepAliveDuration;
    private int retryCount;
    private TimeUnit timeUnit;


    public ConnectionPoolOption() {
        this(DEFAULT_MAX_CONNECTION_COUNT, DEFAULT_KEEP_ALIVE_DURATION, DEFAULT_RETRY_COUNT, DEFAULT_TIME_UNIT);
    }

    public ConnectionPoolOption(int maxConnectionCount, long keepAliveDuration, int retryCount) {
        this(maxConnectionCount, keepAliveDuration, retryCount, DEFAULT_TIME_UNIT);
    }

    public ConnectionPoolOption(int maxConnectionCount, long keepAliveDuration, int retryCount, TimeUnit timeUnit) {
        this.maxConnectionCount = maxConnectionCount;
        this.keepAliveDuration = keepAliveDuration;
        this.retryCount = retryCount;
        this.timeUnit = timeUnit;
    }
}
