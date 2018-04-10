package com.example.mun.ruok;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    
    private Fragment tabFragment;
    private Fragment settingFragment;

    public static MainActivity UserActContext;

    public static String account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom);

        tabMake();
        getUserInfo();

        UserActContext = this;
    }

    private void getUserInfo() {
        Intent intent = getIntent();
        account = intent.getExtras().getString("account");    // 로그인 결과로 넘어온 유저 계정
        Toast.makeText(this, account, Toast.LENGTH_SHORT).show();

    }

    private void tabMake() {

        tabFragment = new Fragment_TabMain();
        settingFragment = new SettingFragment();

        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        ft.replace(R.id.content_fragment_layout, tabFragment);
        ft.commit();

        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        /*tabs.addTab(tabs.newTab().setText("Home"));
        tabs.addTab(tabs.newTab().setText("History"));
        tabs.addTab(tabs.newTab().setText("Alarm"));
        tabs.addTab(tabs.newTab().setText("Settings"));*/
        tabs.addTab(tabs.newTab().setIcon(R.drawable.home));
        tabs.addTab(tabs.newTab().setIcon(R.drawable.history));
        tabs.addTab(tabs.newTab().setIcon(R.drawable.alarm));
        tabs.addTab(tabs.newTab().setIcon(R.drawable.setting));
        tabs.setTabGravity(TabLayout.GRAVITY_FILL);

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                Log.d("MainActivity", "선택된 탭 : " + position);

                Fragment selected = null;
                if(position == 0) {
                    selected = tabFragment;
                } else if(position == 1) {
                    selected = settingFragment;
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
}