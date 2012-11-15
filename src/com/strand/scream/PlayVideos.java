package com.strand.scream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.strand.global.StrandLog;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.widget.VideoView;

/**
 * Plays each video, and records audio if recording isn't already present on
 * the SD card. Completed recordings are queued for transfer.
 * 
 * SD: Looks for videos in videos/ directory.
 * 
 * @author ejc74
 *
 */
public class PlayVideos extends ScreamActivity {

    private ArrayList<String> videos;
    private int index = 0;
    
    private VideoView videoView;
    private OnPreparedListener preparedListener;
    private OnCompletionListener completionListener;
    private OnErrorListener errorListener;
    
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
        
        errorListener = new OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                stopRecording(true);
                postDelayed(runnable, 10000);
                return true;
            }
            
        };
        
        videoView = (VideoView) findViewById(R.id.videoView);
        videoView.setZOrderMediaOverlay(true);
        videoView.setOnPreparedListener(preparedListener);
        videoView.setOnCompletionListener(completionListener);
        videoView.setOnErrorListener(errorListener);
        
        videos = FileManager.getFiles("videos");
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
                StrandLog.d(ScreamService.TAG, "Loading " + path);
                File video = new File(path);
                audioPath = FileManager.DIRECTORY + "/audio/" + video.getName() + ".3gp";
                videoView.setVideoPath(path);
            } else {
                loadVideo();
            }
        } else {
            StrandLog.d(ScreamService.TAG, "No more videos left in queue; ending activity");            
            finish();
        }
    }
    
    private void startRecording() {
        
        File audio = new File(audioPath);
        
        if (!audio.exists()) {
            
            audio.getParentFile().mkdirs();
            
            StrandLog.d(ScreamService.TAG, "Starting audio recording to " + audioPath);
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
                StrandLog.e(ScreamService.TAG, "Record audio prepare() failed with IllegalStateException");
            } catch (IOException e) {
                StrandLog.e(ScreamService.TAG, "Record audio prepare() failed with IOException");
            }
            
        } else {
            StrandLog.d(ScreamService.TAG, "Recording of video already exists: " + audioPath);
        }

    }
    
    private void stopRecording(boolean interrupted) {

        if (recording) {
            recording = false;
            StrandLog.d(ScreamService.TAG, "Stopping audio recording");
            if (mediaRecorder != null) {
                try {
                    mediaRecorder.stop();
                    mediaRecorder.reset();
                    mediaRecorder.release();
                    
                    if (interrupted) {
                        // Delete incomplete recording from SD card
                        (new File(audioPath)).delete();
                    } else {
                        FileManager.getInstance().add(audioPath);
                    }
                    
                } catch (IllegalStateException e) {
                    StrandLog.e(ScreamService.TAG, "IllegalStateException in stopRecording");
                }
                mediaRecorder = null;
            }
        }
        
    }
    
}
