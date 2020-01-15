package com.konka.kksdtr069.util;

import android.content.Context;
import android.util.Log;

import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPPingRequest;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPPingResult;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPTraceRouteRequest;
import net.sunniwell.cwmp.protocol.sdk.aidl.CWMPTraceRouteResult;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinuxUtil {

    public static final String TAG = LinuxUtil.class.getSimpleName();

    /**
     * ping诊断
     *
     * @param request ping请求参数
     * @return ping诊断的结果
     */
    public static CWMPPingResult ping(CWMPPingRequest request) {
        BufferedReader br = null;
        StringBuilder errorMsg = null;
        BufferedReader errorReader = null;
        DataOutputStream dos = null;
        Process p = null;
        int success = 0;
        int fail = 0;
        String command = "ping" +
                " -Q " + request.getDSCP() +
                " -s " + request.getDataBlockSize() +
                " -w " + request.getTimeout() +
                " -c " + request.getNumberOfRepetitions() +
                " " + request.getHost();
        CWMPPingResult result = new CWMPPingResult();

        try {
            p = Runtime.getRuntime().exec("sh");
            dos = new DataOutputStream(p.getOutputStream());
            dos.write(command.getBytes());
            dos.writeBytes("\n");
            dos.writeBytes("exit\n");
            dos.flush();

            int status = p.waitFor();
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = br.readLine()) != null) {
                Log.i(TAG, "ping: " + line);
                if (line.contains("received")) {
                    String[] values = line.split(" ");
                    for (int i = 0; i < values.length; i++) {
                        Log.i(TAG, "value[" + i + "] = " + values[i]);
                    }
                    success = Integer.parseInt(values[3]);
                    fail = request.getNumberOfRepetitions() - success;
                    result.setSuccessCount(success);
                    result.setFailureCount(fail);
                    result.setDiagnosticsState("Complete");
                } else if (line.startsWith("rtt")) {
                    Pattern pattern = Pattern.compile("(\\d+\\.\\d+)");
                    Matcher matcher = pattern.matcher(line);
                    String[] values = new String[4];
                    for (int i = 0; ; i++) {
                        if (matcher.find()) {
                            values[i] = matcher.group();
                            Log.i(TAG, "value[" + i + "] = " + values[i]);
                        } else
                            break;
                    }
                    result.setMinimumResponseTime(Math.round(Double.parseDouble(values[0])));
                    result.setAverageResponseTime(Math.round(Double.parseDouble(values[1])));
                    result.setMaximumResponseTime(Math.round(Double.parseDouble(values[2])));
                }
            }

            errorMsg = new StringBuilder();
            errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((line = errorReader.readLine()) != null) {
                errorMsg.append(line);
            }
            LogUtil.d(TAG, "execute command :" + command + "\n"
                    + "status : " + status + "\n");
            if (errorMsg != null && !(errorMsg.toString().isEmpty())) {
                LogUtil.d(TAG, errorMsg.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (dos != null) {
                    dos.close();
                }
                if (br != null) {
                    br.close();
                }
                if (errorReader != null) {
                    errorReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (p != null) {
                p.destroy();
            }
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
                dscp + " " + request.getHost() + " " + request.getDataBlockSize());
        if (list.size() > 0) {
            list.remove(0);
        }
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
        List<String> list = new ArrayList<String>();
        if (cmd == null || cmd.length() == 0) {
            return null;
        }
        BufferedReader successReader = null;
        BufferedReader errorReader = null;
        StringBuilder errorMsg = null;
        Process process = null;
        DataOutputStream dos = null;
        int status = -1;
        try {
            process = Runtime.getRuntime().exec("sh");
            dos = new DataOutputStream(process.getOutputStream());
            dos.write(cmd.getBytes());
            dos.writeBytes("\n");
            dos.writeBytes("exit\n");
            dos.flush();

            status = process.waitFor();
            successReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            while ((line = successReader.readLine()) != null) {
                LogUtil.i(TAG, "exe line = " + line);
                list.add(line);
            }

            errorMsg = new StringBuilder();
            errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            LogUtil.i(TAG, "exe cmd : " + cmd + "\n"
                    + "status : " + status);
            while ((line = errorReader.readLine()) != null) {
                errorMsg.append(line);
            }
            if (!(errorMsg.toString().isEmpty())) {
                LogUtil.d(TAG, "error msg : " + errorMsg.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (dos != null) {
                    dos.close();
                }
                if (successReader != null) {
                    successReader.close();
                }
                if (errorReader != null) {
                    errorReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
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
    public static int execCommand(String... command) throws IOException, InterruptedException {
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
            status = process.waitFor();
            LogUtil.i(TAG, "execCommand: " + Arrays.toString(command)
                    .replace(",", " ")
                    + " result = " + result + " status = " + status);
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

    public static boolean isAppInstalled(String appName) {
        List<String> apkList = exeCommand("ls -l data/data");
        for (String apkInform : apkList) {
            String[] app = apkInform.split(" ");
            LogUtil.d(TAG, "apk name = " + app[app.length - 1]);
            if (app[app.length - 1].equals(appName)) {
                return true;
            }

        }
        return false;
    }

    public static String execCommandForString(String... command) throws IOException, InterruptedException {
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
            status = process.waitFor();
            LogUtil.i(TAG, "execCommand: " + Arrays.toString(command)
                    .replace(",", " ")
                    + " result = " + result + " status = " + status);
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
        return result;
    }

    public static void removeSubFile(String dirPath) {
        File dir = new File(dirPath);
        if ((!dir.exists()) || (!dir.isDirectory())) {
            dir.mkdirs();
            try {
                execCommand("chmod", "755", dirPath);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            File[] files = dir.listFiles();
            for (File f : files) {
                if (f.isFile()) {
                    f.delete();
                }
            }
        }
    }

    public static String getPacketCaptureName() {
        SimpleDateFormat sdformat = new SimpleDateFormat("yyyyMMddHHmmss");
        String date = sdformat.format(new Date());
        String fileName = PropertyUtil.getProperty("ro.mac")
                .replace(":", "") + "_" + date + ".pcap";
        return fileName;
    }


}
