package com.jams.music.player.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.jams.music.player.R;
import com.jams.music.player.AsyncTasks.AsyncBuildLibraryTask;
import com.jams.music.player.AsyncTasks.AsyncCacheLocalArtworkTask;

public class BuildMusicLibraryService extends Service {
	
	private Context mContext;
	public static NotificationCompat.Builder mBuilder;
	public static Notification mNotification;
	public static NotificationManager mNotifyManager;
	public static int mNotificationId = 92713;
	
	@Override
	public void onCreate() {
		mContext = this.getApplicationContext();
	}
	
	@Override
	public int onStartCommand(Intent intent, int startId, int flags) {
		
		//Create a persistent notification that keeps this service running and displays the scan progress.
		mBuilder = new NotificationCompat.Builder(mContext);
		mBuilder.setSmallIcon(R.drawable.notif_icon);
		mBuilder.setContentTitle(getResources().getString(R.string.building_music_library));
		mBuilder.setTicker(getResources().getString(R.string.building_music_library));
		mBuilder.setContentText(getResources().getString(R.string.preparing_to_build_library));
		mBuilder.setProgress(0, 0, true);
		
		mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotification = mBuilder.build();
		mNotification.flags |= Notification.FLAG_INSISTENT | Notification.FLAG_NO_CLEAR;
		
		startForeground(mNotificationId, mNotification);	
		
		if (intent!=null) {
			
			if (intent.getExtras()!=null) {
				
				if (intent.getExtras().getString("SCAN_TYPE")!=null) {
					if (intent.getExtras().getString("SCAN_TYPE").equals("FULL_SCAN")) {
						//Go crazy with a full-on scan.
				        AsyncBuildLibraryTask task = new AsyncBuildLibraryTask(mContext);
				        task.execute();
				        
					} else if (intent.getExtras().getString("SCAN_TYPE").equals("RESCAN_ALBUM_ART")) {
						//We're only gonna be scanning for album art.
				        AsyncCacheLocalArtworkTask task = new AsyncCacheLocalArtworkTask(mContext);
				        task.execute();
				        
					}
					
				} else {
					//Go crazy with a full-on scan.
			        AsyncBuildLibraryTask task = new AsyncBuildLibraryTask(mContext);
			        task.execute();
				}
				
			} else {
				//Go crazy with a full-on scan.
		        AsyncBuildLibraryTask task = new AsyncBuildLibraryTask(mContext);
		        task.execute();
			}
				
		} else {
			//Go crazy with a full-on scan.
	        AsyncBuildLibraryTask task = new AsyncBuildLibraryTask(mContext);
	        task.execute(); 
		}

		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
