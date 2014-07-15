package com.jams.music.player.GenresFlippedActivity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.jams.music.player.R;
import com.jams.music.player.AsyncTasks.AsyncRemovePinnedSongsTask;
import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.GenresFlippedSongsActivity.GenresFlippedSongsActivity;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.NowPlayingActivity.NowPlayingActivity;
import com.jams.music.player.Utils.Common;
import com.jams.music.player.Utils.TypefaceSpan;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class GenresFlippedFragment extends Fragment {

	private static Context mContext;
	public static GenresFlippedFragment fragment;
	private static String genreName;
	private static SharedPreferences sharedPreferences;
	
	private boolean mPinned = false;
	private ImageView headerImage;
	private static ImageView emptyViewImage;
	private static TextView emptyViewText;
	
	public static ListView genresFlippedListView;
	public static int contextMenuItemIndex;
	public static GenresFlippedListViewAdapter genresFlippedListViewAdapter;
	public static DisplayImageOptions displayImageOptions;
	public static DisplayImageOptions displayImageOptionsHeader;
	private static DBAccessHelper musicLibraryDBHelper;
	private static Cursor cursor;
	private static String selection = "";
	private Common mApp;
	
	private String albumArtPath;
	private String localCopyPath;
	
    @SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_genres_flipped, container, false);
        mContext = getActivity();
        fragment = this;
        mApp = (Common) mContext.getApplicationContext();
        this.setHasOptionsMenu(true);
        
        int currentAPI = android.os.Build.VERSION.SDK_INT;
		if (currentAPI < android.os.Build.VERSION_CODES.JELLY_BEAN) {
			rootView.setBackgroundDrawable(UIElementsHelper.getBackgroundGradientDrawable(mContext));
		} else {
			rootView.setBackground(UIElementsHelper.getBackgroundGradientDrawable(mContext));
		}
		
        sharedPreferences = mContext.getSharedPreferences("com.jams.music.player", Context.MODE_PRIVATE);
        
        //Get the genre name and file path from the parent activity.
        genreName = this.getArguments().getString("GENRE_NAME");
        albumArtPath = this.getArguments().getString("ALBUM_ART_PATH");
        
	    genresFlippedListView = (ListView) rootView.findViewById(R.id.genres_flipped_listview);
        genresFlippedListView.setFastScrollEnabled(true);
        
        selection = DBAccessHelper.SONG_GENRE + "=" + "'" + genreName + "'" + " AND ";
	    musicLibraryDBHelper = new DBAccessHelper(mContext);
	    
	    AsyncLoadCursorTask task = new AsyncLoadCursorTask();
	    task.execute();
        
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
        
        emptyViewText = (TextView) rootView.findViewById(R.id.empty_view_text);
        emptyViewImage = (ImageView) rootView.findViewById(R.id.empty_view_image);
        emptyViewImage.setImageResource(UIElementsHelper.getIcon(mContext, "albums"));
        
        emptyViewText.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
        emptyViewText.setPaintFlags(emptyViewText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        
        //Set the background color based on the theme.
  		if (sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME")) {
  			rootView.setBackgroundColor(0xFFEEEEEE);
  			genresFlippedListView.setDivider(getResources().getDrawable(R.drawable.transparent_drawable));
			genresFlippedListView.setDividerHeight(3);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(20, 20, 20, 20);
			genresFlippedListView.setLayoutParams(layoutParams);
  		} else if (sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
  			rootView.setBackgroundColor(0xFF000000);
  			genresFlippedListView.setDivider(getResources().getDrawable(R.drawable.transparent_drawable));
			genresFlippedListView.setDividerHeight(3);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(20, 20, 20, 20);
			genresFlippedListView.setLayoutParams(layoutParams);
  		} else if (sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("DARK_THEME")) {
			genresFlippedListView.setDivider(getResources().getDrawable(R.drawable.list_divider));
			genresFlippedListView.setDividerHeight(1);
        } else {
        	genresFlippedListView.setDivider(getResources().getDrawable(R.drawable.list_divider_light));
        	genresFlippedListView.setDividerHeight(1);
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
            
            rootView.setPadding(0, topPadding + actionBarHeight, 0, 0);
            genresFlippedListView.setClipToPadding(false);
            genresFlippedListView.setPadding(0, 0, 0, navigationBarHeight);
        }

        headerImage = (ImageView) rootView.findViewById(R.id.genres_flipped_header_image);
        setHeaderImageFromUrl(albumArtPath);
        return rootView;
        
    }
    
    public static void getCursor() {
    	String currentLibrary = sharedPreferences.getString("CURRENT_LIBRARY", mContext.getResources().getString(R.string.all_libraries));
	    currentLibrary = currentLibrary.replace("'", "''");
		
        if (genreName.contains("'")) {
        	genreName = genreName.replace("'", "''");
        }	    
	    
	    if (currentLibrary.equals(mContext.getResources().getString(R.string.all_libraries))) {
	    	if (sharedPreferences.getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
	    		selection = " AND " + DBAccessHelper.SONG_GENRE + "=" + "'" + genreName + "'";
	    	} else {
	    		selection = " AND " + DBAccessHelper.SONG_GENRE + "=" + "'" + genreName + "'"
						  + " AND " + DBAccessHelper.SONG_SOURCE + " <> " + "'GOOGLE_PLAY_MUSIC'";
	    	}
		    
	        cursor = musicLibraryDBHelper.getAllUniqueAlbumsInGenre(selection);
	    } else if (currentLibrary.equals(mContext.getResources().getString(R.string.google_play_music_no_asterisk))) {
	    	if (sharedPreferences.getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
	    		selection = " AND " + DBAccessHelper.SONG_GENRE + "=" + "'" + genreName  + "'"
	    				  + " AND " +  DBAccessHelper.SONG_SOURCE + "=" + "'GOOGLE_PLAY_MUSIC'";
	    		cursor = musicLibraryDBHelper.getAllUniqueAlbumsInGenre(selection);
	    	} else {
	    		emptyViewText.setHint(R.string.google_play_music_disabled_info);
	    		emptyViewImage.setImageResource(UIElementsHelper.getIcon(mContext, "cloud"));
	    		cursor = null;
	    	}
		    
	    } else if (currentLibrary.equals(mContext.getResources().getString(R.string.on_this_device))) { 
	    	//Check if Google Play Music is enabled.
	    	if (sharedPreferences.getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
	    		selection = " AND " + DBAccessHelper.SONG_GENRE + "=" + "'" + genreName + "'"
	    			      + " AND (" + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'" + " OR "
	    				  + DBAccessHelper.LOCAL_COPY_PATH + "<> '')";
	    		cursor = musicLibraryDBHelper.getAllUniqueAlbumsInGenre(selection);
	    		
	    	} else {
	    		selection = " AND " + DBAccessHelper.SONG_GENRE + "=" + "'" + genreName + "'"
	    				  + " AND " + DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'"; 
	    		cursor = musicLibraryDBHelper.getAllUniqueAlbumsInGenre(selection);
	    	}
	    	
    	} else {
	    	if (sharedPreferences.getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
	    		selection = " AND " + DBAccessHelper.SONG_GENRE + "=" + "'" + genreName  + "'" + " AND " 
						  + DBAccessHelper.LIBRARY_NAME + "=" + "'" + currentLibrary + "'";
	    	} else {
	    		selection = " AND " + DBAccessHelper.SONG_GENRE + "=" + "'" + genreName  + "'" + " AND " 
	    				  + DBAccessHelper.LIBRARY_NAME + "=" + "'" + currentLibrary + "'" + " AND "
	    				  + DBAccessHelper.SONG_SOURCE + " <> " + "'GOOGLE_PLAY_MUSIC'";
	    	}
	        cursor = musicLibraryDBHelper.getAllUniqueAlbumsInGenreInLibrary(selection);
	    }
	    
        if (genreName.contains("''")) {
        	genreName = genreName.replace("''", "'");
        }
		
	}

	//Set the header image from the specified url.
    public void setHeaderImageFromUrl(final String url) {
    	mApp.getImageLoader().displayImage(url, headerImage, displayImageOptionsHeader);
    }
    
    public void setAdapter() {
    	//Check if the cursor is null. If so, show an error message.
        if (cursor!=null && cursor.getCount() > 0) {
        	genresFlippedListViewAdapter = new GenresFlippedListViewAdapter(mContext, cursor);
        	genresFlippedListView.setAdapter(genresFlippedListViewAdapter);
        } else {
    		emptyViewText.setVisibility(View.VISIBLE);
    		emptyViewImage.setVisibility(View.VISIBLE);
    		genresFlippedListView.setVisibility(View.GONE);
        }
        
        PauseOnScrollListener listener = new PauseOnScrollListener(mApp.getImageLoader(), true, true);
        genresFlippedListView.setOnScrollListener(listener);
        
        genresFlippedListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
				
				//Retrieve the clicked item's album/artist name from the child view.
				String albumName = (String) view.getTag(R.string.album);
				String albumArtPath = (String) view.getTag(R.string.album_art);
				
				Intent intent = new Intent(getActivity(), GenresFlippedSongsActivity.class);
			    intent.putExtra("ALBUM_NAME", albumName);
			    intent.putExtra("GENRE_NAME", genreName);
			    intent.putExtra("ART_SOURCE", "");
			    intent.putExtra("HEADER_IMAGE_PATH", albumArtPath);
			    
			    startActivity(intent);
			    getActivity().overridePendingTransition(R.anim.fade_in, R.anim.scale_and_fade_out);
				
			}
        	
        });
        
        //Check if the genre has been pinned.
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
        
    }
    
    @Override
    public void onDestroyView() {
    	super.onDestroyView();
    	
    	if (musicLibraryDBHelper!=null) {
        	musicLibraryDBHelper.close();
        	musicLibraryDBHelper = null;
    	}

    	if (cursor!=null) {
        	cursor.close();
        	cursor = null;
    	}

    	mContext = null;
    	
    }
    
    class AsyncLoadCursorTask extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... arg0) {
			getCursor();
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			
			setAdapter();
			
			AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
			fadeIn.setDuration(400);
			
			fadeIn.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationEnd(Animation animation) {
					genresFlippedListView.setVisibility(View.VISIBLE);
					
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub
					
				}
				
			});
			
			genresFlippedListView.startAnimation(fadeIn);
			
		}
    	
    }
    
    public void pinButtonPressed() {
    	//Check if the genre has any GMusic songs.
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
			//Check if the genre has been pinned.
	        cursor.moveToFirst();
	        localCopyPath = cursor.getString(cursor.getColumnIndex(DBAccessHelper.LOCAL_COPY_PATH));
			if (localCopyPath==null || localCopyPath.isEmpty()) {
				//Pin the current album.
				mPinned = true;
				getActivity().invalidateOptionsMenu();
				String toastMessage = getResources().getString(R.string.downloading_no_dot) + " " + genreName + ".";
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
    
    public void playAllPressed() {
    	DBAccessHelper dbHelper = new DBAccessHelper(getActivity().getApplicationContext());
		Cursor cursor = dbHelper.getAllSongsInGenre(selection);
		
		if (cursor!=null && cursor.getCount() > 0) {
        	cursor.moveToFirst();
        	String currentLibrary = sharedPreferences.getString("CURRENT_LIBRARY", mContext.getResources().getString(R.string.all_libraries));
        	
        	Intent intent = new Intent(getActivity(), NowPlayingActivity.class);
			intent.putExtra("SELECTED_SONG_DURATION", cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_DURATION)));
			intent.putExtra("SELECTED_SONG_TITLE", cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_TITLE)));
			intent.putExtra("SELECTED_SONG_ARTIST", cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_ARTIST)));
			intent.putExtra("SELECTED_SONG_ALBUM", cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_ALBUM)));
			intent.putExtra("SELECTED_SONG_GENRE", cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_GENRE)));
			intent.putExtra("SONG_SELECTED_INDEX", 0);
			intent.putExtra("CURRENT_LIBRARY", sharedPreferences.getString("CURRENT_LIBRARY", currentLibrary));
			intent.putExtra("SELECTED_SONG_DATA_URI", cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_FILE_PATH)));
			intent.putExtra("NEW_PLAYLIST", true);
			intent.putExtra("NUMBER_SONGS", genresFlippedListViewAdapter.getCount());
			intent.putExtra("CALLED_FROM_FOOTER", false);
			intent.putExtra("PLAY_ALL", "GENRE");
			intent.putExtra("CALLING_FRAGMENT", "GENRES_FLIPPED_FRAGMENT");
			
			getActivity().startActivity(intent);
			getActivity().overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
			
			cursor.close();
			
        } else {
        	Toast.makeText(getActivity(), R.string.genre_doesnt_have_any_songs, Toast.LENGTH_LONG).show();
        }
		
        dbHelper.close();
        
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
       inflater.inflate(R.menu.inner_browsers_menu, menu);
       
       if (genreName==null) {
    	   genreName = "Unknown Genre";
       }
       
       SpannableString s = new SpannableString(genreName);
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

}
