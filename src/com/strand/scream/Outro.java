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
 * Simply plays MP3 created by primary school children at Our Dynamic Earth
 * science museum.
 * 
 * SD: Looks for /intro/01-cusf.png and /dynamicearth/earth.mp3
 * 
 * @author ejc74
 *
 */
public class Outro extends ScreamActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.window_image_centre);
        
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        Bitmap bitmap = BitmapFactory.decodeFile(FileManager.DIRECTORY + "/intro/01-cusf.png");
        imageView.setImageBitmap(bitmap);
        
        MediaPlayer player = new MediaPlayer();
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
    
}
