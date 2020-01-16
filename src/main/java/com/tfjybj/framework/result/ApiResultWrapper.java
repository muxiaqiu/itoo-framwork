package com.tfjybj.framework.result;


import com.tfjybj.framework.auth.util.StringUtils;
import com.tfjybj.framework.auth.util.WebUtils;
import com.tfjybj.framework.auth.util.WordCampUtil;
import com.tfjybj.framework.auth.util.evn.StackTraceUtil;
import com.tfjybj.framework.json.JsonHelper;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Conventions;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by Jiang on 2015/9/9.
 */
public class ApiResultWrapper implements Serializable {
    public static final HttpStatus STATUS_OK = HttpStatus.OK;
    private static final long serialVersionUID = 1L;
    private static ThreadLocal<Object> responseBodyThreadLocal = new ThreadLocal<Object>();
    private static ThreadLocal<String> requestBodyThreadLocal = new ThreadLocal<String>();
    private static ThreadLocal<String> exceptionThreadLocal = new ThreadLocal<String>();
    protected static Logger logger = LoggerFactory.getLogger(ApiResultWrapper.class);
    private Map<String, Object> resultMap = new HashMap<String, Object>();
    private Object resultObj = null;
    private HttpStatus httpStatus = HttpStatus.OK;
    private boolean isUnderlineToCamel = true;

    public ApiResultWrapper() {
    }

    public ApiResultWrapper(HttpStatus status) {
        httpStatus = status;
    }

    public static String getResponseBody() {
        Object responseBody = responseBodyThreadLocal.get();
        if (responseBody == null) {
            return "";
        }

        String responseContent = "";
        if (responseBody instanceof String) {
            responseContent = responseBody.toString();
        } else {
            responseContent = JsonHelper.toPrettyJson(responseBody);
        }

        int length = 6000;
        if (responseContent.length() > length) {
            long nanoTime = System.nanoTime();
        	logger.info(nanoTime+"@"+responseContent);
            responseContent = responseContent.substring(0, length) + " ......".intern() + nanoTime;
        }
        return responseContent;
    }

    public static void setResponseBody(Object responseBody) {
        responseBodyThreadLocal.set(responseBody);
    }

    public static String getRequestBody() {
        return requestBodyThreadLocal.get();
    }

    public static void setRequestBody(String requestBody) {
        requestBodyThreadLocal.set(requestBody);
    }

    public static String getException() {
        return exceptionThreadLocal.get();
    }

    public static void setException(String exception) {
        ApiResultWrapper.exceptionThreadLocal.set(exception);
    }

    public static boolean isNormalType(Object obj) {
        if (obj instanceof String) {
            return true;
        } else if (obj instanceof Integer) {
            return true;
        } else if (obj instanceof Double) {
            return true;
        } else if (obj instanceof Float) {
            return true;
        } else if (obj instanceof Long) {
            return true;
        } else if (obj instanceof Boolean) {
            return true;
        } else if (obj instanceof Date) {
            return true;
        }
        return false;
    }

    /**
     * 把map中value为null的全部替换为空字符串
     *
     * @param map
     */
    public static Map<String, Object> setMapNullToEmpty(Map<String, Object> map, boolean isUnderlineToCamel) {
        Map<String, Object> tempMap = new HashMap();
        String resultKey = "";
        for (String key : map.keySet()) {
            Object obj = map.get(key);
            resultKey = key;
            if (isUnderlineToCamel) {
                if (resultKey.contains("_")) {
                    resultKey = WordCampUtil.underlineToCamel(key);
                }
            }
            if (obj instanceof Map) {
                tempMap.put(resultKey, setMapNullToEmpty((Map<String, Object>) obj, isUnderlineToCamel));
            } else if (obj instanceof List) {
                tempMap.put(resultKey, setMapListNullToEmpty((List<Object>) obj, isUnderlineToCamel));
            } else {
                if (null == obj) {
                    tempMap.put(resultKey, "");
                } else {
                    tempMap.put(resultKey, obj);
                }
            }
        }
        return tempMap;
    }

