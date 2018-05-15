package com.example.mun.ruok.Fragment;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.mun.ruok.DTO.ConnectDTO;
import com.example.mun.ruok.DTO.UserDTO;
import com.example.mun.ruok.Database.FitSQLiteHelper;
import com.example.mun.ruok.HeartDialog;
import com.example.mun.ruok.Activity.LoginActivity;
import com.example.mun.ruok.Activity.MainActivity;
import com.example.mun.ruok.R;
import com.example.mun.ruok.Service.SensorService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.example.mun.ruok.Activity.MainActivity.UserActContext;
import static com.example.mun.ruok.Activity.MainActivity.account;
import static com.example.mun.ruok.Service.SensorService.CONNECTING_STATE;

public class SettingFragment extends Fragment {

    private static final String TAG = "SettingFragment";

    private static final int DEFAULT_CODE = 0;
    private static final int REQUEST_CONNECTING_CODE = 1;
    private static final int CONNECTING_PERMISSION_CODE = 2;

    private ViewGroup rootView;

    public static int maxhr, minhr;

    public static int fitHour=0, fitMinute=0;

    private GoogleSignInClient mGoogleSignInClient;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    private static final String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
    private static final String SERVER_KEY = "AAAAdnZEnuw:APA91bFVvn4PdlV7NG2ate-xWRrBdsNhkRpcWMfIyHuPS9DVu11K0Q5eWCFii3JtFoJp0pZXXAe8YI9Laa0zs7S1WFZKUQselVsWMC0veJUzQHRFwai2p7MQGZguIWqH7PPAJ3bU-T4M";

    private FitSQLiteHelper Fitsqlhelper = new FitSQLiteHelper();

    public static String[] values = {"로그아웃", "서비스 중지", "심박수 설정", "운동시간", "연결", "만든이"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_setting, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.listview);

        if(CONNECTING_STATE == REQUEST_CONNECTING_CODE) {
            values[4] = "연결 요청 취소";
        } else if(CONNECTING_STATE == CONNECTING_PERMISSION_CODE) {
            values[4] = "연결 해제";
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, values)
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.BLACK);
                return view;
            }
        };

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0) {
                    UserActContext.stopSensorService();
                    signOut();
                }
                else if(position == 1) {
                    UserActContext.stopSensorService();
                }
                else if(position == 2) {
                    HeartDialog heartDialog = new HeartDialog(getContext());
                    heartDialog.callFunction();
                }
                else if(position == 3) {
                    setFitnessTime();
                }
                else if(position == 4) {
                    if(SensorService.UserType == 1) {
                        if(CONNECTING_STATE == CONNECTING_PERMISSION_CODE) {
                            // 연결 해제
                        } else {
                            SendPermissionRequest();    // 연결 요청 메시지 보내기
                        }
                    }
                    else {
                        if(CONNECTING_STATE == DEFAULT_CODE) {
                            //SendPermissionRequest();    // 연결 요청 메시지 보내기
                            Toast.makeText(MainActivity.UserActContext,"사용자는 연결을 요청 할 수 없습니다.", Toast.LENGTH_SHORT).show();
                        } else if(CONNECTING_STATE == REQUEST_CONNECTING_CODE){
                            databaseReference.child("Connection").child(account).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    ConnectDTO connectDTO = dataSnapshot.getValue(ConnectDTO.class);
                                    sendPostToFCM(connectDTO.ConnectionWith, CONNECTING_PERMISSION_CODE);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }
            }
        });

        return rootView;
    }

    private void signOut() {

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // gso로 지정된 옵션을 사용하여 GoogleSignInClient를 빌드합니다.
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        mGoogleSignInClient.signOut();

        Intent intent = new Intent(getActivity(), LoginActivity.class);

        startActivity(intent);
        UserActContext.finish();
    }

    private void setFitnessTime() {
        TimePickerDialog.OnTimeSetListener timeListener = new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                fitHour = hourOfDay;
                fitMinute = minute;
                Fitsqlhelper.insertData(MainActivity.db, fitHour, fitMinute);
            }
        };

        TimePickerDialog fitDialog = new TimePickerDialog(getContext(), timeListener, fitHour, fitMinute, true);

        fitDialog.show();
    }

    private void SendPermissionRequest() {
        final EditText editText = new EditText(MainActivity.UserActContext);
        new AlertDialog.Builder(MainActivity.UserActContext)
                .setMessage("연결 대상의 구글 계정을 입력하여 주세요.")
                .setView(editText)
                .setPositiveButton("보내기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendPostToFCM(editText.getText().toString(), REQUEST_CONNECTING_CODE);
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // not thing..
                    }
                }).show();
    }

    private void sendPostToFCM(final String USER, final int CODE) {
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
                                    JSONObject notification = new JSONObject();
                                    notification.put("title", "Connecting Code");
                                    notification.put("body",CODE);
                                    notification.put("tag",account);
                                    root.put("notification", notification);
                                    root.put("to", userData.fcmToken);
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

                                    ConnectDTO connectDTO = new ConnectDTO();
                                    connectDTO.ConnectionWith = USER;
                                    connectDTO.CONNECTING_CODE = CODE;

                                    databaseReference.child("Connection").child(account).setValue(connectDTO);

                                    connectDTO.ConnectionWith = account;
                                    databaseReference.child("Connection").child(USER).setValue(connectDTO);
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