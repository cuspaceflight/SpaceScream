package com.strand.spacescream;

import java.io.File;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;

import com.strand.global.MessageCode;
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
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
        
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
                    
                case 3:
                    StrandLog.d(TAG, "Starting DisplayWindowImages activity");
                    intent.setClass(ScreamService.this, DisplayWindowImages.class);
                    break;
                    
                default:
                    stage = 0;
                    handler.post(this);
                    
                }
                if (intent.getComponent() != null) {
                    startActivity(intent);
                }
            }
            
        };
        
        handler.postDelayed(runNext, 5000); 
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        
        String params = intent.getStringExtra(MessageCode.PARAM_LIST);
        
        if ("reset".equals(params)) {
            
            // A reset of all data has been requested
            delete(new File(FileManager.DIRECTORY + "/audio"));
            delete(new File(FileManager.DIRECTORY + "/screenshots"));

        } else {
        
            // Check to see if we've been passed a file path to request transfer
            File file = new File(params);
            if (file.exists()) {
                FileManager.getInstance().add(file.getPath());
            }
            
        }
        
        return START_REDELIVER_INTENT;
    }
    
    @Override
    public void onDestroy() {
        StrandLog.d(TAG, "Ending Scream in Space!");
        ending = true;
        handler.removeCallbacks(runNext);
        if (screamActivity != null && !screamActivity.isFinishing()) {
            screamActivity.finish();
        }
        instance = null;
        super.onDestroy();
    }
    
    public void delete(File file) {
        if (file.isDirectory()) {
            for (File c : file.listFiles()) {
                delete(c);
            }
        }
        if (!file.delete()) {
            StrandLog.e(TAG, "Failed to delete file:" + file.getPath());
        }
    }
    
    // Public methods BService or a ScreamActivity might need to call
    
    public static ScreamService getInstance() {
        return instance;
    }
    
    public void registerActivity(ScreamActivity activity) {
        if (screamActivity != null && !screamActivity.isFinishing()) {
            screamActivity.finish();
        }
        screamActivity = activity;
    }
    
    public void unregisterActivity(ScreamActivity activity) {

        screamActivity = null;
        
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
        boolean requested = screenshot;
        screenshot = false;
        return requested;
    }
    
    public void screenshotComplete() {
        screenshot = false;
        StrandLog.d(TAG, "Screenshot has been taken");
        if (screamActivity != null && !screamActivity.isFinishing()) {
            screamActivity.screenshotComplete();
        }
    }
    
}
