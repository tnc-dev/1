package my.mkt;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private long enqueue;
    private DownloadManager dm;

    public static final String DATABASE_NAME = "tasks";

    public static final String APP_LESSONS = "myLessons";
    public static final String APP_WIFI = "myWifi";

    ListView lv;
    WifiManager wifi;
    String wifis[];
    WifiScanReceiver wifiReciever;

    String networkSSID = "TP-LINK_8775B8";
    String networkPass = "74442225";
    TextView textViewViewTasks, textName;
    EditText  editTextDateStart, editTextDateEnd;
    Spinner spinnerType;

    SQLiteDatabase mDatabase;

    int mnsk = 1;

    int DIALOG_DATE = 1;
    int DIALOG_DATE_2 = 2;
    int myYear = 2019;
    int myMonth = 10;
    int myDay = 20;

    String id_mnsk="1223";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //разрешение для записи в память
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        textViewViewTasks = (TextView) findViewById(R.id.textViewViewTasks);
        textName=(TextView) findViewById(R.id.TextName);
        textName.setText("");
        editTextDateStart = (EditText) findViewById(R.id.editTextDateStart);
        editTextDateEnd = (EditText) findViewById(R.id.editTextDateEnd);
        spinnerType = (Spinner) findViewById(R.id.spinnerType);
        findViewById(R.id.buttonAddTask).setOnClickListener(this);
        textViewViewTasks.setOnClickListener(this);


        //creating a database
        mDatabase = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
        createEmployeeTable();
        int k = 0;

        //подключение к wifi
        k=wifiNetworkConnection();

        if(k==0){

       }

        //получение списка файлов
        Intent intent = new Intent(this, MyService.class);
        intent.putExtra("url", "http://192.168.2.112/mnsk/blackbox.php?action=list&user=user&password=111&start=2018-10-17&end=2018-10-22");
        intent.putExtra("type", "list");
        startService(intent);
    }

    private int wifiNetworkConnection() {
        //подключение к wifi сети
        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //Проверяем включен ли WiFi, если нет то включаем
        enableWifi();
        wifiReciever = new WifiScanReceiver();
        wifi.startScan();

        //получение активных wifi сетей
        List<WifiConfiguration> listActiveWifi = wifi.getConfiguredNetworks();

        //получаем список сохраненных сетей
        createTableWifi();

       /* String insertSQL = "INSERT INTO wifi (name, password) VALUES ('"+networkSSID+"','"+networkPass+"')";
        mDatabase.execSQL(insertSQL);*/

        Cursor cursorEmployees = mDatabase.rawQuery("SELECT * FROM wifi",null);
        List<Wifi> listKnownWifi = new ArrayList<Wifi>();
        if (cursorEmployees.moveToFirst()) {
            //looping through all the records
            do {
                //pushing each record in the employee list
                listKnownWifi.add(new Wifi(
                        cursorEmployees.getInt(0),
                        cursorEmployees.getString(1),
                        cursorEmployees.getString(2)
                ));
            } while (cursorEmployees.moveToNext());
        }
        //closing the cursor
        cursorEmployees.close();

        int k=0;

        if (listKnownWifi.size() != 0) {
             for (int i = 0; i < listActiveWifi.size(); i++) {
                 for(int j=0;j<listKnownWifi.size();j++) {
                     if (listActiveWifi.get(i).SSID.contains(listKnownWifi.get(j).name)) {
                         myConnect(listKnownWifi.get(j).name, listKnownWifi.get(j).password);
                         k=1;
                         break;
                     }
                 }
             }

         } else {
             // здесь будет listView где заполняем  данные новой сети
             myConnect(networkSSID, networkPass);
           //  SharedPreferences.Editor editor = mWifi.edit();
            // editor.putString(networkSSID, networkPass);
           //  editor.apply();
            k=1;
         }
        return k;
    }

    //создание таблицы задач в БД
    private void createEmployeeTable() {
        mDatabase.execSQL(
                "CREATE TABLE IF NOT EXISTS tasks (\n" +
                        "    id INTEGER NOT NULL CONSTRAINT tasks_pk PRIMARY KEY AUTOINCREMENT,\n" +
                        "    name varchar(200) NOT NULL,\n" +
                        "    mnsk varchar(200) NOT NULL,\n" +
                        "    status varchar(200) NOT NULL,\n" +
                        "    joiningdate datetime NOT NULL,\n" +
                        "    date_start  varchar(200) NOT NULL,\n" +
                        "    date_end  varchar(200) NOT NULL,\n" +
                        "    type varchar(200) NOT NULL\n" +
                        ");"
        );
    }

    //создание таблицы сетей wifi в БД
    private void createTableWifi() {
        mDatabase.execSQL(
                "CREATE TABLE IF NOT EXISTS wifi (\n" +
                        "    id INTEGER NOT NULL CONSTRAINT wifi_pk PRIMARY KEY AUTOINCREMENT,\n" +
                        "    name varchar(200) NOT NULL,\n" +
                        "    password varchar(200) NOT NULL\n" +
                        ");"
        );
    }

    // подключение к wifi сети
    private void myConnect(String networkSSID, String networkPass) {

        WifiConfiguration wifiConfig = new WifiConfiguration();

        wifiConfig.SSID = String.format("\"%s\"", networkSSID);
        wifiConfig.preSharedKey = String.format("\"%s\"", networkPass);
        wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
//remember id
        WifiInfo info = wifiManager.getConnectionInfo(); //get WifiInfo
        int id = info.getNetworkId();

        int netId = wifiManager.addNetwork(wifiConfig);

        wifiManager.disconnect();
        wifiManager.disableNetwork(id);
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
        startService(new Intent(this, MyService.class).putExtra("url", "http://192.168.2.112/mnsk/blackbox.php?action=download&user=user&password=111&date=2018-10-19&type=media"));
    }

    public void onClickStop(View v) {
        stopService(new Intent(this, MyService.class));
    }

    public void onClickAddLesson(View view) {
    }


    //this method will validate the name and salary
    //dept does not need validation as it is a spinner and it cannot be empty
    private boolean inputsAreCorrect(String name, String date_start, String date_end) {


        if (date_start.isEmpty()) {
            editTextDateStart.setError("Начальная дата не может быть пустой");
            //editTextDateStart.requestFocus();
            return false;
        }

        if (date_end.isEmpty()) {
            editTextDateEnd.setError("Конечная дата не может быть пустой");
            //editTextDateEnd.requestFocus();
            return false;
        }
        return true;
    }

    //In this method we will do the create operation
    private void addEmployee() {


        //String salary = editTextSalary.getText().toString().trim();

        String date_start = editTextDateStart.getText().toString().trim();
        String date_end = editTextDateEnd.getText().toString().trim();

        String type = spinnerType.getSelectedItem().toString();

        String status = "1";
        String name = date_start+"_"+date_end+"_"+id_mnsk+"_"+type;


        //getting the current time for joining date
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        String joiningDate = sdf.format(cal.getTime());

        //validating the inptus
        if (inputsAreCorrect(name, date_start, date_end)) {

            String insertSQL = "INSERT INTO tasks \n" +
                    "(name, status, joiningdate, date_start, date_end, type)\n" +
                    "VALUES \n" +
                    "(?, ?, ?, ?, ?, ?);";

            //using the same method execsql for inserting values
            //this time it has two parameters
            //first is the sql string and second is the parameters that is to be binded with the query
            mDatabase.execSQL(insertSQL, new String[]{name, status, joiningDate, date_start, date_end, type});

            Toast.makeText(this, "Задача успешно добавлена", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.buttonAddTask:

                addEmployee();

                break;
            case R.id.textViewViewTasks:

                startActivity(new Intent(this, TasksActivity.class));

                break;
        }
    }

    public void сlickOnDateStart(View view) {

        showDialog(DIALOG_DATE);

    }

    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_DATE) {
            DatePickerDialog tpd = new DatePickerDialog(this, myCallBack, myYear, myMonth, myDay);
            return tpd;
        } else if (id == DIALOG_DATE_2){
            DatePickerDialog tpd1 = new DatePickerDialog(this, myCallEnd, myYear, myMonth, myDay);
            return tpd1;
        }
        return super.onCreateDialog(id);
    }

    DatePickerDialog.OnDateSetListener myCallBack = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            myYear = year;
            myMonth = monthOfYear;
            myDay = dayOfMonth;

            editTextDateStart.setText(myDay + "." + myMonth + "." + myYear);
            String name = editTextDateStart.getText().toString().trim()+"_"+editTextDateEnd.getText().toString().trim()+"_"+id_mnsk+"_"+spinnerType.getSelectedItem().toString();
            textName.setText(name);
        }
    };

    DatePickerDialog.OnDateSetListener myCallEnd = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            myYear = year;
            myMonth = monthOfYear;
            myDay = dayOfMonth;
            editTextDateEnd.setText(myDay + "." + myMonth + "." + myYear);
            String name = editTextDateStart.getText().toString().trim()+"_"+editTextDateEnd.getText().toString().trim()+"_"+id_mnsk+"_"+spinnerType.getSelectedItem().toString();
            textName.setText(name);
        }
    };

    public void сlickOnDateEnd(View view) {
        showDialog(DIALOG_DATE_2);
    }

    private class WifiScanReceiver extends BroadcastReceiver {

        public void onReceive(Context c, Intent intent) {

            List<ScanResult> wifiScanList = wifi.getScanResults();
            wifis = new String[wifiScanList.size()];

            for (int i = 0; i < wifiScanList.size(); i++) {
                wifis[i] = ((wifiScanList.get(i)).toString());
            }
            lv.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, wifis));
        }
    }

}
