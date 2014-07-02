package com.jams.music.player.ArtistsFragment;

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
 * GridView adapter for ArtistsFragment.
 * 
 * @author Saravan Pantham
 */
public class ArtistsGridViewAdapter extends SimpleCursorAdapter implements Scrollable {
	
	private Context mContext;
	private boolean mLandscape = false;
	private ArtistsFragment mArtistsFragment;
	
	private Common mApp;
    private ArtistsGridViewHolder mHolder = null;
    private String mArtistName = "";
    
    public ArtistsGridViewAdapter(Context context, ArtistsFragment artistsFragment, boolean landscape) {
        super(context, -1, artistsFragment.getCursor(), new String[] {}, new int[] {}, 0);
        mContext = context;
        mArtistsFragment = artistsFragment;
        mApp = (Common) mContext.getApplicationContext();
        mLandscape = landscape;
        
    }
    
    /**
     * Quick scroll indicator implementation.
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
			
			mHolder = new ArtistsGridViewHolder();
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
			mHolder.selector = (RelativeLayout) convertView.findViewById(R.id.selector);

			mHolder.title.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Light"));
			mHolder.title.setPaintFlags(mHolder.title.getPaintFlags() | 
									   Paint.SUBPIXEL_TEXT_FLAG | 
									   Paint.ANTI_ALIAS_FLAG);
			
	        mHolder.gridViewArt.setScaleType(ImageView.ScaleType.CENTER_CROP);
			
			convertView.setTag(mHolder);
		} else {
		    mHolder = (ArtistsGridViewHolder) convertView.getTag();
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

			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mHolder.gridViewArt.getLayoutParams();
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
			
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mHolder.gridViewArt.getLayoutParams();
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
		
		//Retrieve the artist's parameters.
		String artistName = c.getString(c.getColumnIndex(DBAccessHelper.SONG_ARTIST));
		String songSource = c.getString(c.getColumnIndex(DBAccessHelper.SONG_SOURCE));
		String songFilePath = c.getString(c.getColumnIndex(DBAccessHelper.SONG_FILE_PATH));
		String artistArtworkPath = c.getString(c.getColumnIndex(DBAccessHelper.ARTIST_ART_LOCATION));
		String albumArtworkPath = c.getString(c.getColumnIndex(DBAccessHelper.SONG_ALBUM_ART_PATH));
		
		//Set the tag for this child view. The key is required to be an application-defined key.
		convertView.setTag(R.string.artist, artistName);
		convertView.setTag(R.string.song_source, songSource);
		convertView.setTag(R.string.song_file_path, songFilePath);
		convertView.setTag(R.string.artist_art_path, artistArtworkPath);
		convertView.setTag(R.string.album_art, albumArtworkPath);
		
		mHolder.overflowButton.setTag(R.string.artist, artistName);
		mHolder.overflowButton.setTag(R.string.song_source, songSource);
		mHolder.overflowButton.setTag(R.string.song_file_path, songFilePath);
		
		//Set a unique tag for each mHolder image.
		mHolder.gridViewArt.setTag(songFilePath);
		
		//Set the Artist name in the GridView.
		mHolder.title.setText(c.getString(c.getColumnIndex(DBAccessHelper.SONG_ARTIST)));
        
        if (songSource.equals(DBAccessHelper.GMUSIC) && artistArtworkPath!=null) {
        	mApp.getImageLoader().displayImage(artistArtworkPath, mHolder.gridViewArt, mApp.getDisplayImageOptions());
        } else {
        	mApp.getImageLoader().displayImage(albumArtworkPath, mHolder.gridViewArt, mApp.getDisplayImageOptions());
        }
        
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
        		
        		//Update the GridView.
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
     * Holder subclass for ArtistsGridViewAdapter.
     * 
     * @author Saravan Pantham
     */
	static class ArtistsGridViewHolder {
	    public ImageView gridViewArt;
	    public TextView title;
	    public ImageButton overflowButton;
	    public RelativeLayout selector;
	    public LinearLayout cardBackground;
	    
	}
	
}
