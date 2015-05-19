package com.pcinnovations.shoppingtest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;


public class SplashScreen extends ActionBarActivity {

    public static final int SPLASH_DISMISS_TIME = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        getSupportActionBar().hide();
        if(!isOnline()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreen.this);
            builder.setCancelable(false)
                    .setIcon(R.drawable.ic_stop)
                    .setTitle(getString(R.string.alert_no_net_title))
                    .setMessage(getString(R.string.alert_no_net_body))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SplashScreen.this.finish();
                        }
                    })
                    .show();
        } else {
            new SplashDismiss().start();
        }
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

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
