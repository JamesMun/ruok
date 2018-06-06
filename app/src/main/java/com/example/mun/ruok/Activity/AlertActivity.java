package com.example.mun.ruok.Activity;

import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.example.mun.ruok.DTO.ConnectDTO;
import com.example.mun.ruok.DTO.UserDTO;
import com.example.mun.ruok.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.mun.ruok.Service.SensorService.sAccount;
import static com.example.mun.ruok.Service.SensorService.sAlert;
import static com.example.mun.ruok.Service.SensorService.sConnData;
import static com.example.mun.ruok.Service.SensorService.sHeart_Count;
import static com.example.mun.ruok.Service.SensorService.sUserData;

public class AlertActivity extends AppCompatActivity{

    private static final String TAG = "AlertActivity";

    private static Vibrator vib;

    private Button btn1;

    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private static DatabaseReference databaseReference = firebaseDatabase.getReference();

    private static final String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
    private static final String SERVER_KEY = "AAAAdnZEnuw:APA91bFVvn4PdlV7NG2ate-xWRrBdsNhkRpcWMfIyHuPS9DVu11K0Q5eWCFii3JtFoJp0pZXXAe8YI9Laa0zs7S1WFZKUQselVsWMC0veJUzQHRFwai2p7MQGZguIWqH7PPAJ3bU-T4M";

    private static final int EMERGENCY_CODE = 3;

    public static Timer timer;
    public static TimerTask timerTask;

    private static int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

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

        if(sUserData.getUserType()) {
            timerStart();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(sUserData.getUserType()) {
            timer.cancel();
            sAlert = false;
            sHeart_Count = 0;
        }
        vib.cancel();
    }

    public static void timerStart() {

        timer = new Timer(true);
        timerTask = new TimerTask() {
            @Override
            public void run() {
                count++;
                if(count > 5) {
                    sendPostToFCM(sConnData.getConnectionWith(), EMERGENCY_CODE);
                }
            }

            @Override
            public boolean cancel() {
                return super.cancel();
            }
        };
        timer.schedule(timerTask, 0, 1000);
    }

    private static void sendPostToFCM(final String USER, final int CODE) {
        databaseReference.child("Users")
                .child(USER)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        final UserDTO userData = dataSnapshot.getValue(UserDTO.class);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    // FMC 메시지 생성 start
                                    JSONObject root = new JSONObject();
                                    JSONObject data = new JSONObject();
                                    data.put("title", "Connecting Code");
                                    data.put("body",CODE);
                                    data.put("tag",sAccount);
                                    root.put("notification", data);
                                    root.put("to", userData.getFcmToken());
                                    // FMC 메시지 생성 end

                                    URL Url = new URL(FCM_MESSAGE_URL);
                                    HttpURLConnection conn = (HttpURLConnection) Url.openConnection();
                                    conn.setRequestMethod("POST");
                                    conn.setDoOutput(true);
                                    conn.setDoInput(true);
                                    conn.addRequestProperty("Authorization", "key=" + SERVER_KEY);
                                    conn.setRequestProperty("Accept", "application/json");
                                    conn.setRequestProperty("Content-type", "application/json");
                                    OutputStream os = conn.getOutputStream();
                                    os.write(root.toString().getBytes("utf-8"));
                                    os.flush();
                                    conn.getResponseCode();

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }
}
