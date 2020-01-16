package com.tfjybj.framework.auth.util;

import java.io.Serializable;

/**
 * Created by qmx on 2018/5/10.
 */
public class DataAuthInfoForTrace implements Serializable {
    private static final long serialVersionUID = 4885908766129502636L;

    private String token;
    private String userId;
    private String name;
    private String ip;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    private String method;
    private String param;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getMapperId() {
        return mapperId;
    }

    public void setMapperId(String mapperId) {
        this.mapperId = mapperId;
    }

    private String uri;
    private String traceId;

    private String mapperId;
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    private String type;
}
