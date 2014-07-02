package com.jams.music.player.PlaybackKickstarter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.widget.Toast;

import com.jams.music.player.R;
import com.jams.music.player.NowPlayingActivity.NowPlayingActivity;
import com.jams.music.player.NowPlayingActivity.NowPlayingActivity.NowPlayingActivityListener;
import com.jams.music.player.Services.AudioPlaybackService;
import com.jams.music.player.Services.AudioPlaybackService.PrepareServiceListener;
import com.jams.music.player.Utils.Common;

/**
 * Initiates the playback sequence and 
 * starts AudioPlaybackService.
 * 
 * @author Saravan Pantham
 */
public class PlaybackKickstarter implements NowPlayingActivityListener, PrepareServiceListener {

	private Context mContext;
	private Common mApp;
	
	private String mQuerySelection;
	private int mFragmentId;
	private int mCurrentSongIndex;
	
	private BuildCursorListener mBuildCursorListener;
	
	/**
	 * Public interface that provides access to 
	 * major events during the cursor building 
	 * process.
	 * 
	 * @author Saravan Pantham
	 */
	public interface BuildCursorListener {
		
		/**
		 * Called when the service cursor has been prepared successfully.
		 */
		public void onServiceCursorReady(Cursor cursor, int currentSongIndex);
		
		/**
		 * Called when the service cursor failed to be built. 
		 * Also returns the failure reason via the exception 
		 * parameter.
		 */
		public void onServiceCursorFailed(Exception exception);
	
	}
	
	/**
	 * Helper method that calls all the required method(s) 
	 * that initialize music playback. This method should 
	 * always be called when the cursor for the service 
	 * needs to be changed.
	 */
	public void initPlayback(Context context, 
						     String querySelection, 
							 int fragmentId, 
							 int currentSongIndex, 
							 boolean showNowPlayingActivity) {
		
		mContext = context;
		mApp = (Common) context.getApplicationContext();
		mQuerySelection = querySelection;
		mFragmentId = fragmentId;
		mCurrentSongIndex = currentSongIndex;
		
		if (showNowPlayingActivity) {
			//Launch NowPlayingActivity.
			Intent intent = new Intent(mContext, NowPlayingActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(NowPlayingActivity.START_SERVICE, true);
			mContext.startActivity(intent);
			
		} else {
			//Start the playback service if it isn't running.
			if (!mApp.isServiceRunning()) {
				startService();
			} else {
				//Call the callback method that will start building the new cursor.
				mApp.getService()
					.getPrepareServiceListener()
					.onServiceRunning(mApp.getService());
			}
			
		}
		
	}
	
	/**
	 * Starts AudioPlaybackService. Once the service is running, we get a
	 * callback to onServiceRunning() (see below). That's where the method to 
	 * build the cursor is called.
	 */
	private void startService() {
		Intent intent = new Intent(mContext, AudioPlaybackService.class);
		mContext.startService(intent);
	}
	
	public BuildCursorListener getBuildCursorListener() {
		return mBuildCursorListener;
	}
	
	public void setBuildCursorListener(BuildCursorListener listener) {
		mBuildCursorListener = listener;
	}
	
	/**
	 * Builds the cursor that will be used for playback. Once the cursor 
	 * is built, AudioPlaybackService receives a callback via
	 * onServiceCursorReady() (see below). The service then takes over 
	 * the rest of the process.
	 */
	class AsyncBuildCursorTask extends AsyncTask<Boolean, Boolean, Cursor> {

		@Override
		protected Cursor doInBackground(Boolean... params) {
			
			try {
				return mApp.getDBAccessHelper().getFragmentCursor(mContext, mQuerySelection, mFragmentId);
			} catch (Exception exception) {
				exception.printStackTrace();
				getBuildCursorListener().onServiceCursorFailed(exception);
				return null;
			}
			
		}
		
		@Override
		public void onPostExecute(Cursor cursor) {
			super.onPostExecute(cursor);
			if (cursor!=null)
				getBuildCursorListener().onServiceCursorReady(cursor, mCurrentSongIndex);
			
		}
		
	}

	@Override
	public void onServiceRunning(AudioPlaybackService service) {
		//Build the cursor and pass it on to the service.
		mApp = (Common) mContext.getApplicationContext();
		mApp.setIsServiceRunning(true);
		mApp.setService(service);
		mApp.getService().setPrepareServiceListener(this);
		mApp.getService().setCurrentSongIndex(mCurrentSongIndex);
		new AsyncBuildCursorTask().execute();
		
	}

	@Override
	public void onServiceFailed(Exception exception) {
		//Can't move forward from this point.
		exception.printStackTrace();
		Toast.makeText(mContext, R.string.unable_to_start_playback, Toast.LENGTH_SHORT).show();
		
	}

	@Override
	public void onNowPlayingActivityReady() {
		//Start the playback service if it isn't running.
		if (!mApp.isServiceRunning()) {
			startService();
		} else {
			//Call the callback method that will start building the new cursor.
			mApp.getService()
				.getPrepareServiceListener()
				.onServiceRunning(mApp.getService());
		}
		
	}
	
}
