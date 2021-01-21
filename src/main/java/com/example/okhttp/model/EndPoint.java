package com.example.okhttp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class EndPoint {
    public static final String DEFAULT_PROTOCOL = "http";
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 8080;
    public static final String DEFAULT_BASE_PATH = "";

    private String protocol;
    private String host;
    private int port;
    private String basePath;
    private String username;
    private String password;

    public EndPoint() {
        this(DEFAULT_PROTOCOL, DEFAULT_HOST, DEFAULT_PORT, DEFAULT_BASE_PATH);
    }

    public EndPoint(String protocol, String host, int port) {
        this(protocol, host, port, DEFAULT_BASE_PATH);
    }

    public EndPoint(String protocol, String host, int port, String basePath) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.basePath = basePath;
    }

    public static class Builder {
        private EndPoint endPoint;

        public Builder() {
            this.endPoint = new EndPoint();
        }

        public Builder protocol(String val) {
            this.endPoint.protocol = val;
            return this;
        }

        public Builder host(String val) {
            this.endPoint.host = val;
            return this;
        }

        public Builder port(int val) {
            this.endPoint.port = val;
            return this;
        }

        public Builder basePath(String val) {
            this.endPoint.basePath = val;
            return this;
        }

        public Builder username(String val) {
            this.endPoint.username = val;
            return this;
        }

        public Builder password(String val) {
            this.endPoint.password = val;
            return this;
        }

        public EndPoint build() {
            return this.endPoint;
        }
    }

    public static EndPoint.Builder builder() {
        return new EndPoint.Builder();
    }

    public String getUrl() {
       return this.protocol + "://" + this.host + ":" + this.port + this.basePath;
    }
}
