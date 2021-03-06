package com.strand.scream;

import java.io.IOException;

import com.strand.global.StrandLog;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.widget.ImageView;

/**
 * Plays MP3 of composition by primary school children at Our Dynamic Earth
 * science museum, then a generative piece of music, and a choral composition
 * by composer Ed Rex ('On That Pale Blue Dot').
 * 
 * SD: Looks for /intro/01-cusf.png and /dynamicearth/earth.mp3
 * 
 * @author ejc74
 *
 */
public class Outro extends ScreamActivity {

    private MediaPlayer player;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.window_image_centre);
        
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        Bitmap bitmap = BitmapFactory.decodeFile(FileManager.DIRECTORY + "/intro/01-cusf.png");
        imageView.setImageBitmap(bitmap);
        
        player = new MediaPlayer();
        try {
            player.setOnCompletionListener(new OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    StrandLog.d(ScreamService.TAG, "MP3 completed; finishing Activity");
                    finish();
                }
                
            });
            player.setDataSource(FileManager.DIRECTORY + "/dynamicearth/earth.mp3");
            player.prepare();
            player.start();
        } catch (IllegalArgumentException e) {
            StrandLog.e(ScreamService.TAG, "IllegalArgumentException in setDataSource");
            finish();
        } catch (IllegalStateException e) {
            StrandLog.e(ScreamService.TAG, "IllegalStateException in MediaPlayer");
            finish();
        } catch (IOException e) {
            StrandLog.e(ScreamService.TAG, "IllegalStateException in MediaPlayer");
            finish();
        }
        
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        if (player != null) {
            if (player.isPlaying()) {
                player.stop();
            }
            player.release();
        }
    }
    
}
