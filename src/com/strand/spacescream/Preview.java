package com.strand.spacescream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.strand.global.StrandLog;

/**
 * Extends SurfaceView to add Camera preview functionality by default, and also
 * a capturePhoto method for taking pictures.
 * 
 * @author ejc74
 *
 */
public class Preview extends SurfaceView implements SurfaceHolder.Callback {

    private Camera camera;
    private SurfaceHolder holder;
    private String filepath;
    private SavedPhotoCallback savedCallback;
    private Handler handler;
    
    public Preview(Context context) {
        this(context, null);
    }
    
    public Preview(Context context, AttributeSet attrs) {
        this(context, attrs, 0);        
    }
    
    public Preview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        handler = new Handler();
        
        holder = this.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.setKeepScreenOn(true);
        
        StrandLog.d(ScreamService.TAG, "Opening camera");
        camera = Camera.open();
    }
    
    public void capturePhoto(final String path) {
        capturePhoto(path, null);
    }
    
    public void capturePhoto(String path, SavedPhotoCallback callback) {
        
        filepath = path;
        savedCallback = callback;
        
        camera.takePicture(null, null, new Camera.PictureCallback() {
            
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {                
                new SavePhotoTask().execute(data);
                StrandLog.d(ScreamService.TAG, "Picture saving; restarting camera preview");
                camera.startPreview();
            }
            
        });
    }
    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (camera != null) {
            
            Camera.Parameters parameters = camera.getParameters();
            
            parameters.setPreviewSize(width, height);
            parameters.setPictureFormat(PixelFormat.JPEG);
            parameters.setJpegThumbnailQuality(50);

            camera.setParameters(parameters);
            
            StrandLog.d(ScreamService.TAG, "Starting camera preview");
            camera.startPreview();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (camera != null) {
            try {
                camera.setPreviewDisplay(holder);
            } catch (IOException e) {
                StrandLog.e(ScreamService.TAG, "IOException in setPreviewDisplay");
                camera.release();
                camera = null;
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null) {
            StrandLog.d(ScreamService.TAG, "Releasing camera");
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }
    
    public class SavePhotoTask extends AsyncTask<byte[], Void, Void> {

        @Override
        protected Void doInBackground(byte[]... jpeg) {

            final File photo = new File(filepath);
            photo.getParentFile().mkdirs();
            
            if (photo.exists()) {
                photo.delete();
            }
            
            try {
                FileOutputStream fos = new FileOutputStream(photo.getPath());
                fos.write(jpeg[0]);
                fos.close();
            } catch (IOException e) {
                StrandLog.e(ScreamService.TAG, "IOException in SavePhotoTask");
            }
            
            final File thumbnail = new File(photo.getParent() + "/thumbnails/", photo.getName());
            
            try {
                ExifInterface exif = new ExifInterface(photo.getPath());
                byte[] thumbData = exif.getThumbnail();
                
                thumbnail.getParentFile().mkdirs();
                
                Bitmap bitmap = BitmapFactory.decodeByteArray(thumbData, 0, thumbData.length);
                FileOutputStream fos = new FileOutputStream(thumbnail.getPath());
                bitmap.compress(CompressFormat.JPEG, 50, fos);
                fos.close();
                
            } catch (IOException e) {
                StrandLog.e(ScreamService.TAG, "IOException in ExifInterface constructor");
            }
            
            if (savedCallback != null) {
                // Run photo saved callback on main thread
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        savedCallback.photoSaved(photo, thumbnail);
                    }
                    
                });
            }
            
            return null;
        }
    }
    
    public static interface SavedPhotoCallback {
        public void photoSaved(File image, File thumbnail);
    }

}
