package com.example.mun.ruok;

import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.Manifest;

import com.example.mun.ruok.SensorService;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Fragment tabFragment;
    private Fragment settingFragment;
    private Fragment historyFragment;

    public static MainActivity UserActContext;
    public static Activity UserActivity;

    public static SQLiteDatabase db;

    public static String account;

    private final int REQUEST_PERMISSION = 1;

    private static final int REQUEST_ENABLE_BT = 3;  //요청코드 상수 정의
    private BluetoothAdapter mBluetoothAdapter = null;  //객체선언

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom);

        tabMake();
        getUserInfo();
        DBservice();

        UserActContext = this;
        UserActivity = this;

        if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED
                ||   ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ||   ContextCompat.checkSelfPermission( this, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED
                ||   ContextCompat.checkSelfPermission( this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                ||   ContextCompat.checkSelfPermission( this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        { // 바디센서, 위치수신, 저장장치에 대한 권한요청, String배열로 복수개의 요청이 가능함

                    ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.BODY_SENSORS,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,}, REQUEST_PERMISSION);
        }
        else {
            checkBluetooth();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkBluetooth();
                }
                else {
                    Toast.makeText(this, "권한을 모두 동의해야 사용가능합니다.", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 2000);
                }
        }
    }

    public void checkBluetooth() {
        //기기가 블루투스를 지원하는지 확인
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null) {
            Toast.makeText(this, "블루투스를 사용할 수 없습니다.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        //블루투스가 활성화 되어있는지 확인
        if(!mBluetoothAdapter.isEnabled()) {  //블루투스 장치가 켜져 있지 않은 지 체크
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE); //객체 생성
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT); //실행
        } else {
            startRUOK();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            //블루투스 장치를 켜기위한 요청코드인 경우
            case REQUEST_ENABLE_BT:
                //장치 켜짐의 여부에 따라 토스트 메세지 출력
                if(resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "블루투스를 활성화하였습니다.", Toast.LENGTH_LONG).show();
                    startRUOK();
                }
                else {
                    Toast.makeText(this, "블루투스를 활성화하지 못했습니다.", Toast.LENGTH_LONG).show();
                    if(isServiceRunning()) {
                        stopSensorService();
                    }
                }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus) {
            checkGPSService();
        }
    }

    public void startRUOK() {
        if(!isServiceRunning()) {
            Intent intent = new Intent(this,SensorService.class);
            startService(intent);
            Log.d(TAG,"ServiceStart");
        }
    }

    public boolean checkGPSService() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            startActivity(intent);
            return false;
        } else {
            return true;
        }
    }

    public void stopSensorService() {
        Intent intent = new Intent(this, SensorService.class);
        stopService(intent);
        Log.d(TAG,"ServiceStop");
    }

    private void getUserInfo() {
        Intent intent = getIntent();
        account = intent.getExtras().getString("account");    // 로그인 결과로 넘어온 유저 계정
        Toast.makeText(this, account, Toast.LENGTH_SHORT).show();
    }

    private void DBservice() {
        db = openOrCreateDatabase("RUOK", Context.MODE_PRIVATE, null);
    }

    private void tabMake() {

        tabFragment = new Fragment_TabMain();
        settingFragment = new SettingFragment();
        historyFragment = new HistoryFragment();

        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        ft.replace(R.id.content_fragment_layout, tabFragment);
        ft.commit();

        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        /*tabs.addTab(tabs.newTab().setText("Home"));
        tabs.addTab(tabs.newTab().setText("History"));
        tabs.addTab(tabs.newTab().setText("Alarm"));
        tabs.addTab(tabs.newTab().setText("Settings"));*/
        tabs.addTab(tabs.newTab().setIcon(R.drawable.icon_home));
        tabs.addTab(tabs.newTab().setIcon(R.drawable.icon_history));
        tabs.addTab(tabs.newTab().setIcon(R.drawable.icon_alarm));
        tabs.addTab(tabs.newTab().setIcon(R.drawable.icon_setting));
        tabs.setTabGravity(TabLayout.GRAVITY_FILL);

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                Log.d("MainActivity", "선택된 탭 : " + position);
                Log.d("MainActivity", String.valueOf(SensorService.min_heart_rate));
                Log.d("MainActivity", String.valueOf(SensorService.max_heart_rate));

                Fragment selected = null;
                if(position == 0) {
                    selected = tabFragment;
                } else if(position == 1) {
                    selected = historyFragment;
                } else if(position == 2) {
                    selected = settingFragment;
                } else if(position == 3) {
                    selected = settingFragment;
                }

                android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

                ft.replace(R.id.content_fragment_layout, selected);
                ft.commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    public boolean isServiceRunning()
    {
        ActivityManager manager = (ActivityManager) this.getSystemService(Activity.ACTIVITY_SERVICE);

        Intent intent = new Intent(this,SensorService.class);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if ("com.example.mun.ruok.SensorService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}