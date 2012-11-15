package com.strand.scream;

import java.io.File;

import android.os.Bundle;

/**
 * Similar to DisplayImages, but including a live camera preview, and takes
 * phone camera photos when each image is displayed (only transfer of photo
 * thumbnails requested - full image can be sent later by use of app parameter).
 * 
 * @author ejc74
 *
 */
public class DisplayWindowImages extends DisplayImages {
    
    private Preview preview;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        preview = (Preview) findViewById(R.id.surfaceView);
        
        DIR = "DisplayWindowImages";
    }
    
    @Override
    protected void setContentView() {
        setContentView(R.layout.window_image);
    }
    
    @Override
    public void screenshotTaken() {
        
        String path = FileManager.DIRECTORY + "/photos/" + System.currentTimeMillis() + ".jpg";
        
        preview.capturePhoto(path, new Preview.SavedPhotoCallback() {
            
            @Override
            public void photoSaved(File image, File thumbnail) {
                // Request transfer of photo thumbnail; full image can be requested later
                FileManager.getInstance().add(thumbnail.getPath());
                DisplayWindowImages.super.screenshotTaken(); 
            }
            
        });

    }
    
}
