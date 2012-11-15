package com.strand.scream;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import android.os.Environment;

import com.strand.global.MessageCode;
import com.strand.global.StrandLog;

/**
 * A queue of file paths to be transferred, with state saved to SD card, plus
 * other useful file methods.
 * 
 * @author ejc74
 *
 */
public class FileManager {

    public final static String DIRECTORY = Environment.getExternalStorageDirectory().toString() + MessageCode.WORKING_FOLDER + "scream";
    private static FileManager instance;
    
    private LinkedList<String> files;
    
    protected FileManager() {
        files = new LinkedList<String>();
        
        // Load files already in queue from SD log
        try {
            String path;
            FileInputStream fis = new FileInputStream(DIRECTORY + "/file_queue.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            while ((path = br.readLine()) != null && !"".equals(path)) {
                add(path);
            }
        } catch (FileNotFoundException e) {
            StrandLog.d(ScreamService.TAG, "file_queue.txt not found");
        } catch (IOException e) {
            StrandLog.e(ScreamService.TAG, "IOException in readLine() of file_queue.txt");
        }
    }
    
    private void save() {
        FileWriter fWriter;
        BufferedWriter writer;
        try {
            fWriter = new FileWriter(DIRECTORY + "/file_queue.txt");
            writer = new BufferedWriter(fWriter);
            for (String path : files) {
                writer.append(path);
                writer.newLine();
            }
            writer.close();
        } catch (Exception e) {
            StrandLog.e(ScreamService.TAG, "IOException in writing file_queue.txt");
        }
    }
    
    public boolean isEmpty() {
        return files.isEmpty();
    }
    
    public String getNext() {
        try {
            String path = files.pop();
            files.add(path);
            save();
            return path;
        } catch (NoSuchElementException e) {
            return null;
        }
    }
    
    public void add(String path) {
        if (path != null) {
            StrandLog.d(ScreamService.TAG, "Adding file to queue: " + path);
            files.add(path); 
            save();
        }
    }
    
    public void remove(String path) {
        StrandLog.d(ScreamService.TAG, "Removing file from queue: " + path);
        files.remove(path);
        save();
    }
    
    public static FileManager getInstance() {
        if (instance == null) {
            instance = new FileManager();
        }
        return instance;
    }
    
    public static ArrayList<String> getFiles(String subfolder) {
        StrandLog.d(ScreamService.TAG, "Finding files in " + subfolder + " directory on SD card");
        
        File directory = new File(FileManager.DIRECTORY, subfolder);
        File[] files = directory.listFiles();
        
        ArrayList<String> fileList = new ArrayList<String>();
        
        if (files != null) {
            // Sort alphabetically by filename
            Arrays.sort(files);
            for (File file : files) {
                fileList.add(file.getAbsolutePath());
            }
        }
        
        return fileList;
    }
    
}
