package com.example.mun.ruok;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    private static final int DEFAULT_CODE = 0;
    private static final int REQUEST_CONNECTING_CODE = 1;
    private static final int CONNECTING_PERMISSION_CODE = 2;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    // 메시지 수신
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.i(TAG, "onMessageReceived");

        //Map<String, String> data = remoteMessage.getData();
        String title = remoteMessage.getNotification().getTitle();
        int CONNECTING_CODE = Integer.valueOf(remoteMessage.getNotification().getBody());
        String from = remoteMessage.getNotification().getTag();

        if(CONNECTING_CODE == REQUEST_CONNECTING_CODE) {
            sendNotification("연결 요청",from + "님이 연결을 요청하셨습니다.");

            setConnectingCode(from, REQUEST_CONNECTING_CODE);
        } else if(CONNECTING_CODE == CONNECTING_PERMISSION_CODE) {
            sendNotification("연결 승인",from + "님이 연결을 승인하셨습니다.");

            setConnectingCode(from, CONNECTING_PERMISSION_CODE);
        }
    }

    void setConnectingCode(String from, int CODE) {
        ConnectDTO connectDTO = new ConnectDTO();

        connectDTO.ConnectionWith = from;
        connectDTO.CONNECTING_CODE = CODE;

        databaseReference.child("Connection").child(SensorService.account).setValue(connectDTO);

        SensorService.CONNECTING_STATE = CODE;
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