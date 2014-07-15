package com.jams.music.player.AlbumArtistsFlippedSongsActivity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jams.music.player.R;
import com.jams.music.player.AsyncTasks.AsyncRemovePinnedSongsTask;
import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.NowPlayingActivity.NowPlayingActivity;
import com.jams.music.player.Utils.Common;
import com.jams.music.player.Utils.TypefaceSpan;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class AlbumArtistsFlippedSongsFragment extends Fragment {

	public static Context mContext;
	public static AlbumArtistsFlippedSongsFragment fragment;
	public static String albumName;
	public static String albumArtistName;
	private String headerImagePath;
	private static ImageView emptyViewImage;
	private static TextView emptyViewText;
	private boolean mPinned = false;
	
	private ImageView cardFlippedHeaderBackgroundImage;
	public static ListView songsListView;
	public static String selection = DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'";
	public static DBAccessHelper musicLibraryDBHelper;
	public static Cursor cursor;
	public static AlbumArtistsFlippedSongsListViewAdapter songsListViewAdapter;
	public static View childView;
	
	public static int contextMenuItemIndex;
	public static SharedPreferences sharedPreferences;
	private Handler mHandler;
	public static boolean DONE_EDITING_TAGS = false;
	private int index;
	private Common mApp;
	private String localCopyPath;
	private DisplayImageOptions displayImageOptions;
	
    @SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_artists_flipped_songs, container, false);
        mContext = getActivity();
        fragment = this;
        mHandler = new Handler();
        mApp = (Common) mContext.getApplicationContext();
        this.setHasOptionsMenu(true);
        
        int currentAPI = android.os.Build.VERSION.SDK_INT;
		if (currentAPI < android.os.Build.VERSION_CODES.JELLY_BEAN) {
			rootView.setBackgroundDrawable(UIElementsHelper.getBackgroundGradientDrawable(mContext));
		} else {
			rootView.setBackground(UIElementsHelper.getBackgroundGradientDrawable(mContext));
		}
		
        sharedPreferences = mContext.getSharedPreferences("com.jams.music.player", Context.MODE_PRIVATE);
        
	    //Get the album name.
	    albumName = this.getArguments().getString("ALBUM_NAME");
	    
	    //Get the artist name.
	    albumArtistName = this.getArguments().getString("ALBUM_ARTIST_NAME");
	    
	    //Create a set of options to optimize the bitmap memory usage.
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inJustDecodeBounds = false;
        options.inPurgeable = true;
        
        //Display Image Options.
        displayImageOptions = new DisplayImageOptions.Builder()
        						  .showImageForEmptyUri(R.drawable.default_header_image)
        						  .showImageOnFail(R.drawable.default_header_image)
        						  .showStubImage(R.drawable.transparent_drawable)
        						  .cacheInMemory(false)
        						  .cacheOnDisc(true)
        						  .decodingOptions(options)
        						  .imageScaleType(ImageScaleType.EXACTLY)
        						  .bitmapConfig(Bitmap.Config.RGB_565)
        						  .displayer(new FadeInBitmapDisplayer(400))
        						  .delayBeforeLoading(100)
        						  .build();
        
	    //Get the path to the header image and the source type.
	    headerImagePath = this.getArguments().getString("HEADER_IMAGE_PATH");
        
	    cardFlippedHeaderBackgroundImage = (ImageView) rootView.findViewById(R.id.artistsFlippedSongsHeaderBackground);
	    setHeaderImage(headerImagePath);

        musicLibraryDBHelper = new DBAccessHelper(mContext);
        getCursor();
        
        //Set the adapter for the AsyncListView.
        songsListView = (ListView) rootView.findViewById(R.id.artistsCardFlippedSongsListView);
        songsListViewAdapter = new AlbumArtistsFlippedSongsListViewAdapter(mContext, cursor);
        
        songsListView.setAdapter(songsListViewAdapter);
        songsListView.setFastScrollEnabled(true);

        //Check if the album has been pinned.
        if (cursor!=null && cursor.getCount() > 0) {
        	cursor.moveToFirst();
        	localCopyPath = cursor.getString(cursor.getColumnIndex(DBAccessHelper.LOCAL_COPY_PATH));
        	if (localCopyPath==null || localCopyPath.isEmpty()) {
        		mPinned = false;
        	} else {
        		mPinned = true;
        		getActivity().invalidateOptionsMenu();
        	}
        	
        }
        
        if (sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME") ||
        	sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME")) {
        	RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(20, 20, 20, 20);
			songsListView.setLayoutParams(layoutParams);
        }
        
		//Set the background color based on the theme.
		if (sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME")) {
			rootView.setBackgroundColor(0xFFEEEEEE);
			songsListView.setDivider(getResources().getDrawable(R.drawable.transparent_drawable));
			songsListView.setDividerHeight(10);
		} else if (sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
			rootView.setBackgroundColor(0xFF111111);
			songsListView.setDivider(getResources().getDrawable(R.drawable.transparent_drawable));
			songsListView.setDividerHeight(10);
		} else if (sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("DARK_THEME")) {
			songsListView.setDivider(getResources().getDrawable(R.drawable.list_divider));
			songsListView.setDividerHeight(1);
        } else {
        	songsListView.setDivider(getResources().getDrawable(R.drawable.list_divider_light));
        	songsListView.setDividerHeight(1);
		}

		songsListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index, long id) {
	            
				String currentLibrary = sharedPreferences.getString("CURRENT_LIBRARY", mContext.getResources().getString(R.string.all_libraries));
				Intent intent = new Intent(getActivity(), NowPlayingActivity.class);
				intent.putExtra("SELECTED_SONG_DURATION", (String) view.getTag(R.string.duration));
				intent.putExtra("SELECTED_SONG_TITLE", (String) view.getTag(R.string.title));
				intent.putExtra("SELECTED_SONG_ALBUM_ARTIST", (String) view.getTag(R.string.album_artist));
				intent.putExtra("SELECTED_SONG_ALBUM", (String) view.getTag(R.string.album));
				intent.putExtra("SELECTED_SONG_GENRE", (String) view.getTag(R.string.genre));
				intent.putExtra("SONG_SELECTED_INDEX", index);
				intent.putExtra("SELECTED_SONG_DATA_URI", (String) view.getTag(R.string.song_file_path));
				intent.putExtra("NEW_PLAYLIST", true);
				intent.putExtra("NUMBER_SONGS", songsListViewAdapter.getCount());
				intent.putExtra("CALLED_FROM_FOOTER", false);
				intent.putExtra("PLAY_ALL", "ALBUM");
				intent.putExtra("CALLING_FRAGMENT", "ALBUM_ARTISTS_FLIPPED_SONGS_FRAGMENT");
				intent.putExtra("SONG_ID", (String) view.getTag(R.string.song_id));
				intent.putExtra("CURRENT_LIBRARY", currentLibrary);
				
				//Notify NowPlayingActivity.java if the song is coming from Google's servers.
				if (((String) view.getTag(R.string.song_source)).equals(DBAccessHelper.GMUSIC)) {
					intent.putExtra("SONG_SOURCE", DBAccessHelper.GMUSIC);
				} else {
					intent.putExtra("SONG_SOURCE", "LOCAL_FILE");
				}
				
				getActivity().startActivity(intent);
				getActivity().overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
			
			}
			
		});
        
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
            
            rootView.setPadding(0, topPadding + actionBarHeight, 0, 0);
            songsListView.setClipToPadding(false);
            songsListView.setPadding(0, 0, 0, navigationBarHeight);
        }
		
        return rootView;
    }
    
	public static void getCursor() {
	    /*String currentLibrary = sharedPreferences.getString("CURRENT_LIBRARY", mContext.getResources().getString(R.string.all_libraries));
	    currentLibrary = currentLibrary.replace("'", "''");
		albumArtistName = albumArtistName.replace("'", "''");
		albumName = albumName.replace("'", "''");
	    
	    if (currentLibrary.equals(mContext.getResources().getString(R.string.all_libraries))) {
	    	if (sharedPreferences.getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
	    		selection = " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + albumName  + "'" + " AND "
						  + DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + albumArtistName + "'";
	    	} else {
	    		selection = " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + albumName  + "'" + " AND "
						  + DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + albumArtistName + "'"
						  + " AND " + DBAccessHelper.SONG_SOURCE + " <> " + "'GOOGLE_PLAY_MUSIC'";
	    	}
		    
	        cursor = musicLibraryDBHelper.getAllSongsByAlbumArtistAlbum(selection);
	    } else if (currentLibrary.equals(mContext.getResources().getString(R.string.google_play_music_no_asterisk))) {
	    	if (sharedPreferences.getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
	    		selection = " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + albumName  + "'" + " AND "
						  + DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + albumArtistName + "'" + " AND " + 
						  DBAccessHelper.SONG_SOURCE + "=" + "'GOOGLE_PLAY_MUSIC'";
	    		cursor = musicLibraryDBHelper.getAllSongsByAlbumArtistAlbum(selection);
	    	} else {
	    		emptyViewText.setHint(R.string.google_play_music_disabled_info);
	    		emptyViewImage.setImageResource(UIElementsHelper.getIcon(mContext, "cloud"));
	    		cursor = null;
	    	}
		    
	    } else if (currentLibrary.equals(mContext.getResources().getString(R.string.on_this_device))) { 
	    	//Check if Google Play Music is enabled.
	    	if (sharedPreferences.getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
	    		selection = " AND " + DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + albumArtistName + "'"
	    				  + " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + albumName + "'"
	    				  + " AND (" + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'" + " OR "
	    				  + DBAccessHelper.LOCAL_COPY_PATH + "<> '')";
	    		cursor = musicLibraryDBHelper.getAllSongsByAlbumArtistAlbum(selection);
	    	} else {
	    		selection = " AND " + DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + albumArtistName + "'"
	    				  + " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + albumName + "'"
	    				  + " AND " + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'"; 
		        cursor = musicLibraryDBHelper.getAllSongsByAlbumArtistAlbum(selection);
	    	}
	    	
    	} else {
	    	if (sharedPreferences.getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
	    		selection = " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + albumName  + "'" + " AND "
						  + DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + albumArtistName + "'" + " AND " 
						  + DBAccessHelper.LIBRARY_NAME + "=" + "'" + currentLibrary + "'";
	    	} else {selection = " AND " + DBAccessHelper.SONG_ALBUM + "=" + "'" + albumName  + "'" + " AND "
					  + DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + albumArtistName + "'" + " AND " 
					  + DBAccessHelper.LIBRARY_NAME + "=" + "'" + currentLibrary + "'" + " AND "
					  + DBAccessHelper.SONG_SOURCE + " <> " + "'GOOGLE_PLAY_MUSIC'";
	    	}
	        cursor = musicLibraryDBHelper.getAllSongsByAlbumArtistAlbumInLibrary(selection);
	    }
	    albumArtistName = albumArtistName.replace("''", "'");
	    albumName = albumName.replace("''", "'");*/
	}
    
    //Load album art from Google's servers.
    private void setHeaderImage(String url) {
    	mApp.getImageLoader().displayImage(url, cardFlippedHeaderBackgroundImage, displayImageOptions);
    }
    
    //Create a Runnable that will update the CursorLoader as soon as the EditTags fragment is dismissed.
    public Runnable reloadCursorLoaderRunnable = new Runnable() {

		@Override
		public void run() {
			
			if (DONE_EDITING_TAGS==true) {
				songsListViewAdapter.notifyDataSetChanged();
				songsListView.invalidate();
				songsListView.setAdapter(songsListViewAdapter);
				songsListView.smoothScrollToPosition(index);
			} else {
				mHandler.postDelayed(reloadCursorLoaderRunnable, 100);
			}
			
		}
    	
    };
   
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
       inflater.inflate(R.menu.inner_browsers_menu, menu);
       
       if (albumName==null) {
    	   albumName = "Unknown Album";
       }
       
       SpannableString s = new SpannableString(albumName);
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
		getCursor();
		boolean allLocal = true;
		cursor.moveToPosition(-1);
		while (cursor.moveToNext()) {
			if (cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_SOURCE)).equals(DBAccessHelper.GMUSIC)) {
				allLocal = false;
				break;
			}
			
		}
		
		if (allLocal==false) {
			//Check if the album has been pinned.
	        cursor.moveToFirst();
	        localCopyPath = cursor.getString(cursor.getColumnIndex(DBAccessHelper.LOCAL_COPY_PATH));
			if (localCopyPath==null || localCopyPath.isEmpty()) {
				//Pin the current album.
				mPinned = true;
				getActivity().invalidateOptionsMenu();
				String toastMessage = getResources().getString(R.string.downloading_no_dot) + " " + albumName + ".";
				Toast.makeText(mContext, toastMessage, Toast.LENGTH_SHORT).show();
				mApp.queueSongsToPin(false, false, selection);
			} else {
				//Delete the local copies of the album's songs.
				mPinned = false;
				getActivity().invalidateOptionsMenu();
				AsyncRemovePinnedSongsTask task = new AsyncRemovePinnedSongsTask(mContext, selection, null);
				task.execute();
			}
		} else {
			Toast.makeText(mContext, R.string.all_songs_local, Toast.LENGTH_SHORT).show();
		}
		
    }
    
    private void playAllPressed() {
    	String currentLibrary = sharedPreferences.getString("CURRENT_LIBRARY", mContext.getResources().getString(R.string.all_libraries));
		Intent intent = new Intent(getActivity(), NowPlayingActivity.class);
		intent.putExtra("SELECTED_SONG_DURATION", (String) songsListView.getChildAt(0).getTag(R.string.duration));
		intent.putExtra("SELECTED_SONG_TITLE", (String) songsListView.getChildAt(0).getTag(R.string.title));
		intent.putExtra("SELECTED_SONG_ALBUM_ARTIST", (String) songsListView.getChildAt(0).getTag(R.string.album_artist));
		intent.putExtra("SELECTED_SONG_ALBUM", (String) songsListView.getChildAt(0).getTag(R.string.album));
		intent.putExtra("SELECTED_SONG_GENRE", (String) songsListView.getChildAt(0).getTag(R.string.genre));
		intent.putExtra("SONG_SELECTED_INDEX", 0);
		intent.putExtra("SELECTED_SONG_DATA_URI", (String) songsListView.getChildAt(0).getTag(R.string.song_file_path));
		intent.putExtra("NEW_PLAYLIST", true);
		intent.putExtra("NUMBER_SONGS", songsListView.getCount());
		intent.putExtra("CALLED_FROM_FOOTER", false);
		intent.putExtra("PLAY_ALL", "ALBUM");
		intent.putExtra("CALLING_FRAGMENT", "ALBUM_ARTISTS_FLIPPED_SONGS_FRAGMENT");
		intent.putExtra("CURRENT_LIBRARY", currentLibrary);
		
		getActivity().startActivity(intent);
		getActivity().overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
		
    }
    
    @Override
    public void onDestroyView() {
    	super.onDestroyView();
    	musicLibraryDBHelper.close();
    	cursor.close();
    	
    }
    
}