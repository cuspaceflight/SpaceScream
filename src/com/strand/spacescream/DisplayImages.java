package com.strand.spacescream;

import java.io.File;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

public class DisplayImages extends ScreamActivity {

    private ImageView imageView;
    
    private ArrayList<String> images;
    private int index = 0;
    
    private Runnable runnable;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.images);
        
        imageView = (ImageView) findViewById(R.id.imageView);
        images = getImages();
        
        runnable = new Runnable() {

            @Override
            public void run() {
                loadImage();
            }
            
        };
        
    }
    
    @Override
    public void onResume() {
        super.onResume();
        postDelayed(runnable, 5000);
    }
    
    private void loadImage() {
        if (!images.isEmpty() && images.size() > index) {
            String path = images.get(index++);
            if (path != null) {
                log("Loading " + path);
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                imageView.setImageBitmap(bitmap);
                captureScreen();
                postDelayed(runnable, 20000);
            } else {
                loadImage();
            }
        } else {
            log("No more images left in queue; ending activity");            
            finish();
        }
    }
    
    private void captureScreen() {
        // Not yet determined how to request a photo of phone screen.
    }
    
    private ArrayList<String> getImages() {
        log("Finding images on SD card");
        
        File directory = new File(FileManager.DIRECTORY, "images");
        File[] files = directory.listFiles();
        
        ArrayList<String> images = new ArrayList<String>();
        for (File file : files) {
            images.add(file.getAbsolutePath());
        }
        
        return images;
    }
    
}
