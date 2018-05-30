package com.example.mun.ruok.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.example.mun.ruok.R;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView imageView = (ImageView) findViewById(R.id.imageView2);

        Animation animation = AnimationUtils.loadAnimation(SplashScreen.this, R.anim.move);
        animation.setRepeatCount(Animation.INFINITE);
        imageView.startAnimation(animation);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable(){
            public void run() {
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                SplashScreen.this.finish();
        }},2000);
    }

}