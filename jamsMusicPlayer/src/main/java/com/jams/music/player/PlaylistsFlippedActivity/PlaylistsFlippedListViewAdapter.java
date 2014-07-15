package com.jams.music.player.PlaylistsFlippedActivity;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jams.music.player.R;
import com.jams.music.player.AsyncTasks.AsyncRemovePlaylistEntryTask;
import com.jams.music.player.AsyncTasks.AsyncReorderPlaylistEntriesTask;
import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.GMusicHelpers.GMusicClientCalls;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.Utils.Common;
import com.mobeta.android.dslv.SimpleDragSortCursorAdapter;

public class PlaylistsFlippedListViewAdapter extends SimpleDragSortCursorAdapter {

	private Context mContext;
	private SharedPreferences sharedPreferences;
	private ArrayList<Integer> mCursorMappings;
	private String mPlaylistId;
	private String mPlaylistName;
	private ArrayList<String> mEntryIds;
	private ArrayList<String> mSongIds;
	
    public PlaylistsFlippedListViewAdapter(Context context, 
    									   int layoutId,
    									   Cursor cursor, 
    									   String[] columns,
    									   int[] viewIds, 
    									   int something,
    									   String playlistId, 
    									   String playlistName,
    									   ArrayList<String> entryIds,
    									   ArrayList<String> songIds) {
    	
        super(context, layoutId, cursor, columns, viewIds, something);
        mContext = context;
        mPlaylistId = playlistId;
        mPlaylistName = playlistName;
        mEntryIds = entryIds;
        mSongIds = songIds;
        sharedPreferences = mContext.getSharedPreferences("com.jams.music.player", Context.MODE_PRIVATE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        Cursor c = (Cursor) getItem(position);
        PlaylistsFlippedListViewHolder holder = null;
        
		if (convertView == null) {
			holder = new PlaylistsFlippedListViewHolder();
			holder.title = (TextView) v.findViewById(R.id.playlists_flipped_song);
			holder.artist = (TextView) v.findViewById(R.id.playlists_flipped_artist);
			holder.removeSong = (ImageView) v.findViewById(R.id.remove_song_from_queue);
			
			holder.title.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Light"));
			holder.artist.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
			
			holder.title.setPaintFlags(holder.title.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
			holder.artist.setPaintFlags(holder.artist.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
			
			v.setTag(holder);
		} else {
		    holder = (PlaylistsFlippedListViewHolder) convertView.getTag();
		}
        
		//Set the child view's tags.
		v.setTag(R.string.title, c.getString(c.getColumnIndex(DBAccessHelper.SONG_TITLE)));
		v.setTag(R.string.artist, c.getString(c.getColumnIndex(DBAccessHelper.SONG_ARTIST)));
		v.setTag(R.string.album, c.getString(c.getColumnIndex(DBAccessHelper.SONG_ALBUM)));
		v.setTag(R.string.duration, c.getString(c.getColumnIndex(DBAccessHelper.SONG_DURATION)));
		v.setTag(R.string.song_file_path, c.getString(c.getColumnIndex(DBAccessHelper.SONG_FILE_PATH)));
		v.setTag(R.string.genre, c.getString(c.getColumnIndex(DBAccessHelper.SONG_GENRE)));
		v.setTag(R.string.song_index, position);
		v.setTag(R.string.album_art, c.getString(c.getColumnIndex(DBAccessHelper.SONG_ALBUM_ART_PATH)));
		v.setTag(R.string.song_source, c.getString(c.getColumnIndex(DBAccessHelper.SONG_SOURCE)));
		v.setTag(R.string.song_id, c.getString(c.getColumnIndex(DBAccessHelper.SONG_ID)));
		
		//Apply the card layout's background based on the color theme.
		if (sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME")) {
			v.setBackgroundResource(R.drawable.card_light);
		} else if (sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
			v.setBackgroundResource(R.drawable.card_dark);
		}

        return v;
    }
    
    /* Deletes a song from MediaStore from the specified playlist. If the playlist doesn't exist 
     * (maybe it's a GMusic playlist), this method will fail silently. */
    public void removeMediaStorePlaylistMember(Context context, long memberId, long mPlaylistId) {
        try {
            Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", mPlaylistId);
            String where = MediaStore.Audio.Playlists.Members.AUDIO_ID + "=?";
            String columns[] = { String.valueOf(memberId) };
            context.getContentResolver().delete(uri, where, columns);
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
    }
    
    /* Reorders a song in the specified playlist in the MediaStore database.
     * If the playlist doesn't exist (maybe it's a GMusic playlist), this method 
     * will fail silently. */
    private int reorderMediaStorePlaylistMember(ContentResolver res, long playlistId, int from, int to) {
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId)
                									.buildUpon()
                									.appendEncodedPath(String.valueOf(from))
                									.appendQueryParameter("move", "true")
                									.build();
        
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, to);
        return res.update(uri, values, null, null);
    }
    
    @Override
	public void remove(int which) {
		super.remove(which);
		
		//If the current playlist is stored in MediaStore, modify the MediaStore entry.
		try {
			long playlistMemberId = Long.parseLong(mEntryIds.get(which));
			long playlistId = Long.parseLong(mPlaylistId);
			removeMediaStorePlaylistMember(mContext, playlistMemberId, playlistId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		AsyncRemovePlaylistEntryTask task = new AsyncRemovePlaylistEntryTask(mContext, mEntryIds.get(which));
		task.execute();
		mEntryIds.remove(which);
		
	}

	@Override
    public void drop(int from, int to) {
    	super.drop(from, to);
    	
    	if (from==to) {
    		//Nothing to do here.
    		return;
    	}
    	
    	//If the current playlist is stored in MediaStore, modify the MediaStore entry.
    	try {
    		long playlistId = Long.parseLong(mPlaylistId);
        	reorderMediaStorePlaylistMember(mContext.getContentResolver(), playlistId, from, to);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	//Reorder the playlist entries.
    	String tempEntryId = mEntryIds.get(from);
    	mEntryIds.remove(from);
        mEntryIds.add(to, tempEntryId);
        
        String tempSongId = mSongIds.get(from);
        mSongIds.remove(from);
        mSongIds.add(to, tempSongId);
        
    	//Retrieve the current cursor and cursor mapping.
    	Cursor c = getCursor();
    	mCursorMappings = getCursorPositions();
    	
    	//Retrieve the songId and entryId of the song that's just been moved.
    	ArrayList<String> songId = new ArrayList<String>();
    	ArrayList<String> songEntryId = new ArrayList<String>();
    	
    	String afterEntryId = "";
    	String beforeEntryId = "";
    	
    	int originalSongIndex = mCursorMappings.get(to);
    	int originalAfterEntryIndex;
    	int originalBeforeEntryIndex;
    	int afterEntryIncrementer = to;
    	int beforeEntryIncrementer = to;
    	
    	c.moveToPosition(originalSongIndex);
    	songId.add(c.getString(c.getColumnIndex(DBAccessHelper.SONG_FILE_PATH)));
    	songEntryId.add(c.getString(c.getColumnIndex(DBAccessHelper.PLAYLIST_SONG_ENTRY_ID)));
    	
    	do {
    		if ((afterEntryIncrementer+1) < mCursorMappings.size()) {
    			afterEntryIncrementer++;
        		originalAfterEntryIndex = mCursorMappings.get(afterEntryIncrementer);
        		c.moveToPosition(originalAfterEntryIndex);
        		afterEntryId = c.getString(c.getColumnIndex(DBAccessHelper.PLAYLIST_SONG_ENTRY_ID));
    		}
    		
    	} while ((afterEntryId.length()==0) && ((afterEntryIncrementer+1) < mCursorMappings.size()));
    	
    	do {
    		if ((beforeEntryIncrementer-1) > -1) {
    			beforeEntryIncrementer--;
        		originalBeforeEntryIndex = mCursorMappings.get(beforeEntryIncrementer);
        		c.moveToPosition(originalBeforeEntryIndex);
        		beforeEntryId = c.getString(c.getColumnIndex(DBAccessHelper.PLAYLIST_SONG_ENTRY_ID));
    		}
    		
    	} while ((beforeEntryId.length()==0) && ((beforeEntryIncrementer-1) > -1));
    	
    	//Reorder the song on Google's servers.
    	AsyncReorderPlaylistEntriesTask task = new AsyncReorderPlaylistEntriesTask(mContext,
    																			   mPlaylistId,
    																			   mPlaylistName,
    																			   songId,
    																			   songEntryId,
    																			   afterEntryId,
    																			   beforeEntryId,
    																			   mEntryIds, 
    																			   mSongIds);
    	task.execute();
    	
    }
    
	/****************************************************************************************************************
     * This method saves the ListView's order/deletion changes to the database, Google's servers, and the M3U file.
     * <p><b>Note:</b> This method contains code that will access the network, so it <i>must</i> be called from a 
     * different thread. Calling it from the main thread will cause the app to crash with a 
     * NetworkOnMainThreadException.</p>
     * 
     * @deprecated This method is only useful if the MobileClient endpoints are being used to modify (reorder and 
     * delete entries) playlists. The MobileClient endpoints are a pain to use right now for this job, so I'm just 
     * handling each playlist entry modification individually in the overriden drop() and remove() methods with 
     * WebClient endpoints.
     * 
     * @param playlistName The name of the playlist that is being modified.
     * @param playlistId The GMusic playlistId parameter (if modifying a GMusic playlist).
     ****************************************************************************************************************/
    public void persistChanges(String playlistName, String playlistId) {
    	DBAccessHelper dbHelper = new DBAccessHelper(mContext);
        Cursor c = getCursor();
        mCursorMappings = getCursorPositions();
        c.moveToPosition(-1);
        
        /* GMusic's backend server uses horribly misleading JSON keys. The server accepts 
         * "precedingEntryIds" and "followingEntryIds". These "entryIds" are actually the 
         * clientIds that were retrieved from Google when the playlists' entries' info 
         * was initially downloaded. For the sake of clarity, we'll use "songEntryId" as 
         * the variable name(s) in this method, even though we're actually dealing with 
         * clientIds.
         * 
         * "songFilePath" actually stores the value of "trackId" if the song is from 
         * a Google Play Music playlist.
         */
        while (c.moveToNext()) {
            int listPosition = getListPosition(c.getPosition());
            int oldIndex = c.getInt(c.getColumnIndex(DBAccessHelper.PLAYLIST_ORDER));
            String songFilePath = c.getString(c.getColumnIndex(DBAccessHelper.SONG_FILE_PATH));
            String songEntryId = c.getString(c.getColumnIndex(DBAccessHelper.PLAYLIST_SONG_ENTRY_ID));
            String songSource = c.getString(c.getColumnIndex(DBAccessHelper.PLAYLIST_SOURCE));
            
            if (listPosition==REMOVED) {
            	
            	//Add a JSONObject with the delete params to send to Google's servers.
            	if (songSource.equals(DBAccessHelper.GMUSIC)) {
            		//Add a JSONObject that will delete the song from the playlist on Google's servers.
                	try {
						GMusicClientCalls.putDeletePlaylistEntryRequest(songEntryId);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						continue;
					}
            		
            	}
            	
            	//Update the database.
            	//dbHelper.deleteSongFromPlaylist(playlistName, songFilePath);
            	
            } else if (listPosition!=c.getPosition()) {
            	
            	//Add a JSONObject with the update params to send to Google's servers.
            	if (songSource.equals(DBAccessHelper.GMUSIC)) {
                	
            		//Retrieve the new position of the current song.
            		int currentPosition = c.getPosition();
            		int newPosition = mCursorMappings.indexOf(c.getPosition());
            		int followingEntryPosition = newPosition;
            		int precedingEntryPosition = newPosition;
            		int newFollowingEntryMapping;
            		int newPrecedingEntryMapping;
            		
            		/**************************************************************************************
            		 * Retrieve the value of followingEntryPosition (the clientId of the previous song);
            		 **************************************************************************************/
            		String followingEntrySongSource = null;
            		String followingEntryId = null;
            		do {
            			/* The playlist we're dealing with may be a mix of local songs and GMusic songs. The 
                		 * HTTP POST request for updating track orders needs a "followingEntryId" parameter.
                		 * Local songs won't have a valid GMusic entryId, so we need to find the previous GMusic song and get 
                		 * its entryId. We'll keep moving the modified cursor backward until we hit a song that's from 
                		 * GMusic. If we never find a GMusic song, songSource will be equal to "LOCAL", 
                		 * so "followingEntryId" will never be assigned and left as null. The putOpt() 
                		 * method in the JSONObject will skip over "followingEntryId", if this happens.
                		 */
            			followingEntryPosition--; 
            			
            			//Retrieve the ORIGINAL cursor position of the NEW followingEntryId.
            			newFollowingEntryMapping = mCursorMappings.get(followingEntryPosition);
            			
            			//Move the ORIGINAL cursor to the NEW followingEntryId's position.
            			c.moveToPosition(newFollowingEntryMapping);
            			followingEntrySongSource = c.getString(c.getColumnIndex(DBAccessHelper.PLAYLIST_SOURCE));
            		} while ((!followingEntrySongSource.equals(DBAccessHelper.GMUSIC)) && (followingEntryPosition > -1));
            		
            		//Check if a GMusic song was ever found before the current song's new position.	
            		if (songSource.equals(DBAccessHelper.GMUSIC)) {
            			followingEntryId = c.getString(c.getColumnIndex(DBAccessHelper.SONG_FILE_PATH));
            			//Cursor tempCursor = new DBAccessHelper(mContext).getSongBySongID(songFilePath);
            			//tempCursor.moveToFirst();
            			//followingEntryId = c.getString(c.getColumnIndex(DBAccessHelper.SONG_TITLE));
            		}
            		
            		//Move the cursor back to it's original position.
            		c.moveToPosition(currentPosition);
                	
            		/**********************************************************************************
            		 * Retrieve the value of precedingEntryPosition (the clientId of the next song);
            		 **********************************************************************************/
            		String precedingEntrySongSource = null;
            		String precedingEntryId = null;
            		do {
            			/* The playlist we're dealing with may be a mix of local songs and GMusic songs. The 
                		 * HTTP POST request for updating track orders needs a "precedingEntryId" parameter.
                		 * Local songs won't have a valid GMusic entryId, so we need to find the previous GMusic song and get 
                		 * its entryId. We'll keep moving the modified cursor forward until we hit a song that's from 
                		 * GMusic. If we never find a GMusic song, songSource will be equal to "LOCAL", 
                		 * so "precedingEntryId" will never be assigned and left as null. The putOpt() 
                		 * method in the JSONObject will skip over "precedingEntryId", if this happens.
                		 */
            			precedingEntryPosition++; 
            			
            			//Retrieve the ORIGINAL cursor position of the NEW precedingEntryId.
            			newPrecedingEntryMapping = mCursorMappings.get(precedingEntryPosition);
            			
            			//Move the ORIGINAL cursor to the NEW precedingEntryId's position.
            			c.moveToPosition(newPrecedingEntryMapping);
            			precedingEntrySongSource = c.getString(c.getColumnIndex(DBAccessHelper.PLAYLIST_SOURCE));
            		} while ((!precedingEntrySongSource.equals(DBAccessHelper.GMUSIC)) && (precedingEntryPosition < mCursorMappings.size()));
            		
            		//Check if a GMusic song was ever found after the current song's new position.	
            		if (songSource.equals(DBAccessHelper.GMUSIC)) {
            			precedingEntryId = c.getString(c.getColumnIndex(DBAccessHelper.SONG_FILE_PATH));
            			//Cursor tempCursor = new DBAccessHelper(mContext).getSongBySongID(songFilePath);
            			//tempCursor.moveToFirst();
            			//precedingEntryId = c.getString(c.getColumnIndex(DBAccessHelper.SONG_TITLE));
            		}
            		
            		//Move the cursor back to it's original position.
            		c.moveToPosition(currentPosition);
                	
                	//Cursor tempCursor = new DBAccessHelper(mContext).getSongBySongID(songFilePath);
                	//tempCursor.moveToFirst();
            		JSONObject jsonUpdateObject = new JSONObject();
            		try {
            			jsonUpdateObject.put("id", songEntryId);
						jsonUpdateObject.put("creationTimestamp", "-1");
						jsonUpdateObject.put("deleted", false);
						jsonUpdateObject.putOpt("followingEntryId", followingEntryId);
						//jsonUpdateObject.put("CURRENT SONG: ", tempCursor.getString(tempCursor.getColumnIndex(DBAccessHelper.SONG_TITLE)));
						jsonUpdateObject.put("lastModifiedTimestamp", "" + new Date().getTime());
						jsonUpdateObject.put("playlistId", playlistId);
						jsonUpdateObject.put("source", 1);
						jsonUpdateObject.put("trackId", songFilePath);
						jsonUpdateObject.putOpt("precedingEntryId", precedingEntryId);
	
					} catch (JSONException e) {
						e.printStackTrace();
						continue;
					}
            		
                    //Add the JSONObject to mutationsArray if it's not empty.
                    if (jsonUpdateObject.length() > 0) {
                    	try {
                    		GMusicClientCalls.putUpdatePlaylistEntryRequest(jsonUpdateObject);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							continue;
						}
                    	
                    }
                    
            	}
            	
                //Update the database.
                //dbHelper.reorderSongInPlaylist(playlistName, oldIndex, listPosition);
            }
            
        }
        
        Log.e("DEBUG", "---------------------JSON REQUEST------------------------");
        try {
			Log.e("DEBUG", GMusicClientCalls.mPlaylistEntriesMutationsArray.toString(3));
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        //Send the update POST request to Google's servers.
        if (GMusicClientCalls.getQueuedMutationsCount() > 0) {
        	String result = null;
            try {
            	result = GMusicClientCalls.modifyPlaylist(mContext);
            } catch (Exception e) {
            	e.printStackTrace();
            }
            
            Log.e("DEBUG", "----------------JSON RESPONSE----------------");
            Log.e("DEBUG", result);
        }
        
        if (dbHelper!=null) {
        	dbHelper.close();
        	dbHelper = null;
        }
        
    }
    
    //Convert millisseconds to hh:mm:ss format.
    private String convertMillisToMinsSecs(long milliseconds) {
    	
    	int secondsValue = (int) (milliseconds / 1000) % 60;
    	int minutesValue = (int) ((milliseconds / (1000*60)) % 60);
    	int hoursValue  = (int) ((milliseconds / (1000*60*60)) % 24);
    	
    	String seconds = "";
    	String minutes = "";
    	String hours = "";
    	
    	if (secondsValue < 10) {
    		seconds = "0" + secondsValue;
    	} else {
    		seconds = "" + secondsValue;
    	}

    	if (minutesValue < 10) {
    		minutes = "0" + minutesValue;
    	} else {
    		minutes = "" + minutesValue;
    	}
    	
    	if (hoursValue < 10) {
    		hours = "0" + hoursValue;
    	} else {
    		hours = "" + hoursValue;
    	}
    	
    	String output = "";
    	if (hoursValue!=0) {
    		output = hours + ":" + minutes + ":" + seconds;
    	} else {
    		output = minutes + ":" + seconds;
    	}
    	
    	return output;
    }
    
	static class PlaylistsFlippedListViewHolder {
	    public TextView title;
	    public TextView artist;
	    public ImageView removeSong;

	}
    
}