package com.tfjybj.framework.auth.util.convert;


import com.tfjybj.framework.auth.util.validate.Valid;

import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * 常用类型转换
 */
public class ConvertUtil {
    private static String hexStr = "0123456789ABCDEF";

    /**
     * 短整型与字节的转换
     */
    public static byte[] shortToByte(short number) {
        int temp = number;
        byte[] b = new byte[2];
        for (int i = 0; i < b.length; i++) {
            // 将最低位保存在最低位
            b[i] = new Integer(temp & 0xff).byteValue();
            // 向右移8位
            temp = temp >> 8;
        }
        return b;
    }

    /**
     * 字节的转换与短整型
     */
    public static short byteToShort(byte[] b) {
        short s;
        // 最低位
        short s0 = (short) (b[0] & 0xff);
        short s1 = (short) (b[1] & 0xff);
        s1 <<= 8;
        s = (short) (s0 | s1);
        return s;
    }

    /**
     * 整型与字节数组的转换
     */
    public static byte[] intToByte(int i) {
        byte[] bt = new byte[4];
        bt[0] = (byte) (0xff & i);
        bt[1] = (byte) ((0xff00 & i) >> 8);
        bt[2] = (byte) ((0xff0000 & i) >> 16);
        bt[3] = (byte) ((0xff000000 & i) >> 24);
        return bt;
    }

    /**
     * 整型数组转换为字节数组的转换
     *
     * @param arr 整型数组
     */
    public static byte[] intToByte(int[] arr) {
        byte[] bt = new byte[arr.length * 4];
        for (int i = 0; i < arr.length; i++) {
            byte[] t = intToByte(arr[i]);
            System.arraycopy(t, 0, bt, i + 4, 4);
        }
        return bt;
    }

