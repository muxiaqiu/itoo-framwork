package com.tfjybj.framework.mybatis.interceptor;


import com.tfjybj.framework.auth.util.TraceUtil;
import com.tfjybj.framework.auth.util.evn.StackTraceUtil;
import com.tfjybj.framework.json.JsonHelper;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.util.*;

/**
 * Created by will on 24/05/2017.
 */
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})})
public class SqlMonitorInterceptor implements Interceptor {
    private static final Logger logger = LoggerFactory.getLogger(SqlMonitorInterceptor.class);

    private ThreadLocal<Long> beginTime = new ThreadLocal<>();

    private static String getParameterValue(Object obj) {
        String value = null;
        if (obj instanceof String) {
            value = "'" + obj.toString() + "'";
        } else if (obj instanceof Date) {
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
            value = "'" + formatter.format(new Date()) + "'";
        } else {
            if (obj != null) {
                value = obj.toString();
            } else {
                value = "";
            }

        }
        return value;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object result = "";
        beginTime.set(System.currentTimeMillis());
        try {
            result = invocation.proceed();
        } catch (Exception e) {
            doAfterInvocation(invocation, null, e);
            throw e;
        }
        doAfterInvocation(invocation, result, null);
        return result;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this); //mybatis提供的包装工具类
    }

    @Override
    public void setProperties(Properties properties) {
    }

    private void doAfterInvocation(Invocation invocation, Object result, Exception ex) {
        long elapsed = System.currentTimeMillis() - beginTime.get();

        try {
            Object[] arguments = invocation.getArgs();
            MappedStatement mappedStatement = (MappedStatement) arguments[0];
            String sqlId = mappedStatement.getId();

            Object parameter = null;
            if (arguments.length > 1) {
                parameter = arguments[1];
            }

            BoundSql boundSql = mappedStatement.getBoundSql(parameter);
            Object paramObject = boundSql.getParameterObject();

            Configuration configuration = mappedStatement.getConfiguration();
//            String sql = boundSql.getSql().toLowerCase(Locale.CHINA).replaceAll("[\\s]+", " ");

            String real = showSql(configuration, boundSql);

            StringBuilder logInfo = new StringBuilder();
            logInfo.setLength(0);
            logInfo.append("〖SQL〗     " + String.format("%6d ms", elapsed));
            logInfo.append("【 " + real + " 】");

            Map<String, Object> monitorMap = new HashMap<>();
            monitorMap.put("method", sqlId);
            monitorMap.put("commandType", mappedStatement.getSqlCommandType());
            //monitorMap.put("sql", sql);
//            monitorMap.put("real", real);
//            if (paramObject instanceof Map) {
//                monitorMap.put("param", MapUtil.camelToUnderline((Map) paramObject));
//            } else if (paramObject instanceof List) {
//                monitorMap.put("param", MapUtil.camelToUnderline((List) paramObject));
//            } else {
//                monitorMap.put("param", paramObject);
//            }
            monitorMap.put("elapsed", elapsed);

            if (ex == null) {
                monitorMap.put("status", 1);
                //monitorMap.put("result", result);
            } else {
                monitorMap.put("status", 0);
                monitorMap.put("error", ((InvocationTargetException) ex).getCause().getMessage());
            }

            Map traceInfoMap = TraceUtil.getTraceInfo();
            if (traceInfoMap != null) {
                monitorMap.put("trace_info".intern(), JsonHelper.toJson(traceInfoMap));
            }

            // 非现网环境，日志需要限制，否则日志量太大，运维支持力度不够
            /*boolean hasLog = EnvManager.LogDebugMode;
            if (elapsed >= 100) {
                hasLog = true;
            }
            if (mappedStatement.getSqlCommandType() != SqlCommandType.SELECT) {
                hasLog = true;
            }

            if (hasLog) {
                LogCollectManager.common(monitorMap, logInfo.toString(), "sql");
            }*/
        } catch (Exception e) {
            logger.error(StackTraceUtil.getStackTraceEx(e));
        }
    }

    private String getSqlId(Object[] arguments) {
        MappedStatement mappedStatement = (MappedStatement) arguments[0];
        return mappedStatement.getId();
    }

    private String getSqlStatement(Object[] arguments) {
        MappedStatement mappedStatement = (MappedStatement) arguments[0];
        Object parameter = null;
        if (arguments.length > 1) {
            parameter = arguments[1];
        }
        String sqlId = mappedStatement.getId();
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        Configuration configuration = mappedStatement.getConfiguration();
        String sql = showSql(configuration, boundSql);
        StringBuilder str = new StringBuilder(100);
        str.append(sqlId);
        str.append(":");
        str.append(sql);
//        str.append(":");
        return str.toString();
    }

    public String showSql(Configuration configuration, BoundSql boundSql) {
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
        if (parameterMappings.size() > 0 && parameterObject != null) {
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                sql = sql.replaceFirst("\\?", getParameterValue(parameterObject));

            } else {
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                for (ParameterMapping parameterMapping : parameterMappings) {
                    String propertyName = parameterMapping.getProperty();
                    if (metaObject.hasGetter(propertyName)) {
                        Object obj = metaObject.getValue(propertyName);
                        sql = sql.replaceFirst("\\?", getParameterValue(obj));
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        Object obj = boundSql.getAdditionalParameter(propertyName);
                        sql = sql.replaceFirst("\\?", getParameterValue(obj));
                    }
                }
            }
        }
        return sql;
    }

}
