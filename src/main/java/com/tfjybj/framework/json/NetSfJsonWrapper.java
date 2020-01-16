package com.tfjybj.framework.json;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONString;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Created by will on 12/07/2017.
 */
public class NetSfJsonWrapper {

    public static boolean isBadJson(String jsonStr) {
        return !isGoodJson(jsonStr);
    }

    public static boolean isGoodJson(String jsonStr) {
        if (StringUtils.isEmpty(jsonStr)) {
            return false;
        }
        try {
            new JsonParser().parse(jsonStr);
        } catch (JsonParseException e) {
            return false;
        }
        return true;
    }

    public static String toJson(Object obj) {
        if (obj == null) {
            return "";
        }

        return JSONObject.fromObject(obj).toString();
    }

//    public static Map<String, Object> objToMap(Object entity) {
//        return toMap(GsonWrapper.toJson(entity));
//    }
//
//    public static List<Map<String, Object>> objToMapList(Object entity) {
//        return toMapList(GsonWrapper.toJson(entity));
//    }
//
//    public static List<Object> objToList(Object entity) {
//        return toObjList(GsonWrapper.toJson(entity));
//    }

    public static List<Object> toList(String jsonStr) {
        if (StringUtils.isEmpty(jsonStr)) {
            return null;
        }

        List<Object> list = new ArrayList<>();
        JSONArray jsonArr = JSONArray.fromObject(jsonStr);
        for (Iterator iterator = jsonArr.iterator(); iterator.hasNext(); ) {
            list.add(parserValue(iterator.next()));
        }
        return list;
    }

    public static List<Map<String, Object>> toMapList(String jsonStr) {
        if (StringUtils.isEmpty(jsonStr)) {
            return null;
        }

        JSONArray jsonArr = JSONArray.fromObject(jsonStr);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Iterator<JSONObject> it = jsonArr.iterator();
        while (it.hasNext()) {
            list.add(toMap(it.next().toString()));
        }
        return list;
    }

    public static Map<String, Object> toMap(String jsonStr) {
        if (StringUtils.isEmpty(jsonStr)) {
            return null;
        }

        // 方法一
        //paramMap = (Map<String, Object>) JSONObject.toBean(JSONObject.fromObject(jsonStr), HashMap.class);

        // 方法二
        Map<String, Object> map = new HashMap<String, Object>();
        // 最外层解析
        JSONObject json = JSONObject.fromObject(jsonStr);
        for (Object k : json.keySet()) {
            map.put(k.toString(), parserValue(json.get(k)));
        }
        return map;
    }

    private static Object parserValue(Object v) {
        String vstr = v.toString();
        if (v instanceof JSONArray) {
            return toList(vstr);
        } else if (v instanceof JSONObject) {
            return toMap(vstr);
        } else if (v instanceof JSONString) {
            return vstr;
        } else {
            return v;
        }
    }
}
