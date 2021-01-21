package com.example.okhttp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Proxy {
    private java.net.Proxy.Type type;
    private String host;
    private int port;
    private String username;
    private String password;

    public static class Builder {
        public Proxy proxy;
        public Builder() {
            this.proxy = new Proxy();
        }
    }
}
