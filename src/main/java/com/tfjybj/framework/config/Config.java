package com.tfjybj.framework.config;


import com.tfjybj.framework.auth.util.PropertiesLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by will on 16/7/1.
 */
public class Config implements InitializingBean {
    private static Logger logger = LoggerFactory.getLogger(Config.class);
    private String location = null;
    private PropertiesLoader propertiesLoader = null;
    private Resource resource = null;
    //	private static File configFile = null;
    private long lastModified = 0;
    //private static long fileLastModified = 0L;

    public static String getSystemProperty(String key) {
        String systemProperty = System.getProperty(key);
        if (systemProperty != null) {
            return systemProperty;
        }
        return null;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void init() {
        propertiesLoader = new PropertiesLoader(this.location);
        ResourceLoader resourceLoader = new PathMatchingResourcePatternResolver();//DefaultResourceLoader();
        resource = resourceLoader.getResource(location);
        try {
            lastModified = resource.lastModified();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        logger.info("如下配置文件内容已加载:" + location);
        //		URL url = Config.class.getResource(location);
        //		configFile = new File(url.getFile());
        //		fileLastModified = configFile.lastModified();
    }

    public boolean isModified() {
        try {
            return resource.lastModified() != lastModified;
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return false;
    }

    public String getProperty(String key) {
        return getProperty(key, "");
    }

    public String getProperty(String key, String defValue) {
        try {
            if (resource.lastModified() != lastModified) {
                logger.info("如下配置文件内容已更改:".intern() + location);
                init();
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        try {
            return propertiesLoader.getProperty(key);
        } catch (Exception e) {
//            logger.error("属性值获取失败:".intern() + key);
            return defValue;
        }
    }

    /**
     * 取出Integer类型的Property，但以System的Property优先.如果都為Null或内容错误则抛出异常.
     */
    public Integer getInteger(String key) {
        return getInteger(key, 0);
    }

    /**
     * 取出Integer类型的Property，但以System的Property优先.如果都為Null則返回Default值，如果内容错误则抛出异常
     */
    public Integer getInteger(String key, Integer defaultValue) {
        String value = getProperty(key);
        if (!StringUtils.isEmpty(value)) {
            try {
                return Integer.valueOf(value);
            } catch (Exception ex) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public Float getFloat(String key) {
        return getFloat(key, 0F);
    }

    public Float getFloat(String key, Float defaultValue) {
        String value = getProperty(key);
        if (!StringUtils.isEmpty(value)) {
            try {
                return Float.valueOf(value);
            } catch (Exception ex) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * 取出Double类型的Property，但以System的Property优先.如果都為Null或内容错误则抛出异常.
     */
    public Double getDouble(String key) {
        return getDouble(key, 0D);
    }

    /**
     * 取出Double类型的Property，但以System的Property优先.如果都為Null則返回Default值，如果内容错误则抛出异常
     */
    public Double getDouble(String key, Double defaultValue) {
        String value = getProperty(key);
        if (!StringUtils.isEmpty(value)) {
            try {
                return Double.valueOf(value);
            } catch (Exception ex) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * 取出Boolean类型的Property，但以System的Property优先.如果都為Null抛出异常,如果内容不是true/false则返回false.
     */
    public Boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    /**
     * 取出Boolean类型的Property，但以System的Property优先.如果都為Null則返回Default值,如果内容不为true/false则返回false.
     */
    public Boolean getBoolean(String key, Boolean defaultValue) {
        String value = getProperty(key);
        if (!StringUtils.isEmpty(value)) {
            try {
                return Boolean.valueOf(value);
            } catch (Exception ex) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public Properties getProperties() {
        return propertiesLoader.getProperties();
    }

    @Override
    public void afterPropertiesSet() {
        init();
    }
}
