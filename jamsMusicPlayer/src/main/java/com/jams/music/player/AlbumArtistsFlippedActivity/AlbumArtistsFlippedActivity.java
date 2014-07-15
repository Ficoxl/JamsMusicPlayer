package com.jams.music.player.AlbumArtistsFlippedActivity;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.jams.music.player.R;
import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.Drawers.NavigationDrawerAdapter;
import com.jams.music.player.Drawers.NavigationDrawerLibrariesAdapter;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.MainActivity.MainActivity;
import com.jams.music.player.NowPlayingActivity.NowPlayingActivity;
import com.jams.music.player.NowPlayingQueueActivity.NowPlayingQueueListViewAdapter;
import com.jams.music.player.Services.AudioPlaybackService;
import com.jams.music.player.SettingsActivity.SettingsActivity;
import com.jams.music.player.Utils.Common;
import com.jams.music.player.Utils.TypefaceSpan;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.SimpleFloatViewManager;

/****************************************************************
 * This Activity is nothing more than a container for the 
 * AlbumArtistsFlipped fragment. Using a fragment within an activity 
 * gives us the flexibility to use custom transitions while 
 * retaining fragment support for tablet layouts.
 * 
 * @author Saravan Pantham
 ****************************************************************/
public class AlbumArtistsFlippedActivity extends FragmentActivity {

	public Context mContext;
	private String albumArtistName;
	
	public static boolean FINISH_FLAG = false;
	private Handler mHandler = new Handler();
	public SharedPreferences sharedPreferences;
	
    public String MUSIC_PLAYING = "MUSIC_PLAYING";
    public String MAIN_ACTIVITY_VISIBLE = "MAIN_ACTIVITY_VISIBLE";
    public String SERVICE_RUNNING = "SERVICE_RUNNING";
    public String FIRST_RUN = "FIRST_RUN";
	
    public RelativeLayout nowPlayingFooterContentLayout;
    public RelativeLayout nowPlayingFooterLayout;
    public ImageView nowPlayingFooterThumbnail;
    public TextView nowPlayingFooterNowPlayingText;
    public TextView nowPlayingFooterDetails;
    private Common mApp;
    private InputStream is;
    
    private RelativeLayout currentQueueNowPlayingLayout;
    private ImageView currentQueueNowPlayingAlbumArt;
    private ImageButton currentQueueNowPlayingPrevious;
    private ImageButton currentQueueNowPlayingPlayPause;
    private ImageButton currentQueueNowPlayingNext;
    private TextView currentQueueNowPlayingTitle;
    private TextView currentQueueNowPlayingAlbum;
    private TextView currentQueueNowPlayingArtist;
    
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private NavigationDrawerLibrariesAdapter slidingMenuLibrariesAdapter;
    private NavigationDrawerAdapter slidingMenuAdapter;
    
    public RelativeLayout currentQueueLayout;
    public TextView currentQueueEmptyText;
    public DragSortListView currentQueueListView;
    private NowPlayingQueueListViewAdapter queueAdapter;
    
    public ScrollView drawerScrollView;
    private ListView browsersListView;
	private ListView librariesListView;
	public static TextView browsersHeaderText;
	private TextView librariesHeaderText;
	public static ImageView librariesColorTagImageView;
	private ImageView librariesIcon;
    
    private BroadcastReceiver receiver;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
    	mApp = (Common) getApplicationContext();
		mContext = this;
		sharedPreferences = getSharedPreferences("com.jams.music.player", Context.MODE_PRIVATE);
		sharedPreferences.edit().putBoolean("ARTISTS_FLIPPED_ACTIVITY_VISIBLE", true).commit();
		
