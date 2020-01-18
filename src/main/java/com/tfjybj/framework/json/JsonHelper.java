package com.tfjybj.framework.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.tfjybj.framework.exception.ApplicationException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsonHelper {
    private static final Logger logger = LoggerFactory.getLogger(JsonHelper.class);
    private static JsonHelper util = new JsonHelper();

    public static String toPrettyJson(Object value, boolean transferNullToEmpty) {
        if (value == null) {
            return "";
        }

        if (transferNullToEmpty) {
            Object newObj = transferMapAndList(value);
            return GsonWrapper.toPrettyJson(newObj);
        } else {
            return GsonWrapper.toPrettyJson(value);
        }
    }

    public static String toPrettyJson(Object value) {
//        return toPrettyJson(value, true);
        return FastJsonWrapper.toPrettyJson(value);
    }

    public static String toJson(Object value, boolean transferNullToEmpty) {
        if (value == null) {
            return "";
        }

        if (transferNullToEmpty) {
            Object newObj = transferMapAndList(value);
            return GsonWrapper.toJson(newObj);
        } else {
            return GsonWrapper.toJson(value);
        }
    }

    public static String toJson(Object value) {
//        return toJson(value, true);
        return FastJsonWrapper.toJson(value);
    }

    public static Object transferMapAndList(Object object) {
        Object newObj = object;
        if (object instanceof Map) {
            //Map map = objToMap(object);
            newObj = setMapNullToEmpty((Map) object);
        } else if (object instanceof List) {
            //List<Object> list = objToList(object);
            newObj = setMapListNullToEmpty((List<Object>) object);
        }
        return newObj;
    }

    /**
     * 把map中value为null的全部替换为空字符串
     *
     * @param map
     */
    public static Map<String, Object> setMapNullToEmpty(Map<String, Object> map) {
        for (String key : map.keySet()) {
            Object obj = map.get(key);
            if (obj instanceof Map) {
                map.put(key, setMapNullToEmpty((Map<String, Object>) obj));
            } else if (obj instanceof List) {
                map.put(key, setMapListNullToEmpty((List<Object>) obj));
            } else {
                if (null == obj) {
                    map.put(key, "");
                }
            }
        }
        return map;
    }

    /**
     * 把map的List中的所有的Map的value为null的全部替换为空字符串
     *
     * @param maplist
     */
    public static List<Object> setMapListNullToEmpty(List<Object> maplist) {
        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < maplist.size(); i++) {
            Object obj = maplist.get(i);
            if (obj instanceof Map) {
                list.add(setMapNullToEmpty((Map<String, Object>) obj));
            } else if (obj instanceof List) {
                list.add(setMapListNullToEmpty((List<Object>) obj));
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

    public static <T> T toObject(String jsonString, Class<T> cls) throws Exception {
        T t = null;
        try {
            if (isGoodJson(jsonString)) {
                Gson gson = new Gson();
                t = gson.fromJson(jsonString, cls);
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return t;
    }

    public static <T> List<T> toList(String jsonString, TypeToken<T> tt) {
        List<T> list = new ArrayList<T>();
        try {
            if (isGoodJson(jsonString)) {
                Gson gson = new Gson();
                list = gson.fromJson(jsonString, tt.getType());
            }
        } catch (Exception e) {
        }
        return list;
    }

    public static boolean isBadJson(String json) {
        return !isGoodJson(json);
    }

    public static boolean isGoodJson(String json) {
//        if (StringUtils.isEmpty(json)) {
//            return false;
//        }
//        try {
//            new JsonParser().parse(json);
//        } catch (JsonParseException e) {
//            return false;
//        }
//        return true;
        return NetSfJsonWrapper.isGoodJson(json);
    }

    public static List<Object> parseJson2List(String jsonStr) {
//        if (StringUtils.isEmpty(jsonStr)) {
//            return null;
//        }
//
//        List<Object> list = new ArrayList<Object>();
//        JSONArray jsonArr = JSONArray.fromObject(jsonStr);
//        for (Iterator iterator = jsonArr.iterator(); iterator.hasNext(); ) {
//            list.add(parserValue(iterator.next()));
//        }
//        return list;
        return NetSfJsonWrapper.toList(jsonStr);
    }

    public static Map<String, Object> parserJson2Map(String jsonStr) {
//        if (StringUtils.isEmpty(jsonStr)) {
//            return null;
//        }
//
//        Map<String, Object> map = new HashMap<String, Object>();
//        // 最外层解析
//        JSONObject json = JSONObject.fromObject(jsonStr);
//        for (Object k : json.keySet()) {
//            map.put(k.toString(), parserValue(json.get(k)));
//        }
//        return map;
        return NetSfJsonWrapper.toMap(jsonStr);
    }

//    private static Object parserValue(Object v) {
//        String vstr = v.toString();
//        if (v instanceof JSONArray) {
//            return parseJson2List(vstr);
//        } else if (v instanceof JSONObject) {
//            return parserJson2Map(vstr);
//        } else if (v instanceof JSONString) {
//            return vstr;
//        } else {
//            return v;
//        }
//    }

    public static List<Map<String, Object>> toList(String jsonStr) {
//        if (StringUtils.isEmpty(jsonStr)) {
//            return null;
//        }
//
//        JSONArray jsonArr = JSONArray.fromObject(jsonStr);
//        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
//        Iterator<JSONObject> it = jsonArr.iterator();
//        while (it.hasNext()) {
//            list.add(parserJson2Map(it.next().toString()));
//        }
//        return list;
        return NetSfJsonWrapper.toMapList(jsonStr);
    }

    public static final String encodeObject2Json(Object pObject) {
        String jsonString = "[]";
        if (StringUtils.isEmpty(pObject)) {
        } else {
            JsonConfig cfg = new JsonConfig();
            cfg.registerJsonValueProcessor(BigDecimal.class, new JsonValueProcessor() {
                @Override
                public Object processObjectValue(String key, Object value, JsonConfig jsonConfig) {
                    return value.toString();
                }

                @Override
                public Object processArrayValue(Object arg0, JsonConfig arg1) {
                    return arg0.toString();
                }
            });
            cfg.registerJsonValueProcessor(Integer.class, new JsonValueProcessor() {
                @Override
                public Object processObjectValue(String key, Object value, JsonConfig jsonConfig) {
                    return value.toString();
                }

                @Override
                public Object processArrayValue(Object arg0, JsonConfig arg1) {
                    return arg0.toString();
                }
            });
            if (pObject instanceof ArrayList) {
                JSONArray jsonArray = JSONArray.fromObject(pObject, cfg);
                jsonString = jsonArray.toString();
            } else {
                JSONObject jsonObject = JSONObject.fromObject(pObject, cfg);
                jsonString = jsonObject.toString();
            }
        }
        return jsonString;
    }

    public static String jsonFormatter(String uglyJSONString) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(uglyJSONString);
        return gson.toJson(je);
    }

    /**
     * 统一参数格式化<JsonString-Map>
     *
     * @param jsonStr Json字符串参数
     * @return paramMap Map集合
     * @author lxiao
     */
    public static Map<String, Object> convertJsonToMap(String jsonStr) {
//        if (StringUtils.isEmpty(jsonStr)) {
//            return null;
//        }
//
//        try {
//            Map<String, Object> paramMap = new HashMap<String, Object>();
//            if (!org.springframework.util.StringUtils.isEmpty(jsonStr)) {
//                paramMap = (Map<String, Object>) JSONObject.toBean(JSONObject.fromObject(jsonStr), HashMap.class);
//            }
//            return paramMap;
//        } catch (Exception e) {
//            throw new ApplicationException("CM0002");
//        }
        return NetSfJsonWrapper.toMap(jsonStr);
    }

    /**
     * 统一参数格式化<JsonString-Map>
     *
     * @param jsonStr Json字符串参数
     * @return paramMap Map集合
     * @author lxiao
     */
    public static List<Map<String, Object>> convertJsonToMapList(String jsonStr) {
//        if (StringUtils.isEmpty(jsonStr)) {
//            return null;
//        }
//
//        try {
//            List<Map<String, Object>> paramMaplst = new ArrayList<Map<String, Object>>();
//            if (!org.springframework.util.StringUtils.isEmpty(jsonStr)) {
//                paramMaplst =
//                        (List<Map<String, Object>>) JSONArray.toList(JSONArray.fromObject(jsonStr), HashMap.class);
//            }
//            return paramMaplst;
//        } catch (Exception e) {
//            throw new ApplicationException("CM0002");
//        }
        return NetSfJsonWrapper.toMapList(jsonStr);
    }

    public static <T> void map2Bean(T entity, Map map) {
        Class clazz = entity.getClass();
        try {
            Set<String> keys = map.keySet();
            // 变量map 赋值
            for (String key : keys) {
                String fieldName = key;
                // 判断是sql 还是hql返回的结果
                if (key.equals(key.toUpperCase())) {
                    // 获取所有域变量
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        if (field.getName().toUpperCase().equals(key))
                            fieldName = field.getName();
                        break;
                    }
                }
                // 设置赋值
                // 参数的类型 clazz.getField(fieldName)
                Class<?> paramClass = clazz.getDeclaredField(fieldName).getType();
                // 拼装set方法名称
                String methodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                // 根据名称获取方法
                Method method = clazz.getMethod(methodName, paramClass);
                // 调用invoke执行赋值
                method.invoke(entity, map.get(key));
            }
        } catch (Exception e) {
            throw new ApplicationException("CM0002");
        }
    }

    public static Map<String, Object> objToMap(Object entity) {
        String json = NetSfJsonWrapper.toJson(entity);
        return NetSfJsonWrapper.toMap(json);
//        String json = GsonWrapper.toJson(entity);
//        return NetSfJsonWrapper.toMap(json);
    }

    public static List<Map<String, Object>> objToMapList(Object entity) {
//        String json = JSONObject.fromObject(entity).toString();
//        return convertJsonToMapList(json);

        String json = NetSfJsonWrapper.toJson(entity);
        return NetSfJsonWrapper.toMapList(json);
//        String json = GsonWrapper.toJson(entity);
//        return NetSfJsonWrapper.toMapList(json);
    }

    public static List<Object> objToList(Object entity) {
        String json = NetSfJsonWrapper.toJson(entity);
        return NetSfJsonWrapper.toList(json);
//        String json = GsonWrapper.toJson(entity);
//        return NetSfJsonWrapper.toList(json);
    }

    // 请求URL取得响应JSON
    public static String getJsonString(String urlPath) throws Exception {
        URL url = new URL(urlPath);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        InputStream inputStream = connection.getInputStream();
        // 对应的字符编码转换
        Reader reader = new InputStreamReader(inputStream, "UTF-8");
        BufferedReader bufferedReader = new BufferedReader(reader);
        String str = null;
        StringBuffer sb = new StringBuffer();
        while ((str = bufferedReader.readLine()) != null) {
            sb.append(str);
        }
        reader.close();
        connection.disconnect();
        return sb.toString();
    }

    public static void main(String[] args) {

        Map<String, Object> map = new HashedMap();
        map.put("name", null);
        map.put("address", "");
        map.put("phone", "123");
        map.put("age", 123);
        map.put("weight", 23.45F);

        String json = JsonHelper.toJson(map);
        System.out.println(json);
        System.out.println(JsonHelper.isGoodJson(json));

        json = JsonHelper.toPrettyJson(map);
        System.out.println(json);
        System.out.println(JsonHelper.isGoodJson(json));

        Map resultMap = JsonHelper.convertJsonToMap(json);
        System.out.println(resultMap.toString());

        List<Object> list = new ArrayList<>();
        list.add(map);
        list.add(map);

        json = JsonHelper.toJson(list);
        json = JsonHelper.toPrettyJson(list);
        System.out.println(json);
        System.out.println(JsonHelper.isGoodJson(json));

        List resultList = JsonHelper.convertJsonToMapList(json);
        System.out.println(resultList.toString());
    }
}
