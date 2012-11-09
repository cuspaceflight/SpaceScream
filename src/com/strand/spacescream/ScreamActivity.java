package com.strand.spacescream;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

public class ScreamActivity extends Activity {
    
    private Handler handler = new Handler();
    private ArrayList<Runnable> runnables = new ArrayList<Runnable>();
    private Runnable screenshotListener;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreamService.getInstance().registerActivity(this);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ScreamService.getInstance() != null) {
            ScreamService.getInstance().unregisterActivity(this);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        for (Runnable r : runnables) {
            handler.removeCallbacks(r);
        }
        runnables.clear();
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    
    public void postDelayed(Runnable r, long delayMillis) {
        if (!runnables.contains(r)) {
            runnables.add(r);
        }
        handler.postDelayed(r, delayMillis);
    }
    
    protected void registerScreenshotListener(Runnable r) {
        screenshotListener = r;
    }
    
    public void screenshotComplete() {
        if (screenshotListener != null) {
            handler.post(screenshotListener);
        }
    }
}
