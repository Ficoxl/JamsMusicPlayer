package com.jams.music.player.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jams.music.player.Utils.Common;

public class StopServiceBroadcastReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		//Stop the service.
		Common app = (Common) context.getApplicationContext();
		app.getService().stopSelf();
		
	}

}
