package com.example.mun.ruok.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.mun.ruok.DTO.ConnectDTO;
import com.example.mun.ruok.DTO.FitDTO;
import com.example.mun.ruok.DTO.GuardianDTO;
import com.example.mun.ruok.DTO.UserDTO;
import com.example.mun.ruok.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

/**
 * Created by Mun on 2018-03-22.
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "LoginActivity";

    private boolean UserType;

    private GoogleSignInClient mGoogleSignInClient;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    private static final int DEFAULT_CODE = 0;

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

            databaseReference.child("Users").child(email.substring(0, email.indexOf('@'))).child("fcmToken").setValue(FirebaseInstanceId.getInstance().getToken());

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("account", email.substring(0, email.indexOf('@')));

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

            String email = account.getEmail();

            checkUserType();
            makeDBonFirebase(account);

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("account", email.substring(0, email.indexOf('@')));

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

    private void makeDBonFirebase(GoogleSignInAccount account) {
        final String email = account.getEmail().substring(0,account.getEmail().indexOf('@'));

        databaseReference.child("Users").child(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                UserDTO userDTO = dataSnapshot.getValue(UserDTO.class);

                if(userDTO == null) {

                    if(UserType) {
                        UserDTO userData = new UserDTO();
                        userData.setUserData(email, FirebaseInstanceId.getInstance().getToken(), 120, 60, UserType);
                        databaseReference.child("Users").child(email).setValue(userData);

                        FitDTO fitDTO = new FitDTO();
                        fitDTO.setFitData(2,0,60,140);
                        databaseReference.child("Fitness").child(email).setValue(fitDTO);
                    } else if(!UserType) {
                        GuardianDTO guardianData = new GuardianDTO();
                        guardianData.setGuardianData(email, FirebaseInstanceId.getInstance().getToken(), UserType);
                        databaseReference.child("Users").child(email).setValue(guardianData);
                    }

                    ConnectDTO connectDTO = new ConnectDTO();
                    connectDTO.setConnection("연결 해제", DEFAULT_CODE);

                    databaseReference.child("Connection").child(email).setValue(connectDTO);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
            UserType = true;
        }
        else {
            UserType = false;
        }
    }
}
