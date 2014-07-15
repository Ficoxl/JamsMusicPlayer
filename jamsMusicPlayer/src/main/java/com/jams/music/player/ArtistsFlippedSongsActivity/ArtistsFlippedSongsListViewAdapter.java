package com.jams.music.player.ArtistsFlippedSongsActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.jams.music.player.R;
import com.jams.music.player.AsyncTasks.AsyncAddToQueueTask;
import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.Dialogs.AddToPlaylistDialog;
import com.jams.music.player.Dialogs.EditGooglePlayMusicTagsDialog;
import com.jams.music.player.Dialogs.ID3sSongEditorDialog;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.Utils.Common;

public class ArtistsFlippedSongsListViewAdapter extends SimpleCursorAdapter {
	
	private Context mContext;
	private Common mApp;
	private SharedPreferences sharedPreferences;
	
	private String mSongTitle = "";
	private String mSongArtist = "";
	private String mSongAlbum = "";
	private String mSongSource = "";
	private String mSongFilePath = "";
	private String mSongId = "";
	
    public ArtistsFlippedSongsListViewAdapter(Context context, Cursor cursor) {
        super(context, -1, cursor, new String[] {}, new int[] {}, 0);
        
        mContext = context;
        mApp = (Common) context.getApplicationContext();
        sharedPreferences = context.getSharedPreferences("com.jams.music.player", Context.MODE_PRIVATE);
        
    }

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
        Cursor c = (Cursor) getItem(position);
	    ArtistsFlippedSongsListViewHolder holder = null;
	    
