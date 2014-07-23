package com.jams.music.player.ArtistsFlippedActivity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.jams.music.player.R;
import com.jams.music.player.ArtistsFlippedSongsActivity.ArtistsFlippedSongsActivity;
import com.jams.music.player.AsyncTasks.AsyncRemovePinnedSongsTask;
import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.NowPlayingActivity.NowPlayingActivity;
import com.jams.music.player.Utils.Common;
import com.jams.music.player.Utils.TypefaceSpan;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class ArtistsFlippedFragment extends Fragment {

	public static Context mContext;
	public static Fragment fragment;
	public static String artistName;
	public static String headerImagePath = "";
	public static String selection = "";
	private boolean mPinned = false;
	
	private static ImageView headerImageView;
	public static TextView emptyViewText;
	public static ImageView emptyViewImage;
	
	public static ListView artistsFlippedListView;
	public static ArtistsFlippedListViewAdapter artistsFlippedListViewAdapter;
	
	public static SharedPreferences sharedPreferences;
	public static int contextMenuItemIndex;
	
	public static DisplayImageOptions displayImageOptions;
	public static DisplayImageOptions displayImageOptionsHeader;
	public static Cursor cursor;
	
	public static boolean DONE_EDITING_TAGS = false;
	private static Common mApp;
	private String localCopyPath;
	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_artists_flipped, container, false);
        mContext = getActivity();
        fragment = this;
        mApp = (Common) mContext.getApplicationContext();
        setHasOptionsMenu(true);
        
        int currentAPI = android.os.Build.VERSION.SDK_INT;
		if (currentAPI < android.os.Build.VERSION_CODES.JELLY_BEAN) {
			rootView.setBackgroundDrawable(UIElementsHelper.getBackgroundGradientDrawable(mContext));
		} else {
			rootView.setBackground(UIElementsHelper.getBackgroundGradientDrawable(mContext));
		}
		
        sharedPreferences = mContext.getSharedPreferences("com.jams.music.player", Context.MODE_PRIVATE);
        
        //Get the artist name from the parent Fragment.
        artistName = this.getArguments().getString("ARTIST_NAME");
        
        //Get the first albumID for the artist (to display their album art in the header).
        headerImagePath = this.getArguments().getString("HEADER_IMAGE_PATH");
        
        //Create a set of options to optimize the bitmap memory usage.
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inJustDecodeBounds = false;
        options.inPurgeable = true;
        
        //Display Image Options.
        displayImageOptions = new DisplayImageOptions.Builder()
								  .showImageForEmptyUri(R.drawable.default_album_art)
								  .showImageOnFail(R.drawable.default_album_art)
								  .showStubImage(R.drawable.transparent_drawable)
								  .cacheInMemory(false)
								  .cacheOnDisc(true)
								  .decodingOptions(options)
								  .imageScaleType(ImageScaleType.EXACTLY)
								  .bitmapConfig(Bitmap.Config.RGB_565)
								  .displayer(new FadeInBitmapDisplayer(400))
								  .delayBeforeLoading(100)
								  .build();   
        
        displayImageOptionsHeader = new DisplayImageOptions.Builder()
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
        
		headerImageView = (ImageView) rootView.findViewById(R.id.artists_flipped_header_image);
        artistsFlippedListView = (ListView) rootView.findViewById(R.id.artistCardFlippedListView);
        emptyViewText = (TextView) rootView.findViewById(R.id.empty_view_text);
        emptyViewImage = (ImageView) rootView.findViewById(R.id.empty_view_image);
        emptyViewImage.setImageResource(UIElementsHelper.getIcon(mContext, "albums"));
        
        emptyViewText.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
        emptyViewText.setPaintFlags(emptyViewText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
	    
	    //If the artist is on Google Play Music, we'll have to retrieve the artist artwork directly from the Internet.
	    setHeaderImageFromUrl(headerImagePath);
	    
	    getCursor();
	    
        artistsFlippedListView.setFastScrollEnabled(true);
        artistsFlippedListViewAdapter = new ArtistsFlippedListViewAdapter(mContext, cursor);
        artistsFlippedListView.setAdapter(artistsFlippedListViewAdapter);

		//Set the background color based on the theme.
		if (sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME")) {
			rootView.setBackgroundColor(0xFFEEEEEE);
			artistsFlippedListView.setDivider(getResources().getDrawable(R.drawable.transparent_drawable));
			artistsFlippedListView.setDividerHeight(10);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(20, 20, 20, 20);
			artistsFlippedListView.setLayoutParams(layoutParams);
		} else if (sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
			rootView.setBackgroundColor(0xFF111111);
			artistsFlippedListView.setDivider(getResources().getDrawable(R.drawable.transparent_drawable));
			artistsFlippedListView.setDividerHeight(10);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(20, 20, 20, 20);
			artistsFlippedListView.setLayoutParams(layoutParams);
		} else if (sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("DARK_THEME")) {
			artistsFlippedListView.setDivider(getResources().getDrawable(R.drawable.list_divider));
			artistsFlippedListView.setDividerHeight(1);
        } else {
        	artistsFlippedListView.setDivider(getResources().getDrawable(R.drawable.list_divider_light));
        	artistsFlippedListView.setDividerHeight(1);
		}
		
        //Check if the artist has been pinned.
        if (cursor!=null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            localCopyPath = cursor.getString(cursor.getColumnIndex(DBAccessHelper.LOCAL_COPY_PATH));
            if (localCopyPath==null || localCopyPath.isEmpty()) {
            	mPinned = false;
            } else {
            	//Set the pinned flag to true and redraw the actionbar.
            	mPinned = true;
            	getActivity().invalidateOptionsMenu();
            }
            
        }
		
		artistsFlippedListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index, long id) {
				
				//Retrieve the clicked item's album/artist name from the child view.
				String albumName = (String) view.getTag(R.string.album);
				String artistName = (String) view.getTag(R.string.artist);
				String albumArtPath = (String) view.getTag(R.string.album_art);
				
				Intent intent = new Intent(getActivity(), ArtistsFlippedSongsActivity.class);
			    intent.putExtra("ALBUM_NAME", albumName);
			    intent.putExtra("ARTIST_NAME", artistName);
			    intent.putExtra("ART_SOURCE", "");
			    intent.putExtra("HEADER_IMAGE_PATH", albumArtPath);
			    
			    startActivity(intent);
			    getActivity().overridePendingTransition(R.anim.fade_in, R.anim.scale_and_fade_out);
			    
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
            
            artistsFlippedListView.setClipToPadding(false);
            artistsFlippedListView.setPadding(0, 0, 0, navigationBarHeight);
        }
        
        return rootView;
    }
    
    public static void getCursor() {
	    String currentLibrary = sharedPreferences.getString(Common.CURRENT_LIBRARY, mContext.getResources().getString(R.string.all_libraries));
	    currentLibrary = currentLibrary.replace("'", "''");
	    artistName = artistName.replace("'", "''");
    	
	    if (currentLibrary.equals(mContext.getResources().getString(R.string.all_libraries))) {
	    	if (sharedPreferences.getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
	    		selection = " AND " + DBAccessHelper.SONG_ARTIST + "=" + "'" + artistName + "'";
	    	} else {
	    		selection = " AND " + DBAccessHelper.SONG_ARTIST + "=" + "'" + artistName + "'"
	    				  + " AND " + DBAccessHelper.SONG_SOURCE + " <> " + "'GOOGLE_PLAY_MUSIC'";
	    	}
		    
	        cursor = mApp.getDBAccessHelper().getAllUniqueAlbumsByArtist(selection);
	    } else if (currentLibrary.equals(mContext.getResources().getString(R.string.google_play_music_no_asterisk))) {
	    	if (sharedPreferences.getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==false) {
	    		emptyViewText.setText(R.string.google_play_music_disabled_info);
	    		emptyViewImage.setImageResource(UIElementsHelper.getIcon(mContext, "cloud_settings"));
	    	}
		    selection = " AND " + DBAccessHelper.SONG_SOURCE + "=" + "'GOOGLE_PLAY_MUSIC'" + " AND "
		    		  + DBAccessHelper.SONG_ARTIST + "=" + "'" + artistName + "'";
	        cursor = mApp.getDBAccessHelper().getAllUniqueAlbumsByArtist(selection);
	    } else if (currentLibrary.equals(mContext.getResources().getString(R.string.on_this_device))) { 
	    	//Check if Google Play Music is enabled.
	    	if (sharedPreferences.getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
	    		selection = " AND " + DBAccessHelper.SONG_ARTIST + "=" + "'" + artistName + "'"
	    				  + " AND (" + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'" + " OR "
	    				  + DBAccessHelper.LOCAL_COPY_PATH + "<> '')";
	    	} else {
	    		selection = " AND " + DBAccessHelper.SONG_ARTIST + "=" + "'" + artistName + "'"
	    				  + " AND " + DBAccessHelper.SONG_SOURCE + " <> " + "'GOOGLE_PLAY_MUSIC'";
	    	}
	    	
	    	cursor = mApp.getDBAccessHelper().getAllUniqueAlbumsByArtist(selection);
    	} else {
	    	if (sharedPreferences.getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
		    	selection = " AND " + DBAccessHelper.SONG_ARTIST + "=" + "'" + artistName + "'" + " AND "
		    			  + DBAccessHelper.LIBRARY_NAME + "=" + "'" + currentLibrary + "'";;
	    	} else {
		    	selection = " AND " + DBAccessHelper.SONG_ARTIST + "=" + "'" + artistName + "'" + " AND "
		    			  + DBAccessHelper.LIBRARY_NAME + "=" + "'" + currentLibrary + "'" + " AND "
		    			  + DBAccessHelper.SONG_SOURCE + " <> " + "'GOOGLE_PLAY_MUSIC'";
	    	}

	        cursor = mApp.getDBAccessHelper().getAllUniqueAlbumsByArtistInLibrary(selection);
	    }
	    artistName = artistName.replace("''", "'");
	}
    
	//Set the header image from the specified url.
    public static void setHeaderImageFromUrl(final String url) {
    	mApp.getImageLoader().displayImage(url, headerImageView, displayImageOptionsHeader);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
       inflater.inflate(R.menu.inner_browsers_menu, menu);
       
       if (artistName==null) {
    	   artistName = "Unknown Artist";
       }
       
       SpannableString s = new SpannableString(artistName);
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

    public void pinButtonPressed() {
    	//Check if the album artist has any GMusic songs.
		getCursor();
		boolean allLocal = true;
		cursor.moveToPosition(-1);
		Cursor tempCursor = null;
		while (cursor.moveToNext()) {
			String albumName = cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_ALBUM));
			tempCursor = mApp.getDBAccessHelper().getAllSongsInAlbum(albumName, artistName);
			
			if (tempCursor!=null && tempCursor.getCount() > 0) {
				tempCursor.moveToPosition(-1);
				while (tempCursor.moveToNext()) {
					if (tempCursor.getString(tempCursor.getColumnIndex(DBAccessHelper.SONG_SOURCE)).equals(DBAccessHelper.GMUSIC)) {
						allLocal = false;
						break;
					}
				}
				
				if (allLocal==false) {
					break;
				}
				
			} else {
				continue;
			}
			
		}
		tempCursor.close();
		tempCursor = null;
		
		if (allLocal==false && sharedPreferences.getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
			//Check if the artist has been pinned.
	        cursor.moveToFirst();
	        localCopyPath = cursor.getString(cursor.getColumnIndex(DBAccessHelper.LOCAL_COPY_PATH));
			if (localCopyPath==null || localCopyPath.isEmpty()) {
				//Pin the current artist.
				mPinned = true;
				getActivity().invalidateOptionsMenu();
				String toastMessage = getResources().getString(R.string.downloading_no_dot) + " " + artistName + ".";
				Toast.makeText(mContext, toastMessage, Toast.LENGTH_SHORT).show();
				mApp.queueSongsToPin(false, false, selection);
			} else {
				//Delete the local copies of the artist's songs.
				mPinned = false;
				getActivity().invalidateOptionsMenu();
				AsyncRemovePinnedSongsTask task = new AsyncRemovePinnedSongsTask(mContext, selection, null);
				task.execute();
			}
		} else {
			Toast.makeText(mContext, R.string.all_songs_local, Toast.LENGTH_SHORT).show();
		}
		
    }
    
    public void playAllPressed() {
    	String artist = "";
		if (artistsFlippedListView.getChildAt(0)!=null) {
			artist = (String) artistsFlippedListView.getChildAt(0).getTag(R.string.artist);
		} else {
			Toast.makeText(mContext, R.string.artist_doesnt_have_any_songs, Toast.LENGTH_LONG).show();
		}
		
		String currentLibrary = sharedPreferences.getString(Common.CURRENT_LIBRARY, mContext.getResources().getString(R.string.all_libraries));
		Intent intent = new Intent(getActivity(), NowPlayingActivity.class);
		
		cursor.moveToFirst();
		intent.putExtra("SELECTED_SONG_DURATION", cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_DURATION)));
		intent.putExtra("SELECTED_SONG_TITLE", cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_TITLE)));
		intent.putExtra("SELECTED_SONG_ARTIST", artist);
		intent.putExtra("SELECTED_SONG_ALBUM", cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_ALBUM)));
		intent.putExtra("SONG_SELECTED_INDEX", 0);
		intent.putExtra("SELECTED_SONG_DATA_URI", cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_FILE_PATH)));
		intent.putExtra("SELECTED_SONG_GENRE", cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_GENRE)));
		intent.putExtra("NEW_PLAYLIST", true);
		intent.putExtra("NUMBER_SONGS", cursor.getCount());
		intent.putExtra("CALLED_FROM_FOOTER", false);
		intent.putExtra("PLAY_ALL", "ARTIST");
		intent.putExtra("CALLING_FRAGMENT", "ARTISTS_FLIPPED_FRAGMENT");
		intent.putExtra(Common.CURRENT_LIBRARY, currentLibrary);
		
		
		getActivity().startActivity(intent);
		getActivity().overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
		
    }
    
	@Override
    public void onDestroyView() {
    	super.onDestroyView();
    	if (artistsFlippedListViewAdapter!=null) {

    		if (ArtistsFlippedListViewAdapter.cursor!=null) {
    			ArtistsFlippedListViewAdapter.cursor.close();
    			ArtistsFlippedListViewAdapter.cursor = null;
    		}
    		
    	}
    	
    }
    
}
