package com.strand.spacescream;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;

import com.strand.global.MessageCode;
import com.strand.global.StrandLog;

public class ScreamService extends Service {

    public final static String INTENT_STOP = "com.strand.spacescream.STOP";
    public final static String INTENT_FILE = "com.strand.spacescream.FILE";
    public final static String INTENT_ENDING = "com.strand.spacescream.ENDING";
    public final static String EXTRA_STAGE = "com.strand.spacescream.STAGE";
    
    private static ScreamService instance;
    
    private Handler handler;
    private Runnable runnable;
    
    private BroadcastReceiver broadcastReceiver;
    
    private int stage = 0;
    private static int STAGES = 2;
    
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
        log("Scream in Space has started!");
        
        // Set media volume to maximum
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        
        broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                
                if (intent.getAction().equals(INTENT_FILE)) {
                    log("Received file broadcast");
                    FileManager.getInstance().add(intent.getStringExtra(MessageCode.FILE_PATH));
                }
                
                if (intent.getAction().equals(INTENT_ENDING)) {
                    log("Stage " + intent.getIntExtra(EXTRA_STAGE, 0) + " reports it is ending");
                    stage = (intent.getIntExtra(EXTRA_STAGE, 0) + 1) % STAGES;
                    
                    handler.postDelayed(runnable, 5000);
                    log("Scheduled next stage (" + stage + ") to run");
                }
                
            }
            
        };
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_FILE);
        filter.addAction(INTENT_ENDING);
        registerReceiver(broadcastReceiver, filter);
        
        handler = new Handler();
        
        runnable = new Runnable() {

            @Override
            public void run() {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(EXTRA_STAGE, stage);
                
                switch (stage) {
                case 0:
                    log("Starting PlayVideos activity");
                    intent.setClass(ScreamService.this, PlayVideos.class);
                    break;
                    
                case 1:
                    log("Starting DisplayImages activity");
                    intent.setClass(ScreamService.this, DisplayImages.class);
                    break;
                }
                
                startActivity(intent);
            }
            
        };
        
        handler.postDelayed(runnable, 5000); 
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
        log("Ending Scream in Space!");
        handler.removeCallbacks(runnable);
        sendStopBroadcast();
        unregisterReceiver(broadcastReceiver);
        instance = null;
        super.onDestroy();
    }
    
    private void sendStopBroadcast() {
        Intent intent = new Intent();
        intent.setAction(INTENT_STOP);
        sendBroadcast(intent);
    }
    
    public static ScreamService getInstance() {
        return instance;
    }
    
    public static void log(String message) {
        StrandLog.d("SpaceScream", message);
    }
    
}
