package com.example.mun.ruok.Activity;

import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.mun.ruok.R;
import com.example.mun.ruok.Service.SensorService;
import static com.example.mun.ruok.Service.SensorService.sAlert;
import static com.example.mun.ruok.Service.SensorService.sHeart_Count;

public class AlertActivity extends AppCompatActivity{

    private static final String TAG = "AlertActivity";

    Vibrator vib;

    private Button btn1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        Log.d(TAG, "AlertAcitivity.onCreate");

        vib = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        vib.vibrate(new long[]{0,1000}, 0);

        btn1 = (Button)findViewById(R.id.confirmbtn);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Click Button");
                AlertActivity.this.finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        vib.cancel();
        sAlert = false;
        sHeart_Count = 0;
    }
}
