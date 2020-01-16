package com.tfjybj.framework.auth.util;


import java.io.Serializable;
import java.util.Map;

/**
 * Created by will on 16/8/30.
 */
public class TraceUtil implements Serializable {
    private static ThreadLocal<Map<String, Object>> traceInfoThreadLocal = new ThreadLocal<>();

    private static ThreadLocal<DataAuthInfoForTrace> dataAuthInfoThreadLocal = new ThreadLocal<>();

    public static DataAuthInfoForTrace getDataAuthInfoThreadLocal() {
        return dataAuthInfoThreadLocal.get();
    }

    public static void setDataAuthInfoThreadLocal(DataAuthInfoForTrace dataAuthInfoThreadLocal) {
        TraceUtil.dataAuthInfoThreadLocal.set(dataAuthInfoThreadLocal);
    }

    public static Map<String, Object> getTraceInfo() {
        return traceInfoThreadLocal.get();
    }

    public static void setTraceInfo(Map<String, Object> traceInfo) {
        traceInfoThreadLocal.set(traceInfo);
    }
}
