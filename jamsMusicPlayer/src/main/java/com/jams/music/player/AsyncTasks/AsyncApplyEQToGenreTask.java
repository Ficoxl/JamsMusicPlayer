package com.jams.music.player.AsyncTasks;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.widget.Toast;

import com.jams.music.player.R;
import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.Utils.Common;

public class AsyncApplyEQToGenreTask extends AsyncTask<String, Void, Void> {
	
    private Context mContext;
	private Common mApp;
    
	private String titleGenre = "";
    private int mFiftyHertzLevel; 
    private int mOneThirtyHertzLevel; 
    private int mThreeTwentyHertzLevel; 
    private int mEightHundredHertzLevel; 
    private int mTwoKilohertzLevel; 
    private int mFiveKilohertzLevel; 
    private int mTwelvePointFiveKilohertzLevel; 
    private int mVirtualizerLevel; 
    private int mBassBoostLevel; 
    private int mReverbSetting;
	
    public AsyncApplyEQToGenreTask(Context context, 
    							   String genreName,
    							   int fiftyHertzLevel, 
    							   int oneThirtyHertzLevel, 
    							   int threeTwentyHertzLevel, 
    							   int eightHundredHertzLevel, 
    							   int twoKilohertzLevel, 
    							   int fiveKilohertzLevel, 
    							   int twelvePointFiveKilohertzLevel, 
    							   int virtualizerLevel, 
    							   int bassBoostLevel, 
    							   int reverbSetting) {
    	
    	mContext = context.getApplicationContext();
    	mApp = (Common) mContext;
    	titleGenre = genreName;
    	
    	mFiftyHertzLevel = fiftyHertzLevel;
        mOneThirtyHertzLevel = oneThirtyHertzLevel;
        mThreeTwentyHertzLevel = threeTwentyHertzLevel;
        mEightHundredHertzLevel = eightHundredHertzLevel;
        mTwoKilohertzLevel = twoKilohertzLevel;
        mFiveKilohertzLevel = fiveKilohertzLevel;
        mTwelvePointFiveKilohertzLevel = twelvePointFiveKilohertzLevel;
        mVirtualizerLevel = virtualizerLevel;
        mBassBoostLevel = bassBoostLevel;
        mReverbSetting = reverbSetting;
    	
    }
    
    @Override
    protected void onPreExecute() {
    	super.onPreExecute();
    	Toast.makeText(mContext, R.string.applying_equalizer, Toast.LENGTH_SHORT).show();
    }
 
    @Override
    protected Void doInBackground(String... params) {
    	
    	int which = Integer.parseInt(params[0]);
    	
        //Get a cursor with the list of all genres.
        final Cursor cursor = mApp.getDBAccessHelper().getAllUniqueGenres("");
        cursor.moveToPosition(which);
        String selectedGenre = cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_GENRE));
        cursor.close();
        
        //Get a cursor with all the songs in the specified genre.
        final Cursor songsCursor = mApp.getDBAccessHelper().getAllSongsInGenre(selectedGenre);
		
		//Loop through the songs in the genre and add them to the EQ settings DB with the current EQ settings.
		if (songsCursor!=null && songsCursor.getCount() > 0) {

			for (int j=0; j < songsCursor.getCount(); j++) {
				songsCursor.moveToPosition(j);
				
				String songId = songsCursor.getString(songsCursor.getColumnIndex(DBAccessHelper.SONG_ID));
				saveSettingsToDB(songId);
				
			}
			
		}
    	
		songsCursor.close();
    	return null;
	    
    }
    
    /** 
     * Commit the settings to the database.
     */
    public void saveSettingsToDB(String songId) {
    
		//Check if a database entry already exists for this song.
		if (mApp.getDBAccessHelper().hasEqualizerSettings(songId)==false) {
			//Add a new DB entry.
			mApp.getDBAccessHelper().addSongEQValues(songId,
												     mFiftyHertzLevel, 
												     mOneThirtyHertzLevel, 
												     mThreeTwentyHertzLevel, 
												     mEightHundredHertzLevel, 
												     mTwoKilohertzLevel, 
												     mFiveKilohertzLevel,
												     mTwelvePointFiveKilohertzLevel,
												     mVirtualizerLevel, 
												     mBassBoostLevel, 
												     mReverbSetting);
		} else {
			//Update the existing entry.
			mApp.getDBAccessHelper().updateSongEQValues(songId,
												 	    mFiftyHertzLevel, 
												 	    mOneThirtyHertzLevel, 
												 	    mThreeTwentyHertzLevel, 
												 	    mEightHundredHertzLevel, 
												 	    mTwoKilohertzLevel, 
												 	    mFiveKilohertzLevel,
												 	    mTwelvePointFiveKilohertzLevel,
												 	    mVirtualizerLevel, 
												 	    mBassBoostLevel, 
												 	    mReverbSetting);
		}

    }

    @Override
    protected void onPostExecute(Void params) {
	   	Toast.makeText(mContext, 
	 				   mContext.getResources().getString(R.string.equalizer_applied_to_songs_in) + " " + titleGenre + ".", 
	 				   Toast.LENGTH_SHORT).show();

	}

}