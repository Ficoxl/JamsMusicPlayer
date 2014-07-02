package com.jams.music.player.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jams.music.player.Utils.Common;

/**
 * BroadcastReceiver that skips to the specified track.
 * 
 * @author Saravan Pantham
 */
public class ChangeTrackBroadcastReceiver extends BroadcastReceiver {
	
	private Common mApp;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		mApp = (Common) context.getApplicationContext();
		
		//Retrieve the new song's index.
		int index = intent.getIntExtra("INDEX", 0);
		
		if (mApp.isServiceRunning())
			mApp.getService().skipToTrack(index);

	}

}
