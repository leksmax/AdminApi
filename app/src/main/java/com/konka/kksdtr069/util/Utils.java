package com.konka.kksdtr069.util;

import android.annotation.SuppressLint;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils {

    private static final String TAG = "Tr069_Utils";

    /**
     * 线程休眠，传入毫秒值
     *
     * @param time
     */
    public static void ThreadSleep(long time) {
        try {
            Thread.sleep(time);
        } catch ( InterruptedException e ) {
            e.printStackTrace();
        }
    }

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

    /**
     * 执行Linux命令，返回包含逐行打印字符串类型的list
     *
     * @param cmd
     * @return
     */
    public static List<String> exeCommand(String cmd) {
        LogUtil.i(TAG, "exe cmd = " + cmd);
        List<String> list = new ArrayList<String>();
        BufferedReader br = null;
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = br.readLine()) != null) {
                LogUtil.i(TAG, "exe line = " + line);
                list.add(line);
            }
            int status = p.waitFor();
            LogUtil.i(TAG, "exe status = " + status);
        } catch ( Exception e ) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch ( IOException e ) {
                return list;
            }
        }
        return list;
    }

    /**
     * 执行Linux命令，返回状态码，-1代表执行失败，0代表执行成功
     *
     * @param command
     * @return
     */
    public int execCommand(String... command) {
        Process process = null;
        InputStream errIs = null;
        InputStream inIs = null;
        String result = "";
        int status = -1;
        try {
            process = new ProcessBuilder().command(command).start();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int read = -1;
            errIs = process.getErrorStream();
            while ((read = errIs.read()) != -1) {
                baos.write(read);
            }
            inIs = process.getInputStream();
            while ((read = inIs.read()) != -1) {
                baos.write(read);
            }
            result = new String(baos.toByteArray());
            LogUtil.i(TAG, "execCommand: " + Arrays.toString(command).replace(",", " ") + " result = " + result);
            status = process.waitFor();
        } catch ( Exception e ) {
            e.printStackTrace();
        } finally {
            try {
                if (inIs != null) {
                    inIs.close();
                }
                if (errIs != null) {
                    errIs.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch ( IOException e ) {
                e.printStackTrace();
            }
        }
        return status;
    }
}
