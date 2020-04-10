package com.konka.kksdtr069.util;

import android.annotation.SuppressLint;

import com.konka.kksdtr069.constant.MonthConstant;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropertyUtil {

    /**
     * 获取系统属性key对应的值
     *
     * @param key
     * @return
     */
    public static String getProperty(String key) {
        try {
            @SuppressLint("PrivateApi") Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            return (String) (get.invoke(c, key, ""));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 修改系统属性对应的值
     *
     * @param key   系统属性的名称
     * @param value 系统属性对应的值
     */
    public static void setProperty(String key, String value) {
        try {
            @SuppressLint("PrivateApi") Class<?> c = Class.forName("android.os.SystemProperties");
            Method set = c.getMethod("set", String.class, String.class);
            set.invoke(c, key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取系统属性key对应的值,若出现异常，返回空字符串
     *
     * @param key
     * @param defultValue
     * @return
     */
    public static String getProperty(String key, String defultValue) {
        try {
            @SuppressLint("PrivateApi") Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            return (String) (get.invoke(c, key, defultValue));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 修改软件版本号显示的格式
     *
     * @return 格式正确的软件版本号，如0702.101.2003
     */
    public static String formatSoftwareVersion() {
        String[] buildDate = getProperty("ro.build.date").split(" ");
        String month = buildDate[1].toLowerCase();
        String year = buildDate[buildDate.length - 1];
        year = year.substring(2);
        Set set = MonthConstant.months.entrySet();
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            String i = iterator.next().toString().toLowerCase();
            if (i.contains(month)) {
                month = i.split("=")[1];
            }
        }
        StringBuilder result = new StringBuilder();
        result.append("0702.101." + year + month);
        return result.toString();

    }

}
