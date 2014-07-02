package com.jams.music.player.AlbumsFragment;

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
import com.jams.music.player.AsyncTasks.AsyncDeleteAlbumArtTask;
import com.jams.music.player.AsyncTasks.AsyncGetAlbumArtTask;
import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.Dialogs.AddToPlaylistDialog;
import com.jams.music.player.Dialogs.CautionEditAlbumsDialog;
import com.jams.music.player.Dialogs.ID3sAlbumEditorDialog;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.Utils.Common;

/**
 * ListView adapter for AlbumsFragment.
 * 
 * @author Saravan Pantham
 */
public class AlbumsListViewAdapter extends SimpleCursorAdapter implements Scrollable {
	
	private Context mContext;
	private AlbumsFragment mAlbumsFragment;
	private Common mApp;
    private AlbumsListViewHolder mHolder = null;
	private String mArtistName = "";
	private String mAlbumName = "";
	private View mConvertView;
	
    public AlbumsListViewAdapter(Context context, AlbumsFragment albumsFragment) {
        super(context, -1, albumsFragment.getCursor(), new String[] {}, new int[] {}, 0);
        mContext = context;
        mAlbumsFragment = albumsFragment;
        mApp = (Common) mContext.getApplicationContext();
        
    }
    
    /**
     * QuickScroll implementation for this adapter.
     */
    @Override
    public String getIndicatorForPosition(int childPosition, int groupPosition) {
    	Cursor c = (Cursor) getItem(childPosition);
    	String album = c.getString(c.getColumnIndex(DBAccessHelper.SONG_ALBUM));
    	if (album!=null && album.length() > 1)
    		return "  " + album.substring(0, 2) + "  ";
    	else if (album!=null && album.length() > 0)
    		return "  " + album.substring(0, 1) + "  ";
    	else
    		return "  N/A  ";
    }
    
    /**
     * Returns the top view position in the adapter.
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

		if (convertView == null) {
			
			if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME") || 
				mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.artists_list_view_cards_layout, parent, false);
			} else {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.artists_list_view_layout, parent, false);
			}

			mHolder = new AlbumsListViewHolder();
			mHolder.listViewArt = (ImageView) convertView.findViewById(R.id.artistsListAlbumThumbnail);
			mHolder.title = (TextView) convertView.findViewById(R.id.artistName);
			mHolder.numberAlbums = (TextView) convertView.findViewById(R.id.artistNumberAlbums);

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
			mConvertView = convertView;
		} else {
		    mHolder = (AlbumsListViewHolder) convertView.getTag();
		}
		
		//Apply the card layout's background and text color based on the color theme. Also set the image position.
		if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME")) {
			convertView.setBackgroundResource(R.drawable.card_light);
		} else if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
			convertView.setBackgroundResource(R.drawable.card_dark);
		}
		
		//Retrieve the album's parameters.
		String artistName = c.getString(c.getColumnIndex(DBAccessHelper.SONG_ARTIST));
		String albumName = c.getString(c.getColumnIndex(DBAccessHelper.SONG_ALBUM));
		String songSource = c.getString(c.getColumnIndex(DBAccessHelper.SONG_SOURCE));
		String songFilePath = c.getString(c.getColumnIndex(DBAccessHelper.SONG_FILE_PATH));
		String albumArtworkPath = c.getString(c.getColumnIndex(DBAccessHelper.SONG_ALBUM_ART_PATH));
		
/*		//Get the number of songs in the album.
		String selection = " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + albumName.replace("'", "''") + "'"
						 + " AND " + DBAccessHelper.SONG_ARTIST + "=" + "'" + artistName.replace("'", "''") + "'";
		mCursor = mApp.getDBAccessHelper().getMusicLibraryDBHelper().getAllSongsInAlbumArtist(selection);
		int albumsCount = mCursor.getCount();*/
		
		//Set the tag for this child view. The key is required to be an application-defined key.
		convertView.setTag(R.string.album, albumName);
		convertView.setTag(R.string.artist, artistName);
		convertView.setTag(R.string.song_source, songSource);
		convertView.setTag(R.string.song_file_path, songFilePath);
		convertView.setTag(R.string.album_art, albumArtworkPath);
		
		mHolder.overflowButton.setTag(R.string.artist, artistName);
		mHolder.overflowButton.setTag(R.string.album, albumName);
		
		//Set a unique tag for each mHolder image.
		mHolder.listViewArt.setTag(songFilePath);
		
		//Set the Album name in the GridView.
		mHolder.title.setText(albumName);
		mApp.getImageLoader().displayImage(albumArtworkPath, mHolder.listViewArt, mApp.getDisplayImageOptions());
        
/*        if (albumsCount==1) {
        	mHolder.numberAlbums.setText(albumsCount + " song");
        } else {
        	mHolder.numberAlbums.setText(albumsCount + " songs");
        }*/
        
