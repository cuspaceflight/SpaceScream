package com.strand.scream;

import java.io.File;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import com.strand.global.MessageCode;
import com.strand.global.StrandLog;

/**
 * Main Service of the app; launches each activity and schedules next after
 * previous one finishes. Repeats cycle continuously until stop command
 * received. 
 * 
 * @author ejc74
 *
 */
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
                
                switch (++stage) {
                
                case 1:
                    launchActivity(Intro.class, null);
                    break;
                
                case 2:
                    launchActivity(PlayVideos.class, null);
                    break;
                    
                case 3:
                    launchActivity(DisplayImages.class, null);
                    break;
                    
                case 4:
                    launchActivity(DisplayWindowImages.class, null);
                    break;
                    
                case 5:
                    launchActivity(Outro.class, null);
                    break;
                    
                default:
                    stage = 0;
                    handler.post(this);
                    
                }

            }
            
        };
        
        handler.postDelayed(runNext, 5000); 
    }
    
    /**
     * Typically we don't require parameters for the app to run standard
     * schedule. We handle optional PARAM_LIST command here.
     * 
     * Example PARAM_LIST = "?action=video&size=1048576&hq=true"
     * 
     * By default main app cycle won't run after command is followed, unless
     * PARAM_LIST contains "run=true", except in case of video recording, when
     * the run argument is ignored.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        
        String params = intent.getStringExtra(MessageCode.PARAM_LIST);
        // params = "?action=video&size=2048576&hq=true";
        
        if (params != null && !"".equals(params)) {
            StrandLog.d(ScreamService.TAG, "PARAM_LIST received: " + params);
            
            Uri uri = Uri.parse(params);
            
            if (screamActivity == null && "false".equals(uri.getQueryParameter("run"))) {
                // If we have run=false, then we stop main app schedule from running
                // eg. "?action=delete&run=false"
                StrandLog.d(ScreamService.TAG, "Stopping main activity schedule");
                handler.removeCallbacks(runNext);
            }
            
            String action = uri.getQueryParameter("action");
            
            if ("reset".equals(action)) {
                
                // This effectively resets the app
                StrandLog.d(TAG, "A reset of all data has been requested");
                delete(new File(FileManager.DIRECTORY + "/audio"));
                delete(new File(FileManager.DIRECTORY + "/screenshots"));
                
            } else if ("delete".equals(action)) {
                
                // Used to clear SD space
                StrandLog.d(TAG, "Deletion of captured photos/videos requested");
                delete(new File(FileManager.DIRECTORY + "/photos"));
                delete(new File(FileManager.DIRECTORY + "/recorded"));
                
                
            } else if ("video".equals(action)) {
                
                StrandLog.d(TAG, "Command received to record a video from phone");
                if (screamActivity == null) {
                    handler.removeCallbacks(runNext);
                    
                    String size = uri.getQueryParameter("size");
                    String hq = uri.getQueryParameter("hq");
                    
                    Bundle bundle = new Bundle();
                    if (size != null) {
                        try {
                            bundle.putLong("size", Long.parseLong(size));
                        } catch (NumberFormatException e) {
                            StrandLog.e(TAG, "NumberFormatException in parsing size parameter");
                        }
                    }
                    
                    if ("true".equals(hq)) {
                        bundle.putBoolean("hq", true);
                    }
                    
                    launchActivity(RecordVideo.class, bundle);
                } else {
                    StrandLog.d(TAG, "Another activity is already running - cannot record video!");
                }
                
            } else if ("file".equals(action)) {
                
                String path = uri.getQueryParameter("path");
                if (path != null) {
                    File file = new File(path);
                    if (file.exists()) {
                        StrandLog.d(TAG, "File transfer requested: " + file.getPath());
                        FileManager.getInstance().add(file.getPath());
                    }
                }
                
            } else {
                // Not necessarily an error - by default we're passed PARAM_LIST = "1" it seems
                StrandLog.d(ScreamService.TAG, "Action not recognised: " + action);
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
    
    private void launchActivity(Class<? extends ScreamActivity> activity, Bundle bundle) {
        StrandLog.d(TAG, "Starting " + activity.getSimpleName() + " activity");
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        intent.setClass(ScreamService.this, activity);
        startActivity(intent);
    }
    
    private void delete(File file) {
        if (file.isDirectory()) {
            for (File c : file.listFiles()) {
                delete(c);
            }
        }
        if (file.exists() && !file.delete()) {
            StrandLog.e(TAG, "Failed to delete file: " + file.getPath());
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
