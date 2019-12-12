package com.konka.kksdtr069.util;

import android.annotation.SuppressLint;

import java.lang.reflect.Method;

public class PropertyUtils {

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
        } catch ( Exception e ) {
            e.printStackTrace();
            return "";
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
        } catch ( Exception e ) {
            e.printStackTrace();
            return "";
        }
    }

}
