package com.jams.music.player.PlaylistsFragment;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Paint;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;

import com.andraskindler.quickscroll.Scrollable;
import com.jams.music.player.R;
import com.jams.music.player.AsyncTasks.AsyncAddToQueueTask;
import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.Utils.Common;

/**
 * ListView adapter for PlaylistsFragment.
 * 
 * @author Saravan PanthaM
 */
public class PlaylistsListViewAdapter extends SimpleCursorAdapter implements Scrollable {

	private Context mContext;
	private PlaylistsFragment mPlaylistsFragment;
	private Common mApp;
	
	private String mPlaylistId = "";
	private String mPlaylistName = "";
	private int mPlaylistPosition = 0;
	private String mPlaylistFilePath = "";
	
    public PlaylistsListViewAdapter(Context context, PlaylistsFragment playlistsFragment) {
        super(context, -1, playlistsFragment.getCursor(), new String[] {}, new int[] {}, 0);
        mContext = context;
        mPlaylistsFragment = playlistsFragment;
        mApp = (Common) mContext.getApplicationContext();
    }
    
    /**
     * QuickScroll implementation for this adapter.
     */
    @Override
    public String getIndicatorForPosition(int childPosition, int groupPosition) {
    	Cursor c = (Cursor) getItem(childPosition);
    	String playlistName = c.getString(c.getColumnIndex(DBAccessHelper.PLAYLIST_NAME));
    	if (playlistName!=null && playlistName.length() > 1)
    		return "  " + playlistName.substring(0, 2) + "  ";
    	else if (playlistName!=null && playlistName.length() > 0)
    		return "  " + playlistName.substring(0, 1) + "  ";
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
	 * Constructs the row/child view for this adapter.
	 */
    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
        Cursor c = (Cursor) getItem(position);
	    PlaylistsListViewHolder holder = null;

		if (convertView == null) {
			
			if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME") || 
				mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.playlists_list_cards_layout, parent, false);
			} else {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.playlists_list_layout, parent, false);
			}
			
			holder = new PlaylistsListViewHolder();
			holder.playlistImage = (ImageView) convertView.findViewById(R.id.playlistImage);
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
		    holder = (PlaylistsListViewHolder) convertView.getTag();
		}
		
		//Hide the song count for now.
		holder.songCount.setVisibility(View.GONE);
		
		//Apply the card layout's background based on the color theme.
		if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME")) {
			convertView.setBackgroundResource(R.drawable.card_light);
		} else if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
			convertView.setBackgroundResource(R.drawable.card_dark);
		}
		
		String playlistSource = c.getString(c.getColumnIndex(DBAccessHelper.PLAYLIST_SOURCE));
		String playlistId = c.getString(c.getColumnIndex(DBAccessHelper.PLAYLIST_ID));
		String playlistName = c.getString(c.getColumnIndex(DBAccessHelper.PLAYLIST_NAME));
		String playlistFilePath = c.getString(c.getColumnIndex(DBAccessHelper.PLAYLIST_FILE_PATH));
		
		//Set the tag for this child view. The key is required to be an application-defined key.
		convertView.setTag(R.string.playlist_name, playlistName);
		convertView.setTag(R.string.playlist_file_path, playlistFilePath);
		convertView.setTag(R.string.playlist_folder_path, c.getString(c.getColumnIndex(DBAccessHelper.PLAYLIST_FOLDER_PATH)));
		convertView.setTag(R.string.playlist_source, playlistSource);
		convertView.setTag(R.string.playlist_id, playlistId);

		holder.title.setText(playlistName);
		holder.overflowButton.setTag(R.string.playlist_id, playlistId);
		holder.overflowButton.setTag(R.string.playlist_name, playlistName);
		holder.overflowButton.setTag(R.string.position, position);
		holder.overflowButton.setTag(R.string.playlist_file_path, playlistFilePath);
		
		if (position==0) {
			holder.playlistImage.setImageResource(UIElementsHelper.getIcon(mContext, "top_25_played"));
			holder.playlistImage.setScaleType(ScaleType.CENTER_INSIDE);
			return convertView;
		} else if (position==1) {
			holder.playlistImage.setImageResource(UIElementsHelper.getIcon(mContext, "recently_added"));
			holder.playlistImage.setScaleType(ScaleType.CENTER_INSIDE);
			return convertView;
		} else if (position==2) {
			holder.playlistImage.setImageResource(UIElementsHelper.getIcon(mContext, "top_rated"));
			holder.playlistImage.setScaleType(ScaleType.CENTER_INSIDE);
			return convertView;
		} else if (position==3) {
			holder.playlistImage.setImageResource(UIElementsHelper.getIcon(mContext, "recently_played"));
			holder.playlistImage.setScaleType(ScaleType.CENTER_INSIDE);
			return convertView;
		} else if (position >= 4) {
			String songId = c.getString(c.getColumnIndex(DBAccessHelper.SONG_ID));
			String albumArtPath = mApp.getDBAccessHelper().getAlbumArtBySongId(songId);
			mApp.getImageLoader().displayImage(albumArtPath, holder.playlistImage, mApp.getDisplayImageOptions());
			return convertView;
		}
		
		return convertView;
	
	}
    
    /**
     * Click listener for the overflow menu button.
     */
    private OnClickListener overflowClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			PopupMenu menu = new PopupMenu(mContext, v);
			menu.inflate(R.menu.playlist_overflow_menu);
			menu.setOnMenuItemClickListener(popupMenuItemClickListener);
			mPlaylistId = (String) v.getTag(R.string.playlist_id);
			mPlaylistName = (String) v.getTag(R.string.playlist_name);
			mPlaylistPosition = (Integer) v.getTag(R.string.position);
			mPlaylistFilePath = (String) v.getTag(R.string.playlist_file_path);
		    menu.show();
			
		}
    	
    };
    
    /**
     * Menu item click listener for the overflow popup menu.
     */
    private OnMenuItemClickListener popupMenuItemClickListener = new OnMenuItemClickListener() {

		@Override
		public boolean onMenuItemClick(MenuItem item) {
			
			switch(item.getItemId()) {
        	case R.id.add_to_queue:
        		//Add to queue.
        		if (mPlaylistPosition==0) {
        			AsyncAddToQueueTask addToQueueTask = new AsyncAddToQueueTask(mContext,
																				 mPlaylistsFragment,
																				 "TOP_25_PLAYED",
																				 null,
																				 null,
																				 null, 
																				 null, 
																				 mPlaylistId, 
																				 mPlaylistName,
																				 null);
        			addToQueueTask.execute();
        		} else if (mPlaylistPosition==1) {
        			AsyncAddToQueueTask addToQueueTask = new AsyncAddToQueueTask(mContext,
																				 mPlaylistsFragment,
																				 "RECENTLY_ADDED",
																				 null,
																				 null,
																				 null, 
																				 null, 
																				 mPlaylistId, 
																				 mPlaylistName,
																				 null);
        			addToQueueTask.execute();
        		} else if (mPlaylistPosition==2) {
        			AsyncAddToQueueTask addToQueueTask = new AsyncAddToQueueTask(mContext,
																				 mPlaylistsFragment,
																				 "TOP_RATED",
																				 null,
																				 null,
																				 null, 
																				 null, 
																				 mPlaylistId, 
																				 mPlaylistName,
																				 null);
        			addToQueueTask.execute();
        		} else if (mPlaylistPosition==3) {
        			AsyncAddToQueueTask addToQueueTask = new AsyncAddToQueueTask(mContext,
																				 mPlaylistsFragment,
																				 "RECENTLY_PLAYED",
																				 null,
																				 null,
																				 null, 
																				 null, 
																				 mPlaylistId, 
																				 mPlaylistName,
																				 null);
        			addToQueueTask.execute();
        		} else {
        			AsyncAddToQueueTask addToQueueTask = new AsyncAddToQueueTask(mContext,
																				 mPlaylistsFragment,
																				 "PLAYLIST",
																				 null,
																				 null,
																				 null, 
																				 null, 
																				 mPlaylistId, 
																				 mPlaylistName,
																				 null);
        			addToQueueTask.execute();
        		}
        		
        		break;
        	case R.id.play_next:
        		//Add to queue.
        		if (mPlaylistPosition==0) {
        			AsyncAddToQueueTask addToQueueTask = new AsyncAddToQueueTask(mContext,
																				 mPlaylistsFragment,
																				 "TOP_25_PLAYED",
																				 null,
																				 null,
																				 null, 
																				 null, 
																				 mPlaylistId, 
																				 mPlaylistName,
																				 null);
        			addToQueueTask.execute(new Boolean [] { true });
        		} else if (mPlaylistPosition==1) {
        			AsyncAddToQueueTask addToQueueTask = new AsyncAddToQueueTask(mContext,
																				 mPlaylistsFragment,
																				 "RECENTLY_ADDED",
																				 null,
																				 null,
																				 null, 
																				 null, 
																				 mPlaylistId, 
																				 mPlaylistName,
																				 null);
        			addToQueueTask.execute(new Boolean [] { true });
        		} else if (mPlaylistPosition==2) {
        			AsyncAddToQueueTask addToQueueTask = new AsyncAddToQueueTask(mContext,
																				 mPlaylistsFragment,
																				 "TOP_RATED",
																				 null,
																				 null,
																				 null, 
																				 null, 
																				 mPlaylistId, 
																				 mPlaylistName,
																				 null);
        			addToQueueTask.execute(new Boolean [] { true });
        		} else if (mPlaylistPosition==3) {
        			AsyncAddToQueueTask addToQueueTask = new AsyncAddToQueueTask(mContext,
																				 mPlaylistsFragment,
																				 "RECENTLY_PLAYED",
																				 null,
																				 null,
																				 null, 
																				 null, 
																				 mPlaylistId, 
																				 mPlaylistName,
																				 null);
        			addToQueueTask.execute(new Boolean [] { true });
        		} else {
        			AsyncAddToQueueTask addToQueueTask = new AsyncAddToQueueTask(mContext,
																				 mPlaylistsFragment,
																				 "PLAYLIST",
																				 null,
																				 null,
																				 null, 
																				 null, 
																				 mPlaylistId, 
																				 mPlaylistName,
																				 null);
        			addToQueueTask.execute(new Boolean [] { true });
        		}
        		
        		break;
        	case R.id.delete_playlist:
        		//Delete Playlist.
/*				DeletePlaylistWarningDialog dialog = new DeletePlaylistWarningDialog();
				Bundle bundle = new Bundle();
				bundle.putString("PLAYLIST_ID", mPlaylistId);
				bundle.putString("PLAYLIST_FILE_PATH", mPlaylistFilePath);
				dialog.setArguments(bundle);
				dialog.show(mPlaylistsFragment.getChildFragmentManager().beginTransaction(), "deletePlaylistDialog");*/
				break;
            }
    		
			return false;
		}
    	
    };

    /**
     * Holder subclass for PlaylistsListViewAdapter.
     * 
     * @author Saravan Pantham
     */
	static class PlaylistsListViewHolder {
	    public ImageView playlistImage;
	    public TextView title;
	    public TextView songCount;
	    public ImageButton overflowButton;
	}
	
}
