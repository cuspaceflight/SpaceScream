package com.strand.spacescream;

import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import com.strand.global.MessageCode;
import com.strand.global.StrandBindingService;

public class BService extends StrandBindingService{
    
    @Override
    public IBinder onBind(Intent intent) {
    	
    	// Set the log file and working folder
    	init("SpaceScream", Boolean.parseBoolean(intent.getStringExtra(MessageCode.DEBUG_FLAG)));
    	
    	ScreamService.log("Binding Service started");
    	
		Intent i = new Intent(this, ScreamService.class);
		i.putExtra(MessageCode.PARAM_LIST, intent.getStringExtra(MessageCode.PARAM_LIST));
		startService(i);

		return mMessenger.getBinder();
    }

    @Override
    public void onDestroy() {
    	ScreamService.log("Unbinding");
    	
    	if (ScreamService.getInstance() == null) {
    		ScreamService.log("The service is not running");
    	} else {
    	    ScreamService.log("Stopping the service");
    		stopService(new Intent(this, ScreamService.class));
    	}
    }

	@Override
	protected void msgREQ_ACT(Message msg) {
	    ScreamService.log("REQ_ACT message received");
	    
	    if (FileManager.getInstance().isEmpty()) {
            Message reply = Message.obtain(null, MessageCode.N0_ACT, 0, 0);
            
	        try {
                msg.replyTo.send(reply);
            } catch (RemoteException e) {
                ScreamService.log("RemoteExcpetion in replying to REQ_ACT with NO_ACT");
            }
	    } else {
	        
	        String path = FileManager.getInstance().getNext();
	        Bundle bundle = new Bundle();
            bundle.putString(MessageCode.FILE_PATH, path);
            
            Message reply = Message.obtain(null, MessageCode.FILE_TRAN_REQ, 0, 0);
            reply.setData(bundle);
            reply.replyTo = mMessenger;
            
            try {
                msg.replyTo.send(reply);
                ScreamService.log("Sent file to MCA for transfer: " + path);
            } catch (RemoteException e) {
                ScreamService.log("RemoteExcpetion in replying to REQ_ACT with FILE_TRAN_REQ");
            }
	    }
	}

	@Override
	protected void msgTM_DATA(Message msg) {
		// Not required
	}

	@Override
	protected void msgDEBUG(Message msg) {
		// TODO
	}

	@Override
	protected void msgSTATUS(Message msg) {
		// TODO
	}
	
	@Override
	protected void msgEXEC_COMM(Message msg) {
		// TODO
	}
	
	@Override
	protected void msgFILE_RESP(Message msg) {
	    
	    String path = msg.getData().getString(MessageCode.FILE_PATH);
	    ScreamService.log("File transfer acknowledged: " + path);
	    
	    // The file no longer needs to be queued for transfer
	    FileManager.getInstance().remove(path);
	}
	
}