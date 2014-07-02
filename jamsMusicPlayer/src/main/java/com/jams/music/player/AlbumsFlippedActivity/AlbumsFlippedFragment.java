package com.jams.music.player.AlbumsFlippedActivity;

import android.app.ActionBar;
import android.app.Activity;
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
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.NowPlayingActivity.NowPlayingActivity;
import com.jams.music.player.Utils.Common;
import com.jams.music.player.Utils.TypefaceSpan;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class AlbumsFlippedFragment extends Fragment {

	public static Context mContext;
	public static AlbumsFlippedFragment fragment;
	
	private Activity parentActivity;
	public static String albumName;
	public static String artistName;
	private String headerImagePath;
	public static int contextMenuItemIndex;
	private boolean mPinned = false;
	
	private ImageView headerImageView;
	public static TextView emptyViewText;
	public static ImageView emptyViewImage;
	
	public static RelativeLayout cardFlippedHeaderLayout;
	public static ListView albumsFlippedListView;
	public static AlbumsFlippedListViewAdapter albumsFlippedListViewAdapter;
	
	public static DBAccessHelper musicLibraryDBHelper;
	public static Cursor cursor;
	
	public static View childView;
	public static boolean DONE_EDITING_TAGS = false;
	private int index;
	public static String selection = "";
	private Common mApp;
	public static SharedPreferences sharedPreferences;
	private String localCopyPath;
	private DisplayImageOptions displayImageOptions;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_albums_flipped, container, false);
        mContext = getActivity();
        fragment = this;
        
        parentActivity = getActivity();
        mApp = (Common) mContext.getApplicationContext();
        sharedPreferences = parentActivity.getSharedPreferences("com.jams.music.player", Context.MODE_PRIVATE);
        this.setHasOptionsMenu(true);
        
        //Get the album name from the parent Fragment.
        albumName = this.getArguments().getString("ALBUM_NAME");
        
        //Get the artist name from the parent Fragment.
        artistName = this.getArguments().getString("ARTIST_NAME");
        headerImagePath = this.getArguments().getString("HEADER_IMAGE_PATH");
        
	    //Create a set of options to optimize the bitmap memory usage.
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inJustDecodeBounds = false;
        options.inPurgeable = true;
        
        //Display Image Options.
        displayImageOptions = new DisplayImageOptions.Builder()
        						  .showImageForEmptyUri(R.drawable.default_header_image)
        						  .showImageOnFail(R.drawable.default_header_image)
        						  .cacheInMemory(false)
        						  .cacheOnDisc(true)
        						  .decodingOptions(options)
        						  .imageScaleType(ImageScaleType.EXACTLY)
        						  .bitmapConfig(Bitmap.Config.RGB_565)
        						  .displayer(new FadeInBitmapDisplayer(400))
        						  .delayBeforeLoading(100)
        						  .build();
        
	    cardFlippedHeaderLayout = (RelativeLayout) rootView.findViewById(R.id.albumFlippedHeader);
	    headerImageView = (ImageView) rootView.findViewById(R.id.albumsFlippedHeaderImage);

	    emptyViewText = (TextView) rootView.findViewById(R.id.empty_view_text);
        emptyViewImage = (ImageView) rootView.findViewById(R.id.empty_view_image);
        emptyViewImage.setImageResource(UIElementsHelper.getIcon(mContext, "albums"));
        
        emptyViewText.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
        emptyViewText.setPaintFlags(emptyViewText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
	    
	    setHeaderImageFromUrl(headerImagePath);
	    
	    musicLibraryDBHelper = new DBAccessHelper(mContext);
	    getCursor();
	    
        //Set the adapter for the AsyncListView.
        albumsFlippedListView = (ListView) rootView.findViewById(R.id.albumCardFlippedListView);
        albumsFlippedListView.setFastScrollEnabled(true);
        
        if (cursor!=null && cursor.getCount() > 0) {
        	albumsFlippedListViewAdapter = new AlbumsFlippedListViewAdapter(parentActivity, cursor);
        	albumsFlippedListView.setAdapter(albumsFlippedListViewAdapter);
        } else {
    		emptyViewText.setVisibility(View.VISIBLE);
    		emptyViewImage.setVisibility(View.VISIBLE);
    		albumsFlippedListView.setVisibility(View.GONE);
        }

		//Set the background color based on the theme.
		if (sharedPreferences.getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME")) {
			rootView.setBackgroundColor(0xFFEEEEEE);
			albumsFlippedListView.setDivider(getResources().getDrawable(R.drawable.transparent_drawable));
			albumsFlippedListView.setDividerHeight(10);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(20, 20, 20, 20);
			albumsFlippedListView.setLayoutParams(layoutParams);
		} else if (sharedPreferences.getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
			rootView.setBackgroundColor(0xFF111111);
			albumsFlippedListView.setDivider(getResources().getDrawable(R.drawable.transparent_drawable));
			albumsFlippedListView.setDividerHeight(10);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(20, 20, 20, 20);
			albumsFlippedListView.setLayoutParams(layoutParams);
		} else if (sharedPreferences.getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_THEME")) {
			albumsFlippedListView.setDivider(getResources().getDrawable(R.drawable.list_divider));
			albumsFlippedListView.setDividerHeight(1);
        } else {
        	albumsFlippedListView.setDivider(getResources().getDrawable(R.drawable.list_divider_light));
        	albumsFlippedListView.setDividerHeight(1);
		}

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
		
		albumsFlippedListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {

				String currentLibrary = sharedPreferences.getString("CURRENT_LIBRARY", mContext.getResources().getString(R.string.all_libraries));
				Intent intent = new Intent(getActivity(), NowPlayingActivity.class);
				
				intent.putExtra("SELECTED_SONG_DURATION", (String) view.getTag(R.string.duration));
				intent.putExtra("SELECTED_SONG_TITLE", (String) view.getTag(R.string.title));
				intent.putExtra("SELECTED_SONG_ARTIST", (String) view.getTag(R.string.artist));
				intent.putExtra("SELECTED_SONG_ALBUM", (String) view.getTag(R.string.album));
				intent.putExtra("SELECTED_SONG_GENRE", (String) view.getTag(R.string.genre));
				intent.putExtra("SONG_SELECTED_INDEX", index);
				intent.putExtra("SELECTED_SONG_DATA_URI", (String) view.getTag(R.string.song_file_path));
				intent.putExtra("NEW_PLAYLIST", true);
				intent.putExtra("NUMBER_SONGS", albumsFlippedListViewAdapter.getCount());
				intent.putExtra("CALLED_FROM_FOOTER", false);
				intent.putExtra("PLAY_ALL", "ALBUM");
				intent.putExtra("CALLING_FRAGMENT", "ALBUMS_FLIPPED_FRAGMENT");
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
            albumsFlippedListView.setClipToPadding(false);
            albumsFlippedListView.setPadding(0, 0, 0, navigationBarHeight);
        }
		
        return rootView;
    }
    
	public static void getCursor() {
	   
	}
    
    //Set the header image from a URL.
    private void setHeaderImageFromUrl(String url) {
    	mApp.getImageLoader().displayImage(url, headerImageView, displayImageOptions);
    	
    }
    
    public void playAllPressed() {
    	String currentLibrary = sharedPreferences.getString("CURRENT_LIBRARY", mContext.getResources().getString(R.string.all_libraries));
		Intent intent = new Intent(getActivity(), NowPlayingActivity.class);
		
		intent.putExtra("SELECTED_SONG_DURATION", (String) albumsFlippedListView.getChildAt(0).getTag(R.string.duration));
		intent.putExtra("SELECTED_SONG_TITLE", (String) albumsFlippedListView.getChildAt(0).getTag(R.string.title));
		intent.putExtra("SELECTED_SONG_ARTIST", (String) albumsFlippedListView.getChildAt(0).getTag(R.string.artist));
		intent.putExtra("SELECTED_SONG_ALBUM", (String) albumsFlippedListView.getChildAt(0).getTag(R.string.album));
		intent.putExtra("SELECTED_SONG_GENRE", (String) albumsFlippedListView.getChildAt(0).getTag(R.string.genre));
		intent.putExtra("SONG_SELECTED_INDEX", index);
		intent.putExtra("SELECTED_SONG_DATA_URI", (String) albumsFlippedListView.getChildAt(0).getTag(R.string.song_file_path));
		intent.putExtra("NEW_PLAYLIST", true);
		intent.putExtra("NUMBER_SONGS", albumsFlippedListViewAdapter.getCount());
		intent.putExtra("CALLED_FROM_FOOTER", false);
		intent.putExtra("PLAY_ALL", "ALBUM");
		intent.putExtra("CALLING_FRAGMENT", "ALBUMS_FLIPPED_FRAGMENT");
		intent.putExtra("CURRENT_LIBRARY", currentLibrary);
		
		getActivity().startActivity(intent);
		getActivity().overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
		
    }
    
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
    
    public void pinButtonPressed() {
    	//Check if the album has any GMusic songs.
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
    
}
