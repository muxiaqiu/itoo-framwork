package com.tfjybj.framework.auth.util;

import com.alibaba.fastjson.util.TypeUtils;

import com.tfjybj.framework.auth.util.validate.RegexNewUtils;
import com.tfjybj.framework.exception.ApplicationException;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by will on 05/04/2017.
 */
public class ValueUtil {

    private static Logger logger = LoggerFactory.getLogger(ValueUtil.class);

    private static final ConcurrentHashMap<Integer, Map<String, String>> MapToObjectRuleMaps = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, Map<String, String>> ObjectToMapRuleMaps = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, Map<String, String>> MapToMapRuleMaps = new ConcurrentHashMap<>();

    private static Map<String, String> getMapToObjectRuleMap(String... keyRules) {
        if (keyRules == null || keyRules.length == 0) {
            return null;
        }

        Map<String, String> keyRuleMap = null;
        if (MapToObjectRuleMaps.contains(keyRules.hashCode())) {
            keyRuleMap = MapToObjectRuleMaps.get(keyRules.hashCode());
        }

        if (keyRuleMap == null) {
            keyRuleMap = new HashMap<String,String>();
        }

        if (keyRuleMap.size() == 0) {
            for (String keyRule : keyRules) {
                String[] keys = keyRule.split("::");
                keyRuleMap.put(keys.length == 2 ? keys[1] : keys[0], keys[0]);
            }
            MapToObjectRuleMaps.put(keyRules.hashCode(), keyRuleMap);
        }
        return keyRuleMap;
    }

