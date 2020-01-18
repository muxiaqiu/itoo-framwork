package com.tfjybj.framework.auth.interceptor;


import com.tfjybj.framework.auth.util.*;
import com.tfjybj.framework.json.JsonHelper;
import com.tfjybj.framework.log.LogCollectManager;
import com.tfjybj.framework.result.ApiResultWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义拦截器，对请求进行身份验证
 *
 * @author ScienJus
 * @date 2015/7/30.
 */
public class AbstractAuthInterceptor implements HandlerInterceptor {

    private static final String[] HEADER_KEYS = new String[]{"adviserId", "version", "client", "mid"};
    protected Logger logger = LoggerFactory.getLogger(AbstractAuthInterceptor.class);
    private ThreadLocal<Date> requestTime = new ThreadLocal<>();

    /**
     * This implementation always returns {@code true}.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        requestTime.set(new Date());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    protected void beforeAuth(HttpServletRequest request, HttpServletResponse response, Method method) {
        try {
            String key = "";
            String value = "";
            Map<String, Object> traceInfoMap = TraceUtil.getTraceInfo();
            DataAuthInfoForTrace dataAuthInfoForTrace = new DataAuthInfoForTrace();
            if (traceInfoMap == null) {
                traceInfoMap = new HashMap<String, Object>();
            } else {
                traceInfoMap.clear();
            }

            for (int i = 0; i < HEADER_KEYS.length; i++) {
                key = HEADER_KEYS[i];
                value = ValueUtil.toString(request.getHeader(key));
                if (!ValueUtil.isEmpty(value)) {
                    traceInfoMap.put(key, value);
                }
            }
            String traceId = Long.toString(IdWorker.getId());
            dataAuthInfoForTrace.setTraceId("TRC_" + traceId);
            dataAuthInfoForTrace.setUri(request.getRequestURI());
            dataAuthInfoForTrace.setParam(request.getQueryString());
            dataAuthInfoForTrace.setMethod(request.getMethod());
            TraceUtil.setDataAuthInfoThreadLocal(dataAuthInfoForTrace);

            traceInfoMap.put("traceid", "TRC_" + traceId);

            TraceUtil.setTraceInfo(traceInfoMap);
        } catch (Exception ex) {
            logger.error("BeforeAuth Error:" + ex.getMessage());
        }
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {

        httpLog(request, response);
    }

    private void httpLog(HttpServletRequest request, HttpServletResponse response) {
        try {
          /*  if (request.getRequestURI().contains("/ping")) {
                return;
            }*/

//            Map<String, Object> httpInfo = WebUtils.getHttpInfo(request, requestTime.get(), response, new Date());
//            String logInfo = "Request <> ".intern() + httpInfo.get("method") + " " + JsonHelper.toPrettyJson(httpInfo);

//            String responseBody = ApiResultWrapper.getResponseBody();
//            if (!StringUtils.isEmpty(responseBody)) {
//                logInfo = logInfo + "\nResult:" + responseBody;
//            }

//            if (response.getStatus() == 200) {
//                logger.info(logInfo);
//            } else {
//                logger.error(logInfo);
//            }

            LogCollectManager.http(request, requestTime.get(), response, new Date());
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }
}
