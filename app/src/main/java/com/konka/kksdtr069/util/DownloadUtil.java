package com.konka.kksdtr069.util;

import android.content.Context;
import android.os.Environment;

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

public class DownloadUtil {

    private static DownloadUtil downloadUtil;
    private final OkHttpClient okHttpClient;
    private List<Call> calls;

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
     * @param url 下载连接
     * @param path 储存下载文件的SDCard目录
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
     * @throws IOException
     * 判断下载目录是否存在
     */
    private String isExistDir(String saveDir) throws IOException {
        File downloadFile = new File(Environment.getExternalStorageDirectory().getPath() + "/download/", saveDir);
        if (!downloadFile.mkdirs()) {
            downloadFile.createNewFile();
        }
        String savePath = downloadFile.getAbsolutePath();
        return savePath;
    }

    public void cancelAll() {
        for(Call call : calls) {
            call.cancel();
        }
    }

    public interface OnDownloadListener {
        void onDownloadSuccess();
        void onDownloading(long progress);
        void onDownloadFailed(Exception e);
    }
}