    private static <T> void setFieldValue(T object, Field field, Object value) {
        field.setAccessible(true);
        try {
            if (field.getType() == String.class) {
                field.set(object, value != null ? value.toString() : null);
            } else if (field.getType() == Integer.class) {
                field.set(object, value != null ? new BigDecimal(value.toString()).intValue() : null);
            } else if (field.getType() == Float.class) {
                field.set(object, value != null ? new BigDecimal(value.toString()).floatValue() : null);
            } else if (field.getType() == Double.class) {
                field.set(object, value != null ? new BigDecimal(value.toString()).doubleValue() : null);
            } else if (field.getType() == Long.class) {
                field.set(object, value != null ? new BigDecimal(value.toString()).longValue() : null);
            } else if (field.getType() == Short.class) {
                field.set(object, value != null ? new BigDecimal(value.toString()).shortValue() : null);
            } else if (field.getType() == BigDecimal.class) {
                field.set(object, value != null ? new BigDecimal(value.toString()) : null);
            } else if (field.getType() == Date.class) {
                field.set(object, value != null ? TypeUtils.castToTimestamp(value) : null);
            } else {
                field.set(object, value);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static <T> void copyValueFromMapToObject(Map<String, ?> fromMap, T toObject, String... keyRules) {

        if (fromMap == null || fromMap.size() == 0 || toObject == null) {
            throw new ApplicationException("操作的对象不能空！");
        }

        Map<String, String> keyRuleMap = getMapToObjectRuleMap(keyRules);
        Field[] fields = toObject.getClass().getDeclaredFields();
        if (keyRuleMap == null || keyRuleMap.size() == 0) {
            for (Field field : fields) {
                int mod = field.getModifiers();
                if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                    continue;
                }

                String containKey = "";
                KeyMapping mapKey = field.getAnnotation(KeyMapping.class);
                if (mapKey != null) {
                    String keys = mapKey.value();
                    String[] keyArr = keys.split(",");

                    for (String key : keyArr) {
                        if (fromMap.containsKey(key)) {
                            containKey = key;
                            break;
                        }
                    }
                }

                if (!ValueUtil.isEmpty(containKey)) {
                    setFieldValue(toObject, field, fromMap.get(containKey));
                } else {
                    if (!fromMap.containsKey(field.getName())) {
                        continue;
                    }
                    setFieldValue(toObject, field, fromMap.get(field.getName()));
                }
            }
        } else {
            for (Field field : fields) {
                int mod = field.getModifiers();
                if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                    continue;
                }

                if (!keyRuleMap.containsKey(field.getName())) {
                    continue;
                }

                String fromKey = keyRuleMap.get(field.getName());
                setFieldValue(toObject, field, fromMap.get(fromKey));
            }
        }
    }

    public static <T> List<T> copyValueFromMapListToObjectList(List<Map<String, Object>> fromMapList, Class<T> clazz, String... keyRules) {

        if (fromMapList == null || fromMapList.size() == 0) {
            throw new ApplicationException("操作的对象不能空！");
        }

        Map<String, String> keyRuleMap = getMapToObjectRuleMap(keyRules);

        List<T> rtnList = new ArrayList<>();

        for (Map fromMap : fromMapList) {
            T toObject = null;
            try {
                toObject = clazz.newInstance();
            } catch (Exception ex) {
                ex.printStackTrace();
                continue;
            }

            rtnList.add(toObject);

            Field[] fields = toObject.getClass().getDeclaredFields();
            if (keyRuleMap == null || keyRuleMap.size() == 0) {
                for (Field field : fields) {
                    int mod = field.getModifiers();
                    if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                        continue;
                    }

                    String containKey = "";
                    KeyMapping mapKey = field.getAnnotation(KeyMapping.class);
                    if (mapKey != null) {
                        String keys = mapKey.value();
                        String[] keyArr = keys.split(",");

                        for (String key : keyArr) {
                            if (fromMap.containsKey(key)) {
                                containKey = key;
                                break;
                            }
                        }
                    }

                    if (!ValueUtil.isEmpty(containKey)) {
                        setFieldValue(toObject, field, fromMap.get(containKey));
                    } else {
                        if (!fromMap.containsKey(field.getName())) {
                            continue;
                        }
                        setFieldValue(toObject, field, fromMap.get(field.getName()));
                    }
                }
            } else {
                for (Field field : fields) {
                    int mod = field.getModifiers();
                    if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                        continue;
                    }

                    if (!keyRuleMap.containsKey(field.getName())) {
                        continue;
                    }

                    String fromKey = keyRuleMap.get(field.getName());
                    setFieldValue(toObject, field, fromMap.get(fromKey));
                }
            }
        }
        return rtnList;
    }

    private static Map<String, String> getObjectToMapRuleMap(String... keyRules) {
        if (keyRules == null || keyRules.length == 0) {
            return null;
        }

        Map<String, String> keyRuleMap = null;
        if (ObjectToMapRuleMaps.contains(keyRules.hashCode())) {
            keyRuleMap = ObjectToMapRuleMaps.get(keyRules.hashCode());
        }

        if (keyRuleMap == null) {
            keyRuleMap = new HashMap<String, String>();
        }

        if (keyRuleMap.size() == 0) {
            for (String keyRule : keyRules) {
                String[] keys = keyRule.split("::");
                keyRuleMap.put(keys[0], keys.length == 2 ? keys[1] : keys[0]);
            }
            ObjectToMapRuleMaps.put(keyRules.hashCode(), keyRuleMap);
        }
        return keyRuleMap;
    }

    public static <T> void copyValueFromObjectToMap(T fromObject, Map<String, Object> toMap, String... keyRules) {

        if (fromObject == null || toMap == null) {
            throw new ApplicationException("操作的对象不能空！");
        }

        Map<String, String> keyRuleMap = getObjectToMapRuleMap(keyRules);
        if (keyRuleMap == null || keyRuleMap.size() == 0) {
            Field[] fields = fromObject.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);

                String containKey = field.getName();
                KeyMapping mapKey = field.getAnnotation(KeyMapping.class);
                if (mapKey != null) {
                    String keys = mapKey.value();
                    String[] keyArr = keys.split(",");

                    for (String key : keyArr) {
                        containKey = key;
                        break;
                    }
                }

                try {
                    toMap.put(containKey, field.get(fromObject));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        } else {
            for (String key : keyRuleMap.values()) {
                toMap.put(key, null);
            }

            Field[] fields = fromObject.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (!keyRuleMap.containsKey(field.getName())) {
                    continue;
                }

                field.setAccessible(true);
                String toKey = keyRuleMap.get(field.getName());
                try {
                    toMap.put(toKey, field.get(fromObject));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static <T> void copyValueFromMapToMap(Map<String, ?> fromMap, Map<String, Object> toMap, String... keyRules) {

        if (fromMap == null || fromMap.size() == 0 || toMap == null) {
            throw new ApplicationException("操作的对象不能空！");
        }

        Map<String, String> keyRuleMap = getObjectToMapRuleMap(keyRules);
        if (keyRuleMap == null || keyRuleMap.size() == 0) {
            toMap.putAll(fromMap);
        } else {
            for (String key : keyRuleMap.values()) {
                toMap.put(key, null);
            }

            for (String fromKey : keyRuleMap.keySet()) {
                toMap.put(keyRuleMap.get(fromKey), fromMap.get(fromKey));
            }
        }
    }

    public static <T, V> V copyValueFromBeanToBean(T fromObject, V toObject) {
        if (fromObject == null) {
            throw new ApplicationException("操作的对象不能空！");
        }
        Set<Field> fromFields = new HashSet<Field>();
        fromFields.addAll(Arrays.asList(fromObject.getClass().getSuperclass().getDeclaredFields()));
        fromFields.addAll(Arrays.asList(fromObject.getClass().getDeclaredFields()));

        Set<Field> toFields = new HashSet<Field>();
        toFields.addAll(Arrays.asList(toObject.getClass().getSuperclass().getDeclaredFields()));
        toFields.addAll(Arrays.asList(toObject.getClass().getDeclaredFields()));

        for (final Field toField : toFields) {
            int mod = toField.getModifiers();
            if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                continue;
            }

            List<Field> fields = ListHelper.filter(fromFields, new ListFilter<Field>() {
                @Override
                public boolean filter(Field field) {
                    return toField.getName().equals(field.getName());
                }
            });

            if (fields == null || fields.size() == 0) {
                continue;
            }

            try {
                fields.get(0).setAccessible(true);
                setFieldValue(toObject, toField, fields.get(0).get(fromObject));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return toObject;
    }

    public static <T, V> List<V> copyValueFromBeanListToBeanList(List<T> fromBeanList, Class<V> toBeanClazz) {
        if (fromBeanList == null || fromBeanList.size() == 0) {
            throw new ApplicationException("操作的对象不能空！");
        }

        List<V> toObject = new ArrayList<>();
        for (T fromBean : fromBeanList) {
            try {
                V toBean = toBeanClazz.newInstance();
                copyValueFromBeanToBean(fromBean, toBean);
                toObject.add(toBean);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
        return toObject;
    }

    /**
     * 转 String 统一操作接口
     *
     * @param obj 待转换的对象
     * @return 结果
     */
    public static String toString(Object obj) {
        return toString(obj, "");
    }

    /**
     * 转 String 统一操作接口
     *
     * @param obj      待转换的对象
     * @param defValue 默认值
     * @return 结果
     */
    public static String toString(Object obj, String defValue) {
        String temp = StringUtils.safeToString(obj);
        if (StringUtils.isEmpty(temp)) {
            return defValue;
        }
        return temp;
    }

    /**
     * 转 boolean 统一操作接口
     *
     * @param obj 待转换的对象
     * @return 结果
     */
    public static boolean toBoolean(Object obj) {
        return toBoolean(obj, false);
    }

    /**
     * 转 boolean 统一操作接口
     *
     * @param obj      待转换的对象
     * @param defValue 默认值
     * @return 结果
     */
    public static boolean toBoolean(Object obj, boolean defValue) {
        String temp = StringUtils.safeToString(obj);
        if (StringUtils.isEmpty(temp)) {
            return defValue;
        }

        try {
            return Boolean.parseBoolean(temp);
        } catch (Exception ex) {
            return defValue;
        }
    }

    /**
     * 转 int 统一操作接口
     *
     * @param obj 待转换的对象
     * @return 结果
     */
    public static int toInt(Object obj) {
        return toInt(obj, 0);
    }

    /**
     * 转 int 统一操作接口
     *
     * @param obj      待转换的对象
     * @param defValue 默认值
     * @return 结果
     */
    public static int toInt(Object obj, int defValue) {
        String temp = StringUtils.safeToString(obj);
        if (StringUtils.isEmpty(temp)) {
            return defValue;
        }

        try {
            return Double.valueOf(temp).intValue();
        } catch (Exception ex) {
            return defValue;
        }
    }

    /**
     * 转 int 统一操作接口
     *
     * @param obj 待转换的对象
     * @return 结果
     */
    public static Integer toInteger(Object obj) {
        return toInteger(obj, 0);
    }

    /**
     * 转 int 统一操作接口
     *
     * @param obj      待转换的对象
     * @param defValue 默认值
     * @return 结果
     */
    public static Integer toInteger(Object obj, Integer defValue) {
        return Integer.valueOf(toInt(obj, defValue));
    }

    /**
     * 转 long 统一操作接口
     *
     * @param obj 待转换的对象
     * @return 结果
     */
    public static long toLong(Object obj) {
        return toLong(obj, 0);
    }

    /**
     * 转 long 统一操作接口
     *
     * @param obj      待转换的对象
     * @param defValue 默认值
     * @return 结果
     */
    public static long toLong(Object obj, long defValue) {
        String temp = StringUtils.safeToString(obj);
        if (StringUtils.isEmpty(temp)) {
            return defValue;
        }

        try {
            return Double.valueOf(temp).longValue();
        } catch (Exception ex) {
            return defValue;
        }
    }

    /**
     * 转 float 统一操作接口
     *
     * @param obj 待转换的对象
     * @return 结果
     */
    public static float toFloat(Object obj) {
        return toFloat(obj, 0F);
    }

    /**
     * 转 float 统一操作接口
     *
     * @param obj      待转换的对象
     * @param defValue 默认值
     * @return 结果
     */
    public static float toFloat(Object obj, float defValue) {
        String temp = StringUtils.safeToString(obj);
        if (StringUtils.isEmpty(temp)) {
            return defValue;
        }

        try {
            return Float.parseFloat(temp);
        } catch (Exception ex) {
            return defValue;
        }
    }

    /**
     * 转 double 统一操作接口
     *
     * @param obj 待转换的对象
     * @return 结果
     */
    public static double toDouble(Object obj) {
        return toDouble(obj, 0);
    }

    /**
     * 转 double 统一操作接口
     *
     * @param obj      待转换的对象
     * @param defValue 默认值
     * @return 结果
     */
    public static double toDouble(Object obj, double defValue) {
        String temp = StringUtils.safeToString(obj);
        if (StringUtils.isEmpty(temp)) {
            return defValue;
        }

        try {
            return Double.parseDouble(temp);
        } catch (Exception ex) {
            return defValue;
        }
    }

    public static boolean equals(Object objL, Object objR) {
        if (isNull(objL) || isNull(objR)) {
            return false;
        }
        return objL.equals(objR);
    }

    public static boolean isNull(Object obj) {
        if (null == obj) {
            return true;
        }
        return false;
    }

    public static boolean isEmpty(String obj) {
        if (toString(obj).length() == 0) {
            return true;
        }
        return false;
    }

    public static boolean isNotEmpty(String obj) {
        return !isEmpty(obj);
    }

    public static boolean isEmpty(Collection obj) {
        if (null == obj || obj.isEmpty()) {
            return true;
        }
        return false;
    }

    public static boolean isNotEmpty(Collection obj) {
        return !isEmpty(obj);
    }

    public static boolean isEmpty(Map obj) {
        if (null == obj || obj.isEmpty()) {
            return true;
        }
        return false;
    }

    public static boolean isNotEmpty(Map obj) {
        return !isEmpty(obj);
    }

    public static boolean checkBankCard(String bankCard) {
        if (StringUtils.isEmpty(bankCard)) {
            return false;
        }
        return BankCardUtil.checkBankCard(bankCard);
    }

    public static boolean checkIdCard(String idCard) {
        if (StringUtils.isEmpty(idCard)) {
            return false;
        }
        return IdCardUtil.isValidate18Idcard(idCard);
    }

    public static boolean checkPhoneNum(String phoneNum) {
        if (StringUtils.isEmpty(phoneNum)) {
            return false;
        }
        return StringUtils.isPhoneNumber(phoneNum);
    }

    public static boolean checkEmail(String email) {
        if (StringUtils.isEmpty(email)) {
            return false;
        }
        return StringUtils.isEmail(email);
    }

    public static void close(Closeable... closeables) {
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    logger.error("Can't close. Reason: %s %s", e.getClass(), e.getMessage());
                }
            }
        }
    }

    /**
     * 转义正则表达式参数为Map:
     * <p>
     * ^正则====>\^正则
     * *正则====>.*正则
     * |正则====>\|正则
     * ?正则====>.?正则
     *
     * @param map             待转义的map对象
     * @param regexpkeysInMap 待转义的key，若为空，则转义map中的所有字段
     * @return
     */
    public static Map<String, Object> escapeRegexp(Map<String, Object> map, String[] regexpkeysInMap) {

        if (MapUtils.isEmpty(map)) {
            return map;
        }

        if (regexpkeysInMap == null || regexpkeysInMap.length == 0) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String regexp = RegexNewUtils.simpleMatchToRegexp(entry.getKey());
                map.put(entry.getKey(), regexp);
            }
            return map;
        }

        for (String key : regexpkeysInMap) {
            Object obj = map.get(key);
            if (obj instanceof String) {
                map.put(key, RegexNewUtils.simpleMatchToRegexp((String) obj));
            }
        }
        return map;
    }

}
