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

/**
 * Created by Administrator on 2017-08-07.
 */

public class FitHeartDialog {

    private Context context;
    private String TAG = "HeartDialog";
    private String account = SensorService.account;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    public FitHeartDialog(Context context) {
        this.context = context;
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

        maxhredit.setText(String.valueOf(SensorService.fit_max_heart_rate));
        minhredit.setText(String.valueOf(SensorService.fit_min_heart_rate));

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // '확인' 버튼 클릭시 메인 액티비티에서 설정한 main_label에
                // 커스텀 다이얼로그에서 입력한 메시지를 대입한다.

                final int maxhr, minhr;

                String str = maxhredit.getText().toString();
                //SettingFragment.maxhr = Integer.parseInt(str);
                maxhr = Integer.parseInt(str);

                str = minhredit.getText().toString();
                minhr = Integer.parseInt(str);

                if(maxhr > minhr) {

                    FitDTO fitDTO = new FitDTO();
                    fitDTO.Fit_minute = SettingFragment.fitMinute;
                    fitDTO.Fit_hour = SettingFragment.fitHour;
                    fitDTO.Fit_max_heart_rate = maxhr;
                    fitDTO.Fit_min_heart_rate = minhr;

                    databaseReference.child("Fitness").child(account).setValue(fitDTO);

                    Log.d(TAG, "데이터 저장 성공");

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