		return convertView;
	}
    
    /**
     * Click listener for the overflow icon.
     */
    private OnClickListener overflowClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			PopupMenu menu = new PopupMenu(mContext, v);
			menu.inflate(R.menu.album_overflow_menu);
			menu.setOnMenuItemClickListener(popupMenuItemClickListener);
			mAlbumName = (String) v.getTag(R.string.album);
			mArtistName = (String) v.getTag(R.string.artist);
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
        	case R.id.edit_album_tags:
        		//Edit Album Tags.
        		if (mApp.getSharedPreferences().getBoolean("SHOW_ALBUM_EDIT_CAUTION", true)==true) {
            		FragmentTransaction transaction = mAlbumsFragment.getFragmentManager().beginTransaction();
            		Bundle bundle = new Bundle();
            		bundle.putString("EDIT_TYPE", "ALBUM");
            		bundle.putString("ARTIST", mArtistName);
            		bundle.putString("ALBUM", mAlbumName);
            		bundle.putString("CALLING_FRAGMENT", "ALBUMS_FRAGMENT");
            		CautionEditAlbumsDialog dialog = new CautionEditAlbumsDialog();
            		dialog.setArguments(bundle);
            		dialog.show(transaction, "cautionArtistsDialog");
        		} else {
        			FragmentTransaction ft = mAlbumsFragment.getFragmentManager().beginTransaction();
    				Bundle bundle = new Bundle();
    				bundle.putString("EDIT_TYPE", "ALBUM");
    				bundle.putString("ARTIST", mArtistName);
    				bundle.putString("ALBUM", mAlbumName);
    				bundle.putString("CALLING_FRAGMENT", "ALBUMS_FRAGMENT");
    				ID3sAlbumEditorDialog dialog = new ID3sAlbumEditorDialog();
    				dialog.setArguments(bundle);
    				dialog.show(ft, "id3EditorDialog");
        		}
        		
        		break;
        	case R.id.add_to_queue:
        		//Add to queue.
        		AsyncAddToQueueTask addToQueueTask = new AsyncAddToQueueTask(mContext,
        														   mAlbumsFragment,
        														   "ALBUM",
        														   mArtistName,
        														   mAlbumName,
        														   null, 
        														   null, 
        														   null,
        														   null,
        														   null);
        		addToQueueTask.execute();
        		break;
        	case R.id.play_next:
        		//Play next.
        		AsyncAddToQueueTask playNextTask = new AsyncAddToQueueTask(mContext,
		        														   mAlbumsFragment,
		        														   "ALBUM",
		        														   mArtistName,
		        														   mAlbumName,
		        														   null, 
		        														   null, 
		        														   null,
		        														   null,
		        														   null);
        		playNextTask.execute(new Boolean[] { true });
        		break;
        	case R.id.add_to_playlist:
        		//Add to Playlist
        		FragmentTransaction ft = mAlbumsFragment.getFragmentManager().beginTransaction();
				AddToPlaylistDialog dialog = new AddToPlaylistDialog();
				Bundle bundle = new Bundle();
				bundle.putString("ADD_TYPE", "ALBUM");
				bundle.putString("ARTIST", mArtistName);
				bundle.putString("ALBUM", mAlbumName);
				dialog.setArguments(bundle);
				dialog.show(ft, "AddToPlaylistDialog");
        		break;
        	case R.id.get_album_art: 
        		//Download Album Art.
        		AsyncGetAlbumArtTask getAlbumArtTask = new AsyncGetAlbumArtTask(mContext, 
        																		mConvertView, 
        																		R.id.albumsGridViewImage);
        		String[] metadata = { mArtistName, mAlbumName };
        		getAlbumArtTask.execute(metadata);
        		break;
        	case R.id.remove_album_art:
        		//Remove Album Art.
        		AsyncDeleteAlbumArtTask task = new AsyncDeleteAlbumArtTask(mContext, 
        																   mConvertView, 
        																   R.id.albumsGridViewImage, 
        																   mAlbumsFragment.getActivity(), 
        																   "ALBUMS_FRAGMENT");
        		String[] metadata2 = { mArtistName, mAlbumName };
        		task.execute(metadata2);
        		break;
        	case R.id.blacklist_album:
        		//Blacklist Album.
        		mApp.getDBAccessHelper().setBlacklistForAlbum(mAlbumName, mArtistName, true);
        		Toast.makeText(mContext, R.string.album_blacklisted, Toast.LENGTH_SHORT).show();
        		
        		//Update the ListView.
        		mAlbumsFragment.mHandler.post(mAlbumsFragment.queryRunnable);
				
				if (mAlbumsFragment.getSelectedLayout()==0) {
					mAlbumsFragment.getGridViewAdapter().notifyDataSetChanged();
				} else {
					mAlbumsFragment.getListViewAdapter().notifyDataSetChanged();
				}

        		break;
            	
            }
			
			return false;
		}
    	
    };

    /**
     * Holder subclass for AlbumsListViewAdapter.
     * 
     * @author Saravan Pantham
     */
	static class AlbumsListViewHolder {
	    public ImageView listViewArt;
	    public TextView title;
	    public TextView numberAlbums;
	    public ImageButton overflowButton;
	}
	
}
