package com.jams.music.player.ArtistsFragment;

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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andraskindler.quickscroll.Scrollable;
import com.jams.music.player.R;
import com.jams.music.player.AsyncTasks.AsyncAddToQueueTask;
import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.Dialogs.AddToPlaylistDialog;
import com.jams.music.player.Dialogs.CautionEditArtistsDialog;
import com.jams.music.player.Dialogs.ID3sArtistEditorDialog;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.Utils.Common;

/**
 * ListView adapter for mArtistsFragment.
 * 
 * @author Saravan Pantham
 */
public class ArtistsListViewAdapter extends SimpleCursorAdapter implements Scrollable {
	
	private Context mContext;
	private ArtistsFragment mArtistsFragment;
	private Common mApp;
    private ArtistsListViewHolder mHolder = null;
    private String mArtistName = "";
    
    public ArtistsListViewAdapter(Context context, ArtistsFragment artistsFragment) {
        super(context, -1, artistsFragment.getCursor(), new String[] {}, new int[] {}, 0);
        mContext = context;
        mApp = (Common) mContext.getApplicationContext();
        mArtistsFragment = artistsFragment;
        
    }
    
    /**
     * QuickScroll indicator implementation.
     */
    @Override
    public String getIndicatorForPosition(int childPosition, int groupPosition) {
    	Cursor c = (Cursor) getItem(childPosition);
    	String artist = c.getString(c.getColumnIndex(DBAccessHelper.SONG_ARTIST));
    	if (artist!=null && artist.length() > 1)
    		return "  " + artist.substring(0, 2) + "  ";
    	else if (artist!=null && artist.length() > 0)
    		return "  " + artist.substring(0, 1) + "  ";
    	else
    		return "  N/A  ";
    }
    
    /**
     * Returns the current position of the top view in the list/grid.
     */
	@Override
	public int getScrollPosition(int childPosition, int groupPosition) {
		// TODO Auto-generated method stub
		return childPosition;
	}

	/**
	 * Returns the individual row/child in the list/grid.
	 */
    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
        Cursor c = (Cursor) getItem(position);

		if (convertView == null) {
			
			if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME") || 
				mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.artists_list_view_cards_layout, parent, false);
			} else {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.artists_list_view_layout, parent, false);
			}

			mHolder = new ArtistsListViewHolder();
			mHolder.listViewArt = (ImageView) convertView.findViewById(R.id.artistsListAlbumThumbnail);
			mHolder.title = (TextView) convertView.findViewById(R.id.artistName);
			mHolder.numberAlbums = (TextView) convertView.findViewById(R.id.artistNumberAlbums);
			mHolder.selector = (RelativeLayout) convertView.findViewById(R.id.selector);

			mHolder.title.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
			mHolder.title.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Light"));
			mHolder.title.setPaintFlags(mHolder.title.getPaintFlags() | 
									   Paint.SUBPIXEL_TEXT_FLAG | 
									   Paint.ANTI_ALIAS_FLAG);
			
			mHolder.numberAlbums.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Regular"));
			mHolder.numberAlbums.setPaintFlags(mHolder.title.getPaintFlags() | 
									   Paint.SUBPIXEL_TEXT_FLAG | 
									   Paint.ANTI_ALIAS_FLAG);
			
	        mHolder.listViewArt.setScaleType(ImageView.ScaleType.CENTER_CROP);
			
	        mHolder.overflowButton = (ImageButton) convertView.findViewById(R.id.overflow_icon);
			mHolder.overflowButton.setOnClickListener(overflowClickListener);
			mHolder.overflowButton.setFocusable(false);
			mHolder.overflowButton.setFocusableInTouchMode(false);
	        
			convertView.setTag(mHolder);
		} else {
		    mHolder = (ArtistsListViewHolder) convertView.getTag();
		}
		
		//Apply the card layout's background and text color based on the color theme. Also set the image position.
		if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME")) {
			convertView.setBackgroundResource(R.drawable.card_light);
		} else if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
			convertView.setBackgroundResource(R.drawable.card_dark);
		}
		
		//Retrieve the artist's parameters.
		String artistName = c.getString(c.getColumnIndex(DBAccessHelper.SONG_ARTIST));
		String songSource = c.getString(c.getColumnIndex(DBAccessHelper.SONG_SOURCE));
		String songFilePath = c.getString(c.getColumnIndex(DBAccessHelper.SONG_FILE_PATH));
		String artistArtworkPath = c.getString(c.getColumnIndex(DBAccessHelper.ARTIST_ART_LOCATION));
		String albumArtworkPath = c.getString(c.getColumnIndex(DBAccessHelper.SONG_ALBUM_ART_PATH));
		
		mHolder.overflowButton.setTag(R.string.artist, artistName);
		mHolder.overflowButton.setTag(R.string.song_source, songSource);
		mHolder.overflowButton.setTag(R.string.song_file_path, songFilePath);
		