		super.onCreate(savedInstanceState);
        receiver = new BroadcastReceiver() {
        	
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra("INIT_QUEUE_DRAWER_ADAPTER") && 
                	intent.getBooleanExtra("INIT_QUEUE_DRAWER_ADAPTER", false)==true) {
                	//If an enqueue operation occured, we'll have to revalidate the adapter.
                	initQueueDrawer(false, true);
                	queueAdapter.notifyDataSetChanged();
                } else {
                	initQueueDrawer(false, true);
                }
                
                
            }
            
        };
        
    	setContentView(R.layout.activity_artists_flipped);
		
    	mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_root);
        drawerScrollView = (ScrollView) findViewById(R.id.nav_drawer);

        browsersListView = (ListView) findViewById(R.id.browsers_list_view);
		librariesListView = (ListView) findViewById(R.id.libraries_list_view);
		browsersHeaderText = (TextView) findViewById(R.id.browsers_header_text);
		librariesHeaderText = (TextView) findViewById(R.id.libraries_header_text);
		librariesColorTagImageView = (ImageView) findViewById(R.id.library_color_tag);
		librariesIcon = (ImageView) findViewById(R.id.libraries_icon);
		librariesIcon.setImageResource(UIElementsHelper.getIcon(mContext, "libraries"));
        
		currentQueueLayout = (RelativeLayout) findViewById(R.id.main_activity_queue_drawer);
		currentQueueEmptyText = (TextView) findViewById(R.id.empty_queue_text);
		currentQueueListView = (DragSortListView) findViewById(R.id.queue_list_view);
		currentQueueNowPlayingLayout = (RelativeLayout) findViewById(R.id.current_queue_now_playing_layout);
	    currentQueueNowPlayingAlbumArt = (ImageView) findViewById(R.id.notification_expanded_base_image);
	    currentQueueNowPlayingPrevious = (ImageButton) findViewById(R.id.notification_expanded_base_previous);
	    currentQueueNowPlayingPlayPause = (ImageButton) findViewById(R.id.notification_expanded_base_play);
	    currentQueueNowPlayingNext = (ImageButton) findViewById(R.id.notification_expanded_base_next);
	    currentQueueNowPlayingTitle = (TextView) findViewById(R.id.notification_expanded_base_line_one);
	    currentQueueNowPlayingAlbum = (TextView) findViewById(R.id.notification_expanded_base_line_two);
	    currentQueueNowPlayingArtist = (TextView) findViewById(R.id.notification_expanded_base_line_three);
		
		currentQueueLayout.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				//Don't let touch events pass through the drawer.
				return true;
			}
			
		});
		
		currentQueueEmptyText.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
		currentQueueEmptyText.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
		currentQueueEmptyText.setPaintFlags(currentQueueEmptyText.getPaintFlags() | 
											Paint.ANTI_ALIAS_FLAG |
											Paint.SUBPIXEL_TEXT_FLAG);
		
		currentQueueNowPlayingTitle.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
		currentQueueNowPlayingTitle.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Light"));
		currentQueueNowPlayingTitle.setPaintFlags(currentQueueNowPlayingTitle.getPaintFlags() | 
											Paint.ANTI_ALIAS_FLAG |
											Paint.SUBPIXEL_TEXT_FLAG);
		
		
		currentQueueNowPlayingAlbum.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
		currentQueueNowPlayingAlbum.setPaintFlags(currentQueueNowPlayingAlbum.getPaintFlags() | 
											Paint.ANTI_ALIAS_FLAG |
											Paint.SUBPIXEL_TEXT_FLAG);
		
		
		currentQueueNowPlayingArtist.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
		currentQueueNowPlayingArtist.setPaintFlags(currentQueueNowPlayingArtist.getPaintFlags() | 
											Paint.ANTI_ALIAS_FLAG |
											Paint.SUBPIXEL_TEXT_FLAG);
		
        //Set the drawer backgrounds based on the theme.
        if (sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME") ||
        	sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("DARK_THEME")) {
        	drawerScrollView.setBackgroundColor(0xFF191919);
        	currentQueueLayout.setBackgroundColor(0xFF191919);
        } else {
        	drawerScrollView.setBackgroundColor(0xFFFFFFFF);
        	currentQueueLayout.setBackgroundColor(0xFFFFFFFF);
        }

        initializeDrawer();
        
        //Set the header text fonts/colors.
		browsersHeaderText.setTypeface(TypefaceHelper.getTypeface(this, "RobotoCondensed-Light"));
		librariesHeaderText.setTypeface(TypefaceHelper.getTypeface(this, "RobotoCondensed-Light"));
        
		browsersHeaderText.setPaintFlags(browsersHeaderText.getPaintFlags() 
				 						 | Paint.ANTI_ALIAS_FLAG 
				 						 | Paint.FAKE_BOLD_TEXT_FLAG 
				 						 | Paint.SUBPIXEL_TEXT_FLAG);

		librariesHeaderText.setPaintFlags(librariesHeaderText.getPaintFlags() 
				  						  | Paint.ANTI_ALIAS_FLAG 
				  						  | Paint.FAKE_BOLD_TEXT_FLAG 
				  						  | Paint.SUBPIXEL_TEXT_FLAG);
    	
		inflateFragment();
	    
        nowPlayingFooterThumbnail = (ImageView) findViewById(R.id.now_playing_footer_thumbnail);
        nowPlayingFooterNowPlayingText = (TextView) findViewById(R.id.now_playing_text);
        nowPlayingFooterDetails = (TextView) findViewById(R.id.now_playing_song_artist_text);
        nowPlayingFooterLayout = (RelativeLayout) findViewById(R.id.now_playing_footer_layout);
        nowPlayingFooterContentLayout = (RelativeLayout) findViewById(R.id.now_playing_footer_content_layout);
	    
        //KitKat translucent navigation/status bar.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        	int topPadding = Common.getStatusBarHeight(mContext);
            mDrawerLayout.setPadding(0, topPadding, 0, 0);
            
            //Calculate ActionBar height
            TypedValue tv = new TypedValue();
            int actionBarHeight = 0;
            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
            }
            
            //Calculate navigation bar height.
            int navigationBarHeight = 0;
            int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                navigationBarHeight = getResources().getDimensionPixelSize(resourceId);
            }
            
            if (drawerScrollView!=null) {
            	drawerScrollView.setPadding(0, topPadding + actionBarHeight, 0, navigationBarHeight);
            	drawerScrollView.setClipToPadding(false);
            }
            
            if (currentQueueLayout!=null) {
            	currentQueueLayout.setPadding(0, topPadding + actionBarHeight, 0, 0);
            	currentQueueLayout.setClipToPadding(false);
            }
            
            if (currentQueueListView!=null) {
            	currentQueueListView.setPadding(0, 0, 0, navigationBarHeight);
            	currentQueueListView.setClipToPadding(false);
            }

        }
        
	    mHandler.postDelayed(finishRunnable, 100);
	}
	
	//Destroys this activity once the user is finished editing album tags.
	public Runnable finishRunnable = new Runnable() {

		@Override
		public void run() {
			if (FINISH_FLAG==true) {
				finish();
			} else {
				mHandler.postDelayed(finishRunnable, 100);
			}
			
		}
		
	};
	
	public void inflateFragment() {
		String currentArtist = getIntent().getExtras().getString("ALBUM_ARTIST_NAME");
		String dataURI = getIntent().getExtras().getString("HEADER_IMAGE_PATH");
		String artSource = getIntent().getExtras().getString("ART_SOURCE");
		albumArtistName = currentArtist;
		
		//Inflate the fragment within the activity layout.
	    final Fragment albumArtistsFlippedFragment = new AlbumArtistsFlippedFragment();
	    Bundle artistBundle = new Bundle();
	    artistBundle.putString("ALBUM_ARTIST_NAME", currentArtist);
	    artistBundle.putString("HEADER_IMAGE_PATH", dataURI);
	    artistBundle.putString("ART_SOURCE", artSource);
	    albumArtistsFlippedFragment.setArguments(artistBundle);

	    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
	    transaction.replace(R.id.artists_flipped_layout, albumArtistsFlippedFragment, "albumArtistsFlippedFragment");
	    transaction.commit();
	    
	}
	
	private void initializeDrawer() {
		
		//Apply the Browser ListView's adapter.
		List<String> titles = Arrays.asList(getResources().getStringArray(R.array.sliding_menu_array));
		slidingMenuAdapter = new NavigationDrawerAdapter(this, new ArrayList<String>(titles));
		browsersListView.setAdapter(slidingMenuAdapter);
		browsersListView.setOnItemClickListener(browsersClickListener);
		setListViewHeightBasedOnChildren(browsersListView);
		
		//Apply the Libraries ListView's adapter.
		if (sharedPreferences.getBoolean("BUILDING_LIBRARY", false)==false) {
			Cursor cursor = mApp.getDBAccessHelper().getAllUniqueLibraries();
			slidingMenuLibrariesAdapter = new NavigationDrawerLibrariesAdapter(this, cursor);
			librariesListView.setAdapter(slidingMenuLibrariesAdapter);
			librariesListView.setOnItemClickListener(librariesClickListener);
			setListViewHeightBasedOnChildren(librariesListView);
			
		}
		
		//Set the listview dividers.
		if (sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME") ||
        	sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("DARK_THEME")) {
			librariesListView.setDivider(getResources().getDrawable(R.drawable.list_divider));
			librariesListView.setDividerHeight(1);
			
        	browsersListView.setDivider(getResources().getDrawable(R.drawable.list_divider));
        	browsersListView.setDividerHeight(1);
        } else {
        	librariesListView.setDivider(getResources().getDrawable(R.drawable.list_divider_light));
			librariesListView.setDividerHeight(1);
			
        	browsersListView.setDivider(getResources().getDrawable(R.drawable.list_divider_light));
        	browsersListView.setDividerHeight(1);
        }
		
		//Apply the card layout's background based on the color theme.
		if (sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME")) {
			currentQueueListView.setDivider(getResources().getDrawable(R.drawable.transparent_drawable));
			currentQueueListView.setDividerHeight(3);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(7, 3, 7, 3);
			currentQueueListView.setLayoutParams(layoutParams);
		} else if (sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
			currentQueueListView.setDivider(getResources().getDrawable(R.drawable.transparent_drawable));
			currentQueueListView.setDividerHeight(3);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(7, 3, 7, 3);
			currentQueueListView.setLayoutParams(layoutParams);
		}
		
		initQueueDrawer(true, true);
		mDrawerToggle = new ActionBarDrawerToggle(this, 
												  mDrawerLayout, 
												  R.drawable.ic_navigation_drawer, 
												  0, 
												  0) {

            /** Called when a drawer has settled in a completely closed state. */
			@Override
            public void onDrawerClosed(View view) {
				//Reset the ActionBar title.
				SpannableString s = null;
				if (albumArtistName!=null) {
					s = new SpannableString(albumArtistName);
				} else {
					s = new SpannableString("Album Artist");
				}
				
        	    s.setSpan(new TypefaceSpan(mContext, "RobotoCondensed-Light"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        	    getActionBar().setTitle(s); 
        	    
            }

            /** Called when a drawer has settled in a completely open state. */
			@Override
            public void onDrawerOpened(View drawerView) {
				//Close the other drawer if a new one opens up.
                if (drawerView!=null && currentQueueLayout!=null && 
                	drawerView.getId()==R.id.nav_drawer) {
                	mDrawerLayout.closeDrawer(currentQueueLayout);
                } else if (drawerView!=null && currentQueueLayout!=null &&
                		   drawerView.getId()==R.id.main_activity_queue_drawer) {
                	mDrawerLayout.closeDrawer(drawerScrollView);
                	currentQueueListView.setSelection(mApp.getService().getCurrentSongIndex());
                	
                	//Update the ActionBar title.
                	SpannableString s = new SpannableString(mContext.getResources().getString(R.string.current_queue));
            	    s.setSpan(new TypefaceSpan(mContext, "RobotoCondensed-Light"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            	    getActionBar().setTitle(s); 
                }
                
            }
			
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
		
	}
	
	private void initQueueDrawer(boolean initAdapter, boolean setListViewPosition) {
		
		//If the service is running, load the queue. If not, show the empty text.
		if (sharedPreferences.getBoolean("SERVICE_RUNNING", false)==true &&
			mApp.getService().getCurrentMediaPlayer()!=null && 
			mApp.getService().getPlaybackIndecesList()!=null &&
			mApp.getService().getPlaybackIndecesList().size() > 0) {
			
			//The service is running.
			currentQueueEmptyText.setVisibility(View.GONE);
			currentQueueListView.setVisibility(View.VISIBLE);
			currentQueueNowPlayingLayout.setVisibility(View.VISIBLE);
			
			currentQueueNowPlayingPlayPause.setImageResource(UIElementsHelper.getIcon(mContext, "btn_playback_pause"));
			currentQueueNowPlayingPrevious.setImageResource(UIElementsHelper.getIcon(mContext, "btn_playback_previous"));
			currentQueueNowPlayingNext.setImageResource(UIElementsHelper.getIcon(mContext, "btn_playback_next"));
			
			//Initialize the adapter (called the first time the drawer is initialized).
			if (initAdapter) {
				queueAdapter = new NowPlayingQueueListViewAdapter(this, 
															  	  mApp.getService().getPlaybackIndecesList());
			
				currentQueueListView.setAdapter(queueAdapter);
	    		currentQueueListView.setFastScrollEnabled(true);
	    		currentQueueListView.setDropListener(onDrop);
	    		currentQueueListView.setRemoveListener(onRemove);
	    		SimpleFloatViewManager simpleFloatViewManager = new SimpleFloatViewManager(currentQueueListView);
	    		simpleFloatViewManager.setBackgroundColor(Color.TRANSPARENT);
	    		currentQueueListView.setFloatViewManager(simpleFloatViewManager);
	    		
	    		//Set the listview dividers.
	    		if (sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME") ||
	            	sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("DARK_THEME")) {
	    			currentQueueListView.setDivider(getResources().getDrawable(R.drawable.list_divider));
	    			currentQueueListView.setDividerHeight(1);
	            } else {
	            	currentQueueListView.setDivider(getResources().getDrawable(R.drawable.list_divider_light));
	            	currentQueueListView.setDividerHeight(1);
	            }
	    		
	            currentQueueListView.setOnItemClickListener(new OnItemClickListener() {

	    			@Override
	    			public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
	    				
	    				//Change the cursor position in the service to skip to the appropriate track.
	    				if (mApp.getService().getCurrentMediaPlayer()!=null && 
	    					mApp.getService().getMediaPlayer()!=null &&
	    					mApp.getService().getMediaPlayer2()!=null) {
	    					
	    					if (mApp.getService().getCursor().getCount() > index) {
	        					mApp.getService().getMediaPlayer().reset();
	        					mApp.getService().getMediaPlayer2().reset();
	        					mApp.getService().setCurrentSongIndex(index);
	        					
	        					mApp.getService().prepareMediaPlayer(index);
	        					
	    					}
	    					
	    				}
	    				
	    			}
	            	
	            });
	    		
			} else {
				try {
					queueAdapter.notifyDataSetChanged();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
            
    		//Scroll down to the current song only if the drawer isn't already open.
    		if (setListViewPosition) {
    			if (!mDrawerLayout.isDrawerOpen(currentQueueLayout)) {
    				currentQueueListView.setSelection(mApp.getService().getCurrentSongIndex());
    			}
    			
    			//Update the current song details and controls.
    			currentQueueNowPlayingLayout.setVisibility(View.VISIBLE);
    			
    			String songTitle = "Unknown Title";
    			String songArtist = "Unknown Artist";
    			String songAlbum = "Unknown Album";
    			try {
    				mApp.getService().getCursor().moveToPosition(mApp.getService().getPlaybackIndecesList().get(mApp.getService().getCurrentSongIndex()));
            		songTitle = mApp.getService().getCursor().getString(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.SONG_TITLE));
                	songArtist = mApp.getService().getCursor().getString(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.SONG_ARTIST));
                	songAlbum = mApp.getService().getCursor().getString(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.SONG_ALBUM));
    			} catch (Exception e) {
    				e.printStackTrace();
    			}

            	currentQueueNowPlayingTitle.setText(songTitle);
            	currentQueueNowPlayingAlbum.setText(songAlbum);
            	currentQueueNowPlayingArtist.setText(songArtist);
            	
            	//Apply the click listeners to the controls.
            	currentQueueNowPlayingPrevious.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent previousTrackIntent = new Intent();
			        	previousTrackIntent.setAction(AudioPlaybackService.PREVIOUS_ACTION);
			        	sendBroadcast(previousTrackIntent);
						
					}
            		
            	});
            	
            	currentQueueNowPlayingPlayPause.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						
						try {
							if (mApp.getService().getCurrentMediaPlayer().isPlaying()) {
								currentQueueNowPlayingPlayPause.setImageResource(UIElementsHelper.getIcon(mContext, "btn_playback_play"));
							} else {
								currentQueueNowPlayingPlayPause.setImageResource(UIElementsHelper.getIcon(mContext, "btn_playback_pause"));
							}
							
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						Intent playPauseTrackIntent = new Intent();
			        	playPauseTrackIntent.setAction(AudioPlaybackService.PLAY_PAUSE_ACTION);
			        	sendBroadcast(playPauseTrackIntent);
						
					}
            		
            	});
            	
            	currentQueueNowPlayingNext.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {						
						Intent nextTrackIntent = new Intent();
			        	nextTrackIntent.setAction(AudioPlaybackService.NEXT_ACTION);
			        	sendBroadcast(nextTrackIntent);
						
					}
            		
            	});
            	
            	currentQueueNowPlayingLayout.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(getBaseContext(), NowPlayingActivity.class);
						intent.putExtra("CALLED_FROM_FOOTER", true);
						startActivity(intent);
						
					}
            		
            	});
            	
            	//Set the album art.
            	File file = new File(mContext.getExternalCacheDir() + "/current_album_art.jpg");
            	Bitmap bm = null;
            	if (file.exists()) {
            		bm = mApp.decodeSampledBitmapFromFile(file, 100, 100);
            		currentQueueNowPlayingAlbumArt.setScaleX(1.0f);
            		currentQueueNowPlayingAlbumArt.setScaleY(1.0f);
            	} else {
            		bm = mApp.decodeSampledBitmapFromResource(R.drawable.default_album_art, 100, 100);
            		currentQueueNowPlayingAlbumArt.setScaleX(0.75f);
            		currentQueueNowPlayingAlbumArt.setScaleY(0.75f);
            	}
            	
            	currentQueueNowPlayingAlbumArt.setImageBitmap(bm);
    			
    		}

		} else {
			//The service isn't running.
			currentQueueEmptyText.setVisibility(View.VISIBLE);
			currentQueueListView.setVisibility(View.GONE);
			currentQueueNowPlayingLayout.setVisibility(View.GONE);
		}
		
	}
	
	private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
    	
        @Override
        public void drop(int from, int to) {
            if (from!=to) {
                int fromItem = queueAdapter.getItem(from);
                int toItem = queueAdapter.getItem(to);
                queueAdapter.remove(fromItem);
                queueAdapter.insert(fromItem, to);
                
                //If the current song was reordered, change currentSongIndex and update the next song.
                if (from==mApp.getService().getCurrentSongIndex()) {
                	mApp.getService().setCurrentSongIndex(to);
                	
                	//Check which mediaPlayer is currently playing, and prepare the other mediaPlayer.
                	mApp.getService().prepareAlternateMediaPlayer();
                	return;
                	
                } else if (from > mApp.getService().getCurrentSongIndex() && to <= mApp.getService().getCurrentSongIndex()) {
                	//One of the next songs was moved to a position before the current song. Move currentSongIndex forward by 1.
                	mApp.getService().incrementCurrentSongIndex();
                	mApp.getService().incrementEnqueueReorderScalar();
                	
                	//Check which mediaPlayer is currently playing, and prepare the other mediaPlayer.
                	mApp.getService().prepareAlternateMediaPlayer();
                	return;
                	
                } else if (from < mApp.getService().getCurrentSongIndex() && to==mApp.getService().getCurrentSongIndex()) {
                	/* One of the previous songs was moved to the current song's position (visually speaking, 
                	 * the new song will look like it was placed right after the current song.
                	 */
                	mApp.getService().decrementCurrentSongIndex();
                	mApp.getService().decrementEnqueueReorderScalar();
                	
                	//Check which mediaPlayer is currently playing, and prepare the other mediaPlayer.
                	mApp.getService().prepareAlternateMediaPlayer();
                	return;
                
            	} else if (from < mApp.getService().getCurrentSongIndex() && to > mApp.getService().getCurrentSongIndex()) {
                	//One of the previous songs was moved to a position after the current song. Move currentSongIndex back by 1.
                	mApp.getService().decrementCurrentSongIndex();
                	mApp.getService().decrementEnqueueReorderScalar();
                	
                	//Check which mediaPlayer is currently playing, and prepare the other mediaPlayer.
                	mApp.getService().prepareAlternateMediaPlayer();
                	return;
                	
                }
                
                //If the next song was reordered, reload it with the new index.
                if (mApp.getService().getPlaybackIndecesList().size() > (mApp.getService().getCurrentSongIndex()+1)) {
                    if (fromItem==mApp.getService().getPlaybackIndecesList().get(mApp.getService().getCurrentSongIndex()+1) || 
                    	toItem==mApp.getService().getPlaybackIndecesList().get(mApp.getService().getCurrentSongIndex()+1)) {
                    	
                    	//Check which mediaPlayer is currently playing, and prepare the other mediaPlayer.
                    	mApp.getService().prepareAlternateMediaPlayer();
                    	
                    }
                    
                } else {
                	//Check which mediaPlayer is currently playing, and prepare the other mediaPlayer.
                	mApp.getService().prepareAlternateMediaPlayer();
                	
                }

            }
            
        }
        
    };
    
    private DragSortListView.RemoveListener onRemove = new DragSortListView.RemoveListener() {
    	
        @Override
        public void remove(int which) {

        	//Stop the service if we just removed the last (and only) song.
        	if (mApp.getService().getPlaybackIndecesList().size()==1) {
        		mContext.stopService(new Intent(mContext, AudioPlaybackService.class));
        		return;
        	}
        	
            //If the song that was removed is the next song, reload it.
            if (mApp.getService().getPlaybackIndecesList().size() > (mApp.getService().getCurrentSongIndex()+1)) {
                if (queueAdapter.getItem(which)==mApp.getService().getPlaybackIndecesList().get(mApp.getService().getCurrentSongIndex()+1)) {
                	//Check which mediaPlayer is currently playing, and prepare the other mediaPlayer.
                	mApp.getService().prepareAlternateMediaPlayer();
                	
                } else if (queueAdapter.getItem(which)==mApp.getService().getPlaybackIndecesList().get(mApp.getService().getCurrentSongIndex())) {
                	mApp.getService().incrementCurrentSongIndex();
                	mApp.getService().prepareMediaPlayer(mApp.getService().getCurrentSongIndex());
                	mApp.getService().decrementCurrentSongIndex();
                } else if (queueAdapter.getItem(which) < mApp.getService().getPlaybackIndecesList().get(mApp.getService().getCurrentSongIndex())) {
                	mApp.getService().decrementCurrentSongIndex();
                }
                
            } else if (which==(mApp.getService().getPlaybackIndecesList().size()-1) &&
      		   	   	   mApp.getService().getCurrentSongIndex()==(mApp.getService().getPlaybackIndecesList().size()-1)) {	
            	//The current song was the last one and it was removed. Time to back up to the previous song.
            	mApp.getService().decrementCurrentSongIndex();
            	mApp.getService().prepareMediaPlayer(mApp.getService().getCurrentSongIndex());
            } else {
            	//Check which mediaPlayer is currently playing, and prepare the other mediaPlayer.
            	mApp.getService().prepareAlternateMediaPlayer();
            	
            }
            
            //Remove the item from the adapter.
            queueAdapter.remove(queueAdapter.getItem(which));
            
        }
        
    };
	
	public static void setListViewHeightBasedOnChildren(ListView listView) {
	    ListAdapter listAdapter = listView.getAdapter(); 
	    if (listAdapter == null) {
	        // pre-condition
	        return;
	    }
	
	    int totalHeight = 0;
	    for (int i = 0; i < listAdapter.getCount(); i++) {
	        View listItem = listAdapter.getView(i, null, listView);
	        listItem.measure(0, 0);
	        totalHeight += listItem.getMeasuredHeight();
	    }
	
	    ViewGroup.LayoutParams params = listView.getLayoutParams();
	    params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
	    listView.setLayoutParams(params);
	    listView.requestLayout();
	}
	
	private OnItemClickListener librariesClickListener = new OnItemClickListener() {
		
		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int position, long dbID) {
			sharedPreferences.edit().putString("CURRENT_LIBRARY", (String) view.getTag(R.string.library_name)).commit();
			librariesListView.setAdapter(slidingMenuLibrariesAdapter);
			librariesListView.invalidate();
			
			//Refresh the activity to reflect the new library.
			if (mContext==null) {
				mContext = getApplicationContext();
			}
			
			if (sharedPreferences==null) {
				sharedPreferences = mContext.getSharedPreferences("com.jams.music.player", Context.MODE_PRIVATE);
			}
			
			String currentLibrary = "";
			try {
				currentLibrary = sharedPreferences.getString("CURRENT_LIBRARY", mContext.getResources().getString(R.string.all_libraries));
			} catch (Exception e) {
				e.printStackTrace();
				currentLibrary = mContext.getResources().getString(R.string.all_libraries);
			}
			
			currentLibrary = currentLibrary.replace("'", "''");
			
			//Replace the current fragment (refreshes all UI elements with the current library).
			inflateFragment();
			
			//Close the nav drawer.
			mDrawerLayout.closeDrawer(drawerScrollView);	
		}
		
	};
	
	private OnItemClickListener browsersClickListener = new OnItemClickListener() {
	
		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int position, long dbID) {
			
			//Redraw the listview.
			browsersListView.setAdapter(slidingMenuAdapter);
			browsersListView.invalidate();
			
			String targetFragment = "";
			switch (position) {
			case 0:
				targetFragment = "ARTISTS";
				break;
			case 1:
				targetFragment = "ALBUM_ARTISTS";
				break;
			case 2:
				targetFragment = "ALBUMS";
				break;
			case 3:
				targetFragment = "SONGS";
				break;
			case 4:
				targetFragment = "PLAYLISTS";
				break;
			case 5:
				targetFragment = "GENRES";
				break;
			case 6:
				targetFragment = "FOLDERS";
				break;
			}
			
			Intent intent = new Intent(mContext, MainActivity.class);
			intent.putExtra("TARGET_FRAGMENT", targetFragment);
		    mContext.startActivity(intent);
			
			//Close the drawer.
			mDrawerLayout.closeDrawer(drawerScrollView);

		}
		
	};
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
	    }
		
		switch (item.getItemId()) {
	    case R.id.action_settings:
	        Intent intent = new Intent(mContext, SettingsActivity.class);
	        intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	        startActivity(intent);
	        return true;
	    case R.id.action_queue_drawer:
	    	if (mDrawerLayout!=null && currentQueueLayout!=null) {
		    	if (mDrawerLayout.isDrawerOpen(currentQueueLayout)) {
		    		mDrawerLayout.closeDrawer(currentQueueLayout);
		    	} else {
		    		mDrawerLayout.openDrawer(currentQueueLayout);
		    	}
		    	
	    	}
	    	return true;
	    default:
	    	//Return false to allow the fragment to handle the item click.
	        return false;
	    }
		
	}
	
    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
        	
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
            
        }

        return inSampleSize;
    }
    
    //Resamples a byte array image to avoid OOM errors.
    public Bitmap decodeSampledBitmapFromByteArray(byte[] bitmapSource, int reqWidth, int reqHeight) {
        try {
        	
        	is = new ByteArrayInputStream(bitmapSource);

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;
            options.inPurgeable = true;
            
            return BitmapFactory.decodeStream(is, null, options);
        } catch (Exception e) {
        	e.printStackTrace();
        	return null;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }
        
    }
    
    //Resamples a resource image to avoid OOM errors.
    public Bitmap decodeSampledBitmapFromResource(int resID, int reqWidth, int reqHeight) {

	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
	    options.inJustDecodeBounds = false;
	    options.inPurgeable = true;
	
	    return BitmapFactory.decodeResource(getBaseContext().getResources(), resID, options);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	initQueueDrawer(true, true);
    	initializeDrawer();
    	sharedPreferences.edit().putBoolean("ALBUM_ARTISTS_FLIPPED_ACTIVITY_VISIBLE", true).commit();

    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	sharedPreferences.edit().putBoolean("ALBUM_ARTISTS_FLIPPED_ACTIVITY_VISIBLE", false).commit();
    	overridePendingTransition(R.anim.scale_and_fade_in, R.anim.fade_out);
    	
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	sharedPreferences.edit().putBoolean("ALBUM_ARTISTS_FLIPPED_ACTIVITY_VISIBLE", false).commit();
    	
    	if (isFinishing()) {
    		if (AlbumArtistsFlippedListViewAdapter.dbHelper!=null) {
    			AlbumArtistsFlippedListViewAdapter.dbHelper.close();
    			AlbumArtistsFlippedListViewAdapter.dbHelper = null;
    		}
    		
    		if (AlbumArtistsFlippedListViewAdapter.cursor!=null) {
    			AlbumArtistsFlippedListViewAdapter.cursor.close();
    			AlbumArtistsFlippedListViewAdapter.cursor = null;
    		}
    		
    	}
    	
    }
    
    @Override
    public void onBackPressed() {
    	super.onBackPressed();
    	overridePendingTransition(R.anim.scale_and_fade_in, R.anim.fade_out);
    	
    }
    
    //Retrieves the orientation of the device.
    public String getOrientation() {

        if (getResources().getDisplayMetrics().widthPixels > 
           getResources().getDisplayMetrics().heightPixels) { 
            return "LANDSCAPE";
        } else {
            return "PORTRAIT";
        }     
        
    }
    
	@Override
	protected void onStart() {
	    super.onStart();
	    LocalBroadcastManager.getInstance(this)
	    					 .registerReceiver((receiver), new IntentFilter(Common.UPDATE_UI_BROADCAST));
	
	}

	@Override
	protected void onStop() {
	    LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
	    super.onStop();
	    
	}
	
	@Override
	public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }
	
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

}
