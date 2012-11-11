package com.strand.spacescream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.strand.global.StrandLog;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

public class DisplayImages extends ScreamActivity implements ScreamActivity.ScreenshotListener {
    
    protected ImageView imageView;
    
    protected ArrayList<String> images;
    protected int index = 0;
    
    protected String DIR = "DisplayImages";
    private File record;
    
    private Runnable runnable;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView();
        
        imageView = (ImageView) findViewById(R.id.imageView);
        images = getImages();
        
        runnable = new Runnable() {

            @Override
            public void run() {
                loadImage();
            }
            
        };
        
        registerScreenshotListener(this);
        
    }
    
    protected void setContentView() {
        setContentView(R.layout.images);
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
                StrandLog.d(ScreamService.TAG, "Loading " + path);
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                imageView.setImageBitmap(bitmap);
                File image = new File(path);
                record = new File(FileManager.DIRECTORY + "/screenshots/" + DIR + "/" + image.getName() + ".txt");
                if (!record.exists()) {
                    ScreamService.getInstance().requestScreenshot();
                    StrandLog.d(ScreamService.TAG, "Requesting screenshot for " + image.getName());
                } else {
                    StrandLog.d(ScreamService.TAG, "Screenshot previously requested for " + image.getName());
                    postDelayed(runnable, 60000);
                }
            } else {
                loadImage();
            }
        } else {
            StrandLog.d(ScreamService.TAG, "No more images left in queue; ending activity");            
            finish();
        }
    }
    
    private ArrayList<String> getImages() {
        StrandLog.d(ScreamService.TAG, "Finding images on SD card");
        
        File directory = new File(FileManager.DIRECTORY, "images");
        File[] files = directory.listFiles();
        
        ArrayList<String> images = new ArrayList<String>();
        for (File file : files) {
            images.add(file.getAbsolutePath());
        }
        
        return images;
    }
    
    @Override
    public void screenshotTaken() {
        try {
            // Create a record file to indicate that a screenshot has successfully been taken
            record.getParentFile().mkdirs();
            record.createNewFile();
        } catch (IOException e) {
            StrandLog.e(ScreamService.TAG, "IOException in createNewFile()");
        }
        postDelayed(runnable, 10000);
    }
    
}
