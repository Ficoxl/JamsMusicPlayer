package com.jams.music.player.PlaylistsFlippedActivity;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.jams.music.player.R;
import com.jams.music.player.AsyncTasks.AsyncRemovePinnedSongsTask;
import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.NowPlayingActivity.NowPlayingActivity;
import com.jams.music.player.Utils.Common;
import com.jams.music.player.Utils.CyclicTransitionDrawable;
import com.jams.music.player.Utils.TypefaceSpan;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.SimpleFloatViewManager;

public class PlaylistsFlippedFragment extends Fragment {

	private Context mContext;
	private String playlistName;
	private String playlistId;
	private static SharedPreferences sharedPreferences;
	
	private DBAccessHelper musicLibraryDBHelper;
	private boolean mPinned = false;
	private Cursor cursor;
	private ImageView headerImage;
	public static DragSortListView playlistsFlippedListView;
	public static PlaylistsFlippedListViewAdapter adapter;
	public static int contextMenuItemIndex;
	
    private ArrayList<Drawable> drawablesList;
	private Drawable[] playlistAlbumArt;
	
	public Common mApp;
	
	public static int index;
	public static View childView;
	private String selection;
	
    @SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_playlists_flipped, container, false);
        mContext = getActivity();
        mApp = (Common) mContext.getApplicationContext();
        musicLibraryDBHelper = new DBAccessHelper(mContext.getApplicationContext());
        drawablesList = new ArrayList<Drawable>();
        this.setHasOptionsMenu(true);
        
        int currentAPI = android.os.Build.VERSION.SDK_INT;
		if (currentAPI < android.os.Build.VERSION_CODES.JELLY_BEAN) {
			rootView.setBackgroundDrawable(UIElementsHelper.getBackgroundGradientDrawable(mContext));
		} else {
			rootView.setBackground(UIElementsHelper.getBackgroundGradientDrawable(mContext));
		}
		
        sharedPreferences = mContext.getSharedPreferences("com.jams.music.player", Context.MODE_PRIVATE);
        
        //Get the playlist name and file path from the parent activity.
        playlistName = this.getArguments().getString("PLAYLIST_NAME");
        playlistId = this.getArguments().getString("PLAYLIST_ID");
        
        headerImage = (ImageView) rootView.findViewById(R.id.playlist_flipped_header_image);
        playlistsFlippedListView = (DragSortListView) rootView.findViewById(R.id.playlists_flipped_listview);
		playlistsFlippedListView.setFastScrollEnabled(true);
		
		SimpleFloatViewManager simpleFloatViewManager = new SimpleFloatViewManager(playlistsFlippedListView);
		simpleFloatViewManager.setBackgroundColor(Color.TRANSPARENT);
		playlistsFlippedListView.setFloatViewManager(simpleFloatViewManager);

		//Set the background color based on the theme.
		if (sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME")) {
			rootView.setBackgroundColor(0xFFEEEEEE);
			playlistsFlippedListView.setDivider(getResources().getDrawable(R.drawable.transparent_drawable));
			playlistsFlippedListView.setDividerHeight(3);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(7, 3, 7, 3);
			playlistsFlippedListView.setLayoutParams(layoutParams);
		} else if (sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
			rootView.setBackgroundColor(0xFF000000);
			playlistsFlippedListView.setDivider(getResources().getDrawable(R.drawable.transparent_drawable));
			playlistsFlippedListView.setDividerHeight(3);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(7, 3, 7, 3);
			playlistsFlippedListView.setLayoutParams(layoutParams);
		} else if (sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("DARK_THEME")) {
			playlistsFlippedListView.setDivider(getResources().getDrawable(R.drawable.list_divider));
			playlistsFlippedListView.setDividerHeight(1);
        } else {
        	playlistsFlippedListView.setDivider(getResources().getDrawable(R.drawable.list_divider_light));
        	playlistsFlippedListView.setDividerHeight(1);
		}

		String[] columns = { DBAccessHelper.SONG_TITLE, DBAccessHelper.SONG_ARTIST };
        int[] viewIds = { R.id.playlists_flipped_song, R.id.playlists_flipped_artist };
        
        /*//Retrieve the appropriate cursor for the playlist.
        playlistName = playlistName.replace("'", "''");
        selection = " AND " + DBAccessHelper.MUSIC_LIBRARY_PLAYLISTS_NAME + "." 
  			  	  		 + DBAccessHelper.PLAYLIST_NAME 
  			  	  		 + "=" + "'" + playlistName + "'";
        playlistName = playlistName.replace("''", "'");*/
        
        getCursor();
        
        //Retrieve the correct layout for the ListView based on the current theme.
        int layoutId = 0;
        if (sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME") ||
			sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
			layoutId = R.layout.playlist_flipped_listview_cards_layout;
		} else {
			layoutId = R.layout.playlist_flipped_listview_layout;
		}
        
        if (cursor!=null) {
        	ArrayList<String> entryIds = new ArrayList<String>();
        	ArrayList<String> songIds = new ArrayList<String>();
        	while (cursor.moveToNext()) {
        		entryIds.add(cursor.getString(cursor.getColumnIndex(DBAccessHelper.PLAYLIST_SONG_ENTRY_ID)));
        		songIds.add(cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_FILE_PATH)));
        	}
        	
        	adapter = new PlaylistsFlippedListViewAdapter(mContext, 
        												  layoutId, 
        												  null, 
        												  columns, 
        												  viewIds, 
        												  0, 
        												  playlistId, 
        												  playlistName, 
        												  entryIds,
        												  songIds);
        	
        	playlistsFlippedListView.setAdapter(adapter);
        	playlistsFlippedListView.setVisibility(View.VISIBLE);
        	playlistsFlippedListView.setOnItemClickListener(onItemClickListener);
            adapter.changeCursor(cursor);
            
            AsyncGetHeaderImageTask task = new AsyncGetHeaderImageTask();
            task.execute();
            
        } else {

        }
        
        //Check if the playlist has been pinned.
        if (cursor!=null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            String localCopyPath = cursor.getString(cursor.getColumnIndex(DBAccessHelper.LOCAL_COPY_PATH));
            if (localCopyPath==null || localCopyPath.isEmpty()) {
            	mPinned = false;
            } else {
            	mPinned = true;
            	getActivity().invalidateOptionsMenu();
            }
            
        }
        
        //KitKat translucent navigation/status bar.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        	int topPadding = Common.getStatusBarHeight(mContext);

            //Calculate ActionBar height
            TypedValue tv = new TypedValue();
            int actionBarHeight = 0;
            if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
            }
            
            //Calculate navigation bar height.
            int navigationBarHeight = 0;
            int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                navigationBarHeight = getResources().getDimensionPixelSize(resourceId);
            }
            
            playlistsFlippedListView.setClipToPadding(false);
            playlistsFlippedListView.setPadding(0, topPadding + actionBarHeight, 0, navigationBarHeight);
        }
        
        return rootView;
        
    }
    
    public void getCursor() {
        if (sharedPreferences.getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
        	cursor = musicLibraryDBHelper.getAllSongsInPlaylistSearchable(selection);
        } else {
        	//cursor = musicLibraryDBHelper.getLocalSongsInPlaylistSearchable(selection);
        }
        
    }
    
    public OnItemClickListener onItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
			String currentLibrary = sharedPreferences.getString("CURRENT_LIBRARY", mContext.getResources().getString(R.string.all_libraries));
			
			cursor.moveToPosition(index);
			Intent intent = new Intent(mContext, NowPlayingActivity.class);
			intent.putExtra("SELECTED_SONG_DURATION", cursor.getLong(cursor.getColumnIndex(DBAccessHelper.SONG_DURATION)));
			intent.putExtra("SELECTED_SONG_TITLE", cursor.getLong(cursor.getColumnIndex(DBAccessHelper.SONG_TITLE)));
			intent.putExtra("SELECTED_SONG_ARTIST", cursor.getLong(cursor.getColumnIndex(DBAccessHelper.SONG_ARTIST)));
			intent.putExtra("SELECTED_SONG_ALBUM", cursor.getLong(cursor.getColumnIndex(DBAccessHelper.SONG_ALBUM)));
			intent.putExtra("SELECTED_SONG_GENRE", cursor.getLong(cursor.getColumnIndex(DBAccessHelper.SONG_GENRE)));
			intent.putExtra("SONG_SELECTED_INDEX", index);
			intent.putExtra("SELECTED_SONG_DATA_URI", cursor.getLong(cursor.getColumnIndex(DBAccessHelper.SONG_FILE_PATH)));
			intent.putExtra("NEW_PLAYLIST", true);
			intent.putExtra("NUMBER_SONGS", adapter.getCount());
			intent.putExtra("CALLED_FROM_FOOTER", false);
			intent.putExtra("CALLING_FRAGMENT", "PLAYLISTS_FLIPPED_FRAGMENT");
			intent.putExtra("CURRENT_LIBRARY", currentLibrary);
			intent.putExtra("SONG_ID", cursor.getLong(cursor.getColumnIndex(DBAccessHelper.SONG_ID)));
			intent.putExtra("PLAYLIST_NAME", playlistName);
			intent.putExtra("SONG_SOURCE", cursor.getLong(cursor.getColumnIndex(DBAccessHelper.SONG_SOURCE)));
			
			startActivity(intent);
			getActivity().overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
			
		}
    	
    };

    class AsyncPersistChangesTask extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			adapter.persistChanges(playlistName, playlistId);
			return null;
		}
    	
    }
    
    class AsyncGetHeaderImageTask extends AsyncTask<String, String, String> {
    	
		@Override
		protected String doInBackground(String... params) {
            
            //Loop through the five album art paths and store the images in an ArrayList.
			try {
	            for (int i=0; i < 5; i++) {
	            	if (i < cursor.getCount()) {
	            		
	            		BitmapDrawable drawable = null;
	            		try {
	                		cursor.moveToPosition(i);
	                		String albumArtPath = cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_ALBUM_ART_PATH));
	                		Bitmap bitmap = mApp.getImageLoader().loadImageSync(albumArtPath);
	                		
	                		drawable = new BitmapDrawable(getActivity().getResources(), bitmap);
	            		} catch (Exception e) {
	            			e.printStackTrace();
	            			continue;
	            		}

	            		if (drawablesList!=null) {
	            			drawablesList.add(drawable);
	            		}
	            		
	            	} else {
	            		break;
	            	}
	            	
	            }
	            
	            //Load the contents of the ArrayList into the static array.
	            if (drawablesList!=null) {
	                playlistAlbumArt = new Drawable[drawablesList.size()];
	                for (int m=0; m < drawablesList.size(); m++) {
	                	playlistAlbumArt[m] = drawablesList.get(m);
	                }
	                
	            }
	            
			} catch (Exception e) {
				e.printStackTrace();
				return "FAIL";
			}
            
            return "SUCCESS";
		}
		
		@Override
		public void onPostExecute(String result) {
			super.onPostExecute(result);
			
			if (result=="SUCCESS") {
				try {
					if (playlistAlbumArt.length > 0) {
			            CyclicTransitionDrawable cyclicDrawable = new CyclicTransitionDrawable(playlistAlbumArt);
			            headerImage.setImageDrawable(cyclicDrawable);
			            cyclicDrawable.startTransition(1000, 3000);
					}
				} catch (Exception e) {
					e.printStackTrace();
					//Revert to the default header.
					headerImage.setImageResource(R.drawable.default_header_image);
				}
				
			} else {
				//Revert to the default header.
				headerImage.setImageResource(R.drawable.default_header_image);
			}
			
		}
    	
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	if (getActivity().isFinishing()) {
    		drawablesList.clear();
    		drawablesList = null;
    		playlistAlbumArt = null;
    		
    	}

    }
    
    private void playAllPressed() {
    	String currentLibrary = sharedPreferences.getString("CURRENT_LIBRARY", mContext.getResources().getString(R.string.all_libraries));
		
		cursor.moveToPosition(0);
		Intent intent = new Intent(mContext, NowPlayingActivity.class);
		intent.putExtra("SELECTED_SONG_DURATION", cursor.getLong(cursor.getColumnIndex(DBAccessHelper.SONG_DURATION)));
		intent.putExtra("SELECTED_SONG_TITLE", cursor.getLong(cursor.getColumnIndex(DBAccessHelper.SONG_TITLE)));
		intent.putExtra("SELECTED_SONG_ARTIST", cursor.getLong(cursor.getColumnIndex(DBAccessHelper.SONG_ARTIST)));
		intent.putExtra("SELECTED_SONG_ALBUM", cursor.getLong(cursor.getColumnIndex(DBAccessHelper.SONG_ALBUM)));
		intent.putExtra("SELECTED_SONG_GENRE", cursor.getLong(cursor.getColumnIndex(DBAccessHelper.SONG_GENRE)));
		intent.putExtra("SONG_SELECTED_INDEX", 0);
		intent.putExtra("SELECTED_SONG_DATA_URI", cursor.getLong(cursor.getColumnIndex(DBAccessHelper.SONG_FILE_PATH)));
		intent.putExtra("NEW_PLAYLIST", true);
		intent.putExtra("NUMBER_SONGS", adapter.getCount());
		intent.putExtra("CALLED_FROM_FOOTER", false);
		intent.putExtra("CALLING_FRAGMENT", "PLAYLISTS_FLIPPED_FRAGMENT");
		intent.putExtra("CURRENT_LIBRARY", currentLibrary);
		intent.putExtra("SONG_ID", cursor.getLong(cursor.getColumnIndex(DBAccessHelper.SONG_ID)));
		intent.putExtra("PLAYLIST_NAME", playlistName);
		intent.putExtra("SONG_SOURCE", cursor.getLong(cursor.getColumnIndex(DBAccessHelper.SONG_SOURCE)));
		
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
		
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_pin:
			pinButtonPressed();
			return true;
		case R.id.action_play_all:
			playAllPressed();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
    
    private void pinButtonPressed() {
    	//Check if the album artist has any GMusic songs.
    	try {
    		getCursor();
    		boolean allLocal = true;
    		cursor.moveToPosition(-1);
    		while (cursor.moveToNext()) {
    			if (cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_SOURCE)).equals(DBAccessHelper.GMUSIC)) {
    				allLocal = false;
    				break;
    			}
    			
    		}
    		
    		if (allLocal==false && sharedPreferences.getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
    			//Check if the artist has been pinned.
    	        cursor.moveToFirst();
    	        String localCopyPath = cursor.getString(cursor.getColumnIndex(DBAccessHelper.LOCAL_COPY_PATH));
    			if (localCopyPath==null || localCopyPath.isEmpty()) {
    				//Pin the current artist.
    				mPinned = true;
    				getActivity().invalidateOptionsMenu();
    				String toastMessage = getResources().getString(R.string.downloading_no_dot) + " " + playlistName + ".";
    				Toast.makeText(mContext, toastMessage, Toast.LENGTH_SHORT).show();
    				mApp.queueSongsToPin(false, true, selection);
    			} else {
    				//Delete the local copies of the artist's songs.
    				mPinned = true;
    				getActivity().invalidateOptionsMenu();
    				AsyncRemovePinnedSongsTask task = new AsyncRemovePinnedSongsTask(mContext, null, cursor);
    				task.execute();
    			}
    			
    		} else {
    			Toast.makeText(mContext, R.string.all_songs_local, Toast.LENGTH_SHORT).show();
    		}
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    		Toast.makeText(mContext, R.string.error_occurred, Toast.LENGTH_SHORT).show();
    	}
		
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
       inflater.inflate(R.menu.inner_browsers_menu, menu);
       
       if (playlistName==null) {
    	   playlistName = "Unknown Playlist";
       }
       
       SpannableString s = new SpannableString(playlistName);
	   s.setSpan(new TypefaceSpan(getActivity(), "RobotoCondensed-Light"), 0, 
			   						s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	    
	   ActionBar actionBar = getActivity().getActionBar();
	   actionBar.setTitle(s);
	   actionBar.setBackgroundDrawable(UIElementsHelper.getGeneralActionBarBackground(mContext));
	   actionBar.setHomeButtonEnabled(true);
	   
	   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
		   getActivity().getWindow().setBackgroundDrawable(UIElementsHelper.getGeneralActionBarBackground(mContext));
	   }
	   
	   //Set the icons' visibility.
	   if (cursor==null || cursor.getCount() <= 0) {
		   menu.findItem(R.id.action_pin).setVisible(false);
		   menu.findItem(R.id.action_play_all).setVisible(false);
	   }
	   
	   //Set the pin icon.
	   if (menu!=null && mPinned) {
		   menu.findItem(R.id.action_pin).setIcon(R.drawable.pin_highlighted);
	   }
       
    }

}