    public static byte[] encodeBytes(byte[] source, char split) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(source.length);
        for (byte b : source) {
            if (b < 0) {
                b += 256;
            }
            bos.write(split);
            char hex1 = Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, 16));
            char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, 16));
            bos.write(hex1);
            bos.write(hex2);
        }
        return bos.toByteArray();
    }

    /**
     * bytes数组转char数组
     * bytes to chars
     *
     * @param bytes bytes数组
     */
    public static char[] bytesToChars(byte[] bytes) {
        char[] chars = new char[]{};
        if (Valid.valid(bytes)) {
            chars = new char[bytes.length];
            for (int i = 0; i < bytes.length; i++) {
                chars[i] = (char) bytes[i];
            }
        }
        return chars;
    }

    /**
     * 字节数组和整型的转换
     */
    public static int bytesToInt(byte[] bytes) {
        int num = bytes[0] & 0xFF;
        num |= ((bytes[1] << 8) & 0xFF00);
        num |= ((bytes[2] << 16) & 0xFF0000);
        num |= ((bytes[3] << 24) & 0xFF000000);
        return num;
    }

    /**
     * 字节数组和长整型的转换
     */
    public static byte[] longToByte(long number) {
        long temp = number;
        byte[] b = new byte[8];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Long(temp & 0xff).byteValue();
            // 将最低位保存在最低位
            temp = temp >> 8;
            // 向右移8位
        }
        return b;
    }

    /**
     * 字节数组和长整型的转换
     */
    public static long byteToLong(byte[] b) {
        long s;
        long s0 = b[0] & 0xff;// 最低位
        long s1 = b[1] & 0xff;
        long s2 = b[2] & 0xff;
        long s3 = b[3] & 0xff;
        long s4 = b[4] & 0xff;// 最低位
        long s5 = b[5] & 0xff;
        long s6 = b[6] & 0xff;
        long s7 = b[7] & 0xff; // s0不变
        s1 <<= 8;
        s2 <<= 16;
        s3 <<= 24;
        s4 <<= 8 * 4;
        s5 <<= 8 * 5;
        s6 <<= 8 * 6;
        s7 <<= 8 * 7;
        s = s0 | s1 | s2 | s3 | s4 | s5 | s6 | s7;
        return s;
    }

    /**
     * 将byte转换为对应的二进制字符串
     *
     * @param src 要转换成二进制字符串的byte值
     */
    public static String byteToBinary(byte src) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            result.append(src % 2 == 0 ? '0' : '1');
            src = (byte) (src >>> 1);
        }
        return result.reverse().toString();
    }

    /**
     * 将十六进制字符串转为二进制字符串
     *
     * @param hexStr 十六进制字符串
     */
    public static String hexStringtoBinarg(String hexStr) {
        hexStr = hexStr.replaceAll("\\s", "").replaceAll("0x", "");
        char[] achar = hexStr.toCharArray();
        String result = "";
        for (char a : achar) {
            result += Integer.toBinaryString(Integer.valueOf(String.valueOf(a), 16)) + " ";
        }
        return result;
    }

    /**
     * 将二进制转换为十六进制字符输出
     *
     * @param bytes bytes数组
     */
    public static String bytesToHexString(byte[] bytes) {
        String result = "";
        String hex;
        for (byte b : bytes) {
            //字节高4位
            hex = String.valueOf(hexStr.charAt((b & 0xF0) >> 4));
            //字节低4位
            hex += String.valueOf(hexStr.charAt(b & 0x0F));
            result += hex + " ";
        }
        return result;
    }

    /**
     * 把16进制字符串转换成字节数组
     *
     * @param hexString 16进制字符串
     * @return byte[]
     */
    public static byte[] hexStringToByte(String hexString) {
        int len = (hexString.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hexString.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    private static int toByte(char c) {
        return (byte) hexStr.indexOf(c);
    }

    /**
     * 字符串转换为int
     *
     * @param str          待转换的字符串
     * @param defaultValue 默认值
     * @author:chenssy
     * @date : 2016年5月21日 上午10:16:27
     */
    public static int strToInt(String str, int defaultValue) {
        try {
            defaultValue = Integer.parseInt(str);
        } catch (Exception localException) {
        }
        return defaultValue;
    }

    /**
     * String转换为long
     *
     * @param str          待转换字符串
     * @param defaultValue 默认值
     * @author:chenssy
     * @date : 2016年5月21日 上午10:18:44
     */
    public static long strToLong(String str, long defaultValue) {
        try {
            defaultValue = Long.parseLong(str);
        } catch (Exception localException) {
        }
        return defaultValue;
    }

    /**
     * 字符串转换为float
     *
     * @author:chenssy
     * @date : 2016年5月21日 上午10:19:12
     */
    public static float strToFloat(String str, float defaultValue) {
        try {
            defaultValue = Float.parseFloat(str);
        } catch (Exception localException) {
        }
        return defaultValue;
    }

    /**
     * String转换为Double
     *
     * @param str          待转换字符串
     * @param defaultValue 默认值
     * @author:chenssy
     * @date : 2016年5月21日 上午10:21:59
     */
    public static double strToDouble(String str, double defaultValue) {
        try {
            defaultValue = Double.parseDouble(str);
        } catch (Exception localException) {
        }
        return defaultValue;
    }

    /**
     * 字符串转换日期
     *
     * @param str          待转换的字符串
     * @param defaultValue 默认日期
     * @author:chenssy
     * @date : 2016年5月21日 上午10:27:01
     */
    public static java.util.Date strToDate(String str, java.util.Date defaultValue) {
        return strToDate(str, "yyyy-MM-dd HH:mm:ss", defaultValue);
    }

    /**
     * 字符串转换为指定格式的日期
     *
     * @param str          待转换的字符串
     * @param format       日期格式
     * @param defaultValue 默认日期
     * @author:chenssy
     * @date : 2016年5月21日 上午10:27:24
     */
    public static java.util.Date strToDate(String str, String format, java.util.Date defaultValue) {
        SimpleDateFormat fmt = new SimpleDateFormat(format);
        try {
            defaultValue = fmt.parse(str);
        } catch (Exception localException) {
        }
        return defaultValue;
    }

    /**
     * 日期转换为字符串
     *
     * @param date         待转换的日期
     * @param defaultValue 默认字符串
     * @author:chenssy
     * @date : 2016年5月21日 上午10:28:05
     */
    public static String dateToStr(java.util.Date date, String defaultValue) {
        return dateToStr(date, "yyyy-MM-dd HH:mm:ss", defaultValue);
    }

    /**
     * 日期转换为指定格式的字符串
     *
     * @param date         待转换的日期
     * @param format       指定格式
     * @param defaultValue 默认值
     * @author:chenssy
     * @date : 2016年5月21日 上午10:28:51
     */
    public static String dateToStr(java.util.Date date, String format, String defaultValue) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            defaultValue = sdf.format(date);
        } catch (Exception localException) {
        }
        return defaultValue;
    }

    /**
     * 如果字符串为空则使用默认字符串
     *
     * @param str          字符串
     * @param defaultValue 默认值
     * @author:chenssy
     * @date : 2016年5月21日 上午10:29:35
     */
    public static String strToStr(String str, String defaultValue) {
        if ((str != null) && (!(str.isEmpty())))
            defaultValue = str;
        return defaultValue;
    }

    /**
     * util com.intime.commons.date 转换为 sqldate
     *
     * @author:chenssy
     * @date : 2016年5月21日 上午10:30:09
     */
    public static java.sql.Date dateToSqlDate(java.util.Date date) {
        return new java.sql.Date(date.getTime());
    }

    /**
     * sql com.intime.commons.date 转换为 util com.intime.commons.date
     *
     * @author:chenssy
     * @date : 2016年5月21日 上午10:30:26
     */
    public static java.util.Date sqlDateToDate(java.sql.Date date) {
        return new java.util.Date(date.getTime());
    }

    /**
     * com.intime.commons.date 转换为 timestamp
     *
     * @author:chenssy
     * @date : 2016年5月21日 上午10:30:51
     */
    public static Timestamp dateToSqlTimestamp(java.util.Date date) {
        return new Timestamp(date.getTime());
    }

    /**
     * timestamp 转换为date
     *
     * @author:chenssy
     * @date : 2016年5月21日 上午10:31:13
     */
    public static java.util.Date qlTimestampToDate(Timestamp date) {
        return new java.util.Date(date.getTime());
    }
}
