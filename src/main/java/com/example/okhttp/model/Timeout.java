package com.example.okhttp.model;

import lombok.Builder;
import lombok.Data;

import java.util.concurrent.TimeUnit;

@Data
@Builder
public class Timeout {
    public final static TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLISECONDS;
    public static final int DEFAULT_READ_TIMEOUT = 10000;
    public static final int DEFAULT_WRITE_TIMEOUT = 10000;
    public static final int DEFAULT_CONNECT_TIMEOUT = 10000;

    private int readTimeout;
    private int writeTimeout;
    private int connectTimeout;
    private TimeUnit timeUnit;

    public Timeout(){
        this(DEFAULT_READ_TIMEOUT, DEFAULT_WRITE_TIMEOUT, DEFAULT_CONNECT_TIMEOUT, DEFAULT_TIME_UNIT);
    }

    public Timeout(int readTimeout, int writeTimeout, int connectTimeout) {
        this(readTimeout, writeTimeout, connectTimeout, DEFAULT_TIME_UNIT);
    }

    public Timeout(int readTimeout, int writeTimeout, int connectTimeout, TimeUnit timeUnit) {
        this.readTimeout = readTimeout;
        this.writeTimeout = writeTimeout;
        this.connectTimeout = connectTimeout;
        this.timeUnit = timeUnit;
    }
}
