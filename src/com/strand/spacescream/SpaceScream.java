package com.strand.spacescream;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * 
 * This activity is just used for testing at the moment, and can be started
 * from the home screen to start the app independently of MCA.
 *
 */

public class SpaceScream extends Activity {
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        startService(new Intent(this, ScreamService.class));
        
    }  
        
}
