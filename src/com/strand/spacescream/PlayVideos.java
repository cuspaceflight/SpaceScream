package com.strand.spacescream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.strand.global.MessageCode;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.widget.VideoView;

public class PlayVideos extends Activity {

    private ArrayList<CharSequence> videos;
    private int index = 0;
    
    private VideoView videoView;
    private OnPreparedListener preparedListener;
    private OnCompletionListener completionListener;
    
    private MediaRecorder mediaRecorder;
    private String filename;
    private String path;
    private boolean recording = false;
    
    private Handler handler;
    private Runnable runnable;
    
    private BroadcastReceiver broadcastReceiver;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playback);
        
        broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                ScreamService.log("Received stop broadcast from ScreamService");
                finish();
            }
            
        };
        
        handler = new Handler();
        runnable = new Runnable() {

            @Override
            public void run() {
                loadVideo();
            }
            
        };
        
        preparedListener = new OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                startRecording(filename);
                videoView.start();
            }
            
        };
        
        completionListener = new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                stopRecording(false);
                handler.postDelayed(runnable, 5000);
            }
            
        };
        
        videoView = (VideoView) findViewById(R.id.videoView);
        videoView.setZOrderMediaOverlay(true);
        videoView.setOnPreparedListener(preparedListener);
        videoView.setOnCompletionListener(completionListener);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        
        Intent intent = getIntent();
        videos = intent.getCharSequenceArrayListExtra("videos");
        
        // For testing
        if (videos == null) {
            videos = new ArrayList<CharSequence>();
            videos.add("/sdcard/SpaceScream/videos/1331554436487.mp4");
        }
        
    }
    
    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(ScreamService.INTENT_STOP));
        handler.postDelayed(runnable, 5000);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
        if (videoView.isPlaying()) {
            videoView.stopPlayback();
        }
        if (recording) {
            stopRecording(true);
        }
        unregisterReceiver(broadcastReceiver);
    }
    
    private void loadVideo() {
        if (!videos.isEmpty() && videos.size() > index) {
            CharSequence path = videos.get(index++);
            if (path != null) {
                File video = new File(path.toString());
                ScreamService.log("Loading " + video.getAbsolutePath());
                filename = video.getName();
                videoView.setVideoPath(path.toString());
            } else {
                loadVideo();
            }
        } else {
            ScreamService.log("No more videos left in queue; ending activity");
            setResult(RESULT_OK);
            finish();
        }
    }
    
    private void startRecording(String filename) {
        
        path = "/sdcard/SpaceScream/" + filename + ".3gp";
        if (!(new File(path)).exists()) {
            
            ScreamService.log("Starting audio recording of " + filename);
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setOutputFile(path);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            
            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
                recording  = true;
            } catch (IllegalStateException e) {
                ScreamService.log("Record audio prepare() failed with IllegalStateException");
            } catch (IOException e) {
                ScreamService.log("Record audio prepare() failed with IOException");
            }
            
        } else {
            ScreamService.log("Recording of video already exists: " + path);
        }

    }
    
    private void stopRecording(boolean interrupted) {

        if (recording) {
            ScreamService.log("Stopping audio recording");
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            
            if (interrupted) {
                // Delete incomplete recording from SD card
            } else {
                Intent intent = new Intent(ScreamService.INTENT_FILE);
                intent.putExtra(MessageCode.FILE_PATH, path);
                sendBroadcast(intent);
            }
        }
        
    }
    
}
