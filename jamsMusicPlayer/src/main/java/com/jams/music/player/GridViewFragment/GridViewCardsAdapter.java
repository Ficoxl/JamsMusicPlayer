package com.jams.music.player.GridViewFragment;

import it.sephiroth.android.library.picasso.Generator;
import it.sephiroth.android.library.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
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
 * Generic GridView adapter for GridViewFragment.
 * 
 * @author Saravan Pantham
 */
public class GridViewCardsAdapter extends SimpleCursorAdapter implements Scrollable {
	
	private Context mContext;
	private Common mApp;
	private boolean mLandscape = false;
	private GridViewFragment mGridViewFragment;
    public static GridViewHolder mHolder = null;
    private String mName = "";
    
    //HashMap for DB column names.
    private HashMap<Integer, String> mDBColumnsMap;
    public static final int TITLE_TEXT = 0;
    public static final int SOURCE = 1;
    public static final int FILE_PATH = 2;
    public static final int ARTWORK_PATH = 3;
    public static final int FIELD_1 = 4; //Empty fields for other 
    public static final int FIELD_2 = 5;
    public static final int FIELD_3 = 6;
    public static final int FIELD_4 = 7;
    public static final int FIELD_5 = 8;
    
    public GridViewCardsAdapter(Context context, GridViewFragment gridViewFragment, 
    					   boolean landscape, HashMap<Integer, String> dbColumnsMap) {
    	
        super(context, -1, gridViewFragment.getCursor(), new String[] {}, new int[] {}, 0);
        mContext = context;
        mGridViewFragment = gridViewFragment;
        mApp = (Common) mContext.getApplicationContext();
        mLandscape = landscape;
        mDBColumnsMap = dbColumnsMap;
        
    }
    
    /**
     * Quick scroll indicator implementation.
     */
    @Override
    public String getIndicatorForPosition(int childPosition, int groupPosition) {
    	Cursor c = (Cursor) getItem(childPosition);
    	String title = c.getString(c.getColumnIndex(mDBColumnsMap.get(TITLE_TEXT)));
    	if (title!=null && title.length() > 1)
    		return "  " + title.substring(0, 2) + "  ";
    	else if (title!=null && title.length() > 0)
    		return "  " + title.substring(0, 1) + "  ";
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
			
			mHolder = new GridViewHolder();
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
		    mHolder = (GridViewHolder) convertView.getTag();
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
		
		//Retrieve data from the cursor.
		String titleText = "";
		String source = "";
		String filePath = "";
		String artworkPath = "";
		String field1 = "";
		String field2 = "";
		String field3 = "";
		String field4 = "";
		String field5 = "";
		try {
			titleText = c.getString(c.getColumnIndex(mDBColumnsMap.get(TITLE_TEXT)));
			source = c.getString(c.getColumnIndex(mDBColumnsMap.get(SOURCE)));
			filePath = c.getString(c.getColumnIndex(mDBColumnsMap.get(FILE_PATH)));
			artworkPath = c.getString(c.getColumnIndex(mDBColumnsMap.get(ARTWORK_PATH)));
			field1 = c.getString(c.getColumnIndex(mDBColumnsMap.get(FIELD_1)));
			field2 = c.getString(c.getColumnIndex(mDBColumnsMap.get(FIELD_2)));
			field3 = c.getString(c.getColumnIndex(mDBColumnsMap.get(FIELD_3)));
			field4 = c.getString(c.getColumnIndex(mDBColumnsMap.get(FIELD_4)));
			field5 = c.getString(c.getColumnIndex(mDBColumnsMap.get(FIELD_5)));
			
		} catch (NullPointerException e) {
			//e.printStackTrace();
		}
		
		//Set the tags for this grid item.
		convertView.setTag(R.string.title_text, titleText);
		convertView.setTag(R.string.song_source, source);
		convertView.setTag(R.string.song_file_path, filePath);
		convertView.setTag(R.string.artist_art_path, artworkPath);
		convertView.setTag(R.string.field_1, field1);
		convertView.setTag(R.string.field_2, field2);
		convertView.setTag(R.string.field_3, field3);
		convertView.setTag(R.string.field_4, field4);
		convertView.setTag(R.string.field_5, field5);
		
		//Set the tags for this grid item's overflow button.
		mHolder.overflowButton.setTag(R.string.title_text, titleText);
		mHolder.overflowButton.setTag(R.string.source, source);
		mHolder.overflowButton.setTag(R.string.file_path, filePath);
		mHolder.overflowButton.setTag(R.string.field_1, field1);
		mHolder.overflowButton.setTag(R.string.field_2, field2);
		mHolder.overflowButton.setTag(R.string.field_3, field3);
		mHolder.overflowButton.setTag(R.string.field_4, field4);
		mHolder.overflowButton.setTag(R.string.field_5, field5);
		
		//Set the title text in the GridView.
		mHolder.title.setText(titleText);
		
		//Load the album art.
        Picasso.with(mContext)
        	   .load(Uri.parse("custom.resource://" + artworkPath))
        	   .placeholder(R.drawable.transparent_drawable)
        	   .withDelay(100)
        	   .withGenerator(generator)
        	   .into(mHolder.gridViewArt);
		
		return convertView;
	}
    
