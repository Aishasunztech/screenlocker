package com.secureMarket;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.secure.launcher.R;

import com.screenlocker.secure.base.BaseActivity;

public class SecureMarketActivity extends BaseActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secure_market);

        ImageView logo = findViewById(R.id.logo);
        ImageView name = findViewById(R.id.name);

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(1000);



        AnimationSet animation = new AnimationSet(false); //change to false
        animation.addAnimation(fadeIn);
        logo.setAnimation(animation);
        name.setAnimation(animation);

        new Handler().postDelayed(() -> {
            startActivity(new Intent(
                    this, SMActivity.class
            ));
            SecureMarketActivity.this.finish();
        },2000);

    }

    @Override
    public void onBackPressed() {
        //
    }
}
