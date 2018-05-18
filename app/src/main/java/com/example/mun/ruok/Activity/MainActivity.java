package com.example.mun.ruok.Activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.Toast;
import android.Manifest;

import com.example.mun.ruok.Fragment.Fragment_TabMain;
import com.example.mun.ruok.Fragment.HistoryFragment;
import com.example.mun.ruok.Fragment.SettingFragment;
import com.example.mun.ruok.R;
import com.example.mun.ruok.Service.SensorService;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Fragment tabFragment;
    private Fragment settingFragment;
    private Fragment historyFragment;

    public static MainActivity UserActContext;
    public static Activity UserActivity;

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

        UserActContext = this;
        UserActivity = this;

        if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED  //권한 설정 확인
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
        if(!isServiceRunning()) {   // 서비스가 켜져있지 않은 경우
            Intent intent = new Intent(this,SensorService.class);
            startService(intent);   // 서비스 실행
            Log.d(TAG,"ServiceStart");
        } else if(SensorService.mListener == null) { // 서비스는 켜져 있으나 리스너가 등록이 되지 않은 경우
            stopSensorService();    // 서비스 중지
            Intent intent = new Intent(this,SensorService.class);
            startService(intent);   // 서비스 재실행
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

    public void stopSensorService() {   // 서비스 중지
        Intent intent = new Intent(this, SensorService.class);
        stopService(intent);
        Log.d(TAG,"ServiceStop");
    }

    private void getUserInfo() {    // 로그인 액티비티에서 보낸 account 값 저장 함수
        Intent intent = getIntent();
        account = intent.getExtras().getString("account");    // 로그인 결과로 넘어온 유저 계정
        //Toast.makeText(this, account, Toast.LENGTH_SHORT).show();
    }

    private void tabMake() {    // 메인 액티비티에 사용되는 탭 생성

        tabFragment = new Fragment_TabMain();
        settingFragment = new SettingFragment();
        historyFragment = new HistoryFragment();

        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        ft.replace(R.id.content_fragment_layout, tabFragment);  // 맨 처음 시작시 실행되는 프래그먼트 지정
        ft.commit();

        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);

        tabs.addTab(tabs.newTab().setIcon(R.drawable.icon_home));   // 각 탭에 해당하는 아이콘 지정
        tabs.addTab(tabs.newTab().setIcon(R.drawable.icon_history));
        tabs.addTab(tabs.newTab().setIcon(R.drawable.icon_alarm));
        tabs.addTab(tabs.newTab().setIcon(R.drawable.icon_setting));
        tabs.setTabGravity(TabLayout.GRAVITY_FILL); // 탭을 아래쪽으로 위치시킴

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {   // 탭 선택 시 발생 이벤트
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();   // 선택된 탭 포지션 확인

                Fragment selected = null;
                if(position == 0) {
                    selected = tabFragment; // 첫번째 탭 선택시 메인 프래그먼트
                } else if(position == 1) {
                    selected = historyFragment; // 두번째 탭 선택시 히스토리 프래그먼트
                } else if(position == 2) {
                    selected = settingFragment; // 세번째 탭 선택시 알람 프래그먼트
                } else if(position == 3) {
                    selected = settingFragment; // 네번재 탭 선택시 셋팅 프래그먼트
                }

                android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction(); // 프래그먼트 트랜잭션 실행

                ft.replace(R.id.content_fragment_layout, selected); // 선택된 프래그먼트 실행
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
        ActivityManager manager = (ActivityManager) this.getSystemService(Activity.ACTIVITY_SERVICE);   // 현재 액티비티에서 실행되어있는 모든 서비스 목록 저장

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))    // 실행된 서비스의 수만큼 반복
        {
            if ("com.example.mun.ruok.Service.SensorService".equals(service.service.getClassName())) {  // 해당되는 서비스 이름과 동일한 이름의 서비스가 실행되고 있을 시 트루 값 리턴
                return true;
            }
        }
        return false;
    }
}