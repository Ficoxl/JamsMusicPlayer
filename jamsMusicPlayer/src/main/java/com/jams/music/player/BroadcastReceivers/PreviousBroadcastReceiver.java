package com.jams.music.player.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jams.music.player.Utils.Common;

public class PreviousBroadcastReceiver extends BroadcastReceiver {

	private Common mApp;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		mApp = (Common) context.getApplicationContext();
		
		if (mApp.isServiceRunning())
			mApp.getService().skipToPreviousTrack();
		
	}
	  
}