    /**
     * Picasso custom generator for embedded artwork.
     */
    private Generator generator = new Generator() {

		@Override
		public Bitmap decode(Uri uri) throws IOException {
			
			MediaMetadataRetriever mmdr = new MediaMetadataRetriever();
            byte[] imageData = null;
            try {
            	String prefix = "custom.resource://byte://";
            	mmdr.setDataSource(uri.toString().substring(prefix.length()));
                imageData = mmdr.getEmbeddedPicture();
            } catch (Exception e) {
            	return null;
            }
			
            return BitmapFactory.decodeByteArray(imageData , 0, imageData.length);
		}
		   
	};
    
    /**
     * Click listener for overflow button.
     */
    private OnClickListener overflowClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			PopupMenu menu = new PopupMenu(mContext, v);
			menu.inflate(R.menu.artist_overflow_menu);
			menu.setOnMenuItemClickListener(popupMenuItemClickListener);
			mName = (String) v.getTag(R.string.artist);
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
            		FragmentTransaction transaction = mGridViewFragment.getFragmentManager().beginTransaction();
            		Bundle bundle = new Bundle();
            		bundle.putString("EDIT_TYPE", "ARTIST");
            		bundle.putString("ARTIST", mName);
            		CautionEditArtistsDialog dialog = new CautionEditArtistsDialog();
            		dialog.setArguments(bundle);
            		dialog.show(transaction, "cautionArtistsDialog");
        		} else {
    				FragmentTransaction ft = mGridViewFragment.getFragmentManager().beginTransaction();
    				Bundle bundle = new Bundle();
    				bundle.putString("EDIT_TYPE", "ARTIST");
    				bundle.putString("ARTIST", mName);
    				ID3sArtistEditorDialog dialog = new ID3sArtistEditorDialog();
    				dialog.setArguments(bundle);
    				dialog.show(ft, "id3ArtistEditorDialog");
        		}
        		break;
        	case R.id.add_to_queue: 
        		//Add to Queue.
        		AsyncAddToQueueTask task = new AsyncAddToQueueTask(mContext,
        														   mGridViewFragment,
        														   "ARTIST",
        														   mName, 
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
																		   mGridViewFragment,
																		   "ARTIST",
																		   mName, 
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
        		FragmentTransaction ft = mGridViewFragment.getFragmentManager().beginTransaction();
				AddToPlaylistDialog dialog = new AddToPlaylistDialog();
				Bundle bundle = new Bundle();
				bundle.putString("ADD_TYPE", "ARTIST");
				bundle.putString("ARTIST", mName);
				dialog.setArguments(bundle);
				dialog.show(ft, "AddToPlaylistDialog");
				break;
        	case R.id.blacklist_artist:
        		//Blacklist Artist
        		mApp.getDBAccessHelper().setBlacklistForArtist(mName, true);
        		Toast.makeText(mContext, R.string.artist_blacklisted, Toast.LENGTH_SHORT).show();
        		
        		//Update the GridView.
        		mGridViewFragment.mHandler.post(mGridViewFragment.queryRunnable);
        		mGridViewFragment.getGridViewAdapter().notifyDataSetChanged();

        		break;
        	
			}
			
			return false;
		}
    	
    };

    /**
     * Holder subclass for GridViewAdapter.
     * 
     * @author Saravan Pantham
     */
	public static class GridViewHolder {
	    public ImageView gridViewArt;
	    public TextView title;
	    public ImageButton overflowButton;
	    public RelativeLayout selector;
	    public LinearLayout cardBackground;
	    
	}
	
}
