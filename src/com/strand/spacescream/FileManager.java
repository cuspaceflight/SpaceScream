package com.strand.spacescream;

import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * 
 * A queue of file paths to be transferred, with state saved to SD card.
 * TODO: Save/restore state.
 *
 */
public class FileManager {

    private static FileManager instance;
    
    private LinkedList<String> files;
    
    protected FileManager() {
        files = new LinkedList<String>();
        // Load files already in queue from SD log
    }
    
    public boolean isEmpty() {
        return files.isEmpty();
    }
    
    public String getNext() {
        try {
            String path = files.pop();
            files.add(path);
            return path;
        } catch (NoSuchElementException e) {
            return null;
        }
    }
    
    public void add(String path) {
        if (path != null) {
            ScreamService.log("Adding file to queue: " + path);
            files.add(path);   
        }
    }
    
    public void remove(String path) {
        ScreamService.log("Removing file from queue: " + path);
        files.remove(path);
    }
    
    public static FileManager getInstance() {
        if (instance == null) {
            instance = new FileManager();
        }
        return instance;
    }
    
}
