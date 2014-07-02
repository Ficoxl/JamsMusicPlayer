/*package com.jams.music.player.AsyncTasks;

import java.io.File;

import org.json.JSONException;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.jams.music.player.R;
import com.jams.music.player.GMusicHelpers.GMusicClientCalls;
import com.jams.music.player.Utils.Common;

public class AsyncDeletePlaylistTask extends AsyncTask<String, Void, Boolean> {
	
    private Context mContext;
    private Common mApp;
    
    private String mPlaylistId;
    private String mPlaylistFilePath;
    
    public AsyncDeletePlaylistTask(Context context, String playlistId, String playlistFilePath) {
    	
    	mContext = context;
    	mApp = (Common) mApp;
    	mPlaylistId = playlistId;
    	mPlaylistFilePath = playlistFilePath;
    	
    }
 
    @Override
    protected Boolean doInBackground(String... params) {
    	
    	String result = null;
    	if (mApp.isGooglePlayMusicEnabled()) {
        	try {
    			result = GMusicClientCalls.deletePlaylist(mContext, mPlaylistId);
    		} catch (IllegalArgumentException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (JSONException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        	
    	}
    	
    	if (result!=null || mApp.isGooglePlayMusicEnabled()==false) {
			mApp.getDBAccessHelper().deletePlaylistById(mPlaylistId);
			
			if (mPlaylistFilePath!=null) {
				File file = new File(mPlaylistFilePath);
				if (file.exists()) {
					file.delete();
				}
				
			}
			
    	}
    	
    	return true;
    }

    @Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		Toast.makeText(mContext, R.string.playlist_deleted, Toast.LENGTH_SHORT).show();

		//Update the playlists UI.
		mApp.broadcastUpdateUICommand();
		
	}

}*/