    /**
     * 把map的List中的所有的Map的value为null的全部替换为空字符串
     *
     * @param maplist
     */
    public static List<Object> setMapListNullToEmpty(List<Object> maplist, boolean isUnderlineToCamel) {
        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < maplist.size(); i++) {
            Object obj = maplist.get(i);
            if (obj instanceof Map) {
                list.add(setMapNullToEmpty((Map<String, Object>) obj, isUnderlineToCamel));
            } else if (obj instanceof List) {
                list.add(setMapListNullToEmpty((List<Object>) obj, isUnderlineToCamel));
            } else {
                if (null == obj) {
                    list.add("");
                } else {
                    list.add(obj);
                }
            }
        }
        return list;
    }

    public static Map<String, Object> objectToMap(Object obj) throws InvocationTargetException, IllegalAccessException, IntrospectionException {
        if (obj == null)
            return null;

        Map<String, Object> map = new HashMap<String, Object>();

        BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor property : propertyDescriptors) {
            String key = property.getName();
            if (key.compareToIgnoreCase("class") == 0) {
                continue;
            }
            Method getter = property.getReadMethod();
            Object value = getter != null ? getter.invoke(obj) : null;
            map.put(key, value);
        }

        return map;
    }

    public static Object mapToObject(Map<String, Object> map, Class<?> beanClass) throws Exception {
        if (map == null)
            return null;

        Object obj = beanClass.newInstance();

        BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor property : propertyDescriptors) {
            Method setter = property.getWriteMethod();
            if (setter != null) {
                setter.invoke(obj, map.get(property.getName()));
            }
        }

        return obj;
    }

    public static Map<String, Object> filterRequestParams(Map<String, String[]> requestParam) {
        Map<String, Object> bodyMap = new HashedMap();
        for (String key : requestParam.keySet()) {
            if (key.toLowerCase().equals("password")) {
                bodyMap.put(key, "******");
                continue;
            }
            if (key.equals("_")) {
                continue;
            }
            bodyMap.put(key, requestParam.get(key)[0]);
        }
        return bodyMap;
    }

    public ApiResultWrapper setStatus(HttpStatus status) {
        httpStatus = status;
        return this;
    }

    public ApiResultWrapper setMessage(String message) {
        resultMap.put("message", message);
        return this;
    }

    public ApiResultWrapper setCode(String code) {
        resultMap.put("code", code);
        return this;
    }

    public ApiResultWrapper setObj(Object modelData) {
        resultObj = modelData;
        return this;
    }

    public ApiResultWrapper addData(Object modelData) {
        addData(Conventions.getVariableName(modelData), modelData);
        return this;
    }

    public ApiResultWrapper addData(String modelName, Object modelData) {
        resultMap.put(modelName, modelData);
        return this;
    }

    public ApiResultWrapper removeData(String modelName) {
        if (resultMap.containsKey(modelName)) {
            resultMap.remove(modelName);
        }
        return this;
    }

    public boolean isUnderlineToCamel() {
        return isUnderlineToCamel;
    }

    public ApiResultWrapper setUnderlineToCamel(boolean underlineToCamel) {
        isUnderlineToCamel = underlineToCamel;
        return this;
    }

    public ApiResultWrapper clear() {
        resultMap.clear();
        return this;
    }

    public Map<String, Object> toMap() {
        return setMapNullToEmpty(resultMap, isUnderlineToCamel);
    }

    public void toNewResponse() {
        if (!resultMap.containsKey("code")) {
            resultMap.put("code", "");
        }

        if (!resultMap.containsKey("message")) {
            resultMap.put("message", "");
        }

//        Object resultObjTemp = null;
//        if (resultObj != null) {
//            if (resultObj instanceof Map) {
//                resultObjTemp = setMapNullToEmpty((Map) resultObj, isUnderlineToCamel);
//            } else if (resultObj instanceof List) {
//                resultObjTemp = setMapListNullToEmpty((List) resultObj, isUnderlineToCamel);
//            }
//            resultObj = null;
//        }

        if (!resultMap.containsKey("result")) {
            resultMap.put("result", resultObj);
        }
        resultObj = null;

        toResponse();
    }

    public void toResponse() {

        HttpServletResponse response = WebUtils.getHttpServletResponse();
        HttpServletRequest request = WebUtils.getHttpServletRequest();

        response.setStatus(httpStatus.value());
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));

            String responseContent = "";
            //Map map = null;
            if (resultObj != null) {
                ApiResultWrapper.setResponseBody(resultObj);

                if (resultObj instanceof Map) {
                    Map map = setMapNullToEmpty((Map) resultObj, isUnderlineToCamel);
                    responseContent = JsonHelper.toPrettyJson(map);
                } else if (resultObj instanceof List) {
                    List list = setMapListNullToEmpty((List) resultObj, isUnderlineToCamel);
                    responseContent = JsonHelper.toPrettyJson(list);
                } else {
                    if (isNormalType(resultObj)) {
                        responseContent = resultObj.toString();
                    } else {
                        responseContent = JsonHelper.toPrettyJson(resultObj);
                    }
                }
            } else if (resultMap.size() > 0) {
                Map map = toMap();
                ApiResultWrapper.setResponseBody(map);
                responseContent = JsonHelper.toPrettyJson(map);
            }

            if (!StringUtils.isEmpty(responseContent)) {
                writer.write(responseContent);
            }

        } catch (IOException e) {
            logger.error(StackTraceUtil.getStackTraceEx(e));
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                logger.error(StackTraceUtil.getStackTraceEx(e));
            }
        }
    }
}
