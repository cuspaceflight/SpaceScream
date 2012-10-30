package com.strand.spacescream;

import java.io.File;
import java.util.ArrayList;

import com.strand.global.MessageCode;
import com.strand.global.StrandLog;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;

public class ScreamService extends Service {

    public final static String INTENT_STOP = "com.strand.spacescream.STOP";
    public final static String INTENT_FILE = "com.strand.spacescream.FILE";
    
    private static ScreamService instance;
    
    private Handler handler;
    private Runnable runnable;
    
    private BroadcastReceiver broadcastReceiver;
    
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
            }
            
        };
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(ScreamService.INTENT_FILE);
        registerReceiver(broadcastReceiver, filter);
        
        handler = new Handler();
        
        runnable = new Runnable() {

            @Override
            public void run() {
                Intent play = new Intent(ScreamService.this, PlayVideos.class);
                play.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                
                play.putExtra("videos", getVideos());
                
                log("Starting PlayVideos activity");
                startActivity(play);
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

    private ArrayList<String> getVideos() {
        log("Finding videos on SD card");
        
        File directory = new File(Environment.getExternalStorageDirectory(), "SpaceScream/videos");
        File[] files = directory.listFiles();
        
        ArrayList<String> videos = new ArrayList<String>();
        for (File file : files) {
            videos.add(file.getAbsolutePath());
        }
        
        return videos;
    }
}
