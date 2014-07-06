package com.jams.music.player.AsyncTasks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.provider.MediaStore;

import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.DBHelpers.MediaStoreAccessHelper;
import com.jams.music.player.FoldersFragment.FileExtensionFilter;
import com.jams.music.player.R;
import com.jams.music.player.Utils.Common;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

/**
 * The Mother of all AsyncTasks in this app.
 * 
 * 
 * 
 * @author Saravan Pantham
 */
public class AsyncBuildLibraryTask extends AsyncTask<String, String, Void> {

	private Context mContext;
	private Common mApp;
	private OnBuildLibraryProgressUpdate mBuildLibraryProgressUpdate;
	
	private String mCurrentTask = "";
	private int mOverallProgress = 0;
	private Date date = new Date();

	private String mMediaStoreSelection = null;
	private HashMap<String, String> mGenresHashMap = new HashMap<String, String>();
	private HashMap<String, String> mFolderArtHashMap = new HashMap<String, String>();
	private MediaMetadataRetriever mMMDR = new MediaMetadataRetriever();
	
	private PowerManager pm;
	private PowerManager.WakeLock wakeLock;

	public AsyncBuildLibraryTask(Context context) {
		mContext = context;
		mApp = (Common) mContext;
	}
	
	/**
	 * Provides callback methods that expose this 
	 * AsyncTask's progress.
	 * 
	 * @author Saravan Pantham
	 */
	public interface OnBuildLibraryProgressUpdate {
		
		/**
		 * Called when this AsyncTask begins executing 
		 * its doInBackground() method.
		 */
		public void onStartBuildingLibrary();
		
		/**
		 * Called whenever mOverall Progress has been updated.
		 */
		public void onProgressUpdate(String mCurrentTask, int overallProgress, int maxProgress);
		
		/**
		 * Called when this AsyncTask finishes executing 
		 * its onPostExecute() method.
		 */
		public void onFinishBuildingLibrary();
		
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		mApp.setIsBuildingLibrary(true);
		mApp.setIsScanFinished(false);
		
		if (mBuildLibraryProgressUpdate!=null)
			mBuildLibraryProgressUpdate.onStartBuildingLibrary();
		
