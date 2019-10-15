package my.mkt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private long enqueue;
    private DownloadManager dm;

    ListView lv;
    WifiManager wifi;
    String wifis[];
    WifiScanReceiver wifiReciever;

    String networkSSID = "TP-LINK_8775B8";
    String networkPass = "74442225";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //разрешение для записи в память
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

       //подключение к wifi сети          
        wifi=(WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //Проверяем включен ли WiFi, если нет то включаем
        enableWifi();

        wifiReciever = new WifiScanReceiver();
        wifi.startScan();

        myConnect();

        //получение списка файлов
        ListView listView = findViewById(R.id.list);

        Intent intent= new Intent(this, MyService.class);
        intent.putExtra("url","http://192.168.2.107/mnsk/blackbox.php?action=list&user=user&password=111&start=2018-10-17&end=2018-10-22");
        intent.putExtra("type","list");
        startService(intent);

        final String[] catNames = new String[] {};


// используем адаптер данных
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, catNames);

        listView.setAdapter(adapter);
     }

    private void myConnect() {

        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", networkSSID);
        wifiConfig.preSharedKey = String.format("\"%s\"", networkPass);
        wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
//remember id
        int netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
    }

    private void enableWifi() {
        if (!wifi.isWifiEnabled()) {
            wifi.setWifiEnabled(true);

            Toast toast = Toast.makeText(getApplicationContext(), "Wifi Turned On", Toast.LENGTH_SHORT);
            toast.show();
        }        
    }

    public void onClickStart(View v) {
        startService(new Intent(this, MyService.class).putExtra("url","http://192.168.2.107/mnsk/blackbox.php?action=download&user=user&password=111&date=2018-10-19&type=media" ));
    }

    public void onClickStop(View v) {
        stopService(new Intent(this, MyService.class));
    }

    public void onClickAddLesson(View view) {
    }

    private class WifiScanReceiver extends BroadcastReceiver {

        public void onReceive(Context c, Intent intent) {

            List<ScanResult> wifiScanList = wifi.getScanResults();
            wifis = new String[wifiScanList.size()];

            for(int i = 0; i < wifiScanList.size(); i++){
                wifis[i] = ((wifiScanList.get(i)).toString());
            }
            lv.setAdapter(new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,wifis));
        }
    }
}