		if (convertView == null) {
			
			if (sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME") ||
				sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.albums_flipped_list_view_cards_layout, parent, false);
			} else {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.albums_flipped_list_view_layout, parent, false);
			}

			holder = new ArtistsFlippedSongsListViewHolder();
			holder.trackNumber = (TextView) convertView.findViewById(R.id.songsListAlbumsFlippedTrack);
			holder.title = (TextView) convertView.findViewById(R.id.songNameAlbumsFlippedListView);
			holder.artist = (TextView) convertView.findViewById(R.id.artistNameSongAlbumsFlippedListView);
			holder.duration = (TextView) convertView.findViewById(R.id.songDurationAlbumsFlippedListView);

			holder.trackNumber.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
			holder.title.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Light"));
			holder.artist.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
			holder.duration.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
			
			holder.trackNumber.setPaintFlags(holder.trackNumber.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
			holder.title.setPaintFlags(holder.title.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
			holder.artist.setPaintFlags(holder.artist.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
			holder.duration.setPaintFlags(holder.duration.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
			
			holder.overflowButton = (ImageButton) convertView.findViewById(R.id.overflow_icon);
			holder.overflowButton.setOnClickListener(overflowClickListener);
			holder.overflowButton.setFocusable(false);
			holder.overflowButton.setFocusableInTouchMode(false);
			
			convertView.setTag(holder);
		} else {
		    holder = (ArtistsFlippedSongsListViewHolder) convertView.getTag();
		}
		
		//Apply the card layout's background based on the color theme.
		if (sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME")) {
			convertView.setBackgroundResource(R.drawable.card_light);
		} else if (sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
			convertView.setBackgroundResource(R.drawable.card_dark);
		}
		
		String songFilePath = c.getString(c.getColumnIndex(DBAccessHelper.SONG_FILE_PATH));
		String songTitle =  c.getString(c.getColumnIndex(DBAccessHelper.SONG_TITLE));
		String songArtist = c.getString(c.getColumnIndex(DBAccessHelper.SONG_ARTIST));
		String songAlbum = c.getString(c.getColumnIndex(DBAccessHelper.SONG_ALBUM));
		String songSource = c.getString(c.getColumnIndex(DBAccessHelper.SONG_SOURCE));
		String songDuration = c.getString(c.getColumnIndex(DBAccessHelper.SONG_DURATION));
		String songId = c.getString(c.getColumnIndex(DBAccessHelper.SONG_ID));
		
		//Set the tag of the child view with the song's file path.
		convertView.setTag(R.string.song_file_path, songFilePath);
		convertView.setTag(R.string.title, songTitle);
		convertView.setTag(R.string.artist, songArtist);
		convertView.setTag(R.string.album, songAlbum);
		convertView.setTag(R.string.duration, songDuration);
		convertView.setTag(R.string.genre, c.getString(c.getColumnIndex(DBAccessHelper.SONG_GENRE)));
		convertView.setTag(R.string.song_source, songSource);
		convertView.setTag(R.string.song_id, songId);

		//Set the album name.
		holder.title.setText(c.getString(c.getColumnIndex(DBAccessHelper.SONG_TITLE)));
		
		String trackNumber = c.getString(c.getColumnIndex(DBAccessHelper.SONG_TRACK_NUMBER));
		
		//Set the track number. If none exists, leave it blank.
		if (trackNumber==null || trackNumber.equals("") || trackNumber.equals("0")) {
			holder.trackNumber.setText(" ");
		} else {
			
			//Check for bogus track numbers.
			if (trackNumber.length() > 3) {
				trackNumber = Character.toString(trackNumber.charAt(1)) +
							  Character.toString(trackNumber.charAt(2)) +
							  Character.toString(trackNumber.charAt(3));
							  
				if (Character.toString(trackNumber.charAt(0)).equals("0") && 
					Character.toString(trackNumber.charAt(1)).equals("0")) {
					
					trackNumber = Character.toString(trackNumber.charAt(2));
					
				} else if (Character.toString(trackNumber.charAt(0)).equals("0") && 
						   !Character.toString(trackNumber.charAt(1)).equals("0")) {
					trackNumber = Character.toString(trackNumber.charAt(1)) +
								  Character.toString(trackNumber.charAt(2));
				}
				
			}
			
			holder.trackNumber.setText(trackNumber);
		}
        
        //Set the artist name.
        holder.artist.setText(c.getString(c.getColumnIndex(DBAccessHelper.SONG_ARTIST)));
        
		holder.overflowButton.setTag(R.string.title, songTitle);
		holder.overflowButton.setTag(R.string.artist, songArtist);
		holder.overflowButton.setTag(R.string.album, songAlbum);
		holder.overflowButton.setTag(R.string.song_source, songSource);
		holder.overflowButton.setTag(R.string.song_file_path, songFilePath);
		holder.overflowButton.setTag(R.string.song_id, songId);
        
        //Set the duration.
        long duration = 0;
        try {
        	duration = Long.parseLong(songDuration);
        } catch (NumberFormatException e) {
        	e.printStackTrace();
        	duration = 0;
        }
        holder.duration.setText(convertMillisToMinsSecs(duration));
		
		return convertView;
	}
    
	//Convert millisseconds to hh:mm:ss format.
    private String convertMillisToMinsSecs(long milliseconds) {
    	
    	int secondsValue = (int) (milliseconds / 1000) % 60 ;
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
    
    private OnClickListener overflowClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			PopupMenu menu = new PopupMenu(mContext, v);
			menu.inflate(R.menu.song_overflow_menu);
			menu.setOnMenuItemClickListener(popupMenuItemClickListener);
			mSongAlbum = (String) v.getTag(R.string.album);
			mSongTitle = (String) v.getTag(R.string.title);
			mSongArtist = (String) v.getTag(R.string.artist);
			mSongSource = (String) v.getTag(R.string.song_source);
			mSongFilePath = (String) v.getTag(R.string.song_file_path);
			mSongId = (String) v.getTag(R.string.song_id);
		    menu.show();
			
		}
    	
    };
    
    private OnMenuItemClickListener popupMenuItemClickListener = new OnMenuItemClickListener() {

		@Override
		public boolean onMenuItemClick(MenuItem item) {
			
    		switch(item.getItemId()) {
        	case R.id.edit_song_tags:
        		//Edit Song Tags.
        		//Check if the current song is from Google Play Music.
        		if (mSongSource.equals(DBAccessHelper.GMUSIC)) {
        			FragmentTransaction transaction = ArtistsFlippedSongsFragment.fragment.getFragmentManager().beginTransaction();
        			EditGooglePlayMusicTagsDialog dialog = new EditGooglePlayMusicTagsDialog();
        			dialog.show(transaction, "editGooglePlayMusicTagsDialog");
        		} else {
        			FragmentTransaction transaction = ArtistsFlippedSongsFragment.fragment.getFragmentManager().beginTransaction();
            		Bundle bundle = new Bundle();
            		bundle.putString("EDIT_TYPE", "SONG");
            		bundle.putString("SONG", mSongFilePath);
            		bundle.putString("CALLING_FRAGMENT", "ARTISTS_FLIPPED_SONGS_FRAGMENT");
            		ID3sSongEditorDialog dialog = new ID3sSongEditorDialog();
            		dialog.setArguments(bundle);
            		dialog.show(transaction, "id3EditorDialog");
        		}
        		
        		break;
        	case R.id.add_to_queue: 
        		//Add to Queue.
        		AsyncAddToQueueTask task = new AsyncAddToQueueTask(mContext, 
        														   ArtistsFlippedSongsFragment.fragment, 
        														   "SONG",
        														   mSongArtist, 
        														   mSongAlbum, 
        														   mSongTitle,
        														   null,
        														   null,
        														   null,
        														   null);
        		task.execute();
        		break;
        	case R.id.play_next: 
        		//Play next.
        		AsyncAddToQueueTask playNextTask = new AsyncAddToQueueTask(mContext, 
		        														   ArtistsFlippedSongsFragment.fragment, 
		        														   "SONG",
		        														   mSongArtist, 
		        														   mSongAlbum, 
		        														   mSongTitle,
		        														   null,
		        														   null,
		        														   null,
		        														   null);
        		playNextTask.execute(new Boolean[] { true });
        		break;
        	case R.id.add_to_playlist:
        		//Add to Playlist
        		FragmentTransaction ft = ArtistsFlippedSongsFragment.fragment.getFragmentManager().beginTransaction();
				AddToPlaylistDialog playlistDialog = new AddToPlaylistDialog();
				Bundle playlistBundle = new Bundle();
				playlistBundle.putString("ADD_TYPE", "SONG");
				playlistBundle.putString("ARTIST", mSongArtist);
				playlistBundle.putString("ALBUM", mSongAlbum);
				playlistBundle.putString("SONG", mSongTitle);
				playlistDialog.setArguments(playlistBundle);
				playlistDialog.show(ft, "AddToPlaylistDialog");
        		break;
        	case R.id.blacklist_song:
        		//Blacklist song.
        		mApp.getDBAccessHelper().setBlacklistForSong(mSongId, true);
        		Toast.makeText(mContext, R.string.song_blacklisted, Toast.LENGTH_SHORT).show();
        		
        		//Update the Listview.
				ArtistsFlippedSongsFragment.songsListViewAdapter.notifyDataSetChanged();
				
        		break;
    		}
    		
			return false;
		}
    	
    };

	class ArtistsFlippedSongsListViewHolder {
	    public TextView trackNumber;
	    public TextView title;
	    public TextView artist;
	    public TextView duration;
	    public ImageButton overflowButton;
	}
	
}
