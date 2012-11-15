package com.strand.scream;

import java.io.IOException;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;

import com.strand.global.StrandLog;

/**
 * Plays opening soundtrack(!), and shows logos on SD card. Screenshots are
 * requested if not already taken. We also generate some demo radioteletype
 * audio for fun, and to put the phone through it's paces!
 * 
 * SD: Looks for soundtrack.mp3, and images in intro/ directory.
 * 
 * @author ejc74
 *
 */
public class Intro extends DisplayImages {
    
    private Runnable imageRunnable;
    private MediaPlayer player;
    private RadioTeletype rtty;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DIR = "Intro";
        delay = 20000;
        
        images = FileManager.getFiles("intro");
        
        imageRunnable = runnable;
        runnable = new Runnable() {

            @Override
            public void run() {
                
                player = new MediaPlayer();
                try {
                    player.setOnCompletionListener(new OnCompletionListener() {

                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            showLogos();
                        }
                        
                    });
                    player.setDataSource(FileManager.DIRECTORY + "/soundtrack.mp3");
                    player.prepare();
                    StrandLog.d(ScreamService.TAG, "Starting playback of intro MP3");
                    player.start();
                } catch (IllegalArgumentException e) {
                    StrandLog.e(ScreamService.TAG, "IllegalArgumentException in setDataSource");
                    showLogos();
                } catch (IllegalStateException e) {
                    StrandLog.e(ScreamService.TAG, "IllegalStateException in MediaPlayer");
                    showLogos();
                } catch (IOException e) {
                    StrandLog.e(ScreamService.TAG, "IllegalStateException in MediaPlayer");
                    showLogos();
                }
                
            }
            
        };
    }
    
    private void showLogos() {
        StrandLog.d(ScreamService.TAG, "Scheduling logo display");
        postDelayed(runnable = imageRunnable, 5000);
        // RTTY commands will block, so run in separate thread
        (new RadioTeletypeTask()).execute();
    }
    
    @Override
    protected void setContentView() {
        setContentView(R.layout.window_image_centre);
    }
    
    @Override
    public void onStop() {
        super.onStop();
        if (player != null && player.isPlaying()) {
            player.stop();
            player.release();
            player = null;
        }
        if (rtty != null) {
            rtty.stop();
        }
    }
    
    public class RadioTeletypeTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... args) {
            // Generating 44100Hz RTTY is quite intensive...
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            
            rtty = new RadioTeletype();
            rtty.play(Intro.this);
            return null;
        }
    }
    
    
}
