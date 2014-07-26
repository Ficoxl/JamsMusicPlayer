package com.jams.music.player.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.jams.music.player.R;
import com.jams.music.player.AsyncTasks.AsyncBuildLibraryTask;
import com.jams.music.player.WelcomeActivity.WelcomeActivity;

public class BuildMusicLibraryService extends Service implements AsyncBuildLibraryTask.OnBuildLibraryProgressUpdate {
	
	private Context mContext;
	private NotificationCompat.Builder mBuilder;
	private Notification mNotification;
	private NotificationManager mNotifyManager;
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
		mBuilder.setContentText("");
		mBuilder.setProgress(0, 0, true);
		
		mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotification = mBuilder.build();
		mNotification.flags |= Notification.FLAG_INSISTENT | Notification.FLAG_NO_CLEAR;
		
		startForeground(mNotificationId, mNotification);	

        //Go crazy with a full-on scan.
        AsyncBuildLibraryTask task = new AsyncBuildLibraryTask(mContext, this);
        task.setOnBuildLibraryProgressUpdate(WelcomeActivity.mBuildingLibraryProgressFragment);
        task.setOnBuildLibraryProgressUpdate(this);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public void onStartBuildingLibrary() {

    }

    @Override
    public void onProgressUpdate(AsyncBuildLibraryTask task, String mCurrentTask, int overallProgress,
                                 int maxProgress, boolean mediaStoreTransferDone) {
        mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setSmallIcon(R.drawable.notif_icon);
        mBuilder.setContentTitle(mCurrentTask);
        mBuilder.setTicker(mCurrentTask);
        mBuilder.setContentText("");
        mBuilder.setProgress(maxProgress, overallProgress, false);

        mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotification = mBuilder.build();
        mNotification.flags |= Notification.FLAG_INSISTENT | Notification.FLAG_NO_CLEAR;
        mNotifyManager.notify(mNotificationId, mNotification);

    }

    @Override
    public void onFinishBuildingLibrary(AsyncBuildLibraryTask task) {
        mNotifyManager.cancel(mNotificationId);
        stopSelf();

        Toast.makeText(mContext, R.string.finished_scanning_album_art, Toast.LENGTH_LONG).show();

    }

}
