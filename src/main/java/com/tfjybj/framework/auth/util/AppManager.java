package com.tfjybj.framework.auth.util;



import com.tfjybj.framework.auth.util.evn.IpUtil;
import com.tfjybj.framework.config.ConfigManager;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by will on 29/09/2016.
 */
public class AppManager {
    private static String hostName = null;
    private static String localIp = null;
    private static String appLeader = null;
    private static String appName = null;
    private static String dubboPort = null;

    public synchronized static String getAppName() {
        if (!StringUtils.isEmpty(appName)) {
            return appName;
        }

        appName = ConfigManager.getConfig().getSystemProperty("dubbo.app.name".intern());
        return appName;
    }

    public synchronized static String getAppPort() {
        if (!StringUtils.isEmpty(dubboPort)) {
            return dubboPort;
        }

        dubboPort = ConfigManager.getConfig().getSystemProperty("dubbo.protocol.port".intern());
        return dubboPort;
    }

    public synchronized static String getLocalIP() {
        if (!StringUtils.isEmpty(localIp)) {
            return localIp;
        }

        localIp = IpUtil.getLocalIPAddr();
        return localIp;
    }

    public synchronized static String getHostName() {
        if (!StringUtils.isEmpty(hostName)) {
            return hostName;
        }

        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostName = getLocalIP();
        }
        return hostName;
    }

    public synchronized static String getAppLeader() {
        if (!StringUtils.isEmpty(appLeader)) {
            return appLeader;
        }

        appLeader = AppLeaderManager.matchAppLeader(AppManager.getAppName());
        return appLeader;
    }

    public static boolean checkMID(String mid) {
        if (!ValueUtil.isEmpty(mid) && (mid.startsWith("A-") || mid.startsWith("I-"))) {
            return true;
        }
        return false;
    }

    public static boolean checkClient(String client) {
        if (!ValueUtil.isEmpty(client) && (client.equals("B") || client.equals("C"))) {
            return true;
        }
        return false;
    }
}
