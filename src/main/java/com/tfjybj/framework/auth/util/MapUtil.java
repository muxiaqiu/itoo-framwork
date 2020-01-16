package com.tfjybj.framework.auth.util;

import java.util.*;

/**
 * Created by will on 09/02/2017.
 */
public class MapUtil {
    /**
     * 把map中value为null的全部替换为空字符串
     *
     * @param map
     */
    public static Map<String, Object> underlineToCamel(Map<String, Object> map) {
        Map<String, Object> tempMap = new HashMap();
        String resultKey = "";
        for (String key : map.keySet()) {
            Object obj = map.get(key);
            resultKey = key;
//            if (resultKey.contains("_")) {
            resultKey = WordCampUtil.underlineToCamel(key);
//            }

            if (obj instanceof Map) {
                tempMap.put(resultKey, underlineToCamel((Map<String, Object>) obj));
            } else if (obj instanceof List) {
                tempMap.put(resultKey, underlineToCamel((List<Object>) obj));
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
    public static List<Object> underlineToCamel(List<Object> maplist) {
        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < maplist.size(); i++) {
            Object obj = maplist.get(i);
            if (obj instanceof Map) {
                list.add(underlineToCamel((Map<String, Object>) obj));
            } else if (obj instanceof List) {
                list.add(underlineToCamel((List<Object>) obj));
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

    /**
     * 把map中value为null的全部替换为空字符串
     *
     * @param map
     */
    public static Map<String, Object> camelToUnderline(Map<String, Object> map) {
        Map<String, Object> tempMap = new HashMap();
        String resultKey = "";
        for (String key : map.keySet()) {
            Object obj = map.get(key);
            resultKey = key;
            resultKey = WordCampUtil.camelToUnderline(key);

            if (obj instanceof Map) {
                tempMap.put(resultKey, camelToUnderline((Map<String, Object>) obj));
            } else if (obj instanceof List) {
                tempMap.put(resultKey, camelToUnderline((List<Object>) obj));
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
    public static List<Object> camelToUnderline(List<Object> maplist) {
        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < maplist.size(); i++) {
            Object obj = maplist.get(i);
            if (obj instanceof Map) {
                list.add(camelToUnderline((Map<String, Object>) obj));
            } else if (obj instanceof List) {
                list.add(camelToUnderline((List<Object>) obj));
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

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static <K extends Comparable<? super K>, V> Map<K, V> sortByKey(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o1.getKey()).compareTo(o2.getKey());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
