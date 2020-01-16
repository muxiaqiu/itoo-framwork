package com.tfjybj.framework.auth.util;

import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by will on 09/12/2016.
 */
public class AppLeaderManager {
    private final static String NAME_DEGAO = "@deng.degao".intern();
    private final static String NAME_XIAOFENG = "@jiang.xiaofeng".intern();
    private final static String NAME_CHUNPING = "@huang.chunping".intern();
    private final static String NAME_XUFENG = "@xu.feng".intern();
    private final static String NAME_ANQI = "@liu.anqi".intern();
    private final static String NAME_BAOFENG = "@kang.baofeng".intern();
    private final static String NAME_GUOLIN = "@wang.guolin".intern();
    private final static String NAME_ANHUI = "@an.hui".intern();
    private final static String NAME_CONGWEI = "@liu.congwei".intern();
    protected static Logger logger = LoggerFactory.getLogger(AppLeaderManager.class);
    private static Map<String, String> appLeaderMap = null;

    static {
        appLeaderMap = new HashedMap();
        appLeaderMap.put("soa-auth-api", joinName(NAME_XUFENG, NAME_ANHUI));
        appLeaderMap.put("soa-app-api", joinName(NAME_XUFENG, NAME_ANHUI));
        appLeaderMap.put("soa-simuyunadmin-api", joinName(NAME_XUFENG, NAME_ANHUI));
        appLeaderMap.put("soa-liveroom-api", joinName(NAME_XUFENG));
        appLeaderMap.put("soa-1314-api", joinName(NAME_CHUNPING));
        appLeaderMap.put("soa-intime-api", joinName(NAME_ANQI, NAME_BAOFENG, NAME_DEGAO));
        appLeaderMap.put("soa-monitor-web", joinName(NAME_XIAOFENG));
        appLeaderMap.put("soa-assets-api", joinName(NAME_DEGAO));
        appLeaderMap.put("soa-promotion-api", joinName(NAME_CONGWEI));

        appLeaderMap.put("soa-order-service", joinName(NAME_DEGAO));
        appLeaderMap.put("soa-organization-service", joinName(NAME_ANQI));
        appLeaderMap.put("soa-auth-service", joinName(NAME_CONGWEI));
        appLeaderMap.put("soa-redemption-service", joinName(NAME_BAOFENG));
        appLeaderMap.put("soa-renew-service", joinName(NAME_ANQI));
        appLeaderMap.put("soa-product-service", joinName(NAME_DEGAO));
        appLeaderMap.put("soa-information-service", joinName(NAME_XUFENG, NAME_ANHUI));
        appLeaderMap.put("soa-contract-service", joinName(NAME_ANQI));
        appLeaderMap.put("soa-liquidation-service", joinName(NAME_BAOFENG));
        appLeaderMap.put("soa-user-service", joinName(NAME_ANQI));
        appLeaderMap.put("soa-finance-service", joinName(NAME_BAOFENG));
        appLeaderMap.put("soa-common-service", joinName(NAME_XIAOFENG));
        appLeaderMap.put("soa-monitor-service", joinName(NAME_XIAOFENG));
        appLeaderMap.put("soa-task-service", joinName(NAME_XUFENG));
        appLeaderMap.put("soa-test-service", joinName(NAME_XIAOFENG));
        appLeaderMap.put("soa-cache-service", joinName(NAME_XIAOFENG));
        appLeaderMap.put("soa-liveroom-service", joinName(NAME_XUFENG));
        appLeaderMap.put("soa-datacollect-service", joinName(NAME_XIAOFENG));
        appLeaderMap.put("soa-premotion-service", joinName(NAME_CONGWEI));
        appLeaderMap.put("soa-ta-service", joinName(NAME_ANQI, NAME_BAOFENG));
        appLeaderMap.put("soa-assets-service", joinName(NAME_DEGAO));
        appLeaderMap.put("soa-ydbank-service", joinName(NAME_XUFENG));
    }

    public synchronized static String matchAppLeader(String appName) {
        if (StringUtils.isEmpty(appName)) {
            return "@all";
        }
        String leader = appLeaderMap.get(appName.trim());//"〖 " + appLeaderMap.get(appName.trim()) + " 〗";
        if (StringUtils.isEmpty(leader)) {
            return "@all";
        }
        return leader;
    }

    private static String joinName(String... names) {
        return StringUtils.joinString(names, ",");
    }

    public static void main(String[] args) {
        logger.info(joinName(NAME_ANHUI));
        logger.info(joinName(NAME_ANHUI, NAME_BAOFENG));
        logger.info(joinName(NAME_ANHUI, NAME_BAOFENG, NAME_DEGAO));
    }
}
