package com.konka.kksdtr069.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.google.gson.Gson;
import com.konka.kksdtr069.IStbParmService;
import com.konka.kksdtr069.base.BaseApplication;
import com.konka.kksdtr069.constant.NetConstant;
import com.konka.kksdtr069.model.ReportResult;
import com.konka.kksdtr069.model.ReportResultResponse;
import com.konka.kksdtr069.model.RequestURL;
import com.konka.kksdtr069.model.ResponseURL;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class NetMeasureUtils {

    private static final String TAG = NetMeasureUtils.class.getSimpleName();
    private static final String FENGHUO_URL = "http://111.48.20.10/speedtest/speedtest.ts";
    private static final String FILE_PATH = "/data/SpeedTest.zip";

    private static final int MSG_NET_ERROR = 0;
    private static final int MSG_START_TEST = 1;
    private static final int MSG_SHOW_SPEED = 2;
    private static final int MSG_SHOW_SPEED2 = 3;
    private static final int MSG_REPORT_RESULT = 4;
    private static final int MSG_MEASURE = 5;

    private Timer timer;
    private Handler handler;
    private TimerTask task = new TimerTask() {

        @Override
        public void run() {
            dix = currDownLoad - preDowdLoad;
            preDowdLoad = currDownLoad;
            Log.i(TAG, "current speed" + dix / 1024);
            Message msg = handler.obtainMessage();
            msg.what = MSG_SHOW_SPEED;
            msg.arg1 = (int) dix / 1024;
            if (dix != 0) {
                handler.sendMessage(msg);
            }
        }
    };
    private IStbParmService iStbParmService;
    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            iStbParmService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iStbParmService = IStbParmService.Stub.asInterface(service);
            try {
                account = iStbParmService.getStbParameter("Account");
                handler.sendEmptyMessage(MSG_MEASURE);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    private SpeedTestCompletedListener listener;

    private Context context;

    private String type = "1";
    private String account = "";
    private String cityCode = "";
    private String terminalNo = "";
    private String userIP = "";
    private String bandwidth = "100M";
    private String terminalType = "1";

    private boolean scanRetry = true;

    private ResponseURL responseURL = null;

    private long dix = 0L;
    private long preDowdLoad = 0L;
    private long currDownLoad = 0L;
    private long maxSpeed = 0L;
    private long curSpeed = 0L;
    private int count = 0;
    private int time = 60;

    private static NetMeasureUtils instance;

    public static NetMeasureUtils getInstance() {
        if (instance == null) {
            instance = new NetMeasureUtils();
        }
        return instance;
    }

    private NetMeasureUtils() {
        context = BaseApplication.instance.getApplicationContext();
        Looper.prepare();
        handler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_NET_ERROR:
                        Log.i(TAG, "net error");
                        if ("0".equals(type)) {
                            downLoad(FENGHUO_URL);
                        } else {
                            handler.removeMessages(MSG_SHOW_SPEED2);
                        }
                        break;

                    case MSG_START_TEST:
                        timer.schedule(task, 1000, 1000);
                        break;

                    case MSG_SHOW_SPEED:
                        Log.i(TAG, "show speed = " + msg.arg1);
                        break;

                    case MSG_SHOW_SPEED2:
                        count++;
                        if (count == time) {
                            String maxSpeedStr = String.format("%.2f",
                                    (double) maxSpeed * 8 / (1024 * 1024));
                            String averSpeedStr = String.format("%.2f",
                                    (double) currDownLoad * 8 / (60 * 1024 * 1024));
                            reportResult(maxSpeedStr, averSpeedStr);
                            DownloadUtil.get().cancelAll();
                            return;
                        }
                        curSpeed = currDownLoad - preDowdLoad;
                        preDowdLoad = currDownLoad;
                        Log.i(TAG, "current speed" + curSpeed);
                        if (maxSpeed < curSpeed) {
                            maxSpeed = curSpeed;
                        }
                        Log.i(TAG, String.format("%.2f",
                                (double) curSpeed * 8 / (1024 * 1024)) + "Mb/s");
                        handler.sendEmptyMessageDelayed(MSG_SHOW_SPEED2, 1000);
                        break;

                    case MSG_REPORT_RESULT:
                        if (msg.arg1 == 0) {
                            Log.i(TAG, "result upload success!");
                        } else {
                            Log.i(TAG, "result upload success!");
                        }
                        listener.submitResult(msg.arg1 == 0);
                        break;

                    case MSG_MEASURE:
                        startSpeedTest();
                        break;
                    default:
                        break;
                }
            }
        };
        Looper.loop();
    }

    public void startSpeedTest() {
        scanRetry = true;
        if ("0".equals(type)) {
            Log.i(TAG, "type: old");
            timer = new Timer();
            timer.schedule(task, 1000, 1000);
            downLoad(FENGHUO_URL);
        } else {
            Log.i(TAG, "type: new");
            cityCode = Integer.toString(NetConstant.AREA_CODE.get(
                    PropertyUtils.getProperty("persist.sys.area")));
            terminalNo = PropertyUtils.getProperty("ro.mac").replace(":", "");
            userIP = getClientIP();
            RequestURL requestURL = new RequestURL(account, cityCode, bandwidth,
                    terminalType, terminalNo, userIP);
            final Gson gson = new Gson();
            String params = gson.toJson(requestURL);
            Log.i(TAG, "params:" + params);
            MediaType mediaType = MediaType.parse("text/x-markdown; charset=utf-8");
            Request request = new Request.Builder()
                    .url("http://weixin.sd.chinamobile" +
                            ".com:9081/networkspeedtest/paramurl/paramTest.do")
                    .post(RequestBody.create(mediaType, params))
                    .build();
            OkHttpClient okHttpClient = new OkHttpClient();
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, "onFailure: " + e.getMessage());
                    handler.sendEmptyMessage(MSG_NET_ERROR);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d(TAG, response.protocol() + " " + response.code() + " " + response
                            .message());
                    Headers headers = response.headers();
                    for (int i = 0; i < headers.size(); i++) {
                        Log.d(TAG, headers.name(i) + ":" + headers.value(i));
                    }
                    String result = response.body().string();
                    Log.d(TAG, "onResponse: " + result);
                    responseURL = gson.fromJson(result, ResponseURL.class);
                    Log.i(TAG, "desc:" + responseURL.getDesc()
                            + "  URL:" + responseURL.getResponseURLInfo().getSpeedTestUrl());
                    if ("success".equals(responseURL.getDesc())) {
                        downLoad(responseURL.getResponseURLInfo().getSpeedTestUrl());
                        handler.sendEmptyMessageDelayed(MSG_SHOW_SPEED2, 1000);

                    } else {
                        handler.sendEmptyMessage(MSG_NET_ERROR);
                    }
                }
            });
        }
    }

    private String getClientIP() {
        try {
            Enumeration<NetworkInterface> enumerationNi = NetworkInterface.getNetworkInterfaces();
            while (enumerationNi.hasMoreElements()) {
                NetworkInterface networkInterface = enumerationNi.nextElement();
                String interfaceName = networkInterface.getDisplayName();
                Log.i(TAG, "interfaceName = " + interfaceName);
                if (interfaceName.equals("eth0")) {
                    Enumeration<InetAddress> enumIpAddr = networkInterface.getInetAddresses();
                    while (enumIpAddr.hasMoreElements()) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress() && inetAddress instanceof
                                Inet4Address) {
                            Log.i(TAG, "eth host = " + inetAddress.getHostAddress());
                            return inetAddress.getHostAddress();
                        }
                    }
                } else if (interfaceName.equals("wlan0")) {
                    Enumeration<InetAddress> enumIpAddr = networkInterface.getInetAddresses();
                    while (enumIpAddr.hasMoreElements()) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress() && inetAddress instanceof
                                Inet4Address) {
                            Log.i(TAG, "wifi host = " + inetAddress.getHostAddress());
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void downLoad(String downLoadURL) {
        Log.i(TAG, "downLoadURL:" + downLoadURL);
        File file = new File(FILE_PATH);
        if (file.exists()) {
            Log.i(TAG, "data existed in the files.");
            file.delete();
        }
        preDowdLoad = 0L;
        currDownLoad = 0L;
        downLoad(downLoadURL, FILE_PATH);
    }

    private void downLoad(String url, String filePath) {
        DownloadUtil.get().download(context, url, filePath, new DownloadUtil
                .OnDownloadListener() {
            @Override
            public void onDownloadSuccess() {
                Log.i(TAG, "onDownloadSuccess");
            }

            @Override
            public void onDownloading(long progress) {
                currDownLoad = progress;
            }

            @Override
            public void onDownloadFailed(Exception e) {
                if (!e.toString().contains("closed")) {
                    Log.i(TAG, "onDownloadFailed");
                    if ("0".equals(type)) {
                        if (!scanRetry) {
                            return;
                        }
                        handler.sendEmptyMessageDelayed(MSG_NET_ERROR, 3000);
                    } else {
                        handler.sendEmptyMessage(MSG_NET_ERROR);
                    }
                } else {
                    Log.i(TAG, "user close");
                }
            }
        });

    }

    private void reportResult(String maxSpeed, String averSpeed) {
        ReportResult reportResult = new ReportResult(responseURL.getResponseURLInfo()
                .getSpeedTestId(), account, maxSpeed + "Mb/s",
                averSpeed + "Mb/s");
        final Gson gson = new Gson();
        String params = gson.toJson(reportResult);
        Log.i(TAG, "reportParams:" + params);
        MediaType mediaType = MediaType.parse("text/x-markdown; charset=utf-8");
        Request request = new Request.Builder()
                .url("http://weixin.sd.chinamobile" +
                        ".com:9081/networkspeedtest/paramurl/acceptSpeedResult.do")
                .post(RequestBody.create(mediaType, params))
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(Call arg0, Response arg1) throws IOException {
                Log.d(TAG, arg1.protocol() + " " + arg1.code() + " " + arg1.message());
                Headers headers = arg1.headers();
                for (int i = 0; i < headers.size(); i++) {
                    Log.d(TAG, headers.name(i) + ":" + headers.value(i));
                }
                String result = arg1.body().string();
                Log.d(TAG, "onResponse: " + result);
                ReportResultResponse repResponse = gson.fromJson(result,
                        ReportResultResponse.class);
                if ("success".equals(repResponse.getDesc())) {
                    Message msg = handler.obtainMessage(MSG_REPORT_RESULT, 0, 0);
                    handler.sendMessage(msg);
                } else {
                    Message msg = handler.obtainMessage(MSG_REPORT_RESULT, -1, 0);
                    handler.sendMessage(msg);
                }
            }

            @Override
            public void onFailure(Call arg0, IOException arg1) {
                Log.d(TAG, "onFailure: " + arg1.getMessage());
                Message msg = handler.obtainMessage(MSG_REPORT_RESULT, -1, 0);
                handler.sendMessage(msg);
            }
        });
    }

    public void speedTest() {
        Intent intent = new Intent("com.certus.ottstb.bestv.StbParmService");
        context.bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    public void setSpeedTestCompletedListener(SpeedTestCompletedListener listener) {
        this.listener = listener;
    }

    public interface SpeedTestCompletedListener {
        void submitResult(boolean success);
    }

}
