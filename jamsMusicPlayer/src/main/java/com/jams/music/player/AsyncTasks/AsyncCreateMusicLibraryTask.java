package com.jams.music.player.AsyncTasks;

import java.util.HashSet;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.jams.music.player.R;
import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.Utils.Common;

/**************************************************************************************
 * This AsyncTask creates the specified music library.
 * 
 * @author Saravan Pantham
 **************************************************************************************/
public class AsyncCreateMusicLibraryTask extends AsyncTask<String, Void, Void> {
	
	private Activity mActivity;
    private Context mContext;
    private Common mApp;
    private HashSet<String> mSongDBIds = new HashSet<String>();
    private String mLibraryName;
    private String mLibraryColorCode;
    
    public AsyncCreateMusicLibraryTask(Activity activity,
    								   Context context, 
    								   HashSet<String> songDBIds, 
    								   String libraryName, 
    								   String libraryColorCode) {
    	mActivity = activity;
    	mContext = context;
    	mApp = (Common) context.getApplicationContext();
    	mSongDBIds = songDBIds;
    	mLibraryName = libraryName;
    	mLibraryColorCode = libraryColorCode;
    	
    }
 
    @Override
    protected Void doInBackground(String... params) {

    	//Delete the library if it currently exists.
    	mApp.getDBAccessHelper().deleteLibrary(mLibraryName, mLibraryColorCode);
    	
    	try {
    		mApp.getDBAccessHelper().getWritableDatabase().beginTransaction();
    		
    		//HashSets aren't meant to be browsable, so convert it into an array.
    		String[] songIdsArray = new String[mSongDBIds.size()];
    		mSongDBIds.toArray(songIdsArray);

    		//Loop through the array and add the songIDs to the library.
    		for (int i=0; i < songIdsArray.length; i++) {
    			ContentValues values = new ContentValues();
    			values.put(DBAccessHelper.LIBRARY_NAME, mLibraryName);
    			values.put(DBAccessHelper.SONG_ID, songIdsArray[i]);
    			values.put(DBAccessHelper.LIBRARY_TAG, mLibraryColorCode);
    			
        		mApp.getDBAccessHelper().getWritableDatabase().insert(DBAccessHelper.LIBRARIES_TABLE, null, values);
    		}
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    		return null;
    	} finally {
    		mApp.getDBAccessHelper().getWritableDatabase().setTransactionSuccessful();
    		mApp.getDBAccessHelper().getWritableDatabase().endTransaction();
    	}
    	
    	return null;
	    
    }

    @Override
    protected void onPostExecute(Void arg0) {
    	mActivity.finish();
    	Toast.makeText(mContext, R.string.done_creating_library, Toast.LENGTH_LONG).show();
       
    }

}
