package com.jams.music.player.AlbumsFragment;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
 * GridView adapter for AlbumsFragment.
 * 
 * @author Saravan Pantham
 */
public class AlbumsGridViewAdapter extends SimpleCursorAdapter implements Scrollable {
	
	private Context mContext;
	private AlbumsFragment mAlbumsFragment;
	private Common mApp;
	private boolean mLandscape;
	private String mArtistName = "";
	private String mAlbumName = "";
	private View mConvertView;
	
    public AlbumsGridViewAdapter(Context context, AlbumsFragment albumsFragment, boolean landscape) {
        super(context, -1, albumsFragment.getCursor(), new String[] {}, new int[] {}, 0);
        mContext = context;
        mAlbumsFragment = albumsFragment;
        mApp = (Common) mContext.getApplicationContext();
        mLandscape = landscape;
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
     * Returns the position of the top view in the list/grid.
     */
	@Override
	public int getScrollPosition(int childPosition, int groupPosition) {
		// TODO Auto-generated method stub
		return childPosition;
	}

	/**
	 * Constructs the row/child for the specified position in the adapter.
	 */
    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
        Cursor c = (Cursor) getItem(position);
	    AlbumsViewHolder holder = null;
	    
		if (convertView == null) {
			
			holder = new AlbumsViewHolder();
			if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME") || 
				mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.albums_grid_view_cards_layout, parent, false);
				holder.cardBackground = (LinearLayout) convertView.findViewById(R.id.album_card_layout);
			} else {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.albums_grid_view_layout, parent, false);
			}

			holder.image = (ImageView) convertView.findViewById(R.id.albumsGridViewImage);
			holder.title = (TextView) convertView.findViewById(R.id.albumsName);
			holder.overflowButton = (ImageButton) convertView.findViewById(R.id.overflow_icon);
			holder.overflowButton.setOnClickListener(overflowClickListener);
			holder.overflowButton.setFocusable(false);
			holder.overflowButton.setFocusableInTouchMode(false);

			holder.title.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Light"));
			holder.title.setPaintFlags(holder.title.getPaintFlags() 
									   | Paint.SUBPIXEL_TEXT_FLAG 
									   | Paint.ANTI_ALIAS_FLAG);
			
			convertView.setTag(holder);
			mConvertView = convertView;
		} else {
		    holder = (AlbumsViewHolder) convertView.getTag();
		}
    	
    	DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
		int width = (metrics.widthPixels)/2;
		int height = (metrics.widthPixels)/2;
		int nonCardsThemeWidth = (metrics.widthPixels)/4;
		
		//Apply the card layout's background and text color based on the color theme. Also set the image position.
		if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME")) {
			holder.cardBackground.setBackgroundResource(R.drawable.card_gridview_light);

			if (mLandscape) {
				width = (metrics.widthPixels)/4;
				height = width;
			} else {
				width = (metrics.widthPixels)/2;
				height = width;
			}
			
			LayoutParams params = holder.image.getLayoutParams();
			params.width = width;
			params.height = height;
			holder.image.setLayoutParams(params);
			
			holder.title.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
			
		} else if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
			holder.cardBackground.setBackgroundResource(R.drawable.card_gridview_dark);

			if (mLandscape) {
				width = (metrics.widthPixels)/4;
				height = width;
			} else {
				width = (metrics.widthPixels)/2;
				height = width;
			}
			
			LayoutParams params = holder.image.getLayoutParams();
			params.width = width;
			params.height = height;
			holder.image.setLayoutParams(params);
			
			holder.title.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
			
		} else {
			LayoutParams params = holder.image.getLayoutParams();

			if (mLandscape) {
				params.width = nonCardsThemeWidth;
				params.height = nonCardsThemeWidth;
			} else {
				params.width = width;
				params.height = height;
			}
			
			holder.image.setLayoutParams(params);
		}
        
		//Get the current album's parameters.
		String albumName = c.getString(c.getColumnIndex(DBAccessHelper.SONG_ALBUM));
		String artistName = c.getString(c.getColumnIndex(DBAccessHelper.SONG_ARTIST));
		String albumArtist = c.getString(c.getColumnIndex(DBAccessHelper.SONG_ALBUM_ARTIST));
		String songFilePath = c.getString(c.getColumnIndex(DBAccessHelper.SONG_FILE_PATH));
		String albumArtPath = c.getString(c.getColumnIndex(DBAccessHelper.SONG_ALBUM_ART_PATH));
		String songSource = c.getString(c.getColumnIndex(DBAccessHelper.SONG_SOURCE));
		
		//Set the album+artist pair and a file path as tags for this child view.
		convertView.setTag(R.string.album, albumName);
		convertView.setTag(R.string.artist, artistName);
		convertView.setTag(R.string.album_artist, albumArtist);
		convertView.setTag(R.string.song_file_path, songFilePath);
		convertView.setTag(R.string.album_art, albumArtPath);
		convertView.setTag(R.string.song_source, songSource);
		
		holder.overflowButton.setTag(R.string.artist, artistName);
		holder.overflowButton.setTag(R.string.album, albumName);

		//Set the Artist name in the GridView.
		holder.title.setText(albumName);
		
        mApp.getImageLoader().displayImage(albumArtPath, holder.image, mApp.getDisplayImageOptions());
		
		return convertView;
	}
    
    /**
     * Click listener for the overflow button.
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
     * Menu item click listener for the popup menu.
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
     * Holder subclass for AlbumsGridViewAdapter.
     * 
     * @author Saravan Pantham
     */
	static class AlbumsViewHolder {
	    public ImageView image;
	    public TextView title;
	    public ImageButton overflowButton;
	    public LinearLayout cardBackground;
	}
	
}
