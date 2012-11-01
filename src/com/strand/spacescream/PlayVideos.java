package com.strand.spacescream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.widget.VideoView;

public class PlayVideos extends ScreamActivity {

    private ArrayList<String> videos;
    private int index = 0;
    
    private VideoView videoView;
    private OnPreparedListener preparedListener;
    private OnCompletionListener completionListener;
    
    private MediaRecorder mediaRecorder;
    private String audioPath;
    private boolean recording = false;
    
    private Runnable runnable;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playback);
        
        runnable = new Runnable() {

            @Override
            public void run() {
                loadVideo();
            }
            
        };
        
        preparedListener = new OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                startRecording();
                videoView.start();
            }
            
        };
        
        completionListener = new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                stopRecording(false);
                postDelayed(runnable, 5000);
            }
            
        };
        
        videoView = (VideoView) findViewById(R.id.videoView);
        videoView.setZOrderMediaOverlay(true);
        videoView.setOnPreparedListener(preparedListener);
        videoView.setOnCompletionListener(completionListener);
        
        videos = getVideos();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        postDelayed(runnable, 5000);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        if (videoView.isPlaying()) {
            videoView.stopPlayback();
        }
        if (recording) {
            stopRecording(true);
        }
    }
    
    private void loadVideo() {
        if (!videos.isEmpty() && videos.size() > index) {
            String path = videos.get(index++);
            if (path != null) {
                log("Loading " + path);
                File video = new File(path);
                audioPath = FileManager.DIRECTORY + "/audio/" + video.getName() + ".3gp";
                videoView.setVideoPath(path);
            } else {
                loadVideo();
            }
        } else {
            log("No more videos left in queue; ending activity");            
            finish();
        }
    }
    
    private void startRecording() {
        
        if (!(new File(audioPath)).exists()) {
            
            log("Starting audio recording to " + audioPath);
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setOutputFile(audioPath);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            
            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
                recording  = true;
            } catch (IllegalStateException e) {
                log("Record audio prepare() failed with IllegalStateException");
            } catch (IOException e) {
                log("Record audio prepare() failed with IOException");
            }
            
        } else {
            log("Recording of video already exists: " + audioPath);
        }

    }
    
    private void stopRecording(boolean interrupted) {

        if (recording) {
            recording = false;
            log("Stopping audio recording");
            if (mediaRecorder != null) {
                try {
                    mediaRecorder.stop();
                    mediaRecorder.release();
                    
                    if (interrupted) {
                        // Delete incomplete recording from SD card
                        (new File(audioPath)).delete();
                    } else {
                        // Send broadcast message of new file to transfer
                        transferFile(audioPath);
                    }
                    
                } catch (IllegalStateException e) {
                    log("IllegalStateException in stopRecording");
                }
                mediaRecorder = null;
            }
        }
        
    }
    
    private ArrayList<String> getVideos() {
        log("Finding videos on SD card");
        
        File directory = new File(FileManager.DIRECTORY, "videos");
        File[] files = directory.listFiles();
        
        ArrayList<String> videos = new ArrayList<String>();
        for (File file : files) {
            videos.add(file.getAbsolutePath());
        }
        
        return videos;
    }
    
}
