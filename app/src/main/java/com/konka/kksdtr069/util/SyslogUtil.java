package com.konka.kksdtr069.util;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class SyslogUtil {

    /**
     * Syslog日志输出工具类
     * 1、必须先执行Syslog()构造方法初始化
     * 2、然后执行start()方法开始抓取日志
     * 3、实时传输需要手动调用stopSendToSyslogServer()
     */

    public final static int LEVEL_ALL = 0;// 不过滤，输出所有级别
    public final static int LEVEL_ERROR = 3;// 输出Error
    public final static int LEVEL_INFO = 6;// 输出Info
    public final static int LEVEL_DEBUG = 7;// 输出Debug

    public final static int TYPE_ALL = 0;// 不过滤，输出所有类型
    public final static int TYPE_OPERATE = 16;// 操作日志
    public final static int TYPE_KERNEL = 17;// 运行日志
    public final static int TYPE_SECURITY = 19;// 安全日志
    public final static int TYPE_USER = 20;// 用户日志

    public final static int OUTPUTTYPE_CLOSE = 0;// 关闭日志功能
    public final static int OUTPUTTYPE_SFTP = 1;// 仅发送SFTP
    public final static int OUTPUTTYPE_SYSLOG = 2;// 仅发送实时日志
    public final static int OUTPUTTYPE_BOTH = 3;// SFTP和实时日志均发送

    private static final String TAG = SyslogUtil.class.getSimpleName();

    private int OutPutType;// 控制发送实时日志或SFTP日志的开关
    private int LogLevel;// 输出级别
    private int LogType;// 输出类型

    // syslog服务器地址
    private String SyslogServerIP;
    private String SyslogServerPort;

    // sftp服务器地址和路径
    private String remotePath;
    private String remoteFileName;
    private String localPath;
    private String localFileName;
    private String SftpIP;
    private String SftpPort;
    private String SftpUsername;
    private String SftpPassword;
    private Date startTime; // 开始抓取日志时间
    private int sysLogContinueTime; // 日志持续抓取时间，单位：分钟

    private Process syslogProcess = null;
    private InputStream syslogErrIs = null;
    private InputStream syslogInIs = null;


    private Process sftpProcess = null;
    private InputStream sftpErrIs = null;
    private InputStream sftpInIs = null;
    private FileOutputStream fos = null;

    private String[] command = null;

    private Timer timer;

    public SyslogUtil(int logOutputType, int logLevel, int logType, String sysLogServerAd, Date
            time, int sysLogContinueTime, String sftpServerAd) {
        this.OutPutType = logOutputType;
        this.LogLevel = logLevel;
        this.LogType = logType;

        if (!TextUtils.isEmpty(sysLogServerAd)) {
            String[] strings = sysLogServerAd.split(":");
            this.SyslogServerIP = strings[0];
            if (!TextUtils.isEmpty(strings[1])) {
                this.SyslogServerPort = strings[1];
            } else {
                this.SyslogServerPort = "37027";
            }
        }

        this.startTime = time;
        this.sysLogContinueTime = sysLogContinueTime;

        String sftpIP = "";
        String sftpUsername = "";
        String sftpPsw = "";
        if (!TextUtils.isEmpty(sftpServerAd)) {
            sftpIP = sftpServerAd.split("@")[1];
            sftpUsername = sftpServerAd.split("@")[0].split("//")[1].split(":")[0];
            sftpPsw = sftpServerAd.split("@")[0].split("//")[1].split(":")[1];
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
        String date = sdf.format(new Date());
        String sfileName = PropertyUtil.getProperty("ro.mac").replace(":", "") + "_" + date + "log";

        this.remotePath = "/";
        this.remoteFileName = sfileName;
        this.localPath = "/data/data/com.konka.kksdtr069/cache/log/";
        this.localFileName = sfileName;
        this.SftpIP = sftpIP;
        this.SftpPort = "22";
        this.SftpUsername = sftpUsername;
        this.SftpPassword = sftpPsw;
    }

    public void start() {
        String mac = PropertyUtil.getProperty("ro.mac", "unknowmac").replace(":", "").toUpperCase();
        String tag = PropertyUtil.getProperty("ro.product.model", "UnknowModel") + "_" +
                PropertyUtil.getProperty("ro.build.version.incremental", "UnknowVersion");
        String PRI = "";
        if (LogType == 0 || LogLevel == 0) {
            PRI = "135";
        }else{
            PRI = String.valueOf(LogType * 8 + LogLevel);
        }
        String level = "";
        if (LogLevel == LEVEL_ALL) {
            level = "V";
        } else if (LogLevel == LEVEL_DEBUG) {
            level = "D";
        } else if (LogLevel == LEVEL_INFO) {
            level = "I";
        } else if (LogLevel == LEVEL_ERROR) {
            level = "E";
        }
        // 构造执行的命令例如：logcat -v detector -m 001A34B774D1 -T MGV2000 -p 135 -s *:*
        String cmd = "logcat -v detector -m " + mac + " -T " + tag + " -p " + PRI + " -s *:" +
                level;
        Log.d(TAG, "start: " + cmd);
        command = cmd.split(" ");
        timer = new Timer();
        if (OutPutType == OUTPUTTYPE_SFTP) {
            // 2019.03.20更新后取消该功能
           /* // 定时启动
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    startSftpLog();
                }
            }, startTime);*/
        } else if (OutPutType == OUTPUTTYPE_SYSLOG) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    startSysLog();
                }
            }, startTime);
        } else if (OutPutType == OUTPUTTYPE_BOTH) {
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

    //  抓取syslog日志保存在本地文档，再上传到sftp服务器，在20190320版本规范中已删除，未验证
    public void startSftpLog() {
        try {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    stopSaveAndSend();
                }
            }, sysLogContinueTime * 60 * 1000);
            sftpProcess = new ProcessBuilder().command(command).start();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int read = -1;
            sftpErrIs = sftpProcess.getErrorStream();
            while ((read = sftpErrIs.read()) != -1) {
                baos.write(read);
            }
            String errorInfo = new String(baos.toByteArray());
            Log.i(TAG, "sftpProcess execCommand: " + Arrays.toString(command).replace(",", " " +
                    "") + " error:" + errorInfo);
            sftpInIs = sftpProcess.getInputStream();
            saveToFile(localPath, localFileName);

        } catch ( IOException e ) {
            e.printStackTrace();
            destroy(sftpProcess, sftpErrIs, sftpInIs);
        }
    }

    public void startSysLog() {
        Log.d(TAG, "startSysLog()");
        try {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    destroy(syslogProcess, syslogErrIs, syslogInIs);
                }
            }, sysLogContinueTime * 60 * 1000);
            syslogProcess = new ProcessBuilder().command(command).start();
            syslogInIs = syslogProcess.getInputStream();
            sendToSyslogServer();
        } catch ( IOException e ) {
            Log.d(TAG, "startSysLog failed :" + e.getMessage());
            e.printStackTrace();
            destroy(syslogProcess, syslogErrIs, syslogInIs);
        }
    }

    public void sendToSyslogServer() {
        Log.d(TAG, "sendToSyslogServer()");
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 创建客户端的DatagramSocket对象,使用UDP协议传输syslog日志
                DatagramSocket ds = null;
                BufferedReader br = null;
                try {
                    ds = new DatagramSocket();
                    DatagramPacket dp = null;
                    br = new BufferedReader(new InputStreamReader(syslogInIs));
                    String line;
                    Log.d(TAG, "sendToSyslogServer() start loop");
                    while ((line = br.readLine()) != null) {
                        byte[] b = (line).getBytes("UTF-8");
                        // 将字节数组的数据放入数据包并发送
                        dp = new DatagramPacket(b, b.length,
                                InetAddress.getByName(SyslogServerIP),
                                Integer.parseInt(SyslogServerPort));
                        ds.send(dp);
                    }
                } catch ( IOException e ) {
                    Log.d(TAG, "sendToSyslogServer : failed!\n" + e.getMessage());
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

    public void saveToFile(final String localPath, final String localFileName) {
        Log.d(TAG, "saveToFile");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File dir = new File(localPath);
                    File file = new File(localPath + localFileName);
                    if ((!dir.exists()) || (!dir.isDirectory())) {
                        dir.mkdirs();
                    } else {
                        File[] files = dir.listFiles();
                        for (File f : files) {
                            if (f.isFile()) {
                                f.delete();
                            }
                        }
                    }
                    if (!file.exists()) {
                        file.createNewFile();
                        Log.d(TAG, file.getName() + "is not exists, creat it.");
                        Thread.sleep(500);
                    }
                    if (fos == null) {
                        fos = new FileOutputStream(localPath + localFileName);
                    }
                    byte[] b = new byte[1024];
                    int length;
                    while ((length = sftpInIs.read(b)) != 0) {
                        fos.write(b, 0, length);
                        fos.flush();
                    }
                } catch ( InterruptedException e ) {
                    e.printStackTrace();
                } catch ( FileNotFoundException e ) {
                    e.printStackTrace();
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void sendToSftp(final String remotePath, final String remoteFileName, final String
            localPath, final String localFileName) {
        Log.d(TAG, "sendToSftp");
        SFTPUtil sftpUtil = new SFTPUtil(SftpIP, SftpPort, SftpUsername, SftpPassword);
        sftpUtil.connect();
        sftpUtil.uploadFile(remotePath, remoteFileName, localPath, localFileName);
        sftpUtil.disconnect();
        LinuxUtil.exeCommand("rm -r " + localFileName + localFileName);
    }

    public void stopSaveAndSend() {
        Log.d(TAG, "stopSaveAndSend");
        try {
            if (fos != null) {
                fos.close();
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        destroy(sftpProcess, sftpErrIs, sftpInIs);
        sendToSftp(this.remotePath, this.remoteFileName, this.localPath, this.localFileName);
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
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }
}
