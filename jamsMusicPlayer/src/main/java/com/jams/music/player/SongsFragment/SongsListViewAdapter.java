package com.jams.music.player.SongsFragment;

import android.content.Context;
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
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.andraskindler.quickscroll.Scrollable;
import com.jams.music.player.R;
import com.jams.music.player.AsyncTasks.AsyncAddToQueueTask;
import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.Dialogs.AddToPlaylistDialog;
import com.jams.music.player.Dialogs.EditGooglePlayMusicTagsDialog;
import com.jams.music.player.Dialogs.ID3sSongEditorDialog;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.Utils.Common;

public class SongsListViewAdapter extends SimpleCursorAdapter implements Scrollable {
	
	private Context mContext;
	private Common mApp;
	private SongsFragment mSongsFragment;
	
	private String mSongTitle = "";
	private String mSongArtist = "";
	private String mSongAlbum = "";
	private String mSongSource = "";
	private String mSongFilePath = "";
	private String mSongId = "";
	
    public SongsListViewAdapter(Context context, SongsFragment songsFragment) {
        super(context, -1, songsFragment.getCursor(), new String[] {}, new int[] {}, 0);
        mContext = context;
        mSongsFragment = songsFragment;
        mApp = (Common) mContext.getApplicationContext();
    }
    
    /**
     * QuickScroll implementation for this adapter.
     */
    @Override
    public String getIndicatorForPosition(int childPosition, int groupPosition) {
    	Cursor c = (Cursor) getItem(childPosition);
    	String songTitle = c.getString(c.getColumnIndex(DBAccessHelper.SONG_TITLE));
    	if (songTitle!=null && songTitle.length() > 1)
    		return "  " + songTitle.substring(0, 2) + "  ";
    	else if (songTitle!=null && songTitle.length() > 0)
    		return "  " + songTitle.substring(0, 1) + "  ";
    	else
    		return "  N/A  ";
    }
    
    /**
     * Returns the position of the top view in the adapter.
     */
	@Override
	public int getScrollPosition(int childPosition, int groupPosition) {
		// TODO Auto-generated method stub
		return childPosition;
	}

	/**
	 * Constructs the row/child view for the given position in the adapter.
	 */
    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
        Cursor c = (Cursor) getItem(position);
	    SongsListViewHolder holder = null;

