package com.example.mun.ruok.Fragment;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.mun.ruok.DTO.ConnectDTO;
import com.example.mun.ruok.DTO.UserDTO;
import com.example.mun.ruok.FitHeartDialog;
import com.example.mun.ruok.HeartDialog;
import com.example.mun.ruok.Activity.LoginActivity;
import com.example.mun.ruok.Activity.MainActivity;
import com.example.mun.ruok.ListViewAdapter;
import com.example.mun.ruok.R;
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
import static com.example.mun.ruok.Service.SensorService.sConnData;
import static com.example.mun.ruok.Service.SensorService.sUserData;
import static com.example.mun.ruok.Service.SensorService.sFitData;

public class SettingFragment extends Fragment {

    private static final String TAG = "SettingFragment";

    private static final int DEFAULT_CODE = 0;
    private static final int REQUEST_CONNECTING_CODE = 1;
    private static final int CONNECTING_PERMISSION_CODE = 2;

    private ViewGroup rootView;

    private GoogleSignInClient mGoogleSignInClient;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    private static final String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
    private static final String SERVER_KEY = "AAAAdnZEnuw:APA91bFVvn4PdlV7NG2ate-xWRrBdsNhkRpcWMfIyHuPS9DVu11K0Q5eWCFii3JtFoJp0pZXXAe8YI9Laa0zs7S1WFZKUQselVsWMC0veJUzQHRFwai2p7MQGZguIWqH7PPAJ3bU-T4M";

    public static String[] values = {"로그아웃", "서비스 중지", "심박수 설정", "운동시간", "연결", "만든이"};

    public static View settingview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_setting, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.listview);

        if(sConnData.getConnectingCode() == REQUEST_CONNECTING_CODE) {
            //if(UserType == 0) {
            if(sUserData.getUserType() == 0) {
                values[4] = "연결 승인";
            } else {
                values[4] = "연결 취소";
            }
        } else if(sConnData.getConnectingCode() == CONNECTING_PERMISSION_CODE) {
            values[4] = "연결 해제";
        } else {
            values[4] = "연결";
        }

        ListViewAdapter adapter = new ListViewAdapter(MainActivity.UserActContext, R.layout.listview_item, values);
        listView.setAdapter(adapter);
        listView.setClickable(true);
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
                    setFitnessTime(view);
                }
                else if(position == 4) {
                    if (sConnData.getConnectingCode() == CONNECTING_PERMISSION_CODE) {   // 연결이 되어 있을 경우 유저 타입에 상관없이 연결 해제 가능
                        sConnData.setConnectingCode(DEFAULT_CODE);
                        sendPostToFCM(sConnData.getConnectionWith(), DEFAULT_CODE);// 연결 해제
                        Toast.makeText(MainActivity.UserActContext, "연결이 해제 되었습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        if(sUserData.getUserType() == 1) {
                            if (sConnData.getConnectingCode() == DEFAULT_CODE) {
                                SendPermissionRequest();    // 연결 요청 메시지 보내기
                            } else if(sConnData.getConnectingCode() == REQUEST_CONNECTING_CODE) {
                                sendPostToFCM(sConnData.getConnectionWith(), DEFAULT_CODE);
                                Toast.makeText(MainActivity.UserActContext, "연결을 취소 하셨습니다.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            if (sConnData.getConnectingCode() == DEFAULT_CODE) {
                                //SendPermissionRequest();    // 연결 요청 메시지 보내기
                                Toast.makeText(MainActivity.UserActContext, "사용자는 연결을 요청 할 수 없습니다.", Toast.LENGTH_SHORT).show();
                            } else if (sConnData.getConnectingCode() == REQUEST_CONNECTING_CODE) {
                                databaseReference.child("Connection").child(account).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        ConnectDTO connectDTO = dataSnapshot.getValue(ConnectDTO.class);
                                        sendPostToFCM(connectDTO.getConnectionWith(), CONNECTING_PERMISSION_CODE);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                                Toast.makeText(MainActivity.UserActContext, "연결을 승인 하셨습니다.", Toast.LENGTH_SHORT).show();
                            }
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

    private void setFitnessTime(final View settingView) {
        TimePickerDialog.OnTimeSetListener timeListener = new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                FitHeartDialog fitheartDialog = new FitHeartDialog(getContext(), settingView, hourOfDay, minute);
                fitheartDialog.callFunction();
            }
        };

        TimePickerDialog fitDialog = new TimePickerDialog(getContext(), timeListener, sFitData.getFitHour(), sFitData.getFitMinute(), true);

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

                                    ConnectDTO connectDTO = new ConnectDTO();
                                    if(CODE == DEFAULT_CODE) {
                                        connectDTO.setConnection("연결 해제", CODE);

                                        databaseReference.child("Connection").child(account).setValue(connectDTO);
                                        databaseReference.child("Connection").child(USER).setValue(connectDTO);
                                    } else {
                                        connectDTO.setConnection(USER,CODE);

                                        databaseReference.child("Connection").child(account).setValue(connectDTO);

                                        connectDTO.setConnectionWith(account);
                                        databaseReference.child("Connection").child(USER).setValue(connectDTO);
                                    }

                                    sConnData.setConnectingCode(CODE);

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