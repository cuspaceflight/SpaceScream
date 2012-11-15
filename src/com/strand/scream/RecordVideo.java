package com.strand.scream;

import java.io.File;
import java.io.IOException;

import com.strand.global.StrandLog;

import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Records a video and request transfer, with a max file size and quality set
 * via delivered Intent when Activity starts.
 * 
 * @author ejc74
 *
 */
public class RecordVideo extends ScreamActivity {

    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private Camera camera;
    private MediaRecorder recorder;
    private boolean recording = false;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.window);
        
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        holder = surfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.setKeepScreenOn(true);
        
        camera = Camera.open();
    }
    
    @Override
    public void onStart() {
        super.onStart();

        postDelayed(new Runnable() {

            @Override
            public void run() {
                Intent intent = getIntent();
                long size = intent.getLongExtra("size", 1048576);
                boolean highQuality = intent.getBooleanExtra("hq", false);
                StrandLog.d(ScreamService.TAG, "Video recording requested with size "
                        + size + " and high quality = " + highQuality);
                
                String path = FileManager.DIRECTORY + "/recorded/" + System.currentTimeMillis() + ".3gp";
                
                StrandLog.d(ScreamService.TAG, "Starting video recording");
                recordVideo(path, size, highQuality);
            }
            
        }, 5000);
        
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        if (recording) {
            StrandLog.d(ScreamService.TAG, "Activity pausing while recording in progress - attempting stop()");
            try {
                recorder.stop();
                recording = false;
                try {Thread.sleep(500);} catch (InterruptedException e) {}                
            } catch (IllegalStateException e) {
                StrandLog.d(ScreamService.TAG, "IllegalStateException in stop()");
            } catch (RuntimeException e) {
                StrandLog.d(ScreamService.TAG, "RuntimeException in stop()");
            }
            
            release();
        }
        
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        if (camera != null) {
            camera.release();
        }
    }
    
    private void recordVideo(String path, long size, boolean highQuality) {
        final File output = new File(path);
        output.getParentFile().mkdirs();
        
        if (camera != null) {
            camera.unlock();
            
            recorder = new MediaRecorder();
            
            recorder.setOnInfoListener(new OnInfoListener() {

                @Override
                public void onInfo(MediaRecorder mr, int what, int extra) {
                    if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED
                            || what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
                        
                        StrandLog.d(ScreamService.TAG, "Video recording has ended");
                        try {
                            recorder.stop();
                            recording = false;
                            try {Thread.sleep(500);} catch (InterruptedException e) {}
                            
                            // If recorder stops without problems, assume file is safe to transfer
                            StrandLog.d(ScreamService.TAG, "Requesting transfer of video");
                            FileManager.getInstance().add(output.getAbsolutePath());
                            StrandLog.d(ScreamService.TAG, "Finishing activity");
                            finish();
                            
                        } catch (IllegalStateException e) {
                            StrandLog.d(ScreamService.TAG, "IllegalStateException in stop()");
                        } catch (RuntimeException e) {
                            StrandLog.d(ScreamService.TAG, "RuntimeException in stop()");
                        }
                        
                        release();

                    }
                }
                
            });
            
            recorder.setPreviewDisplay(holder.getSurface());
            recorder.setCamera(camera);
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            if (highQuality) {
                recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
            } else {
                recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));
            }
            recorder.setVideoFrameRate(25);

            recorder.setOutputFile(path);
            recorder.setMaxDuration(60000); // 1 minute max

            try {
                recorder.setMaxFileSize(size);
            } catch (RuntimeException exception) {
                // Ignore failure of setMaxFileSize (good reasons!)
            }
            
            try {
                try {Thread.sleep(500);} catch (InterruptedException e) {}
                recorder.prepare();
                try {Thread.sleep(500);} catch (InterruptedException e) {}
                recorder.start();
                recording = true;
            } catch (IllegalStateException e) {
                StrandLog.e(ScreamService.TAG, "IllegalStateException in recording video");
                release();
            } catch (IOException e) {
                StrandLog.e(ScreamService.TAG, "IOException in recording video");
                release();
            }
        }
    }
    
    private void release() {
        recorder.reset();
        recorder.release();
        try {
            camera.reconnect();
        } catch (IOException e) {
            StrandLog.e(ScreamService.TAG, "IOException in reconnect()");
        }
    }
    
}
