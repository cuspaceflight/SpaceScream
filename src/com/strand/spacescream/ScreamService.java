package com.strand.spacescream;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;

import com.strand.global.StrandLog;

public class ScreamService extends Service {
    
    public final static String TAG = "SpaceScream";
    
    private static ScreamService instance;
    
    private ScreamActivity screamActivity;
    
    private Handler handler;
    private Runnable runNext;
    
    private int stage;
    private boolean ending = false;
    private boolean screenshot = false;
    
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
    
    /**
     * We do most of the initialisation in here since it only needs to be run
     * once in the Service life-cycle (not each time startService is called).
     */
    @Override
    public void onCreate() {
        super.onCreate();
        
        instance = this;
        StrandLog.d(TAG, "Scream in Space has started!");
        
        // Set media volume to maximum
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        
        handler = new Handler();
        
        runNext = new Runnable() {

            @Override
            public void run() {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                
                switch (++stage) {
                
                case 1:
                    StrandLog.d(TAG, "Starting PlayVideos activity");
                    intent.setClass(ScreamService.this, PlayVideos.class);
                    break;
                    
                case 2:
                    StrandLog.d(TAG, "Starting DisplayImages activity");
                    intent.setClass(ScreamService.this, DisplayImages.class);
                    break;
                    
                default:
                    stage = 1;
                    handler.post(this);
                    
                }
                
                startActivity(intent);
            }
            
        };
        
        handler.postDelayed(runNext, 5000); 
    }
    
    /**
     * If Service is already running and start command is received, not much
     * needs to be done (we don't particularly need the params in the intent).
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_REDELIVER_INTENT;
    }
    
    @Override
    public void onDestroy() {
        StrandLog.d(TAG, "Ending Scream in Space!");
        ending = true;
        handler.removeCallbacks(runNext);
        if (screamActivity != null && screamActivity.isRunning()) {
            screamActivity.finish();
        }
        instance = null;
        super.onDestroy();
    }
    
    public static ScreamService getInstance() {
        return instance;
    }
    
    // Public methods a ScreamActivity might need to call:
    
    public void registerActivity(ScreamActivity activity) {
        if (screamActivity != null) {
            if (screamActivity.isRunning()) {
                screamActivity.finish();
            }
        }
        screamActivity = activity;
    }
    
    public void unregisterActivity(ScreamActivity activity) {
        String activityName = activity.getClass().getName();
        String currentName = screamActivity.getClass().getName();
        
        if (currentName.equals(activityName)) {
            screamActivity = null;
        }
    }
    
    public void activityEnding(Activity activity) {
        StrandLog.d(TAG, activity.getClass().getName() + " reports it is ending");
        if (!ending) {
            StrandLog.d(TAG, "Scheduled next stage to run");
            handler.postDelayed(runNext, 5000);
        }
    }
    
    public void requestScreenshot() {
        screenshot = true;
        StrandLog.d(TAG, "Screenshot requested");
    }
    
    public boolean screenshotRequested() {
        return screenshot;
    }
    
    public void screenshotComplete() {
        screenshot = false;
        StrandLog.d(TAG, "Screenshot has been taken");
        if (screamActivity.isRunning()) {
            screamActivity.screenshotComplete();
        }
    }
    
}
