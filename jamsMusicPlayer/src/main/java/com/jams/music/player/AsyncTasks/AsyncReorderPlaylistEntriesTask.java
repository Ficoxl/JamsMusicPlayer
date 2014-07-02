package com.jams.music.player.AsyncTasks;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

public class AsyncReorderPlaylistEntriesTask extends AsyncTask<String, Integer, Boolean> {
    private Context mContext;
    private SharedPreferences sharedPreferences;
    
    private String mPlaylistId;
    private ArrayList<String> mSongId;
    private ArrayList<String> mSongEntryId;
    private String mAfterEntryId;
    private String mBeforeEntryId;
    private String mPlaylistName;
    private ArrayList<String> mEntryIds;
    private ArrayList<String> mSongIds;
    
    public AsyncReorderPlaylistEntriesTask(Context context, 
    								   	   String playlistId,
    								   	   String playlistName,
    								   	   ArrayList<String> songId,
    								   	   ArrayList<String> songEntryId,
    								   	   String afterEntryId,
    								   	   String beforeEntryId,
    								   	   ArrayList<String> entryIds,
    								   	   ArrayList<String> songIds) {
    	
    	mContext = context;
    	sharedPreferences = mContext.getSharedPreferences("com.jams.music.player", Context.MODE_PRIVATE);
    	
    	mPlaylistId = playlistId;
    	mSongId = songId;
    	mSongEntryId = songEntryId;
    	mAfterEntryId = afterEntryId;
    	mBeforeEntryId = afterEntryId;
    	mPlaylistName = playlistName;
    	mEntryIds = entryIds;
    	mSongIds = songIds;
    	
    }
 
    @Override
    protected Boolean doInBackground(String... params) {
    	
/*    	if (sharedPreferences.getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
        	try {
    			GMusicClientCalls.reorderPlaylistEntryWebClient(mContext, 
    															mPlaylistId, 
    															mSongId, 
    															mSongEntryId, 
    															mAfterEntryId, 
    															mBeforeEntryId);
    		} catch (JSONException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        	
    	}
    	
		//Update the playlists database.
		DBAccessHelper dbHelper = new DBAccessHelper(mContext);
		dbHelper.reorderSongInPlaylist(mContext, mPlaylistName, mPlaylistId, mEntryIds, mSongIds);
		dbHelper.close();
		dbHelper = null;*/
    	
    	return true;
    }
    
    @Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		
    }

}