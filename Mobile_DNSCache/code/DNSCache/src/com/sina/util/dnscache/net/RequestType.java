package com.sina.util.dnscache.net;

/**
 * 请求的类型。GET/POST
 * 
 * @author xingyu10
 *
 */
public enum RequestType {
    GET("GET"), POST("POST");

    private String method;

    RequestType(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }
}