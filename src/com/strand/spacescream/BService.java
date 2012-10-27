package com.strand.spacescream;

import com.strand.global.MessageCode;
import com.strand.global.StrandBindingService;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

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
	    try{
            Message reply = Message.obtain(null, MessageCode.N0_ACT, 0, 0);
            msg.replyTo.send(reply);
        } catch (RemoteException e) {
            ScreamService.log("RemoteExcpetion in replying to REQ_ACT");
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
		// TODO
	}
	
}