/*		//Get the number of albums by the artist.
		String selection = " AND " + DBAccessHelper.SONG_ARTIST + "=" + "'" + artistName.replace("'", "''") + "'";
		mCursor = mApp.getDBAccessHelper().getMusicLibraryDBHelper().getAllUniqueAlbumsByArtist(selection);
		int albumsCount = mCursor.getCount();*/
		
		//Set the tag for this child view. The key is required to be an application-defined key.
		convertView.setTag(R.string.artist, artistName);
		convertView.setTag(R.string.song_source, songSource);
		convertView.setTag(R.string.song_file_path, songFilePath);
		convertView.setTag(R.string.artist_art_path, artistArtworkPath);
		convertView.setTag(R.string.album_art, albumArtworkPath);
		
		//Set a unique tag for each mHolder image.
		mHolder.listViewArt.setTag(songFilePath);
		
		//Set the Artist name in the GridView.
		mHolder.title.setText(c.getString(c.getColumnIndex(DBAccessHelper.SONG_ARTIST)));
        
        if (songSource.equals(DBAccessHelper.GMUSIC) && artistArtworkPath!=null) {
        	mApp.getImageLoader().displayImage(artistArtworkPath, mHolder.listViewArt, mApp.getDisplayImageOptions());
        } else {
        	mApp.getImageLoader().displayImage(albumArtworkPath, mHolder.listViewArt, mApp.getDisplayImageOptions());
        }
        
/*        if (albumsCount==1) {
        	mHolder.numberAlbums.setText(albumsCount + " album");
        } else {
        	mHolder.numberAlbums.setText(albumsCount + " albums");
        }*/
        
		return convertView;
	}
    
    /**
     * Click listener for overflow button.
     */
    private OnClickListener overflowClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			PopupMenu menu = new PopupMenu(mContext, v);
			menu.inflate(R.menu.artist_overflow_menu);
			menu.setOnMenuItemClickListener(popupMenuItemClickListener);
			mArtistName = (String) v.getTag(R.string.artist);
		    menu.show();
			
		}
    	
    };
    
    /**
     * Menu item click listener for the pop up menu.
     */
    private OnMenuItemClickListener popupMenuItemClickListener = new OnMenuItemClickListener() {

		@Override
		public boolean onMenuItemClick(MenuItem item) {
			
			switch(item.getItemId()) {
        	case R.id.edit_artist_tags:
        		//Edit Artist Tags.
        		if (mApp.getSharedPreferences().getBoolean("SHOW_ARTIST_EDIT_CAUTION", true)==true) {
            		FragmentTransaction transaction = mArtistsFragment.getFragmentManager().beginTransaction();
            		Bundle bundle = new Bundle();
            		bundle.putString("EDIT_TYPE", "ARTIST");
            		bundle.putString("ARTIST", mArtistName);
            		CautionEditArtistsDialog dialog = new CautionEditArtistsDialog();
            		dialog.setArguments(bundle);
            		dialog.show(transaction, "cautionArtistsDialog");
        		} else {
    				FragmentTransaction ft = mArtistsFragment.getFragmentManager().beginTransaction();
    				Bundle bundle = new Bundle();
    				bundle.putString("EDIT_TYPE", "ARTIST");
    				bundle.putString("ARTIST", mArtistName);
    				ID3sArtistEditorDialog dialog = new ID3sArtistEditorDialog();
    				dialog.setArguments(bundle);
    				dialog.show(ft, "id3ArtistEditorDialog");
        		}
        		break;
        	case R.id.add_to_queue: 
        		//Add to Queue.
        		AsyncAddToQueueTask task = new AsyncAddToQueueTask(mContext,
        														   mArtistsFragment,
        														   "ARTIST",
        														   mArtistName, 
        														   null,
        														   null, 
        														   null, 
        														   null,
        														   null,
        														   null);
        		task.execute();
        		break;
        	case R.id.play_next:
        		AsyncAddToQueueTask playNextTask = new AsyncAddToQueueTask(mContext,
																		   mArtistsFragment,
																		   "ARTIST",
																		   mArtistName, 
																		   null,
																		   null, 
																		   null, 
																		   null,
																		   null,
																		   null);
        		playNextTask.execute(new Boolean[] { true });
        		break;
        	case R.id.add_to_playlist:
        		//Add to Playlist
        		FragmentTransaction ft = mArtistsFragment.getFragmentManager().beginTransaction();
				AddToPlaylistDialog dialog = new AddToPlaylistDialog();
				Bundle bundle = new Bundle();
				bundle.putString("ADD_TYPE", "ARTIST");
				bundle.putString("ARTIST", mArtistName);
				dialog.setArguments(bundle);
				dialog.show(ft, "AddToPlaylistDialog");
				break;
        	case R.id.blacklist_artist:
        		//Blacklist Artist
        		mApp.getDBAccessHelper().setBlacklistForArtist(mArtistName, true);
        		Toast.makeText(mContext, R.string.artist_blacklisted, Toast.LENGTH_SHORT).show();
        		
        		//Update the ListView.
        		mArtistsFragment.mHandler.post(mArtistsFragment.queryRunnable);

				if (mArtistsFragment.getSelectedLayout()==0) {
					mArtistsFragment.getGridViewAdapter().notifyDataSetChanged();
				} else {
					mArtistsFragment.getListViewAdapter().notifyDataSetChanged();
				}

        		break;
        	
			}
			
			return false;
		}
    	
    };

    /**
     * Holder subclass for ArtistsListViewAdapter.
     * 
     * @author Saravan Pantham
     */
	static class ArtistsListViewHolder {
	    public ImageView listViewArt;
	    public TextView title;
	    public TextView numberAlbums;
	    public ImageButton overflowButton;
	    public RelativeLayout selector;
	    
	}
	
}
