package com.konka.kksdtr069.util;

import android.content.Context;

import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPPingRequest;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPPingResult;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPTraceRouteRequest;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPTraceRouteResult;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinuxUtils {

    public static final String TAG = LinuxUtils.class.getSimpleName();

    /**
     * ping诊断
     *
     * @param request ping请求参数
     * @return ping诊断的结果
     */
    public static CWMPPingResult ping(CWMPPingRequest request) {
        BufferedReader br = null;
        int success = 0;
        int fail = 0;
        CWMPPingResult result = new CWMPPingResult();
        try {
            Process p = Runtime.getRuntime().exec("ping" +
                    " -Q " + request.getDSCP() +
                    " -s " + request.getDataBlockSize() +
                    " -w " + request.getTimeout() +
                    " -c " + request.getNumberOfRepetitions() +
                    " " + request.getHost());
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = br.readLine()) != null) {
                LogUtils.i("hjx", "ping: " + line);
                if (line.contains("received")) {
                    String[] values = line.split(" ");
                    success = Integer.parseInt(values[3]);
                    fail = request.getNumberOfRepetitions() - success + 1;
                    result.setSuccessCount(success);
                    result.setFailureCount(fail);
                    result.setDiagnosticsState(line);
                } else if (line.startsWith("rtt")) {
                    Pattern pattern = Pattern.compile("(\\d+\\.\\d+)");
                    Matcher matcher = pattern.matcher(line);
                    String[] values = new String[4];
                    for (int i = 0; ; i++) {
                        if (matcher.find()) {
                            values[i] = matcher.group();
                            LogUtils.i("hjx", "value[" + i + "] = " + values[i]);
                        } else
                            break;
                    }
                    result.setMinimumResponseTime(Math.round(Double.parseDouble(values[0])));
                    result.setAverageResponseTime(Math.round(Double.parseDouble(values[1])));
                    result.setMaximumResponseTime(Math.round(Double.parseDouble(values[2])));
                }
            }
            int status = p.waitFor();
            LogUtils.i("hjx", "status = " + status);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * trace route 诊断
     */
    public static CWMPTraceRouteResult traceRoute(CWMPTraceRouteRequest request) {
        double max = 0;
        double min = 0;
        double responseTime = 0;
        int numberOfRouteHops = 0;
        int count = 0;
        CWMPTraceRouteResult result = new CWMPTraceRouteResult();
        ArrayList<String> routeHops = new ArrayList<String>();
        List<String> list = null;

        String dscp = request.getDSCP();
        if (dscp.equals("0"))
            dscp = "";
        else
            dscp = " -t " + dscp;
        list = exeCommand("/data/data/com.konka.kksdtr069/files/traceroute" +
                " -m " + request.getMaxHopCount() +
                " -w " + request.getTimeout() +
                dscp +
                " " + request.getHost() + " " + request.getDataBlockSize());
        if (list.size() > 0)
            list.remove(0);
        for (String line : list) {
            count++;
            String[] ss = line.trim().split("\\s+");

            if (line.indexOf('(') == -1) {
                if (count == request.getMaxHopCount()) {
                    numberOfRouteHops = 0;
                    responseTime = 0;
                    routeHops.clear();
                }
                continue;
            } else {
                numberOfRouteHops++;
            }

            StringBuffer ip = new StringBuffer();
            List<Double> times = new ArrayList<Double>();

            for (int i = 0; i < ss.length; i++) {
                if (ss[i].contains("(")) {
                    ip.append(ss[i].substring(1, ss[i].length() - 1)).append('#');
                }
                if (ss[i].equals("ms")) {
                    times.add(Double.parseDouble(ss[i - 1]));
                }
            }

            if (count == request.getMaxHopCount()) {
                max = times.get(0);
                min = times.get(0);
                for (int i = 0; i < times.size(); i++) {
                    if (max < times.get(i))
                        max = times.get(i);
                    if (min > times.get(i))
                        min = times.get(i);
                    responseTime += times.get(i);
                }
                responseTime /= times.size();
            }
            routeHops.add(ip.substring(0, ip.length() - 1));
        }

        result.setMaximumResponseTime(Math.round(max));
        result.setMinimumResponseTime(Math.round(min));
        result.setAverageResponseTime(Math.round(responseTime));
        result.setResponseTime(Math.round(responseTime));
        result.setNumberOfRouteHops(numberOfRouteHops);
        result.setRouteHops((ArrayList<String>) routeHops);
        return result;
    }

    /**
     * 修改文件读写权限
     */
    public static void varifyFile(Context context, String fileName) {
        try {
            context.openFileInput(fileName);
        } catch (FileNotFoundException notfoundE) {
            try {
                copyFromAssets(context, fileName, fileName);
                String script = "chmod 777 " + context.getFilesDir().getAbsolutePath()
                        + "/" + fileName;
                exeCommand(script);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void copyFromAssets(Context context, String source,
                                       String destination) throws IOException {
        InputStream is = context.getAssets().open(source);
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        FileOutputStream output = context.openFileOutput(destination, Context.MODE_PRIVATE);
        output.write(buffer);
        output.close();
    }

    /**
     * 执行Linux命令，返回包含逐行打印字符串类型的list
     *
     * @param cmd
     * @return
     */
    public static List<String> exeCommand(String cmd) {
        LogUtils.i(TAG, "exe cmd = " + cmd);
        List<String> list = new ArrayList<String>();
        BufferedReader br = null;
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = br.readLine()) != null) {
                LogUtils.i(TAG, "exe line = " + line);
                list.add(line);
            }
            int status = p.waitFor();
            LogUtils.i(TAG, "exe status = " + status);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
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
    public static int execCommand(String... command) {
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
            LogUtils.i(TAG, "execCommand: " + Arrays.toString(command).replace(",", " ") + " result = " + result);
            status = process.waitFor();
        } catch (Exception e) {
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return status;
    }

}
