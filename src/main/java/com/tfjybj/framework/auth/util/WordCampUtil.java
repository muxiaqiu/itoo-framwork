package com.tfjybj.framework.auth.util;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by will on 16/9/5.
 */
public class WordCampUtil {

    public static final char UNDERLINE = '_';

    public static String camelToUnderline(String param) {
        if (param == null || "".equals(param.trim())) {
            return "";
        }
        int len = param.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append(UNDERLINE);
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String underlineToCamel(String param) {
        if (param == null || "".equals(param.trim())) {
            return "";
        }
        int len = param.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (c == UNDERLINE) {
                if (++i < len) {
                    sb.append(Character.toUpperCase(param.charAt(i)));
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String underlineToCamel2(String param) {
        if (param == null || "".equals(param.trim())) {
            return "";
        }
        StringBuilder sb = new StringBuilder(param);
        Matcher mc = Pattern.compile("_").matcher(param);
        int i = 0;
        while (mc.find()) {
            int position = mc.end() - (i++);
            //String.valueOf(Character.toUpperCase(sb.charAt(position)));
            sb.replace(position - 1, position + 1, sb.substring(position, position + 1).toUpperCase());
        }
        return sb.toString();
    }

    public static Map underlineToCamel(Map map) {
        Map<String, Object> resultMap = new HashMap();
        String resultKey = "";
        for (String key : (Set<String>) map.keySet()) {
            Object obj = map.get(key);
            resultKey = WordCampUtil.underlineToCamel(key);

            if (obj instanceof Map) {
                resultMap.put(resultKey, underlineToCamel((Map) obj));
            } else if (obj instanceof List) {
                resultMap.put(resultKey, underlineToCamel((List) obj));
            } else if (obj == null) {
                resultMap.put(resultKey, "");
            } else {
                resultMap.put(resultKey, obj);
            }
        }
        return resultMap;
    }

    public static List underlineToCamel(List maplist) {
        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < maplist.size(); i++) {
            Object obj = maplist.get(i);
            if (obj instanceof Map) {
                list.add(underlineToCamel((Map) obj));
            } else if (obj instanceof List) {
                list.add(underlineToCamel((List) obj));
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
}
