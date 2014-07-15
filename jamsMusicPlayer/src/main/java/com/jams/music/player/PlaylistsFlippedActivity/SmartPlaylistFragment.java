package com.jams.music.player.PlaylistsFlippedActivity;

import java.util.ArrayList;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ListView;
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
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class SmartPlaylistFragment extends Fragment {

	private Context mContext;
	public static String CURRENT_LOADER;
	
	private ViewGroup rootView;
	private ImageView headerImage;
	private boolean mPinned = false;
	private String playlistName;

	public static DragSortListView otherListView;
	public static ListView songsListView;
	public static SmartPlaylistListViewAdapter songsListViewAdapter;
	
	private ArrayList<Drawable> drawablesList = new ArrayList<Drawable>();
	private Drawable[] playlistAlbumArt;
	
	public static boolean SONGS_DATA_LOADING_FLAG = true;
	public static boolean DONE_EDITING_TAGS = false;
	private String selection = "";
	public static View childView;
	private String defaultPlaylistType;
	
	private SharedPreferences sharedPreferences;
	private DBAccessHelper dbHelper;
	private Cursor cursor;
	public static DisplayImageOptions displayImageOptions;
	private Common mApp;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_playlists_flipped, container, false);
        mContext = getActivity();
        sharedPreferences = mContext.getSharedPreferences("com.jams.music.player", Context.MODE_PRIVATE);
        this.setHasOptionsMenu(true);
        
        dbHelper = new DBAccessHelper(mContext.getApplicationContext());
        mApp = (Common) mContext.getApplicationContext();

        defaultPlaylistType = getArguments().getString("DEFAULT_PLAYLIST_TYPE");
        
        if (sharedPreferences.getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==false) {
        	selection = DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'" + " AND "
        			  + DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'";
        } else {
        	selection = DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'";
        }
        
        if (defaultPlaylistType.equals("TOP_25_PLAYED_SONGS")) {
        	CURRENT_LOADER = "TOP_25_PLAYED";
            cursor = dbHelper.getTop25PlayedTracks(selection);
            playlistName = "Top 25 Played Songs";
        } else if (defaultPlaylistType.equals("RECENTLY_ADDED")) {
        	CURRENT_LOADER = "RECENTLY_ADDED";
        	cursor = dbHelper.getRecentlyAddedSongs(selection);
        	playlistName = "Recently Added";
        } else if (defaultPlaylistType.equals("TOP_RATED")) {
        	CURRENT_LOADER = "TOP_RATED";
        	selection = DBAccessHelper.RATING + "<>" + "'" + "0" +"'";
        	cursor = dbHelper.getTopRatedSongs(selection);
        	playlistName = "Top Rated";
        } else if (defaultPlaylistType.equals("RECENTLY_PLAYED")) {
        	CURRENT_LOADER = "RECENTLY_PLAYED";
        	selection = DBAccessHelper.LAST_PLAYED_TIMESTAMP + "<>" + "'" + "0" +"'";
        	cursor = dbHelper.getRecentlyPlayedSongs(selection);
        	playlistName = "Recently Played";
        }

        //Create a set of options to optimize the bitmap memory usage.
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inJustDecodeBounds = false;
        options.inPurgeable = true;
        
        //Display Image Options.
        displayImageOptions = new DisplayImageOptions.Builder()
		  						  .showImageForEmptyUri(R.drawable.default_album_art)
		  						  .showImageOnFail(R.drawable.default_album_art)
		  						  .cacheInMemory(false)
		  						  .cacheOnDisc(true)
		  						  .decodingOptions(options)
		  						  .imageScaleType(ImageScaleType.EXACTLY)
		  						  .bitmapConfig(Bitmap.Config.RGB_565)
		  						  .displayer(new FadeInBitmapDisplayer(400))
		  						  .build();
        
        headerImage = (ImageView) rootView.findViewById(R.id.playlist_flipped_header_image);
	    
	    otherListView = (DragSortListView) rootView.findViewById(R.id.playlists_flipped_listview);
	    otherListView.setVisibility(View.GONE);
	    
	    songsListView = (ListView) rootView.findViewById(R.id.default_playlists_flipped_listview);
	    songsListView.setVisibility(View.VISIBLE);
        songsListViewAdapter = new SmartPlaylistListViewAdapter(mContext, cursor);
        songsListView.setAdapter(songsListViewAdapter);
        
        PauseOnScrollListener listener = new PauseOnScrollListener(mApp.getImageLoader(), true, true);
        songsListView.setOnScrollListener(listener);
        
		//Set the background color based on the theme.
		if (sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME")) {
			rootView.setBackgroundColor(0xFFEEEEEE);
			songsListView.setDivider(getResources().getDrawable(R.drawable.transparent_drawable));
			songsListView.setDividerHeight(10);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(7, 3, 7, 3);
			songsListView.setLayoutParams(layoutParams);
		} else if (sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
			rootView.setBackgroundColor(0xFF111111);
			songsListView.setDivider(getResources().getDrawable(R.drawable.transparent_drawable));
			songsListView.setDividerHeight(10);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(7, 3, 7, 3);
			songsListView.setLayoutParams(layoutParams);
		} else if (sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("DARK_THEME")) {
			songsListView.setDivider(getResources().getDrawable(R.drawable.list_divider));
			songsListView.setDividerHeight(1);
        } else {
        	songsListView.setDivider(getResources().getDrawable(R.drawable.list_divider_light));
        	songsListView.setDividerHeight(1);
		}
        
        songsListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
					
				Intent intent = new Intent(getActivity(), NowPlayingActivity.class);
				intent.putExtra("SELECTED_SONG_DURATION", (String) view.getTag(R.string.duration));
				intent.putExtra("SELECTED_SONG_TITLE", (String) view.getTag(R.string.title));
				intent.putExtra("SELECTED_SONG_ARTIST", (String) view.getTag(R.string.artist));
				intent.putExtra("SELECTED_SONG_ALBUM", (String) view.getTag(R.string.album));
				intent.putExtra("SELECTED_SONG_GENRE", (String) view.getTag(R.string.genre));
				intent.putExtra("SONG_SELECTED_INDEX", index);
				intent.putExtra("SELECTED_SONG_DATA_URI", (String) view.getTag(R.string.song_file_path));
				intent.putExtra("NEW_PLAYLIST", true);
				intent.putExtra("NUMBER_SONGS", songsListViewAdapter.getCount());
				intent.putExtra("CALLED_FROM_FOOTER", false);
				intent.putExtra("CALLING_FRAGMENT", defaultPlaylistType);
				intent.putExtra("SONG_ID", (String) view.getTag(R.string.song_id));
				
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
        
		//Fetch the album art.
        AsyncGetHeaderImageTask task = new AsyncGetHeaderImageTask();
        task.execute();
        
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
            
            songsListView.setClipToPadding(false);
            songsListView.setPadding(0, topPadding + actionBarHeight, 0, navigationBarHeight);
        }
        
        return rootView;
    }
	
    class AsyncGetHeaderImageTask extends AsyncTask<String, String, String> {
    	
		@Override
		protected String doInBackground(String... params) {

            //Loop through the five album art paths and store the images in an ArrayList.
			if (cursor!=null) {
	            for (int i=0; i < 5; i++) {
	            	if (cursor!=null && i < cursor.getCount()) {
	            		
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

	            		drawablesList.add(drawable);
	            	} else {
	            		break;
	            	}
	            	
	            }
	            
			}
            
            //Load the contents of the ArrayList into the static array.
			try {
				playlistAlbumArt = new Drawable[drawablesList.size()];
	            for (int m=0; m < drawablesList.size(); m++) {
	            	playlistAlbumArt[m] = drawablesList.get(m);
	            }
			} catch (Exception e) {
				e.printStackTrace();
			}
            
            return null;
		}
		
		@Override
		public void onPostExecute(String result) {
			super.onPostExecute(result);

			try {
				if (playlistAlbumArt.length > 0) {
					CyclicTransitionDrawable cyclicDrawable = new CyclicTransitionDrawable(playlistAlbumArt);
		            headerImage.setImageDrawable(cyclicDrawable);
		            cyclicDrawable.startTransition(1000, 3000);
				}
	            
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
    	
    }
    
    @Override
    public void onDestroyView() {
    	super.onDestroyView();
    	dbHelper.close();
    	dbHelper = null;
    	cursor.close();
    	cursor = null;
    	
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
    
    private void playAllPressed() {
    	if (cursor!=null && cursor.getCount() > 0) {
        	cursor.moveToFirst();
        	Intent intent = new Intent(getActivity(), NowPlayingActivity.class);
			intent.putExtra("SELECTED_SONG_DURATION", cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_DURATION)));
			intent.putExtra("SELECTED_SONG_TITLE", cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_TITLE)));
			intent.putExtra("SELECTED_SONG_ARTIST", cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_ARTIST)));
			intent.putExtra("SELECTED_SONG_ALBUM", cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_ALBUM)));
			intent.putExtra("SELECTED_SONG_GENRE", cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_GENRE)));
			intent.putExtra("SONG_SELECTED_INDEX", 0);
			intent.putExtra("SELECTED_SONG_DATA_URI", cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_FILE_PATH)));
			intent.putExtra("NEW_PLAYLIST", true);
			intent.putExtra("NUMBER_SONGS", songsListViewAdapter.getCount());
			intent.putExtra("CALLED_FROM_FOOTER", false);
			intent.putExtra("CALLING_FRAGMENT", defaultPlaylistType);
			
			getActivity().startActivity(intent);
			getActivity().overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
			
        } else {
        	Toast.makeText(getActivity(), R.string.playlist_doesnt_have_any_songs, Toast.LENGTH_LONG).show();
        }
    	
    }
    
    private void pinButtonPressed() {
    	//Check if the album artist has any GMusic songs.
		boolean allLocal = true;
		cursor.moveToPosition(-1);
		while (cursor.moveToNext()) {
			if (cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_FILE_PATH)).equals(DBAccessHelper.GMUSIC)) {
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
				mApp.queueSongsToPin(false, false, CURRENT_LOADER);
			} else {
				//Delete the local copies of the artist's songs.
				mPinned = false;
				getActivity().invalidateOptionsMenu();
				AsyncRemovePinnedSongsTask task = new AsyncRemovePinnedSongsTask(mContext, selection, cursor);
				task.execute();
			}
			
		} else {
			Toast.makeText(mContext, R.string.all_songs_local, Toast.LENGTH_SHORT).show();
		}
		
    }
    
}
