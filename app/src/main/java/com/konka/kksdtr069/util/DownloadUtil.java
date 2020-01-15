package com.konka.kksdtr069.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

public class DownloadUtil {

    private static DownloadUtil downloadUtil;
    private final OkHttpClient okHttpClient;
    private List<Call> calls;
    public final static String TAG = DownloadUtil.class.getSimpleName();

    public static DownloadUtil get() {
        if (downloadUtil == null) {
            downloadUtil = new DownloadUtil();
        }
        return downloadUtil;
    }

    private DownloadUtil() {
        okHttpClient = new OkHttpClient();
        calls = new ArrayList<Call>();
    }

    /**
     * @param url      下载连接
     * @param path     储存下载文件的SDCard目录
     * @param listener 下载监听
     */
    public void download(Context context, final String url, final String path, final OnDownloadListener listener) {
        // 需要token的时候可以这样做
        // SharedPreferences sp = MyApp.getAppContext().getSharedPreferences("loginInfo", MODE_PRIVATE);
        // Request request = new Request.Builder().header("token",sp.getString("token" , "")).url(url).build();

        Request request = new Request.Builder().url(url).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                listener.onDownloadFailed(e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                InputStream is = null;
                byte[] buf = new byte[1024 * 4];
                int len = 0;
                FileOutputStream fos = null;
                try {
                    is = response.body().byteStream();
                    File file = new File(path);
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        listener.onDownloading(sum);
                    }
                    fos.flush();
                    listener.onDownloadSuccess();
                } catch (Exception e) {
                    listener.onDownloadFailed(e);
                } finally {
                    try {
                        if (is != null) is.close();
                        if (fos != null) fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        calls.add(call);
    }

    /**
     * @param saveDir
     * @return
     * @throws IOException 判断下载目录是否存在
     */
    private String isExistDir(String saveDir) throws IOException {
        File downloadFile = new File(Environment.getExternalStorageDirectory().getPath() + "/download/", saveDir);
        if (!downloadFile.mkdirs()) {
            downloadFile.createNewFile();
        }
        String savePath = downloadFile.getAbsolutePath();
        return savePath;
    }

    public static String[] getApkInfo(Context context, String apkPath) {

        String label = "";
        String version = "";
        String packageName = "";

        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath,
                PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            appInfo.sourceDir = apkPath;
            appInfo.publicSourceDir = apkPath;
            label = appInfo.loadLabel(pm).toString();
            version = info.versionName == null ? "1.0" : info.versionName;
            packageName = appInfo.packageName;
        }
        String[] strings = {label, version, packageName};
        return strings;
    }

    public void cancelAll() {
        for (Call call : calls) {
            call.cancel();
        }
    }

    public interface OnDownloadListener {
        void onDownloadSuccess();

        void onDownloading(long progress);

        void onDownloadFailed(Exception e);
    }

    public static class httpClient {

        MCallBack mCallBack;

        public httpClient(MCallBack mCallBack) {
            this.mCallBack = mCallBack;
        }

        public void doWork(final String url, final String path) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    LogUtil.d(TAG, "doWork: begin download from " + url);
                    Request request = new Request.Builder().url(url).build();
                    new OkHttpClient().newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                            LogUtil.i(TAG, "download failed");
                            mCallBack.onFailure(e.getMessage());
                        }
                        @Override
                        public void onResponse(Call call, Response response) throws
                                IOException {
                            Sink sink;
                            BufferedSink bufferedSink = null;
                            try {
                                File dest = new File(path);
                                sink = Okio.sink(dest);
                                bufferedSink = Okio.buffer(sink);
                                bufferedSink.writeAll(response.body().source());
                                bufferedSink.close();
                                Log.i(TAG, "download " + path + " success");
                                mCallBack.onSuccess();
                            } catch (Exception e) {
                                e.printStackTrace();
                                LogUtil.i(TAG, "download failed");
                                mCallBack.onFailure(e.getMessage());
                            } finally {
                                if (bufferedSink != null) {
                                    bufferedSink.close();
                                }
                            }
                        }
                    });
                }
            }).start();
        }
    }

    public interface MCallBack {
        void onSuccess();

        void onFailure(String string);
    }
}