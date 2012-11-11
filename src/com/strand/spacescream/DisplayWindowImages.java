package com.strand.spacescream;

import java.io.File;

import android.os.Bundle;

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
        
        preview.capturePhoto(path, new Preview.SavedCallback() {
            
            @Override
            public void photoSaved(File image, File thumbnail) {
                // Request transfer of photo thumbnail; full image can be requested later
                FileManager.getInstance().add(thumbnail.getPath());
                DisplayWindowImages.super.screenshotTaken(); 
            }
            
        });

    }
    
}
