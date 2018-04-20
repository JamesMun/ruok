package com.example.mun.ruok;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Administrator on 2017-08-07.
 */

public class HeartDialog {

    private Context context;

    public HeartDialog(Context context) {
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

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // '확인' 버튼 클릭시 메인 액티비티에서 설정한 main_label에
                // 커스텀 다이얼로그에서 입력한 메시지를 대입한다.

                String str = maxhredit.getText().toString();
                SettingFragment.maxhr = Integer.parseInt(str);

                str = minhredit.getText().toString();
                SettingFragment.minhr = Integer.parseInt(str);

                if(SettingFragment.maxhr > SettingFragment.minhr) {
                    SensorService.max_heart_rate = SettingFragment.maxhr;
                    SensorService.min_heart_rate = SettingFragment.minhr;

                    // 커스텀 다이얼로그를 종료한다.
                    dlg.dismiss();
                }
                else {
                    Toast.makeText(MainActivity.UserActContext, "최대 심박수가 최소 심박수보다 커야합니다.", Toast.LENGTH_SHORT).show();
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