package com.tfjybj.framework.log;


import com.tfjybj.framework.auth.util.*;
import com.tfjybj.framework.auth.util.date.DateUtils;
import com.tfjybj.framework.auth.util.evn.StackTraceUtil;
import com.tfjybj.framework.auth.util.web.HtmlUtil;
import com.tfjybj.framework.json.JsonHelper;
import com.tfjybj.framework.result.ApiResultWrapper;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by will on 27/09/2016.
 */
public class LogCollectManager {

    private final static String ES_INDEX = "es_index".intern();
    private final static String ES_TYPE = "es_type".intern();
    protected static Logger logger = LoggerFactory.getLogger(LogCollectManager.class);
    private static Date lastCheckDate = null;

    public static void http(HttpServletRequest request, Date requestTime, HttpServletResponse response, Date responseTime) {
        try {
            Map<String, Object> httpMap = WebUtils.getHttpInfo(request, requestTime, response, responseTime);

            String responseBody = ApiResultWrapper.getResponseBody();
            if (!StringUtils.isEmpty(responseBody)) {
                httpMap.put("result", responseBody);
            }

            String exception = ApiResultWrapper.getException();
            if (!StringUtils.isEmpty(exception)) {
                httpMap.put("exception", exception);
            }

            httpMap.put("trace_info", JsonHelper.toJson(TraceUtil.getTraceInfo()));

            String message = "";
            if (StringUtils.isEmpty(StringUtils.safeToString(httpMap.get("params")))) {
                message = response.getStatus() + "_".intern() + request.getMethod() + "_".intern() + request.getRequestURI() + "_".intern()
                        + StringUtils.safeToString(httpMap.get("elapsed"));
            } else {
                message = response.getStatus() + "_".intern() + request.getMethod() + "_".intern() + request.getRequestURI() + "_".intern() +
                        StringUtils.safeToString(httpMap.get("params".intern())) + "_".intern() + StringUtils.safeToString(httpMap.get("elapsed"));
            }

            common(httpMap, message, "http", "response".intern(), requestTime);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }

    public static void warn(String warnMsg) {
        common(null, warnMsg, "warn", new Date());
    }

    public static void warn(String warnMsg, Logger logger) {
        warn(warnMsg);
    }

    public static void error(String errorMsg) {
        common(null, errorMsg, "error", new Date());
    }

    public static void error(String errorMsg, Logger logger) {
        error(errorMsg);
    }

    public static void error(String errorMsg, Throwable ex) {
        try {
            String errorLog = errorMsg + "\n" + StackTraceUtil.getStackTraceEx(ex);
            common(null, errorLog, "error", new Date());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public static void error(String errorMsg, Throwable ex, Logger logger) {
        error(errorMsg, ex);
    }

    public static void info(String message) {
        common(null, message, "info", new Date());
    }

    public static void info(String message, Logger logger) {
        info(message);
    }

    public static void monitor(String dataType, String message) {
        common(null, message, "monitor", dataType, new Date());
    }

    public static void rcMsg(Map<String, Object> paramMap, String dataType, String message) {
        common(paramMap, message, "rcmsg", dataType, new Date());
    }

    public static void yd(Map<String, Object> paramMap, String message) {
        common(paramMap, message, "yd", new Date());
    }

    public static void dubboAccess(Map<String, Object> dubboMap, String message, Date startDate) {
        common(dubboMap, message, "dubbo_access", startDate);
    }

 /*   public static void dubboMonitor(Statistics statistics) {
        try {
            Map dataMap = JsonHelper.objToMap(statistics);
            common(dataMap, JsonHelper.toPrettyJson(statistics), "dubbo_monitor", new Date());
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }*/

    public static void order(Map<String, Object> orderMap, String message) {
        try {
            //特殊处理
            if (orderMap.containsKey("product_nameh_sort")) {
                orderMap.put("product_name_short", orderMap.get("product_nameh_sort"));
                orderMap.remove("product_nameh_sort");
            }

            common(orderMap, message, "order");
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * 精准数据采集通用方法
     *
     * @param message   日志内容，要求便于搜索和区分
     * @param indexType 日志类型，多种日志不建议重复
     */
    public static void common(String message, String indexType) {
        common(null, message, indexType, null, new Date());
    }

    /**
     * 精准数据采集通用方法
     *
     * @param message   日志内容，要求便于搜索和区分
     * @param indexType 日志类型，多种日志不建议重复
     * @param dataType  日志子类型
     */
    public static void common(String message, String indexType, String dataType) {
        common(null, message, indexType, dataType, new Date());
    }

    /**
     * 精准数据采集通用方法
     *
     * @param paramMap  用于统计的参数字段，最好能包含特征值，如用户ID、手机号等
     * @param indexType 日志类型，多种日志不建议重复
     * @param indexType 日志类型
     */
    public static void common(Map<String, Object> paramMap, String message, String indexType) {
        common(paramMap, message, indexType, null, new Date());
    }

    /**
     * 精准数据采集通用方法
     *
     * @param paramMap  用于统计的参数字段，最好能包含特征值，如用户ID、手机号等
     * @param message   日志内容，要求便于搜索和区分
     * @param indexType 日志类型，多种日志不建议重复
     * @param date      日志采集时间
     */
    public static void common(Map<String, Object> paramMap, String message, String indexType, Date date) {
        common(paramMap, message, indexType, null, date);
    }

    /**
     * 精准数据采集通用方法
     *
     * @param paramMap  用于统计的参数字段，最好能包含特征值，如用户ID、手机号等
     * @param message   日志内容，要求便于搜索和区分
     * @param indexType 日志类型，多种日志不建议重复
     * @param dataType  日志子类型
     */
    public static void common(Map<String, Object> paramMap, String message, String indexType, String dataType) {
        common(paramMap, message, indexType, dataType, new Date());
    }

    /**
     * 精准数据采集通用方法
     *
     * @param paramMap  用于统计的参数字段，最好能包含特征值，如用户ID、手机号等
     * @param message   日志内容，要求便于搜索和区分
     * @param indexType 日志类型，多种日志不建议重复
     * @param dataType  日志子类型
     * @param date      日志采集时间
     */
    public static void common(Map<String, Object> paramMap, String message, String indexType, String dataType, Date date) {
        // 输出日志
        if (!ValueUtil.isEmpty(message)) {
            if ("error".equals(indexType)) {
                logger.error(message);
            } else if ("error".equals(dataType)) {
                logger.error(message);
            } else {
                logger.info(message);
            }
        }

        try {
            // 处理字段
            paramMap = setAppInfo(paramMap, message, date);

            if (TraceUtil.getTraceInfo() == null) {
                Map<String, Object> traceInfoMap = new HashMap<>();
                traceInfoMap.put("traceid", "TRC_" + IdWorker.getId());
                TraceUtil.setTraceInfo(traceInfoMap);
            }

            // 生成链路追踪
            if (!paramMap.containsKey("trace_info".intern())) {
                Map traceInfoMap = TraceUtil.getTraceInfo();
                if (traceInfoMap != null) {
                    paramMap.put("trace_info".intern(), JsonHelper.toJson(traceInfoMap));
                }
            }

            // 生成索引
            if (StringUtils.isNotEmpty(dataType)) {
                paramMap.put("data_type", dataType);
            }
            paramMap.put(ES_INDEX, "idx_log_".intern() + indexType.toLowerCase() + "_".intern() + DateUtils.getYear() + DateUtils.getMonth());
            paramMap.put(ES_TYPE, AppManager.getAppName());

            // 上传到 kafka
            sendKafkaMessage("tpc_log_" + indexType.toLowerCase(), JsonHelper.toJson(paramMap));
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }

    private static String replaceSecretMessage(String message, String secretKey) {
        if (!message.contains(secretKey)) {
            return message;
        }

        String format = "\"" + secretKey + "\":\"{secretValue}\"";
        String regex = "\"" + secretKey + "\":\"(.*?)\"";//使用非贪婪模式
        String replaceBy = "\"" + secretKey + "\":\"******\"";

        Matcher matcher = Pattern.compile(regex).matcher(message);
        while (matcher.find()) {
            String ret = matcher.group(1);
            String strReplace = format.replace("{secretValue}", ret);
            message = message.replace(strReplace, replaceBy);
        }
        return message;
    }

    private static Map<String, Object> setAppInfo(Map<String, Object> dataMap, String message, Date date) {
        if (dataMap == null) {
            dataMap = new HashedMap();
        } else {
            dataMap = MapUtil.camelToUnderline(dataMap);
        }
        dataMap.put("@thread".intern(), Thread.currentThread().getName());
        dataMap.put("@host".intern(), AppManager.getHostName()); //mac not used
        dataMap.put("@port".intern(), AppManager.getAppPort());
        dataMap.put("@leader".intern(), AppManager.getAppLeader());
        dataMap.put("@appname".intern(), AppManager.getAppName());
        dataMap.put("@detail".intern(), HtmlUtil.html2Text(message).replace("\\\"", "\""));

        try {
            for (String key : dataMap.keySet()) {
                String strOldValue = ValueUtil.toString(dataMap.get(key));
                // newPassword
                String strNewValue = strOldValue;
                strNewValue = replaceSecretMessage(strNewValue, "newPassword");
                // tPasswd
                strNewValue = replaceSecretMessage(strNewValue, "tPasswd");
                strNewValue = replaceSecretMessage(strNewValue, "tpasswd");
                // password
                strNewValue = replaceSecretMessage(strNewValue, "password");
                // newpwd
                strNewValue = replaceSecretMessage(strNewValue, "newPwd");

                if (!strNewValue.equals(strOldValue)) {
                    dataMap.put(key, strNewValue);
                }
            }

        } catch (Exception ex) {
        }


        if (dataMap.containsKey("ip")) {
            String ip = StringUtils.safeToString(dataMap.get("ip"));
            if (StringUtils.isEmpty(ip)) {
                dataMap.put("ip", "127.0.0.1");
            }
        }

        dataMap.put("@timestamp".intern(), formatISO8601Date(date));
        return dataMap;
    }



    private static Date parseISO8601Date(String dateString) {
        final String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'".intern();
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.US);
        dateFormat.setTimeZone(new SimpleTimeZone(0, "GMT".intern()));
        Date date = null;
        try {
            date = dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    private static String formatISO8601Date(Date date) {
        final String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'".intern();
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.US);
        dateFormat.setTimeZone(new SimpleTimeZone(0, "GMT".intern()));
        return dateFormat.format(date);
    }

    private static void sendKafkaMessage(String topic, String data) {
        /*if (kafkaService == null) {
            kafkaService = (KafkaService) SpringContextHolder.getBean("kafkaService".intern());
        }
        kafkaService.sendMessage(topic, data);*/

        System.out.println(String.format("打印Kafka日志 param <> topic : {1} , data : {2}",topic,data));
    }

    public static void main(String[] args) {
    }
}
