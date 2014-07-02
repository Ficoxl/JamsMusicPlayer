package com.jams.music.player.GenresFragment;

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

import com.andraskindler.quickscroll.Scrollable;
import com.jams.music.player.R;
import com.jams.music.player.AsyncTasks.AsyncAddToQueueTask;
import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.Dialogs.AddToPlaylistDialog;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.Utils.Common;

/**
 * ListView adapter for GenresFragment.
 * 
 * @author Saravan Pantham
 */
public class GenresListViewAdapter extends SimpleCursorAdapter implements Scrollable {
	
	private Context mContext;
	private GenresFragment mGenresFragment;
	private Common mApp;
	private String mGenreName = "";
	
    public GenresListViewAdapter(Context context, GenresFragment genresFragment) {
        super(context, -1, genresFragment.getCursor(), new String[] {}, new int[] {}, 0);
        mContext = context;
        mGenresFragment = genresFragment;
        mApp = (Common) mContext.getApplicationContext();
    }

    /**
     * QuickScroll implemenation for this adapter.
     */
    @Override
    public String getIndicatorForPosition(int childPosition, int groupPosition) {
    	Cursor c = (Cursor) getItem(childPosition);
    	String songTitle = c.getString(c.getColumnIndex(DBAccessHelper.SONG_GENRE));
    	if (songTitle!=null && songTitle.length() > 1)
    		return "  " + songTitle.substring(0, 2) + "  ";
    	else if (songTitle!=null && songTitle.length() > 0)
    		return "  " + songTitle.substring(0, 1) + "  ";
    	else
    		return "  N/A  ";
    }
    
    /**
     * Returns the position of the top view in this adapter.
     */
	@Override
	public int getScrollPosition(int childPosition, int groupPosition) {
		// TODO Auto-generated method stub
		return childPosition;
	}
    
    /**
     * Constructs the row/child view for the given position in this adapter.
     * We'll just reuse the playlist fragment's UI elements here since the genre's elements are the same.
     */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        Cursor c = (Cursor) getItem(position);
	    GenresListViewHolder holder = null;

		if (convertView == null) {
			
			if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME") || 
				mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.playlists_list_cards_layout, parent, false);
			} else {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.playlists_list_layout, parent, false);
			}
			
			holder = new GenresListViewHolder();
			holder.genreImage = (ImageView) convertView.findViewById(R.id.playlistImage);
			holder.title = (TextView) convertView.findViewById(R.id.playlistListViewName);
			holder.songCount = (TextView) convertView.findViewById(R.id.playlistNumberOfSongs);

			holder.title.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
			holder.title.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Light"));
			holder.title.setPaintFlags(holder.title.getPaintFlags() | 
									   Paint.SUBPIXEL_TEXT_FLAG | 
									   Paint.ANTI_ALIAS_FLAG);

			holder.songCount.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
			holder.songCount.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
			holder.songCount.setPaintFlags(holder.songCount.getPaintFlags() | 
									   	   Paint.SUBPIXEL_TEXT_FLAG | 
									   	   Paint.ANTI_ALIAS_FLAG);
			
			holder.overflowButton = (ImageButton) convertView.findViewById(R.id.overflow_icon);
			holder.overflowButton.setOnClickListener(overflowClickListener);
			holder.overflowButton.setFocusable(false);
			holder.overflowButton.setFocusableInTouchMode(false);
			
			convertView.setTag(holder);
		} else {
		    holder = (GenresListViewHolder) convertView.getTag();
		}
		
		//Apply the card layout's background based on the color theme.
		if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME")) {
			convertView.setBackgroundResource(R.drawable.card_light);
		} else if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
			convertView.setBackgroundResource(R.drawable.card_dark);
		}
		
		//Set the tag for this child view. The key is required to be an application-defined key.
		convertView.setTag(R.string.genre, c.getString(c.getColumnIndex(DBAccessHelper.SONG_GENRE)));

		String title = c.getString(c.getColumnIndex(DBAccessHelper.SONG_GENRE));
		String songSource = c.getString(c.getColumnIndex(DBAccessHelper.SONG_SOURCE));
		String songAlbumArtPath = c.getString(c.getColumnIndex(DBAccessHelper.SONG_ALBUM_ART_PATH));
		String songFilePath = c.getString(c.getColumnIndex(DBAccessHelper.SONG_FILE_PATH));
		
		convertView.setTag(R.string.song_file_path, songFilePath);
		convertView.setTag(R.string.album_art, songAlbumArtPath);
		convertView.setTag(R.string.song_source, songSource);

		int songCount;
		try {
			songCount = Integer.parseInt(c.getString(c.getColumnIndex(DBAccessHelper.GENRE_SONG_COUNT)));
		} catch (Exception e) {
			songCount = 0;
		}
		
		if (title==null || title.equals(null) || title.isEmpty()) {
			title = "No Genre";
		}
		
		holder.title.setText(title);
		holder.overflowButton.setTag(R.string.genre, title);
		
		if (songCount==0 || songCount > 1) {
			holder.songCount.setText(songCount + " songs");
		} else {
			holder.songCount.setText(songCount + " song");
		}
		
		mApp.getImageLoader().displayImage(songAlbumArtPath, holder.genreImage, mApp.getDisplayImageOptions());
        
		return convertView;
	}
	
	/**
	 * Click listener for the overflow menu button.
	 */
	private OnClickListener overflowClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			PopupMenu menu = new PopupMenu(mContext, v);
			menu.inflate(R.menu.genre_overflow_menu);
			menu.setOnMenuItemClickListener(popupMenuItemClickListener);
			mGenreName = (String) v.getTag(R.string.genre);
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
        	case R.id.add_to_queue: 
        		//Add to Queue.
        		AsyncAddToQueueTask task = new AsyncAddToQueueTask(mContext,
        														   mGenresFragment,
        														   "GENRE",
        														   null,
        														   null,
        														   null, 
        														   mGenreName, 
        														   null,
        														   null,
        														   null);
        		task.execute();
        		break;
        	case R.id.play_next: 
        		//Add to Queue.
        		AsyncAddToQueueTask playNextTask = new AsyncAddToQueueTask(mContext,
        														   mGenresFragment,
        														   "GENRE",
        														   null,
        														   null,
        														   null, 
        														   mGenreName, 
        														   null,
        														   null,
        														   null);
        		playNextTask.execute(new Boolean[] { true });
        		break;
        	case R.id.add_to_playlist:
        		//Add to Playlist
        		FragmentTransaction ft = mGenresFragment.getFragmentManager().beginTransaction();
				AddToPlaylistDialog dialog = new AddToPlaylistDialog();
				Bundle bundle = new Bundle();
				bundle.putString("ADD_TYPE", "GENRE");
				bundle.putString("GENRE", mGenreName);
				dialog.setArguments(bundle);
				dialog.show(ft, "AddToPlaylistDialog");
				break;
        	
			}
			
			return false;
		}
    	
    };

    /**
     * Holder subclass for GenresListViewAdapter.
     * 
     * @author Saravan Pantham
     */
	static class GenresListViewHolder {
	    public ImageView genreImage;
	    public TextView title;
	    public TextView songCount;
	    public ImageButton overflowButton;
	}
	
}
