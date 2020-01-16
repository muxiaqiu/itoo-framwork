package com.tfjybj.framework.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by will on 16/7/1.
 */
public class ConfigManager {
    private static Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private static Map<String, Config> configMap = new HashMap<String, Config>();

    public static synchronized boolean hasConfig(String configName) {
        return configMap.containsKey(configName);
    }

    /**
     * 默认取 Config 下的配置信息
     *
     * @return
     */
    public static synchronized Config getConfig() {
        if (hasConfig("Config")) {
            return configMap.get("Config");
        }
        return null;
    }

    public static synchronized Config getConfig(String configName) {
        if (hasConfig(configName)) {
            return (Config) configMap.get(configName);
        }
        return null;
    }

    public static synchronized boolean addConfig(String configName, String location) {
        if (hasConfig(configName)) {
            logger.info("已存在如下配置:" + configName);
            return false;
        }
        Config configInfo = new Config();
        try {
            configInfo.setLocation(location);
            configInfo.init();
        } catch (Exception e) {
            logger.error("初始化配置失败:" + location);
            return false;
        }
        configMap.put(configName, configInfo);
        return true;
    }

    public static synchronized boolean deleteConfig(String configName) {
        if (!hasConfig(configName)) {
            logger.error("不存在如下配置:" + configName);
            return false;
        }
        configMap.remove(configName);
        logger.info("已删除如下配置:" + configName);
        return true;
    }
}
