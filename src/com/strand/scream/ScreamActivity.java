package com.strand.scream;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

/**
 * Base class for all activites in app, with extra functionality to deal with
 * the activity lifecycle, plus a few extra helpers.
 * 
 * @author ejc74
 *
 */
public abstract class ScreamActivity extends Activity {
    
    private Handler handler = new Handler();
    private ArrayList<Runnable> runnables = new ArrayList<Runnable>();
    private ScreenshotListener screenshotListener;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreamService.getInstance().registerActivity(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
    
    protected void registerScreenshotListener(ScreenshotListener r) {
        screenshotListener = r;
    }
    
    public void screenshotComplete() {
        if (screenshotListener != null) {
            screenshotListener.screenshotTaken();
        }
    }
    
    public static interface ScreenshotListener {
        public void screenshotTaken();
    }
}
