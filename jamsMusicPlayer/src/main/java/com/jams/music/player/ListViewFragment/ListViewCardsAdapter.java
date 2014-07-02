package com.jams.music.player.ListViewFragment;

import it.sephiroth.android.library.picasso.Generator;
import it.sephiroth.android.library.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
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
import com.jams.music.player.Dialogs.AddToPlaylistDialog;
import com.jams.music.player.Dialogs.CautionEditArtistsDialog;
import com.jams.music.player.Dialogs.ID3sArtistEditorDialog;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.Utils.Common;

/**
 * Generic ListView adapter for ListViewFragment.
 * 
 * @author Saravan Pantham
 */
public class ListViewCardsAdapter extends SimpleCursorAdapter implements Scrollable {
	
	private Context mContext;
	private Common mApp;
	private ListViewFragment mListViewFragment;
    public static ListViewHolder mHolder = null;
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
    
    public ListViewCardsAdapter(Context context, ListViewFragment listViewFragment, 
    					   		HashMap<Integer, String> dbColumnsMap) {
    	
        super(context, -1, listViewFragment.getCursor(), new String[] {}, new int[] {}, 0);
        mContext = context;
        mListViewFragment = listViewFragment;
        mApp = (Common) mContext.getApplicationContext();
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
			convertView = LayoutInflater.from(mContext).inflate(R.layout.songs_list_view_cards_layout, parent, false);
			
			if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME"))
				convertView.setBackgroundResource(R.drawable.card_gridview_light);
			else
				convertView.setBackgroundResource(R.drawable.card_gridview_dark);
			
			mHolder = new ListViewHolder();
			mHolder.image = (ImageView) convertView.findViewById(R.id.songsListAlbumThumbnail);
			mHolder.title = (TextView) convertView.findViewById(R.id.songNameListView);
			mHolder.artist = (TextView) convertView.findViewById(R.id.artistNameSongListView);
			mHolder.duration = (TextView) convertView.findViewById(R.id.songDurationListView);

			mHolder.title.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
			
			mHolder.title.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Light"));
			mHolder.artist.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Regular"));
			mHolder.duration.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Regular"));
			
			mHolder.title.setPaintFlags(mHolder.title.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
			mHolder.artist.setPaintFlags(mHolder.artist.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
			mHolder.duration.setPaintFlags(mHolder.duration.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
			
			mHolder.overflowButton = (ImageButton) convertView.findViewById(R.id.overflow_icon);
			mHolder.overflowButton.setOnClickListener(overflowClickListener);
			mHolder.overflowButton.setFocusable(false);
			mHolder.overflowButton.setFocusableInTouchMode(false);
			
			convertView.setTag(mHolder);
		} else {
		    mHolder = (ListViewHolder) convertView.getTag();
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
		
		//Set the title text in the ListView.
		mHolder.title.setText(titleText);
		mHolder.artist.setText(field2);
		
		try {
			//mHolder.duration.setText(convertMillisToMinsSecs(Long.parseLong(field1)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Load the album art.
		mApp.getImageLoader().displayImage(artworkPath, mHolder.image, mApp.getDisplayImageOptions());
/*        Picasso.with(mContext)
        	   .load(Uri.parse("custom.resource://" + artworkPath))
        	   .placeholder(R.drawable.transparent_drawable)
        	   .withDelay(400)
        	   .withGenerator(generator)
        	   .into(mHolder.image);*/
		
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
            		FragmentTransaction transaction = mListViewFragment.getFragmentManager().beginTransaction();
            		Bundle bundle = new Bundle();
            		bundle.putString("EDIT_TYPE", "ARTIST");
            		bundle.putString("ARTIST", mName);
            		CautionEditArtistsDialog dialog = new CautionEditArtistsDialog();
            		dialog.setArguments(bundle);
            		dialog.show(transaction, "cautionArtistsDialog");
        		} else {
    				FragmentTransaction ft = mListViewFragment.getFragmentManager().beginTransaction();
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
        														   mListViewFragment,
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
																		   mListViewFragment,
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
        		FragmentTransaction ft = mListViewFragment.getFragmentManager().beginTransaction();
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
        		
        		//Update the ListView.
        		mListViewFragment.mHandler.post(mListViewFragment.queryRunnable);
        		mListViewFragment.getListViewAdapter().notifyDataSetChanged();

        		break;
        	
			}
			
			return false;
		}
    	
    };
    
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
     * Holder subclass for ListViewCardsAdapter.
     * 
     * @author Saravan Pantham
     */
	static class ListViewHolder {
	    public ImageView image;
	    public TextView title;
	    public TextView artist;
	    public TextView duration;
	    public ImageButton overflowButton;

	}
	
}