		// Acquire a wakelock to prevent the CPU from sleeping while the process is running.
		pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
								  "com.jams.music.player.AsyncTasks.AsyncBuildLibraryTask");
		wakeLock.acquire();

	}

	@Override
    protected Void doInBackground(String... params) {
		
		/* 
		 * Get a cursor of songs from MediaStore. The cursor 
		 * is limited by the folders that have been selected 
		 * by the user.
		 */
		mCurrentTask = mContext.getResources().getString(R.string.building_music_library);
		Cursor mediaStoreCursor = getSongsFromMediaStore();
		
		/* 
		 * Transfer the content in mediaStoreCursor over to 
		 * Jams' private database.
		 */
		if (mediaStoreCursor!=null) {
			saveMediaStoreDataToDB(mediaStoreCursor);
			mediaStoreCursor.close();
		}
		
		
		
		
    	/* 
    	 * Save EQ presets to the database. 
    	 */
		saveEQPresets();
		
		/*
		 * Save album art paths for each song to the database.
		 */
		getAlbumArt();
		
    	return null;
       
        /*/****************************************************************
         * BUILD THE PLAYLISTS LIBRARY
         ****************************************************************//*
        Cursor defaultPlaylistsCursor = mApp.getDBAccessHelper().getAllPlaylistsSimplified();
        
        //Create a set of SmartPlaylists.
        if (defaultPlaylistsCursor.getCount()==0) {
        	mApp.getDBAccessHelper().getMusicLibraryPlaylistsDBHelper().addDefaultPlaylist("Top 25 Played Songs", 0);
    		mApp.getDBAccessHelper().getMusicLibraryPlaylistsDBHelper().addDefaultPlaylist("Recently Added", 1);
    		mApp.getDBAccessHelper().getMusicLibraryPlaylistsDBHelper().addDefaultPlaylist("Top Rated", 2);
    		mApp.getDBAccessHelper().getMusicLibraryPlaylistsDBHelper().addDefaultPlaylist("Recently Played", 3);	

        }
        
        //Close the playlists cursor.
        if (defaultPlaylistsCursor!=null) {
            defaultPlaylistsCursor.close();
            defaultPlaylistsCursor = null;
        }

		mCurrentTask = "Building Playlists";
		
		//Avoid "Divide by zero" errors.
		int buildingPlaylistsIncrement;
		if (playlistsList.size()!=0) {
			buildingPlaylistsIncrement = 100000/playlistsList.size();
		} else {
			buildingPlaylistsIncrement = 100000/1;
		}
		
		//Loop through the playlist files that were found by the scanner.
        for (int k=0; k < playlistsList.size(); k++) {
        	
        	mOverallProgress = mOverallProgress + buildingPlaylistsIncrement;
        	publishProgress();
        	
        	 First, we'll check if the playlist already exists in the database.
        	 * If it does, we'll move on to comparing the timestamps on the playlist 
        	 * file and the timestamp that was saved from the previous scan. If the 
        	 * timestamps match, the playlist wasn't modified and we can skip this 
        	 * playlist file. If the timestamps are different, the playlist was 
        	 * modified so we'll delete all instances of this playlist in the DB. 
        	 * We'll then reparse the playlist file and add it to the database. 
        	 
	    	String playlistFilePath = playlistsList.get(k);
	    	
	    	//Construct a File from the playlist file path.
	    	File playlistFile = new File(playlistFilePath);
	    	
	    	//Retrieve the last modified date from the playlist file.
	    	long fileLastModified = playlistFile.lastModified();
	    	
	    	//Retrieve a cursor with the current playlist (if it exists).
	    	Cursor playlistCursor = mApp.getDBAccessHelper().getMusicLibraryPlaylistsDBHelper().getPlaylistByFilePath(playlistFilePath);
	    	
	    	if (playlistCursor.getCount()==0) {
	    		//The playlist doesn't exist in the DB, so continue on to the parsing process.
	    	} else { 
	    		//The playlist exists in the DB, so compare it with the timestamps of the actual file.
	    		playlistCursor.moveToFirst();
	    		long dbLastModified = playlistCursor.getLong(playlistCursor.getColumnIndex(DBAccessHelper.PLAYLIST_LAST_MODIFIED));
	    		playlistCursor.close();
	    		
	    		if (fileLastModified==dbLastModified) {
	    			//The file wasn't modified since it was last parsed, so skip to the next playlist file.
	    			continue;
	    		} else {
	    			//The file was modified since it was last parsed, so delete the current instance in the DB.
	    			mApp.getDBAccessHelper().getMusicLibraryPlaylistsDBHelper().deletePlaylistByFilePath(playlistFilePath);
	    		}
	    		
	    	}
	    	
	    	//Close the playlists cursor.
	    	if (playlistCursor!=null) {
		    	playlistCursor.close();
		    	playlistCursor = null;
	    	}
	    	
	    	//Use the name of the playlist file as the playlist name.
	    	int lastSlashIndex = playlistFilePath.lastIndexOf("/")+1;
	    	int lastDotIndex = playlistFilePath.lastIndexOf(".");
	    	String playlistName = playlistFilePath.substring(lastSlashIndex, lastDotIndex);
	        
	        //Read through the playlist file and add each element to the playlists DB.
	        BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(playlistFile));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			//Trim down the playlist's file path to retrieve the playlist's folder path.
			String playlistFolderPath = null;
	        if (!playlistFilePath.isEmpty()) {
	            int lastSlash = playlistFilePath.lastIndexOf("/");
	            playlistFolderPath = playlistFilePath.substring(0, lastSlash);
	        }
			
	        String line = null;
	        try {
	        	
	        	while ((line = br.readLine())!=null) {
			    	line = line.trim();
			    	
			    	//Skip over empty lines and comments.
			    	if (line.isEmpty()) {
			    		continue;
			    	}
			    	
			    	if (line.charAt(0)=='#') {
			    		continue;
			    	}
			    	
			    	//Remove any quotes that might be in the file paths.
			    	if (line.contains("\"")) {
			    		line = line.replace("\"", "");
			    	}
			    	
			    	//Check if the file paths are absolute. If they are, use them as they are.
			    	String elementFilePath;
			    	if (line.charAt(1)==':') {
			    		//We're probably dealing with Windows absolute paths (C:\, D:\, F:\, etc.)
			    		elementFilePath = line;
			    	} else if (line.charAt(0)=='/') {
			    		//We're probably dealing with Unix absolute paths (the root directory, "/")
			    		elementFilePath = line;
			    	} else {
			    		//Relative paths FTW!
			    		elementFilePath = playlistFolderPath + "/" + line;
			    	}
			    	
			    	mApp.getDBAccessHelper().getMusicLibraryPlaylistsDBHelper().addNewPlaylist(playlistName, 
			    									 playlistFilePath, 
			    									 playlistFolderPath,
			    									 elementFilePath,
			    									 "LOCAL",
			    									 "",
			    									 "",
			    									 fileLastModified,
			    									 0,
			    									 UUID.randomUUID().toString());
			    	
			    }	
				
			} catch (IOException e) {
				e.printStackTrace();
			}
	        
	        try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
        }
		
		//Query Android's MediaStore for playlists.
		mApp.getDBAccessHelper().getMusicLibraryPlaylistsDBHelper().deleteAllPlaylists();
		
		Cursor playlistMembersCursor = null;
		Cursor songCursor = null;
		Cursor playlistsCursor = mContext.getContentResolver().query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, 
															null, 
															null, 
															null, 
															null);
		
		if (playlistsCursor!=null) {
			while (playlistsCursor.moveToNext()) {
				String playlistName = playlistsCursor.getString(playlistsCursor.getColumnIndex(MediaStore.Audio.Playlists.NAME));
				long playlistId = playlistsCursor.getLong(playlistsCursor.getColumnIndex(MediaStore.Audio.Playlists._ID));
				Uri playlistContentUri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
				playlistMembersCursor = mContext.getContentResolver().query(playlistContentUri, 
																				   null, 
																				   MediaStore.Audio.Media.IS_MUSIC + "!=0", 
																				   null, 
																				   null);
				
				if (playlistMembersCursor!=null) {
					
					int playlistElementsOrder = 0;
					while (playlistMembersCursor.moveToNext()) {
						//Check if the playlist song exists in Jams' newly built library. If not, skip it.
						String songArtist = playlistMembersCursor.getString(playlistMembersCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.ARTIST));
						String songAlbum = playlistMembersCursor.getString(playlistMembersCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.ALBUM));
						String songTitle = playlistMembersCursor.getString(playlistMembersCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.TITLE));
						String songEntryId = playlistMembersCursor.getString(playlistMembersCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID));
						
						String selection = " AND " + DBAccessHelper.SONG_TITLE + "=" + "'" + songTitle.replace("'", "''") + "'"
								         + " AND " + DBAccessHelper.SONG_ARTIST + "=" + "'" + songArtist.replace("'", "''") + "'"
								         + " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + songAlbum.replace("'", "''") + "'";
						songCursor = mApp.getDBAccessHelper().getMusicLibraryDBHelper().getAllSongsSearchable(selection);

						//Check if the song was found in Jams' database.
						if (songCursor==null || songCursor.getCount() <= 0) {
							//The song wasn't found. Continue on to the next song in the playlist.
							continue;
						} else {
							songCursor.moveToFirst();
						}
						
						//Save the playlist entry into Jams' private database.
						String songFilePath = songCursor.getString(songCursor.getColumnIndex(DBAccessHelper.SONG_FILE_PATH));
						String songId = songCursor.getString(songCursor.getColumnIndex(DBAccessHelper.SONG_ID));
						
						mApp.getDBAccessHelper().getMusicLibraryPlaylistsDBHelper().addNewPlaylist(playlistName, null, null, 
														 songFilePath, "LOCAL", "" + playlistId, 
														 songId, new Date().getTime(), playlistElementsOrder, 
														 songEntryId);
						
						playlistElementsOrder++;
					}
					
				}
				
			}
			
		}
		
		if (playlistMembersCursor!=null) {
			playlistMembersCursor.close();
			playlistMembersCursor = null;
		}
        
		if (songCursor!=null) {
			songCursor.close();
			songCursor = null;
		}
		
		if (playlistsCursor!=null) {
			playlistsCursor.close();
			playlistsCursor = null;
		} */
        
    }
	
	/**
	 * Retrieves a cursor of songs from MediaStore. The cursor 
	 * is limited to songs that are within the folders that the user 
	 * selected.
	 */
	private Cursor getSongsFromMediaStore() {
		//Get a cursor of all active music folders.
        Cursor musicFoldersCursor = mApp.getDBAccessHelper().getAllMusicFolderPaths();
        
        //Build the appropriate selection statement.
        Cursor mediaStoreCursor = null;
        String sortOrder = null;
        String projection[] = { MediaStore.Audio.Media.TITLE, 
        						MediaStore.Audio.Media.ARTIST, 
        						MediaStore.Audio.Media.ALBUM, 
        						MediaStore.Audio.Media.ALBUM_ID, 
        						MediaStore.Audio.Media.DURATION, 
        						MediaStore.Audio.Media.TRACK, 
        						MediaStore.Audio.Media.YEAR, 
        						MediaStore.Audio.Media.DATA, 
        						MediaStore.Audio.Media.DATE_ADDED, 
        						MediaStore.Audio.Media.DATE_MODIFIED, 
        						MediaStore.Audio.Media._ID, 
        						MediaStoreAccessHelper.ALBUM_ARTIST };
        
        //Grab the cursor of MediaStore entries.
        if (musicFoldersCursor==null || musicFoldersCursor.getCount() < 1) {
        	//No folders were selected by the user. Grab all songs in MediaStore.
        	mediaStoreCursor = MediaStoreAccessHelper.getAllSongs(mContext, projection, sortOrder);
        } else {
        	//Build a selection statement for querying MediaStore.
            mMediaStoreSelection = buildMusicFoldersSelection(musicFoldersCursor);
            mediaStoreCursor = MediaStoreAccessHelper.getAllSongsWithSelection(mContext, 
            																   mMediaStoreSelection, 
            																   projection, 
            																   sortOrder);
            
            //Close the music folders cursor.
            musicFoldersCursor.close(); 
        }
    	
    	return mediaStoreCursor;
	}
	
	/**
	 * Iterates through mediaStoreCursor and transfers its data 
	 * over to Jams' private database.
	 */
	private void saveMediaStoreDataToDB(Cursor mediaStoreCursor) {
		try {
    		//Initialize the database transaction manually (improves performance).
    		mApp.getDBAccessHelper().getWritableDatabase().beginTransaction();
    		
    		//Clear out the table.
    		mApp.getDBAccessHelper()
    			.getWritableDatabase()
    			.delete(DBAccessHelper.MUSIC_LIBRARY_TABLE, 
    					null, 
    					null);
    		
    		//Tracks the progress of this method.
    		int subProgress = 0;
    		if (mediaStoreCursor.getCount()!=0) {
    			subProgress = 250000/(mediaStoreCursor.getCount());
    		} else {
    			subProgress = 250000/1;
    		}
    		
    		//Populate a hash of all songs in MediaStore and their genres.
    		buildGenresLibrary();
    		
    		//Prefetch each column's index.
    		final int titleColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
    		final int artistColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
    		final int albumColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
    		final int durationColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
    		final int trackColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.TRACK);
    		final int yearColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.YEAR);
    		final int dateAddedColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED);
    		final int dateModifiedColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED);
    		final int filePathColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
    		final int idColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media._ID);
    		int albumArtistColIndex = mediaStoreCursor.getColumnIndex(MediaStoreAccessHelper.ALBUM_ARTIST);
    		
    		/* The album artist field is hidden by default and we've explictly exposed it.
    		 * The field may cease to exist at any time and if it does, use the artists 
    		 * field instead.
    		 */
    		if (albumArtistColIndex==-1) {
    			albumArtistColIndex = artistColIndex;
    		}
    		
    		//Iterate through MediaStore's cursor and save the fields to Jams' DB.
            for (int i=0; i < mediaStoreCursor.getCount(); i++) {
            	
            	mediaStoreCursor.moveToPosition(i);
            	mOverallProgress += subProgress;
            	publishProgress();
            	
            	String songTitle = mediaStoreCursor.getString(titleColIndex);
            	String songArtist = mediaStoreCursor.getString(artistColIndex);
            	String songAlbum = mediaStoreCursor.getString(albumColIndex);
            	String songAlbumArtist = mediaStoreCursor.getString(albumArtistColIndex);
            	String songFilePath = mediaStoreCursor.getString(filePathColIndex);
            	String songGenre = getSongGenre(songFilePath);
            	String songDuration = mediaStoreCursor.getString(durationColIndex);
            	String songTrackNumber = mediaStoreCursor.getString(trackColIndex);
            	String songYear = mediaStoreCursor.getString(yearColIndex);
            	String songDateAdded = mediaStoreCursor.getString(dateAddedColIndex);
            	String songDateModified = mediaStoreCursor.getString(dateModifiedColIndex);
            	String songId = mediaStoreCursor.getString(idColIndex);
            	String songSource = DBAccessHelper.LOCAL;
            	String songSavedPosition = "-1";

            	//Check if any of the other tags were empty/null and set them to "Unknown xxx" values.
            	if (songArtist==null || songArtist.isEmpty()) {
            		songArtist = mContext.getResources().getString(R.string.unknown_artist);
            	}
            	
            	if (songAlbumArtist==null || songAlbumArtist.isEmpty()) {
            		if (songArtist!=null && !songArtist.isEmpty()) {
            			songAlbumArtist = songArtist;
            		} else {
            			songAlbumArtist = mContext.getResources().getString(R.string.unknown_album_artist);
            		}
            		
            	}
            	
            	if (songAlbum==null || songAlbum.isEmpty()) {
            		songAlbum = mContext.getResources().getString(R.string.unknown_album);;
            	}
            	
            	if (songGenre==null || songGenre.isEmpty()) {
            		songGenre = mContext.getResources().getString(R.string.unknown_genre);
            	}
            	
            	//Filter out track numbers and remove any bogus values.
            	if (songTrackNumber!=null) {
        			if (songTrackNumber.contains("/")) {
        				int index = songTrackNumber.lastIndexOf("/");
        				songTrackNumber = songTrackNumber.substring(0, index);            	
        			}
                	
            	}

                long durationLong = 0;
                try {
                    durationLong = Long.parseLong(songDuration);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            	
            	ContentValues values = new ContentValues();
            	values.put(DBAccessHelper.SONG_TITLE, songTitle);
            	values.put(DBAccessHelper.SONG_ARTIST, songArtist);
            	values.put(DBAccessHelper.SONG_ALBUM, songAlbum);
            	values.put(DBAccessHelper.SONG_ALBUM_ARTIST, songAlbumArtist);
            	values.put(DBAccessHelper.SONG_DURATION, convertMillisToMinsSecs(durationLong));
            	values.put(DBAccessHelper.SONG_FILE_PATH, songFilePath);
            	values.put(DBAccessHelper.SONG_TRACK_NUMBER, songTrackNumber);
            	values.put(DBAccessHelper.SONG_GENRE, songGenre);
            	values.put(DBAccessHelper.SONG_YEAR, songYear);
            	values.put(DBAccessHelper.SONG_LAST_MODIFIED, songDateModified);
            	values.put(DBAccessHelper.BLACKLIST_STATUS, false);
            	values.put(DBAccessHelper.ADDED_TIMESTAMP, date.getTime());
            	values.put(DBAccessHelper.RATING, 0);
            	values.put(DBAccessHelper.LAST_PLAYED_TIMESTAMP, songDateModified);
            	values.put(DBAccessHelper.SONG_SOURCE, songSource);
            	values.put(DBAccessHelper.SONG_ID, songId);
            	values.put(DBAccessHelper.SAVED_POSITION, songSavedPosition);
            	
            	//Add all the entries to the database to build the songs library.
            	mApp.getDBAccessHelper().getWritableDatabase().insert(DBAccessHelper.MUSIC_LIBRARY_TABLE, 
            												 		  null, 
            												 		  values);	
            	
            	
            }
    		
    	} catch (SQLException e) {
    		// TODO Auto-generated method stub.
    		e.printStackTrace();
    	} finally {
    		//Close the transaction.
            mApp.getDBAccessHelper().getWritableDatabase().setTransactionSuccessful();
    		mApp.getDBAccessHelper().getWritableDatabase().endTransaction();
    	}

	}
	
	/**
	 * Constructs the selection string for limiting the MediaStore 
	 * query to specific music folders.
	 */
	private String buildMusicFoldersSelection(Cursor musicFoldersCursor) {
		String mediaStoreSelection = MediaStore.Audio.Media.IS_MUSIC + "!= 0 AND (";
        int folderPathColIndex = musicFoldersCursor.getColumnIndex(DBAccessHelper.FOLDER_PATH);
        int includeColIndex = musicFoldersCursor.getColumnIndex(DBAccessHelper.INCLUDE);
        
        for (int i=0; i < musicFoldersCursor.getCount(); i++) {
        	musicFoldersCursor.moveToPosition(i);
        	boolean include = musicFoldersCursor.getInt(includeColIndex) > 0;
        	
        	//Set the correct LIKE clause.
        	String likeClause;
        	if (include)
        		likeClause = " LIKE ";
        	else
        		likeClause = " NOT LIKE ";
        	
        	//The first " AND " clause was already appended to mediaStoreSelection.
        	if (i!=0 && !include)
        		mediaStoreSelection += " AND ";
        	else if (i!=0 && include)
        		mediaStoreSelection += " OR ";
        	
        	mediaStoreSelection += MediaStore.Audio.Media.DATA + likeClause
								+ "'%" + musicFoldersCursor.getString(folderPathColIndex) 
								+ "/%'";

        }
        
        //Append the closing parentheses.
        mediaStoreSelection += ")";
        return mediaStoreSelection;
	}
	
	/**
	 * Builds a HashMap of all songs and their genres.
	 */
	private void buildGenresLibrary() {
		//Get a cursor of all genres in MediaStore.
		Cursor genresCursor = mContext.getContentResolver().query(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
																  new String[] { MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME },
		            								  			  null, 
		            								  			  null, 
		            								  			  null);
		 
		//Iterate thru all genres in MediaStore.
        for (genresCursor.moveToFirst(); !genresCursor.isAfterLast(); genresCursor.moveToNext()) {
        	String genreId = genresCursor.getString(0);
        	String genreName = genresCursor.getString(1);
        	 
        	/* Grab a cursor of songs in the each genre id. Limit the songs to 
        	 * the user defined folders using mMediaStoreSelection.
        	 */
        	Cursor cursor = mContext.getContentResolver().query(makeGenreUri(genreId),
        														new String[] { MediaStore.Audio.Media.DATA },
				     											mMediaStoreSelection,
				     											null, 
				     											null);
        	 
        	//Add the songs' file paths and their genre names to the hash.
        	if (cursor!=null) {
        		for (int i=0; i < cursor.getCount(); i++) {
        			cursor.moveToPosition(i);
        			mGenresHashMap.put(cursor.getString(0), genreName);
            	}
            	 
            	cursor.close();
        	}
        	 
        }         
         
        if (genresCursor!=null)
        	genresCursor.close();
         
	}
	
	/**
	 * Returns the genre of the song at the specified file path.
	 */
	private String getSongGenre(String filePath) {
		return mGenresHashMap.get(filePath);
	}
	
	/**
	 * Returns a Uri of a specific genre in MediaStore. 
	 * The genre is specified using the genreId parameter.
	 */
	private Uri makeGenreUri(String genreId) {
        String CONTENTDIR = MediaStore.Audio.Genres.Members.CONTENT_DIRECTORY;
        return Uri.parse(new StringBuilder().append(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI.toString())
        									.append("/")
        									.append(genreId)
        									.append("/")
        									.append(CONTENTDIR)
        									.toString());
    }
	
	/**
	 * Saves premade equalizer presets to the database.
	 */
	private void saveEQPresets() {
		Cursor eqPresetsCursor = mApp.getDBAccessHelper().getAllEQPresets();
        
		//Check if this is the first startup (eqPresetsCursor.getCount() will be 0).
        if (eqPresetsCursor!=null && eqPresetsCursor.getCount()==0) {
        	mApp.getDBAccessHelper().addNewEQPreset("Flat", 16, 16, 16, 16, 16, 16, 16, (short) 0, (short) 0, (short) 0);
        	mApp.getDBAccessHelper().addNewEQPreset("Bass Only", 31, 31, 31, 0, 0, 0, 31, (short) 0, (short) 0, (short) 0);
        	mApp.getDBAccessHelper().addNewEQPreset("Treble Only", 0, 0, 0, 31, 31, 31, 0, (short) 0, (short) 0, (short) 0);
        	mApp.getDBAccessHelper().addNewEQPreset("Rock", 16, 18, 16, 17, 19, 20, 22, (short) 0, (short) 0, (short) 0);
        	mApp.getDBAccessHelper().addNewEQPreset("Grunge", 13, 16, 18, 19, 20, 17, 13, (short) 0, (short) 0, (short) 0);
        	mApp.getDBAccessHelper().addNewEQPreset("Metal", 12, 16, 16, 16, 20, 24, 16, (short) 0, (short) 0, (short) 0);
        	mApp.getDBAccessHelper().addNewEQPreset("Dance", 14, 18, 20, 17, 16, 20, 23, (short) 0, (short) 0, (short) 0);
        	mApp.getDBAccessHelper().addNewEQPreset("Country", 16, 16, 18, 20, 17, 19, 20, (short) 0, (short) 0, (short) 0);
        	mApp.getDBAccessHelper().addNewEQPreset("Jazz", 16, 16, 18, 18, 18, 16, 20, (short) 0, (short) 0, (short) 0);
        	mApp.getDBAccessHelper().addNewEQPreset("Speech", 14, 16, 17, 14, 13, 15, 16, (short) 0, (short) 0, (short) 0);
        	mApp.getDBAccessHelper().addNewEQPreset("Classical", 16, 18, 18, 16, 16, 17, 18, (short) 0, (short) 0, (short) 0);
        	mApp.getDBAccessHelper().addNewEQPreset("Blues", 16, 18, 19, 20, 17, 18, 16, (short) 0, (short) 0, (short) 0);
        	mApp.getDBAccessHelper().addNewEQPreset("Opera", 16, 17, 19, 20, 16, 24, 18, (short) 0, (short) 0, (short) 0);
        	mApp.getDBAccessHelper().addNewEQPreset("Swing", 15, 16, 18, 20, 18, 17, 16, (short) 0, (short) 0, (short) 0);
        	mApp.getDBAccessHelper().addNewEQPreset("Acoustic", 17, 18, 16, 19, 17, 17, 14, (short) 0, (short) 0, (short) 0);
        	mApp.getDBAccessHelper().addNewEQPreset("New Age", 16, 19, 15, 18, 16, 16, 18, (short) 0, (short) 0, (short) 0);

        }
        
        //Close the cursor.
        if (eqPresetsCursor!=null)
        	eqPresetsCursor.close();
        
	}
	
	/**
	 * Loops through a cursor of all local songs in 
	 * the library and searches for their album art.
	 */
	private void getAlbumArt() {
		
		//Get a cursor with a list of all local music files on the device.
		Cursor cursor = mApp.getDBAccessHelper().getAllLocalSongs();
		mCurrentTask = mContext.getResources().getString(R.string.building_album_art);
		
		if (cursor==null || cursor.getCount() < 1)
			return;
		
		//Tracks the progress of this method.
		int subProgress = 0;
		if (cursor.getCount()!=0) {
			subProgress = 750000/(cursor.getCount());
		} else {
			subProgress = 750000/1;
		}
		
		try {
			mApp.getDBAccessHelper().getWritableDatabase().beginTransaction();
			
			//Loop through the cursor and retrieve album art.
			for (int i=0; i < cursor.getCount(); i++) {
				
				try {
	 				cursor.moveToPosition(i);
	 				mOverallProgress += subProgress;
					publishProgress();
					
					String filePath = cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_FILE_PATH));
					
					String artworkPath = "";
					if (mApp.getSharedPreferences().getInt("ALBUM_ART_SOURCE", 0)==0 || 
						mApp.getSharedPreferences().getInt("ALBUM_ART_SOURCE", 0)==1) {
						artworkPath = getEmbeddedArtwork(filePath);
					} else {
						artworkPath = getArtworkFromFolder(filePath);
					}
						
					String normalizedFilePath = filePath.replace("'", "''");
					
					//Store the artwork file path into the DB.
					ContentValues values = new ContentValues();
					values.put(DBAccessHelper.SONG_ALBUM_ART_PATH, artworkPath);
					String where = DBAccessHelper.SONG_FILE_PATH + "='" + normalizedFilePath + "'";
					
					mApp.getDBAccessHelper().getWritableDatabase().update(DBAccessHelper.MUSIC_LIBRARY_TABLE, values, where, null);
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
				
			}
			
			mApp.getDBAccessHelper().getWritableDatabase().setTransactionSuccessful();
			mApp.getDBAccessHelper().getWritableDatabase().endTransaction();
			cursor.close();
			cursor = null;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Searchs for folder art within the specified file's 
	 * parent folder. Returns a path string to the artwork 
	 * image file if it exists. Returns an empty string 
	 * otherwise.
	 */
	public String getArtworkFromFolder(String filePath) {
		
		File file = new File(filePath);
		if (!file.exists()) {
			return "";
			
		} else {
			//Create a File that points to the parent directory of the album.
			File directoryFile = file.getParentFile();
			String directoryPath = "";
			String albumArtPath = "";
			try {
				directoryPath = directoryFile.getCanonicalPath();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			//Check if album art was already found in this directory.
			if (mFolderArtHashMap.containsKey(directoryPath))
				return mFolderArtHashMap.get(directoryPath);
			
			//Get a list of images in the album's folder.
			FileExtensionFilter IMAGES_FILTER = new FileExtensionFilter(new String[] {".jpg", ".jpeg", 
																					  ".png", ".gif"});
			File[] folderList = directoryFile.listFiles(IMAGES_FILTER);
			
			//Check if any image files were found in the folder.
			if (folderList.length==0) {
				//No images found.
				return "";
				
			} else {
				
				//Loop through the list of image files. Use the first jpeg file if it's found.
				for (int i=0; i < folderList.length; i++) {
					
					try {
						albumArtPath = folderList[i].getCanonicalPath();
						if (albumArtPath.endsWith("jpg") ||
							albumArtPath.endsWith("jpeg")) {
							
							//Add the folder's album art file to the hash.
							mFolderArtHashMap.put(directoryPath, albumArtPath);
							return albumArtPath;
						}
						
					} catch (Exception e) {
						//Skip the file if it's corrupted or unreadable.
						continue;
					}
					
				}
				
				//If an image was not found, check for gif or png files (lower priority).
				for (int i=0; i < folderList.length; i++) {
    				
    				try {
    					albumArtPath = folderList[i].getCanonicalPath();
						if (albumArtPath.endsWith("png") ||
							albumArtPath.endsWith("gif")) {

							//Add the folder's album art file to the hash.
							mFolderArtHashMap.put(directoryPath, albumArtPath);
							return albumArtPath;
						}
						
					} catch (Exception e) {
						//Skip the file if it's corrupted or unreadable.
						continue;
					}
    				
    			}
				
			}
    		
			//Add the folder's album art file to the hash.
			mFolderArtHashMap.put(directoryPath, albumArtPath);
			return "";
    	}
		
	}
		
	/**
	 * Searchs for embedded art within the specified file.
	 * Returns a path string to the artwork if it exists.
	 * Returns an empty string otherwise.
	 */
	public String getEmbeddedArtwork(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) {
			if (mApp.getSharedPreferences().getInt("ALBUM_ART_SOURCE", 0)==0) {
				return getArtworkFromFolder(filePath);
			} else {
				return "";
			}
			
		} else {
        	mMMDR.setDataSource(filePath);
        	byte[] embeddedArt = mMMDR.getEmbeddedPicture();
        	
        	if (embeddedArt!=null) {
        		return "byte://" + filePath;
        	} else {
    			if (mApp.getSharedPreferences().getInt("ALBUM_ART_SOURCE", 0)==0) {
    				return getArtworkFromFolder(filePath);
    			} else {
    				return "";
    			}
    			
        	}
        	
		}
		
	}

    /**
     * Convert millisseconds to hh:mm:ss format.
     *
     * @param milliseconds The input time in milliseconds to format.
     * @return The formatted time string.
     */
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

    	minutes = "" + minutesValue;
    	hours = "" + hoursValue;

    	String output = "";
    	if (hoursValue!=0) {
    		minutes = "0" + minutesValue;
        	hours = "" + hoursValue;
    		output = hours + ":" + minutes + ":" + seconds;
    	} else {
    		minutes = "" + minutesValue;
        	hours = "" + hoursValue;
    		output = minutes + ":" + seconds;
    	}

    	return output;
    }
	
	@Override
	protected void onProgressUpdate(String... progressParams) {
		super.onProgressUpdate(progressParams);
		if (mBuildLibraryProgressUpdate!=null)
			mBuildLibraryProgressUpdate.onProgressUpdate(mCurrentTask, mOverallProgress, 1000000);
		
	}

	@Override
	protected void onPostExecute(Void arg0) {
		//Release the wakelock.
		wakeLock.release();
		mApp.setIsBuildingLibrary(false);
		mApp.setIsScanFinished(true);
		
		if (mBuildLibraryProgressUpdate!=null)
			mBuildLibraryProgressUpdate.onFinishBuildingLibrary();

	}
	
	/**
	 * Setter methods.
	 */
	public void setOnBuildLibraryProgressUpdate(OnBuildLibraryProgressUpdate 
												 buildLibraryProgressUpdate) {
		mBuildLibraryProgressUpdate = buildLibraryProgressUpdate;
	}

}
