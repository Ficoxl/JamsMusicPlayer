package com.jams.music.player.AsyncTasks;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.widget.Toast;

import com.jams.music.player.R;
import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.EqualizerAudioFXActivity.EqualizerFragment;
import com.jams.music.player.Utils.Common;

public class AsyncApplyEQToAllSongsTask extends AsyncTask<String, Void, Void> {
	
    private Context mContext;
    private Common mApp;
    private EqualizerFragment mEqualizerFragment;
    
    public AsyncApplyEQToAllSongsTask(Context context, EqualizerFragment fragment) {
    	mContext = context;
    	mEqualizerFragment = fragment;
    	mApp = (Common) context.getApplicationContext();
    }
    
    protected void onPreExecute() {
		Toast.makeText(mContext, R.string.applying_equalizer_to_all_songs, Toast.LENGTH_SHORT).show();

    }
 
    @Override
    protected Void doInBackground(String... params) {
    	
		//Get a cursor with all the songs in the library.
		Cursor songsCursor = mApp.getDBAccessHelper().getAllSongs();
    	
		//Loop through the songs and add them to the EQ settings DB with the current EQ settings.
		if (songsCursor!=null && songsCursor.getCount() > 0) {
			
			for (int j=0; j < songsCursor.getCount(); j++) {
				songsCursor.moveToPosition(j);

				String songId = songsCursor.getString(songsCursor.getColumnIndex(DBAccessHelper.SONG_ID));
				mEqualizerFragment.setEQValuesForSong(songId);

			}

		}
    	
		if (songsCursor!=null)
			songsCursor.close();
		
    	return null;
	    
    }

    @Override
    protected void onPostExecute(Void arg0) {
		Toast.makeText(mContext, R.string.equalizer_applied_to_all_songs, Toast.LENGTH_SHORT).show();
		
	}

}