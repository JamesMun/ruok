package com.example.mun.ruok;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mun.ruok.Database.UserSQLiteHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.example.mun.ruok.Fragment_TabMain;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.concurrent.atomic.AtomicMarkableReference;

/**
 * Created by Mun on 2018-03-22.
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    public static Context LoginContext;

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "LoginActivity";

    private int UserType;

    private GoogleSignInClient mGoogleSignInClient;
    private UserSQLiteHelper Usersqlhelper = new UserSQLiteHelper();

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    private SQLiteDatabase db;

    GoogleSignInAccount userAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();
    }

    //초기화 함수
    protected void init() {

        // Button listeners
        findViewById(R.id.sign_in_button).setOnClickListener(this);


        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // gso로 지정된 옵션을 사용하여 GoogleSignInClient를 빌드합니다.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }


    @Override
    public void onStart() {
        super.onStart();

        // [START on_start_sign_in]
        //account가 null이 아닐 경우, 이 사용자는 이미 구글 로그인 상태이다.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        // [END on_start_sign_in]
        //updateUI(account);

        //이미 구글 로그인이 된(이 앱에 로그인 된) 상태
        if (account != null) {
            String email = account.getEmail();

            UserDTO userData = new UserDTO();
            userData.userEmailID = email.substring(0, email.indexOf('@'));
            userData.fcmToken = FirebaseInstanceId.getInstance().getToken();

            databaseReference.child("Users").child(userData.userEmailID).setValue(userData);

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("account", userData.userEmailID);

            startActivity(intent);
            finish();

        } else {//로그인이 되지 않았으니, 로그인을 받아야 한다.

        }
    }

    // [START onActivityResult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {//이 시점에서 이미 로그인이 되어 있다.
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    // [END onActivityResult]

    // [START handleSignInResult]
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            userAccount = account;//로그인된 계정 정보

            DBservice();

            if(!Usersqlhelper.isTable(db)) {
                checkUserType();
                Usersqlhelper.createTable(db);
            }
            else {
                checkUserType();
                Usersqlhelper.removeData(db);
            }

            Usersqlhelper.insertData(db, userAccount.getId(), UserType);

            String email = userAccount.getEmail();

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("account", email.substring(0, email.indexOf('@')));
            db.close();

            startActivity(intent);
            finish();

            // Signed in successfully, show authenticated UI.
            //updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }
    // [END handleSignInResult]

    // [START signIn]
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);//구글 로그인 창(구글아이디 고르기 를 연다)
    }
    // [END signIn]

    // [START revokeAccess]

    //로그인 된 사용자의 개인정보 확인.
    //메일, 이름, 나이, 성별 확인 필요.
    private void profile() {
        String email = userAccount.getEmail();
        String id = userAccount.getId();
        String familyname = userAccount.getFamilyName();
        String givenname = userAccount.getGivenName();

        Toast.makeText(getApplicationContext(), "email : " + email + "\nid = " + id + "\nfamilyname = " + familyname + "\ngivenname = " + givenname, Toast.LENGTH_SHORT).show();

    }

    private void DBservice() {
        db = openOrCreateDatabase("RUOK", Context.MODE_PRIVATE, null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

    private void checkUserType() {
        RadioGroup rg = (RadioGroup) findViewById(R.id.userradiogroup);
        int id = rg.getCheckedRadioButtonId();
        RadioButton rb = (RadioButton) findViewById(id);
        if(rb.getText().toString().equals("사용자")) {
            UserType = 0;
        }
        else {
            UserType = 1;
        }
    }
}
