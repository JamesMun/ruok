package com.example.mun.ruok.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.mun.ruok.Activity.MainActivity;
import com.example.mun.ruok.AlarmClock.AlarmDBHelper;
import com.example.mun.ruok.AlarmClock.AlarmDetailsActivity;
import com.example.mun.ruok.AlarmClock.AlarmListAdapter;
import com.example.mun.ruok.AlarmClock.AlarmManagerHelper;
import com.example.mun.ruok.AlarmClock.AlarmModel;
import com.example.mun.ruok.R;

import static android.app.Activity.RESULT_OK;

public class AlarmFragment extends Fragment {

    private String TAG = "AlarmFragment";

    public static AlarmListAdapter mAdapter;
    private Context mContext;
    public static AlarmDBHelper dbHelper;

    private Button mButton;

    private ViewGroup rootView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = MainActivity.UserActContext;
        dbHelper = new AlarmDBHelper(mContext);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_alarm_list, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.alarmlist);
        mAdapter = new AlarmListAdapter(mContext, dbHelper.getAlarms());
        mButton = (Button) rootView.findViewById(R.id.addbtn);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAlarmDetailsActivity(-1);
            }
        });

        listView.setAdapter(mAdapter);
        listView.setDivider(new ColorDrawable(Color.GRAY));
        listView.setDividerHeight(1);
        listView.setClickable(true);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void startAlarmDetailsActivity(long id) {
        Intent intent = new Intent(mContext, AlarmDetailsActivity.class);
        intent.putExtra("id", id);
        startActivityForResult(intent, 0);
    }
}
