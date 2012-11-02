package com.strand.spacescream;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

public class ScreamActivity extends Activity {

    private boolean active = false;
    
    private Handler handler = new Handler();
    private ArrayList<Runnable> runnables = new ArrayList<Runnable>();
    private Runnable screenshotListener;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreamService.getInstance().registerActivity(this);
        active = true;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            ScreamService.getInstance().activityEnding(this);
            ScreamService.getInstance().unregisterActivity(this);
        } catch (NullPointerException e) {
            // Probably ending app as Service is being destroyed
        }
        active = false;
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
    
    public boolean isRunning() {
        return !isFinishing() && active;
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
