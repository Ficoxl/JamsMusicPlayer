package com.jams.music.player.AsyncTasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

public class AsyncRemovePlaylistEntryTask extends AsyncTask<String, Integer, Boolean> {
    private Context mContext;
    private SharedPreferences sharedPreferences;
    private String mEntryId;
    
    public AsyncRemovePlaylistEntryTask(Context context, String entryId) {
    	mContext = context;
    	sharedPreferences = mContext.getSharedPreferences("com.jams.music.player", Context.MODE_PRIVATE);
    	mEntryId = entryId;
    	
    }
 
    @Override
    protected Boolean doInBackground(String... params) {

/*    	if (sharedPreferences.getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
    		try {
    			GMusicClientCalls.putDeletePlaylistEntryRequest(mEntryId);
    			GMusicClientCalls.modifyPlaylist(mContext);
    		} catch (IllegalArgumentException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (JSONException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		
    	}

		//Update the playlists database.
		DBAccessHelper dbHelper = new DBAccessHelper(mContext);
		dbHelper.deleteSongFromPlaylist(mEntryId);
		dbHelper.close();
		dbHelper = null;*/
    	
    	return true;
    }
    
    @Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		
    }

}