		if (convertView == null) {
			
			if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME") || 
				mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.songs_list_view_cards_layout, parent, false);
			} else {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.songs_list_view_layout, parent, false);
			}
			
			//Apply the card layout's background based on the color theme.
			if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME")) {
				convertView.setBackgroundResource(R.drawable.card_light);
			} else if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
				convertView.setBackgroundResource(R.drawable.card_dark);
			}
			
			holder = new SongsListViewHolder();
			holder.image = (ImageView) convertView.findViewById(R.id.songsListAlbumThumbnail);
			holder.title = (TextView) convertView.findViewById(R.id.songNameListView);
			holder.artist = (TextView) convertView.findViewById(R.id.artistNameSongListView);
			holder.duration = (TextView) convertView.findViewById(R.id.songDurationListView);

			holder.title.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
			
			holder.title.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Light"));
			holder.artist.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Regular"));
			holder.duration.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Regular"));
			
			holder.title.setPaintFlags(holder.title.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
			holder.artist.setPaintFlags(holder.artist.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
			holder.duration.setPaintFlags(holder.duration.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
			
			holder.overflowButton = (ImageButton) convertView.findViewById(R.id.overflow_icon);
			holder.overflowButton.setOnClickListener(overflowClickListener);
			holder.overflowButton.setFocusable(false);
			holder.overflowButton.setFocusableInTouchMode(false);
			
			convertView.setTag(holder);
		} else {
		    holder = (SongsListViewHolder) convertView.getTag();
		}
		
		String songFilePath = c.getString(c.getColumnIndex(DBAccessHelper.SONG_FILE_PATH));
		String songAlbumArtPath = c.getString(c.getColumnIndex(DBAccessHelper.SONG_ALBUM_ART_PATH));
		String songSource = c.getString(c.getColumnIndex(DBAccessHelper.SONG_SOURCE));
		String songTitle = c.getString(c.getColumnIndex(DBAccessHelper.SONG_TITLE));
		String songArtist = c.getString(c.getColumnIndex(DBAccessHelper.SONG_ARTIST));
		String songAlbum = c.getString(c.getColumnIndex(DBAccessHelper.SONG_ALBUM));
		
		holder.overflowButton.setTag(R.string.title, songTitle);
		holder.overflowButton.setTag(R.string.artist, songArtist);
		holder.overflowButton.setTag(R.string.album, songAlbum);
		holder.overflowButton.setTag(R.string.song_source, songSource);
		holder.overflowButton.setTag(R.string.song_file_path, songFilePath);
		
		//Set the song title.
		holder.title.setText(songTitle);

        mApp.getImageLoader().displayImage(songAlbumArtPath, holder.image, mApp.getDisplayImageOptions());
        
        //Set the artist.
        holder.artist.setText(songArtist);
        
        //Set the duration of the song.
        long songDurationInMillis = 0;
        try {
          songDurationInMillis = Long.parseLong(c.getString(c.getColumnIndex(DBAccessHelper.SONG_DURATION)));
        } catch (Exception e) {
        	e.printStackTrace();
        	songDurationInMillis = 0;
        }
        
		holder.duration.setText(convertMillisToMinsSecs(songDurationInMillis));
		return convertView;
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
    
    /**
     * Click listener for the overflow menu.
     */
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
    
    /**
     * Menu item click listener for the popup menu.
     */
    private OnMenuItemClickListener popupMenuItemClickListener = new OnMenuItemClickListener() {

		@Override
		public boolean onMenuItemClick(MenuItem item) {
			
    		switch(item.getItemId()) {
        	case R.id.edit_song_tags:
        		//Edit Song Tags.
        		//Check if the current song is from Google Play Music.
        		if (mSongSource.equals(DBAccessHelper.GMUSIC)) {
        			FragmentTransaction transaction = mSongsFragment.getFragmentManager().beginTransaction();
        			EditGooglePlayMusicTagsDialog dialog = new EditGooglePlayMusicTagsDialog();
        			dialog.show(transaction, "editGooglePlayMusicTagsDialog");
        		} else {
        			FragmentTransaction transaction = mSongsFragment.getFragmentManager().beginTransaction();
            		Bundle bundle = new Bundle();
            		bundle.putString("EDIT_TYPE", "SONG");
            		bundle.putString("SONG", mSongFilePath);
            		bundle.putString("CALLING_FRAGMENT", "SONGS_FRAGMENT");
            		ID3sSongEditorDialog dialog = new ID3sSongEditorDialog();
            		dialog.setArguments(bundle);
            		dialog.show(transaction, "id3EditorDialog");
        		}
        		
        		break;
        	case R.id.add_to_queue: 
        		//Add to Queue.
        		AsyncAddToQueueTask task = new AsyncAddToQueueTask(mContext, 
        														   mSongsFragment, 
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
        		//Add to Queue.
        		AsyncAddToQueueTask playNextTask = new AsyncAddToQueueTask(mContext, 
		        														   mSongsFragment, 
		        														   "SONG",
		        														   mSongArtist, 
		        														   mSongAlbum, 
		        														   mSongTitle,
		        														   null,
		        														   null,
		        														   null,
		        														   null);
        		playNextTask.execute(new Boolean [] { true });
        		break;
        	case R.id.add_to_playlist:
        		//Add to Playlist
        		FragmentTransaction ft = mSongsFragment.getFragmentManager().beginTransaction();
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
        		mSongsFragment.mHandler.post(mSongsFragment.queryRunnable);
				mSongsFragment.getListViewAdapter().notifyDataSetChanged();
				
        		break;
    		}
    		
			return false;
		}
    	
    };
   
    /**
     * Holder subclass for SongsListViewAdapter.
     * 
     * @author Saravan Pantham
     */
	static class SongsListViewHolder {
	    public ImageView image;
	    public TextView title;
	    public TextView artist;
	    public TextView duration;
	    public ImageButton overflowButton;

	}
	
}
