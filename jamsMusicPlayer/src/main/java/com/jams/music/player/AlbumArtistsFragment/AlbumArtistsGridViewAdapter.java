package com.jams.music.player.AlbumArtistsFragment;

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

import com.andraskindler.quickscroll.Scrollable;
import com.jams.music.player.R;
import com.jams.music.player.AsyncTasks.AsyncAddToQueueTask;
import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.Dialogs.AddToPlaylistDialog;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.Utils.Common;

/**
 * GridView adapter for AlbumArtistsFragment.
 * 
 * @author Saravan Pantham
 */
public class AlbumArtistsGridViewAdapter extends SimpleCursorAdapter implements Scrollable {
	
	private Context mContext;
	private Common mApp;
	private AlbumArtistsFragment mAlbumArtistsFragment;
    private AlbumArtistsGridViewHolder mHolder = null;
    private boolean mLandscape = false;
    private String mAlbumArtistName = "";
	
    public AlbumArtistsGridViewAdapter(Context context, AlbumArtistsFragment albumArtistsFragment, boolean landscape) {
        super(context, -1, albumArtistsFragment.getCursor(), new String[] {}, new int[] {}, 0);
        mContext = context;
        mAlbumArtistsFragment = albumArtistsFragment;
        mApp = (Common) mContext.getApplicationContext();
        mLandscape = landscape;
        
    }

