package my.mkt;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MyService extends Service {

   final String LOG_TAG = "myLogs";
   public List<String> listFile;

    public void onCreate() {

        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        String file_url=intent.getStringExtra("url");
        String type=intent.getStringExtra("type");
        Log.d(LOG_TAG, "onStartCommand");
        someTask(file_url,type);

        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
    }

    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return null;
    }

    public String[] someTask(String url, String type) {
        //скачивание файлов
        if (type.equals("list")){
            listFile=new ArrayList<String>();
           // listFile.add();

        }
        else if (type.equals("download")) {
            final DownloadFileFromURL downloadTask = new DownloadFileFromURL(this);
            downloadTask.execute(url);
        }
        return null;
    }

    class DownloadFileFromURL extends AsyncTask<String, String, String> {
        private Context context;
        private PowerManager.WakeLock mWakeLock;


        public DownloadFileFromURL(Context context){
            this.context=context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                DownloadManager downloadManager = (DownloadManager)getSystemService(Activity.DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(f_url[0]));
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
                request.setVisibleInDownloadsUi(false);
                request.setDestinationInExternalFilesDir(MyService.this,Environment.DIRECTORY_DOWNLOADS, "data.7z");

                Log.i("!!!!path:", String.valueOf(Environment.DIRECTORY_DOWNLOADS));

                //запускаем скачивание
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                    if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                        downloadManager.enqueue(request);
                        // Toast.makeText(context, "Downloading...", Toast.LENGTH_SHORT).show();

                    }
                }
                else{
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    downloadManager.enqueue(request);

                }

            } catch (Exception e) {
                Log.e("Error: ", e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
          //  Toast.makeText(MyService.this, "Файл скачен!", Toast.LENGTH_SHORT).show();
        }
    }
}
