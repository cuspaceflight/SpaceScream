package com.strand.spacescream;

import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import com.strand.global.MessageCode;
import com.strand.global.StrandBindingService;
import com.strand.global.StrandLog;

public class BService extends StrandBindingService{
    
    @Override
    public IBinder onBind(Intent intent) {
    	
    	// Set the log file and working folder
    	init("SpaceScream", Boolean.parseBoolean(intent.getStringExtra(MessageCode.DEBUG_FLAG)));
    	
    	StrandLog.d(ScreamService.TAG, "Binding Service started");
    	
		Intent i = new Intent(this, ScreamService.class);
		i.putExtra(MessageCode.PARAM_LIST, intent.getStringExtra(MessageCode.PARAM_LIST));
		startService(i);

		return mMessenger.getBinder();
    }

    @Override
    public void onDestroy() {
        StrandLog.d(ScreamService.TAG, "Unbinding");
    	
    	// If Service isn't running, this will do nothing
		stopService(new Intent(this, ScreamService.class));
    }

	@Override
	protected void msgREQ_ACT(Message msg) {
	    
	    Message reply;
	    if (ScreamService.getInstance().screenshotRequested()) {
	        reply = Message.obtain(null, MessageCode.TAKE_SCR_PICT, 0, 0);
	        reply.replyTo = mMessenger;
	    } else if (!FileManager.getInstance().isEmpty()) {
	        
	        String path = FileManager.getInstance().getNext();
            Bundle bundle = new Bundle();
            bundle.putString(MessageCode.FILE_PATH, path);
            
            reply = Message.obtain(null, MessageCode.FILE_TRAN_REQ, 0, 0);
            reply.setData(bundle);
            reply.replyTo = mMessenger;
            
            StrandLog.d(ScreamService.TAG, "Sending file to MCA for transfer: " + path);

	    } else {
            reply = Message.obtain(null, MessageCode.N0_ACT, 0, 0);
	    }
	    
        try {
            msg.replyTo.send(reply);
        } catch (RemoteException e) {
            StrandLog.e(ScreamService.TAG, "RemoteExcpetion in replying to REQ_ACT with code " + reply.what);
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
	    StrandLog.d(ScreamService.TAG, "File transfer acknowledged: " + path);
	    
	    // The file no longer needs to be queued for transfer
	    FileManager.getInstance().remove(path);
	    
	}
	
	@Override
	protected void msgTAKE_SCR_PICT_RESP(Message msg) {
	    StrandLog.d(ScreamService.TAG, "Screenshot acknowledged");
	    ScreamService.getInstance().screenshotComplete();
	}
	
}