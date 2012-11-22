package com.strand.scream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import com.strand.global.StrandLog;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.ViewPager.LayoutParams;
import android.widget.ImageView;

/**
 * Loads images from SD card, displays on screen one by one, and requests
 * screenshots (if not previously requested).
 * 
 * SD: Looks for images in images/ directory.
 * 
 * @author ejc74
 *
 */
public class DisplayImages extends ScreamActivity implements ScreamActivity.ScreenshotListener {
    
    protected ImageView imageView;
    
    protected ArrayList<String> images;
    protected int index = 0;
    
    protected long delay = 10000;
    
    private File record;
    
    protected Runnable runnable;
    
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
                
                // Scale up images smaller than screen size like this, so that
                // image can then be aligned right in case of full screen display.
                // Will result in pixelisation, but VGA camera is low quality...
                if (imageView.getLayoutParams().height == LayoutParams.MATCH_PARENT
                        && bitmap.getWidth() < 800 && bitmap.getHeight() < 480) {
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    int h, w;
                    double ratio = width * 1.0 / height;
                    if (ratio < 800.0/480) {
                        h = 480;
                        w = (int) Math.round(ratio * h);
                    } else {
                        w = 800;
                        h = (int) Math.round(w / ratio);
                    }
                    bitmap = Bitmap.createScaledBitmap(bitmap, w, h, false);
                }
                
                imageView.setImageBitmap(bitmap);
                
                record = getRecordFile(path);
                if (!record.exists()) {
                    ScreamService.getInstance().requestScreenshot();
                    StrandLog.d(ScreamService.TAG, "Requesting screenshot for " + path);
                } else {
                    StrandLog.d(ScreamService.TAG, "Screenshot previously requested for " + path);
                    postDelayed(runnable, delay);
                }
            } else {
                loadImage();
            }
        } else {
            StrandLog.d(ScreamService.TAG, "No more images left in queue; ending activity");            
            finish();
        }
    }
    
    protected ArrayList<String> getImages() {
        ArrayList<String> images = FileManager.getFiles("images");
        
        if (!ScreamService.repeat) {
            // Remove images which have already had screenshots taken
            // On repeat cycle, we'll show everything for fun!
            Iterator<String> i = images.iterator();
            while (i.hasNext()) {
               String path = i.next();
               if (getRecordFile(path).exists()) {
                   i.remove();
               }
            }
        }
        
        return images;
    }
    
    private File getRecordFile(String path) {
        File image = new File(path);
        return new File(FileManager.DIRECTORY + "/screenshots/" + getClass().getSimpleName() + "/" + image.getName() + ".txt");
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
        postDelayed(runnable, delay);
    }
    
}
