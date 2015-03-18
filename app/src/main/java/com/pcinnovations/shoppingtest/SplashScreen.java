package com.pcinnovations.shoppingtest;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class SplashScreen extends ActionBarActivity {

    public static final int SPLASH_DISMISS_TIME = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        getSupportActionBar().hide();
        new SplashDismiss().start();
    }

    private class SplashDismiss extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(SPLASH_DISMISS_TIME);
                Intent startIntent = new Intent(SplashScreen.this, HelloActivity.class);
                SplashScreen.this.startActivity(startIntent);
                SplashScreen.this.finish();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
