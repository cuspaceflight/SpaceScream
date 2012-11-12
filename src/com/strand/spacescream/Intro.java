package com.strand.spacescream;

import java.io.IOException;

import com.strand.global.StrandLog;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;

/**
 * Plays opening soundtrack(!), and shows logos on SD card. Screenshots are
 * requested if not already taken.
 * 
 * SD: Looks for soundtrack.mp3, and images in intro/ directory.
 * 
 * @author ejc74
 *
 */
public class Intro extends DisplayImages {
    
    private MediaPlayer player;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DIR = "Intro";
        
        images = FileManager.getFiles("intro");
        
        final Runnable mainRunnable = runnable;
        runnable = new Runnable() {

            @Override
            public void run() {
                
                player = new MediaPlayer();
                try {
                    player.setOnCompletionListener(new OnCompletionListener() {

                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            postDelayed(runnable = mainRunnable, 5000);
                        }
                        
                    });
                    player.setDataSource(FileManager.DIRECTORY + "/soundtrack.mp3");
                    player.prepare();
                    player.start();
                } catch (IllegalArgumentException e) {
                    StrandLog.e(ScreamService.TAG, "IllegalArgumentException in setDataSource");
                    postDelayed(runnable = mainRunnable, 5000);
                } catch (IllegalStateException e) {
                    StrandLog.e(ScreamService.TAG, "IllegalStateException in MediaPlayer");
                    postDelayed(runnable = mainRunnable, 5000);
                } catch (IOException e) {
                    StrandLog.e(ScreamService.TAG, "IllegalStateException in MediaPlayer");
                    postDelayed(runnable = mainRunnable, 5000);
                }
                
            }
            
        };
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
    }
    
}
