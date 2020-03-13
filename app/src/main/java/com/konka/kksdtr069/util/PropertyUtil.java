package com.konka.kksdtr069.util;

import android.annotation.SuppressLint;

import java.lang.reflect.Method;
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
     * @param sfversion 软件版本号
     * @return 格式正确的软件版本号，如0001.001.0017
     */
    public static String formatSoftwareVersion(String sfversion) {
        Pattern pattern = Pattern.compile("\\d{4}\\.\\d{3}\\.\\d{4}");
        Matcher m = pattern.matcher(sfversion);
        if (!m.matches()) {
            String[] sfList = sfversion.split("\\.");
            sfList[2] = sfList[2].split("_")[0];
            while (sfList[0].length() < 4) {
                sfList[0] = "0" + sfList[0];
            }
            while (sfList[2].length() < 4) {
                sfList[2] = "0" + sfList[2];
            }
            while (sfList[1].length() < 3) {
                sfList[1] = "0" + sfList[1];
            }
            return sfList[0] + "." + sfList[1] + "." + sfList[2];
        } else {
            return sfversion;
        }
    }

}
