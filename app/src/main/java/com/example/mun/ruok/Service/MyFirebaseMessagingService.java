package com.example.mun.ruok.Service;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Switch;

import com.example.mun.ruok.Activity.AlertActivity;
import com.example.mun.ruok.Activity.MainActivity;
import com.example.mun.ruok.DTO.ConnectDTO;
import com.example.mun.ruok.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.RemoteMessage;

import static com.example.mun.ruok.Service.SensorService.sConnData;

public class MyFirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    private static final int DEFAULT_CODE = 0;
    private static final int REQUEST_CONNECTING_CODE = 1;
    private static final int CONNECTING_PERMISSION_CODE = 2;
    private static final int EMERGENCY_CODE = 3;


    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    // 메시지 수신
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.i(TAG, "onMessageReceived");

        if(remoteMessage.getData().size() > 0) {
            int CONNECTING_CODE = Integer.valueOf(remoteMessage.getData().get("body"));
            String from = remoteMessage.getData().get("tag");

            switch(CONNECTING_CODE) {
                case REQUEST_CONNECTING_CODE :
                    sendNotification("연결 요청", from + "님이 연결을 요청하셨습니다.");
                    //sConnData.setConnection(from, REQUEST_CONNECTING_CODE);
                    break;
                case CONNECTING_PERMISSION_CODE :
                    sendNotification("연결 승인", from + "님이 연결을 승인하셨습니다.");
                    //sConnData.setConnection(from, CONNECTING_PERMISSION_CODE);
                    break;
                case DEFAULT_CODE :
                    sendNotification("연결 해제", from + "님이 연결을 해제하셨습니다.");
                    //sConnData.setConnection("연결 해제", DEFAULT_CODE);
                    break;
                case EMERGENCY_CODE :
                    sendNotification("위험 알림", from + "님의 상태가 위험합니다.");
                    Intent intent = new Intent(getApplicationContext(), AlertActivity.class);
                    startActivity(intent);
                    break;
            }

            /*if (CONNECTING_CODE == REQUEST_CONNECTING_CODE) {
                sendNotification("연결 요청", from + "님이 연결을 요청하셨습니다.");
                //sConnData.setConnection(from, REQUEST_CONNECTING_CODE);
            } else if (CONNECTING_CODE == CONNECTING_PERMISSION_CODE) {
                sendNotification("연결 승인", from + "님이 연결을 승인하셨습니다.");
                //sConnData.setConnection(from, CONNECTING_PERMISSION_CODE);
            } else if (CONNECTING_CODE == DEFAULT_CODE) {
                sendNotification("연결 해제", from + "님이 연결을 해제하셨습니다.");
                //sConnData.setConnection("연결 해제", DEFAULT_CODE);
            } else if (CONNECTING_CODE == EMERGENCY_CODE) {
                sendNotification("위험 알림", from + "님의 상태가 위험합니다.");
                Intent intent = new Intent(getApplicationContext(), AlertActivity.class);
                startActivity(intent);
            }*/
        }
    }

    private void sendNotification(String title, String content) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "channel_id")
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_dialog_info))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}