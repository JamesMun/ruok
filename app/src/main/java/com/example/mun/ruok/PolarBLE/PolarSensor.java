package com.example.mun.ruok.PolarBLE;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.widget.TextView;

//import com.example.khseob0715.sanfirst.Database.PolarDatabase;
import com.example.mun.ruok.MainActivity;
import com.example.mun.ruok.Fragment_TabMain;

import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

public class PolarSensor extends Service {

    private SharedPreferences prefs;

    public static int heartrateValue = 0;
    public static int RR_value = 0;

    PolarBleService mPolarBleService;
    String mpolarBleDeviceAddress="00:22:D0:A4:9D:83";  // 우리꺼
    int batteryLevel=0;

    private TimerTask m_Task;
    private Timer m_Timer;

    TextView displayheartvalue;

    //PolarDatabase polardb = new PolarDatabase();
    MainActivity userAct = new MainActivity();

    String mDefaultDeviceAddress;

    public PolarSensor() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onCreate() {
        super.onCreate();
        prefs = getSharedPreferences(HConstants.DEVICE_CONFIG, Context.MODE_MULTI_PROCESS);
        mDefaultDeviceAddress = prefs.getString(HConstants.CONFIG_DEFAULT_DEVICE_ADDRESS, null);
        activatePolar();

        /*m_Task = new TimerTask() {
            @Override
            public void run() {//실제 기능 구현
                polardb.PolarDB_heartratevalue = heartrateValue;
                userAct.HeartSendHandler(); //Server로 실질적으로 전송하는 소스
            }
        };
        m_Timer = new Timer();
        m_Timer.schedule(m_Task, 3000, 1000);   // 원래 period 5000 임*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        deactivatePolar();
    }

    // activate PolarSensor
    protected void activatePolar() {

        Intent gattactivatePolarServiceIntent = new Intent(this, PolarBleService.class);
        bindService(gattactivatePolarServiceIntent, mPolarBleServiceConnection, BIND_AUTO_CREATE);
        registerReceiver(mPolarBleUpdateReceiver, makePolarGattUpdateIntentFilter());
    }

    protected void deactivatePolar() {
        try{
            if(mPolarBleService!=null){
                unbindService(mPolarBleServiceConnection);
            }
        }catch(Exception e){
        }

        try{
            unregisterReceiver(mPolarBleUpdateReceiver);
        }catch(Exception e){
        }
    }

    private final BroadcastReceiver mPolarBleUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            // receive msg from PolarSensor
            final String action = intent.getAction();
            if (PolarBleService.ACTION_GATT_CONNECTED.equals(action)) {
            } else if (PolarBleService.ACTION_GATT_DISCONNECTED.equals(action)) {
            } else if (PolarBleService.ACTION_HR_DATA_AVAILABLE.equals(action)) {

                String data = intent.getStringExtra(PolarBleService.EXTRA_DATA);
                StringTokenizer tokens = new StringTokenizer(data, ";");
                int hr = Integer.parseInt(tokens.nextToken());  //
                int prrPercenteage = Integer.parseInt(tokens.nextToken());
                int prrCount = Integer.parseInt(tokens.nextToken());    //
                int rrThreshold = Integer.parseInt(tokens.nextToken());	//50%, 30%, etc.
                int rrTotal = Integer.parseInt(tokens.nextToken()); //
                int rrValue = Integer.parseInt(tokens.nextToken());
                long sid = Long.parseLong(tokens.nextToken());

                heartrateValue = hr;
                RR_value = rrValue;

                Fragment_TabMain.heart_rate_value = hr;
                Fragment_TabMain.rr_rate_value = rrValue;

            }else if (PolarBleService.ACTION_BATTERY_DATA_AVAILABLE.equals(action)) {
                String data = intent.getStringExtra(PolarBleService.EXTRA_DATA);
                batteryLevel = Integer.parseInt(data);
            }else if (PolarBleService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                String data = intent.getStringExtra(PolarBleService.EXTRA_DATA);
                StringTokenizer tokens = new StringTokenizer(data, ";");
                int totalNN = Integer.parseInt(tokens.nextToken());
                long lSessionId = Long.parseLong(tokens.nextToken());
            }
        }
    };

    private static IntentFilter makePolarGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PolarBleService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(PolarBleService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(PolarBleService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(PolarBleService.ACTION_HR_DATA_AVAILABLE);
        intentFilter.addAction(PolarBleService.ACTION_BATTERY_DATA_AVAILABLE);
        return intentFilter;
    }

    private final ServiceConnection mPolarBleServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mPolarBleService = ((PolarBleService.LocalBinder) service).getService();
            if (!mPolarBleService.initialize()) {
            }
            // Automatically connects to the device upon successful start-up initialization.
            //mPolarBleService.connect(app.polarBleDeviceAddress, false);
            mPolarBleService.connect(mpolarBleDeviceAddress, false);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

            mPolarBleService = null;
        }
    };
}

