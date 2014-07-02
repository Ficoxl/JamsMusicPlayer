package com.jams.music.player.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jams.music.player.Utils.Common;

/**
 * BroadcastReceiver that handles and processes all headset 
 * unplug/plug actions and events.
 * 
 * @author Saravan Pantham
 */
public class HeadsetPlugBroadcastReceiver extends BroadcastReceiver {

	private Common mApp;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		mApp = (Common) context.getApplicationContext();

 	    if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
	        int state = intent.getIntExtra("state", -1);
	        switch (state) {
	        case 0:
	            //Headset unplug event.
	        	mApp.getService().pausePlayback();
	            break;
	        case 1:
	            //Headset plug-in event.
	        	mApp.getService().startPlayback();
	            break;
	        default:
	            //No idea what just happened.
	        }

		}
	    
	}
	  
}