    /**
     * QuickScroll implementation for this adapter.
     */
    @Override
    public String getIndicatorForPosition(int childPosition, int groupPosition) {
    	Cursor c = (Cursor) getItem(childPosition);
    	String artist = c.getString(c.getColumnIndex(DBAccessHelper.SONG_ALBUM_ARTIST));
    	if (artist!=null && artist.length() > 1)
    		return "  " + artist.substring(0, 2) + "  ";
    	else if (artist!=null && artist.length() > 0)
    		return "  " + artist.substring(0, 1) + "  ";
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

		if (convertView == null) {
			
			mHolder = new AlbumArtistsGridViewHolder();
			if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME") || 
				mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.artists_grid_view_cards_layout, parent, false);
				mHolder.cardBackground = (LinearLayout) convertView.findViewById(R.id.artist_card_layout);
			} else {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.artists_grid_view_layout, parent, false);
			}

			mHolder.gridViewArt = (ImageView) convertView.findViewById(R.id.artistsGridViewImage);
			mHolder.title = (TextView) convertView.findViewById(R.id.artistName);
			mHolder.overflowButton = (ImageButton) convertView.findViewById(R.id.overflow_icon);
			mHolder.overflowButton.setOnClickListener(overflowClickListener);
			mHolder.overflowButton.setFocusable(false);
			mHolder.overflowButton.setFocusableInTouchMode(false);
			
			mHolder.title.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Light"));
			mHolder.title.setPaintFlags(mHolder.title.getPaintFlags() | 
									   Paint.SUBPIXEL_TEXT_FLAG | 
									   Paint.ANTI_ALIAS_FLAG);

			convertView.setTag(mHolder);
		} else {
		    mHolder = (AlbumArtistsGridViewHolder) convertView.getTag();
		}
		
		DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
		int width = (metrics.widthPixels)/3;
		int height = (metrics.widthPixels)/2;
		int nonCardsThemeWidth = (metrics.widthPixels)/4;
		
		//Apply the card layout's background and text color based on the color theme. Also set the image position.
		if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME")) {
			mHolder.cardBackground.setBackgroundResource(R.drawable.card_gridview_light);

			if (mLandscape) {
				width = (metrics.widthPixels)/4;
				height = width;
			} else {
				width = (metrics.widthPixels)/2;
				height = width;
			}
			
			LayoutParams params = mHolder.gridViewArt.getLayoutParams();
			params.width = width;
			params.height = height;
			mHolder.gridViewArt.setLayoutParams(params);
			
			mHolder.title.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
			
		} else if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
			mHolder.cardBackground.setBackgroundResource(R.drawable.card_gridview_dark);

			if (mLandscape) {
				width = (metrics.widthPixels)/4;
				height = width;
			} else {
				width = (metrics.widthPixels)/2;
				height = width;
			}
			
			LayoutParams params = mHolder.gridViewArt.getLayoutParams();
			params.width = width;
			params.height = height;
			mHolder.gridViewArt.setLayoutParams(params);
			
			mHolder.title.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
			
		} else {
			LayoutParams params = mHolder.gridViewArt.getLayoutParams();
			
			if (mLandscape) {
				params.width = nonCardsThemeWidth;
				params.height = nonCardsThemeWidth;
			} else {
				params.width = width;
				params.height = height;
			}
			
			mHolder.gridViewArt.setLayoutParams(params);
		}
		
		//Retrieve the album artist's parameters.
		String albumArtistName = c.getString(c.getColumnIndex(DBAccessHelper.SONG_ALBUM_ARTIST));
		String songSource = c.getString(c.getColumnIndex(DBAccessHelper.SONG_SOURCE));
		String songFilePath = c.getString(c.getColumnIndex(DBAccessHelper.SONG_FILE_PATH));
		String artistArtworkPath = c.getString(c.getColumnIndex(DBAccessHelper.ARTIST_ART_LOCATION));
		String albumArtworkPath = c.getString(c.getColumnIndex(DBAccessHelper.SONG_ALBUM_ART_PATH));
		String albumArtist = c.getString(c.getColumnIndex(DBAccessHelper.SONG_ALBUM_ARTIST));
		
		//Set the tag for this child view. The key is required to be an application-defined key.
		convertView.setTag(R.string.artist, albumArtistName);
		convertView.setTag(R.string.song_source, songSource);
		convertView.setTag(R.string.song_file_path, songFilePath);
		convertView.setTag(R.string.artist_art_path, artistArtworkPath);
		convertView.setTag(R.string.album_art, albumArtworkPath);
		convertView.setTag(R.string.album_artist, albumArtist);
		
		mHolder.overflowButton.setTag(R.string.album_artist, albumArtist);
		
		//Set a unique tag for each mHolder image.
		mHolder.gridViewArt.setTag(songFilePath);
		
		//Set the Artist name in the GridView.
		mHolder.title.setText(albumArtistName);
		
        mHolder.gridViewArt.setScaleType(ImageView.ScaleType.CENTER_CROP);
        
        if (songSource.equals(DBAccessHelper.GMUSIC) && artistArtworkPath!=null) {
        	mApp.getImageLoader().displayImage(artistArtworkPath, mHolder.gridViewArt, mApp.getDisplayImageOptions());
        } else {
        	mApp.getImageLoader().displayImage(albumArtworkPath, mHolder.gridViewArt, mApp.getDisplayImageOptions());
        }
        
		return convertView;
	}
    
    /**
     * Click listener for the overflow button.
     */
    private OnClickListener overflowClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			PopupMenu menu = new PopupMenu(mContext, v);
			menu.inflate(R.menu.album_artist_overflow_menu);
			menu.setOnMenuItemClickListener(popupMenuItemClickListener);
			mAlbumArtistName = (String) v.getTag(R.string.album_artist);
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
        														   mAlbumArtistsFragment,
        														   "ALBUM_ARTIST",
        														   null,
        														   null,
        														   null, 
        														   null, 
        														   null,
        														   null,
        														   mAlbumArtistName);
        		task.execute();
        		break;
        	case R.id.play_next: 
        		//Play Next.
        		AsyncAddToQueueTask playNextTask = new AsyncAddToQueueTask(mContext,
		        														   mAlbumArtistsFragment,
		        														   "ALBUM_ARTIST",
		        														   null,
		        														   null,
		        														   null, 
		        														   null, 
		        														   null,
		        														   null,
		        														   mAlbumArtistName);
        		playNextTask.execute(new Boolean[] { true });
        		break;
        	case R.id.add_to_playlist:
        		//Add to Playlist
        		FragmentTransaction ft = mAlbumArtistsFragment.getFragmentManager().beginTransaction();
				AddToPlaylistDialog dialog = new AddToPlaylistDialog();
				Bundle bundle = new Bundle();
				bundle.putString("ADD_TYPE", "ALBUM_ARTIST");
				bundle.putString("ALBUM_ARTIST", mAlbumArtistName);
				dialog.setArguments(bundle);
				dialog.show(ft, "AddToPlaylistDialog");
				break;
        	
			}
			
			return false;
		}
    	
    };

    /**
     * Holder subclass for AlbumArtistsGridViewAdapter.
     * 
     * @author Saravan Pantham
     */
	static class AlbumArtistsGridViewHolder {
	    public ImageView gridViewArt;
	    public TextView title;
	    public ImageButton overflowButton;
	    public LinearLayout cardBackground;
	}
	
}
