package com.konka.kksdtr069.util;

import android.util.Log;

import com.konka.kksdtr069.constant.SysLogConstant;
import com.konka.kksdtr069.model.SysLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class UploadLogUtils {

    public static final String TAG = UploadLogUtils.class.getSimpleName();
    private String[] command = null;
    private Timer timer;
    private Process syslogProcess = null;
    private InputStream syslogErrIs = null;
    private InputStream syslogInIs = null;

    private static UploadLogUtils instance;

    public static UploadLogUtils getInstance() {
        if (instance == null) {
            instance = new UploadLogUtils();
        }
        return instance;
    }

    public void start(final SysLog sysLog) {
        int logLevel = sysLog.getLevel();
        int logType = sysLog.getType();
        int outPutType = sysLog.getOutPutType();
        Date startTime = sysLog.getStartTime();

        String mac = PropertyUtils.getProperty("ro.mac", "unknowmac")
                .replace(":", "").toUpperCase();
        String tag = PropertyUtils.getProperty("ro.product.model", "UnknowModel")
                + "_" + PropertyUtils.getProperty("ro.build.version.incremental",
                "UnknowVersion");
        String PRI = String.valueOf(logType * 8 + logLevel);
        if ("0".equals(PRI)) {
            PRI = "135";
        }
        String level = "";
        if (logLevel == SysLogConstant.LEVEL_ALL) {
            level = "V";
        } else if (logLevel == SysLogConstant.LEVEL_DEBUG) {
            level = "D";
        } else if (logLevel == SysLogConstant.LEVEL_INFO) {
            level = "I";
        } else if (logLevel == SysLogConstant.LEVEL_ERROR) {
            level = "E";
        }
        // 构造执行的命令例如：logcat -v detector -m 001A34B774D1 -T MGV2000 -p 135 -s *:*
        String cmd = "logcat -v detector -m " + mac + " -T " + tag + " -p " + PRI + " -s *:" +
                level;
        Log.d(TAG, "start: " + cmd);
        command = cmd.split(" ");
        timer = new Timer();
        if (outPutType == SysLogConstant.OUTPUTTYPE_SFTP) {
            // 2019.03.20更新后取消该功能
           /* // 定时启动
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    startSftpLog();
                }
            }, startTime);*/
        } else if (outPutType == SysLogConstant.OUTPUTTYPE_SYSLOG) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    startSysLog(sysLog);
                }
            }, startTime);
        } else if (outPutType == SysLogConstant.OUTPUTTYPE_BOTH) {
            // 2019.03.20更新后取消该功能
            /*startSysLog();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    startSftpLog();
                }
            }, startTime);*/
        }
    }

    public void startSysLog(SysLog sysLog) {
        int sysLogContinueTime = sysLog.getContinueTime();
        Log.d(TAG, "startSysLog");
        try {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    destroy(syslogProcess, syslogErrIs, syslogInIs);
                }
            }, sysLogContinueTime * 60 * 1000);
            syslogProcess = new ProcessBuilder().command(command).start();
            syslogInIs = syslogProcess.getInputStream();
            sendToSyslogServer(sysLog);
        } catch (IOException e) {
            Log.d(TAG, "startSysLog failed:" + e.getMessage());
            e.printStackTrace();
            destroy(syslogProcess, syslogErrIs, syslogInIs);
        }
    }

    public void sendToSyslogServer(SysLog sysLog) {
        final String syslogServerIP = sysLog.getServerIP();
        final String syslogServerPort = sysLog.getServerPort();
        Log.d(TAG, "sendToSyslogServer");
        new Thread(new Runnable() {
            @Override
            public void run() {
                //创建客户端的DatagramSocket对象,使用UDP协议传输syslog日志
                DatagramSocket ds = null;
                BufferedReader br = null;
                try {
                    ds = new DatagramSocket();
                    DatagramPacket dp = null;
                    br = new BufferedReader(new InputStreamReader(syslogInIs));
                    String line;
                    Log.d(TAG, "sendToSyslogServer: start loop");
                    while ((line = br.readLine()) != null) {
                        byte[] b = (line).getBytes("UTF-8");
                        //将字节数组的数据放入数据包并发送
                        dp = new DatagramPacket(b, b.length,
                                InetAddress.getByName(syslogServerIP),
                                Integer.parseInt(syslogServerPort));
                        ds.send(dp);
                    }
                } catch (IOException e) {
                    Log.d(TAG, "sendToSyslogServer: failed!\n" + e.getMessage());
                    e.printStackTrace();
                    stopSendToSyslogServer();
                }
            }
        }).start();
    }

    public void stopSendToSyslogServer() {
        Log.d(TAG, "stopSendToSyslogServer");
        destroy(syslogProcess, syslogErrIs, syslogInIs);
    }

    public void destroy(Process process, InputStream errIs, InputStream inIs) {
        Log.d(TAG, "destroy");
        try {
            if (process != null) {
                process.destroy();
            }
            if (errIs != null) {
                errIs.close();
            }
            if (inIs != null) {
                inIs.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
