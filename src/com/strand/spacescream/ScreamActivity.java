package com.strand.spacescream;

import java.util.ArrayList;

import com.strand.global.MessageCode;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

public class ScreamActivity extends Activity {

    private int stage;
    private boolean forceStop = false;
    
    private Handler handler = new Handler();
    private ArrayList<Runnable> runnables = new ArrayList<Runnable>();
    
    private BroadcastReceiver stopReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            ScreamService.log("Received stop broadcast from ScreamService");
            forceStop = true;
            finish();
        }
        
    };
    
    @Override
    public void onStart() {
        super.onStart();
        Intent intent = getIntent();
        stage = intent.getIntExtra(ScreamService.EXTRA_STAGE, 0);
    }
    
    @Override
    public void finish() {
        if (!forceStop) {
            Intent intent = new Intent(ScreamService.INTENT_ENDING);
            intent.putExtra(ScreamService.EXTRA_STAGE, stage);
            sendBroadcast(intent);
        }
        
        super.finish();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(stopReceiver, new IntentFilter(ScreamService.INTENT_STOP));
    }
    
    @Override
    public void onPause() {
        super.onPause();
        for (Runnable r : runnables) {
            handler.removeCallbacks(r);
        }
        runnables.clear();
        unregisterReceiver(stopReceiver);
    }
    
    @Override
    public void onBackPressed() {
        forceStop = true;
        super.onBackPressed();
    }
    
    public void postDelayed(Runnable r, long delayMillis) {
        if (!runnables.contains(r)) {
            runnables.add(r);
        }
        handler.postDelayed(r, delayMillis);
    }
    
    public void log(String message) {
        ScreamService.log(message);
    }
    
    public void transferFile(String path) {
        Intent intent = new Intent(ScreamService.INTENT_FILE);
        intent.putExtra(MessageCode.FILE_PATH, path);
        sendBroadcast(intent);
    }
    
}
