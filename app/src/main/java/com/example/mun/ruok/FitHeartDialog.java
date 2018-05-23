package com.example.mun.ruok;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mun.ruok.Activity.MainActivity;
import com.example.mun.ruok.DTO.FitDTO;
import com.example.mun.ruok.Fragment.SettingFragment;
import com.example.mun.ruok.Service.SensorService;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.example.mun.ruok.Activity.MainActivity.account;
import static com.example.mun.ruok.Service.SensorService.sFitData;

public class FitHeartDialog {

    private Context context;
    private String TAG = "HeartDialog";
    private String account = SensorService.sAccount;

    private int fithour, fitminute;

    private View settingView;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    public FitHeartDialog(Context context, View settingView, int fithour, int fitminute) {
        this.context = context;
        this.settingView = settingView;
        this.fithour = fithour;
        this.fitminute = fitminute;
    }

    // 호출할 다이얼로그 함수를 정의한다.
    public void callFunction() {

        // 커스텀 다이얼로그를 정의하기위해 Dialog클래스를 생성한다.
        final Dialog dlg = new Dialog(context);

        // 액티비티의 타이틀바를 숨긴다.
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 커스텀 다이얼로그의 레이아웃을 설정한다.
        dlg.setContentView(R.layout.dialog_heartrate);

        // 커스텀 다이얼로그를 노출한다.
        dlg.show();

        // 커스텀 다이얼로그의 각 위젯들을 정의한다.
        final Button okButton = (Button) dlg.findViewById(R.id.okButton);
        final Button cancelButton = (Button) dlg.findViewById(R.id.cancelButton);
        final EditText maxhredit = (EditText)dlg.findViewById(R.id.maxheartrate);
        final EditText minhredit = (EditText)dlg.findViewById(R.id.minheartrate);

        maxhredit.setText(String.valueOf(sFitData.getFitMaxHeartRate()));
        minhredit.setText(String.valueOf(sFitData.getFitMinHeartRate()));

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // '확인' 버튼 클릭시 메인 액티비티에서 설정한 main_label에
                // 커스텀 다이얼로그에서 입력한 메시지를 대입한다.

                final int maxhr, minhr;

                String str = maxhredit.getText().toString();
                maxhr = Integer.parseInt(str);

                str = minhredit.getText().toString();
                minhr = Integer.parseInt(str);

                if(maxhr > minhr) {

                    /*sFitData.Fit_minute = fitminute;
                    sFitData.Fit_hour = fithour;
                    sFitData.Fit_max_heart_rate = maxhr;
                    sFitData.Fit_min_heart_rate = minhr;*/

                    sFitData.setFitData(fithour, fitminute, maxhr, minhr);

                    databaseReference.child("Fitness").child(account).setValue(sFitData);

                    Log.d(TAG, "데이터 저장 성공");

                    SensorService.fitStart();
                    Toast.makeText(MainActivity.UserActContext, "운동을 시작합니다.",Toast.LENGTH_SHORT).show();

                    android.support.v7.widget.SwitchCompat switchCompat = (android.support.v7.widget.SwitchCompat) settingView.findViewById(R.id.fit_switch);
                    switchCompat.setChecked(true);

                    // 커스텀 다이얼로그를 종료한다.
                    dlg.dismiss();
                }
                else {
                    Toast.makeText(MainActivity.UserActContext, "최대 심박수가 최소 심박수보다 커야합니다.", Toast.LENGTH_LONG).show();
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "취소 했습니다.", Toast.LENGTH_SHORT).show();

                // 커스텀 다이얼로그를 종료한다.
                dlg.dismiss();
            }
        });
    }
}