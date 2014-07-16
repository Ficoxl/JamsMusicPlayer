package com.jams.music.player.MainActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.jams.music.player.Drawers.NavigationDrawerFragment;
import com.jams.music.player.Drawers.QueueDrawerFragment;
import com.jams.music.player.GridViewFragment.GridViewFragment;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.ListViewFragment.ListViewFragment;
import com.jams.music.player.R;
import com.jams.music.player.SettingsActivity.SettingsActivity;
import com.jams.music.player.Utils.Common;

public class MainActivity extends FragmentActivity {

	//Context and Common object(s).
	private Context mContext;
	private Common mApp;

	//DrawerLayout UI elements.
	private FrameLayout mDrawerParentLayout;
	private DrawerLayout mDrawerLayout;
	private RelativeLayout mNavDrawerLayout;
	private RelativeLayout mCurrentQueueDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
    private QueueDrawerFragment mQueueDrawerFragment;
	
	//Current fragment params.
	private Fragment mCurrentFragment;
	public static int mCurrentFragmentId;
	public static int mCurrentFragmentLayout;
	
	//Layout flags.
	public static final String CURRENT_FRAGMENT = "CurrentFragment";
	public static final String ARTISTS_FRAGMENT_LAYOUT = "ArtistsFragmentLayout";
	public static final String ALBUM_ARTISTS_FRAGMENT_LAYOUT = "AlbumArtistsFragmentLayout";
	public static final String ALBUMS_FRAGMENT_LAYOUT = "AlbumsFragmentLayout";
	public static final String PLAYLISTS_FRAGMENT_LAYOUT = "PlaylistsFragmentLayout";
	public static final String GENRES_FRAGMENT_LAYOUT = "GenresFragmentLayout";
	public static final String FOLDERS_FRAGMENT_LAYOUT = "FoldersFragmentLayout";
    public static final String FRAGMENT_HEADER = "FragmentHeader";
	public static final int LIST_LAYOUT = 0;
	public static final int GRID_LAYOUT = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		//Context and Common object(s).
        mContext = getApplicationContext();
        mApp = (Common) getApplicationContext();
        
        //Set the theme and inflate the layout.
        setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //Init the UI elements.
        mDrawerParentLayout = (FrameLayout) findViewById(R.id.main_activity_root);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_activity_drawer_root);
        mNavDrawerLayout = (RelativeLayout) findViewById(R.id.nav_drawer_container);
        mCurrentQueueDrawerLayout = (RelativeLayout) findViewById(R.id.current_queue_drawer_container);
        
        //Load the drawer fragments.
        loadDrawerFragments();
		
        //KitKat specific translucency.
        applyKitKatTranslucency();
        
        //Load the fragment.
        loadFragment(savedInstanceState);
        
    	/**
    	 * Navigation drawer toggle.
    	 */
    	mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, 
    			  								  R.drawable.ic_navigation_drawer, 
    			  								  0, 0) {

    		@Override
    		public void onDrawerClosed(View view) {
    			if (mQueueDrawerFragment!=null &&
                    view==mCurrentQueueDrawerLayout)
                    mQueueDrawerFragment.setIsDrawerOpen(false);
    		
    		}

    		@Override
    		public void onDrawerOpened(View view) {
                if (mQueueDrawerFragment!=null &&
                    view==mCurrentQueueDrawerLayout)
                    mQueueDrawerFragment.setIsDrawerOpen(true);

    		}

    	};

    	//Apply the drawer toggle to the DrawerLayout.
    	mDrawerLayout.setDrawerListener(mDrawerToggle);
    	getActionBar().setDisplayHomeAsUpEnabled(true);
    	getActionBar().setDisplayShowHomeEnabled(true);
    	
	}
	
	/**
	 * Broadcast receiver interface that will update this activity as necessary.
	 */
	BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();

			if (bundle.containsKey(Common.UPDATE_PAGER_POSTIION)) {
                //Update the queue fragment with the new song info.
                if (mQueueDrawerFragment!=null)
                    mQueueDrawerFragment.initListViewAdapter(false);

            }

            //Show the "No music playing." message.
            if (bundle.containsKey(Common.SERVICE_STOPPING))
                if (mQueueDrawerFragment!=null)
                    mQueueDrawerFragment.showEmptyTextView();

		}
		
	};
	
	/**
	 * Sets the entire activity-wide theme.
	 */
	private void setTheme() {
    	//Set the UI theme.
    	if (mApp.getCurrentTheme()==Common.DARK_THEME) {
    		setTheme(R.style.AppTheme);
    	} else {
    		setTheme(R.style.AppThemeLight);
    	}
    	
	}
	
	/**
	 * Apply KitKat specific translucency.
	 */
	private void applyKitKatTranslucency() {
		
		//KitKat translucent navigation/status bar.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

        	//Set the window background.
        	getWindow().setBackgroundDrawable(UIElementsHelper.getGeneralActionBarBackground(mContext));
        	
    		int topPadding = Common.getStatusBarHeight(mContext);
    		if (mDrawerLayout!=null) {
    			mDrawerLayout.setPadding(0, topPadding, 0, 0);
    			mNavDrawerLayout.setPadding(0, topPadding, 0, 0);
    			mCurrentQueueDrawerLayout.setPadding(0, topPadding, 0, 0);
    		}

            //Calculate ActionBar height.
            TypedValue tv = new TypedValue();
            int actionBarHeight = 0;
            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
            }
            
            if (mDrawerParentLayout!=null) {
            	mDrawerParentLayout.setPadding(0, actionBarHeight, 0, 0);
            	mDrawerParentLayout.setClipToPadding(false);
            }
            
        }
        
	}
	
	/**
	 * Loads the correct fragment based on the selected browser.
	 */
	public void loadFragment(Bundle savedInstanceState) {
		//Get the target fragment from savedInstanceState if it's not null (orientation changes?).
		if (savedInstanceState!=null) {
			mCurrentFragmentId = savedInstanceState.getInt(CURRENT_FRAGMENT);
			
		} else {
			//Set the current fragment based on the intent's extras.
    		if (getIntent().hasExtra(CURRENT_FRAGMENT)) {
    			mCurrentFragmentId = getIntent().getExtras().getInt(CURRENT_FRAGMENT);
    		}
    		
    		switch (mCurrentFragmentId) {
    		case Common.ARTISTS_FRAGMENT:
    			mCurrentFragment = getLayoutFragment(Common.ARTISTS_FRAGMENT);
    			break;
    		case Common.ALBUM_ARTISTS_FRAGMENT:
    			mCurrentFragment = getLayoutFragment(Common.ALBUM_ARTISTS_FRAGMENT);
    			break;
    		case Common.ALBUMS_FRAGMENT:
    			mCurrentFragment = getLayoutFragment(Common.ALBUMS_FRAGMENT);
    			break;
    		case Common.SONGS_FRAGMENT:
    			mCurrentFragment = getLayoutFragment(Common.SONGS_FRAGMENT);
    			break;
    		case Common.PLAYLISTS_FRAGMENT:
    			mCurrentFragment = getLayoutFragment(Common.PLAYLISTS_FRAGMENT);
    			break;
    		case Common.GENRES_FRAGMENT:
    			mCurrentFragment = getLayoutFragment(Common.GENRES_FRAGMENT);
    			break;
    		case Common.FOLDERS_FRAGMENT:
    			mCurrentFragment = getLayoutFragment(Common.FOLDERS_FRAGMENT);
    			break;
    		}
    		
    		switchContent(mCurrentFragment);
		}
		
	}
	
	/**
	 * Retrieves the correct fragment based on the saved layout preference.
	 */
	private Fragment getLayoutFragment(int fragmentId) {
		
		//Instantiate a new bundle.
		Fragment fragment = null;
		Bundle bundle = new Bundle();
		
		//Retrieve layout preferences for the current fragment.
		switch (fragmentId) {
		case Common.ARTISTS_FRAGMENT:
			mCurrentFragmentLayout = mApp.getSharedPreferences().getInt(ARTISTS_FRAGMENT_LAYOUT, GRID_LAYOUT);
			bundle.putInt(Common.FRAGMENT_ID, Common.ARTISTS_FRAGMENT);
            bundle.putString(FRAGMENT_HEADER, mContext.getResources().getString(R.string.artists));
			break;
		case Common.ALBUM_ARTISTS_FRAGMENT:
			mCurrentFragmentLayout = mApp.getSharedPreferences().getInt(ALBUM_ARTISTS_FRAGMENT_LAYOUT, GRID_LAYOUT);
			bundle.putInt(Common.FRAGMENT_ID, Common.ALBUM_ARTISTS_FRAGMENT);
            bundle.putString(FRAGMENT_HEADER, mContext.getResources().getString(R.string.album_artists));
			break;
		case Common.ALBUMS_FRAGMENT:
			mCurrentFragmentLayout = mApp.getSharedPreferences().getInt(ALBUMS_FRAGMENT_LAYOUT, GRID_LAYOUT);
			bundle.putInt(Common.FRAGMENT_ID, Common.ALBUMS_FRAGMENT);
            bundle.putString(FRAGMENT_HEADER, mContext.getResources().getString(R.string.albums));
			break;
		case Common.SONGS_FRAGMENT:
			mCurrentFragmentLayout = LIST_LAYOUT;
			bundle.putInt(Common.FRAGMENT_ID, Common.SONGS_FRAGMENT);
            bundle.putString(FRAGMENT_HEADER, mContext.getResources().getString(R.string.songs));
			break;
		case Common.PLAYLISTS_FRAGMENT:
			mCurrentFragmentLayout = mApp.getSharedPreferences().getInt(PLAYLISTS_FRAGMENT_LAYOUT, LIST_LAYOUT);
			bundle.putInt(Common.FRAGMENT_ID, Common.PLAYLISTS_FRAGMENT);
            bundle.putString(FRAGMENT_HEADER, mContext.getResources().getString(R.string.playlists));
			break;
		case Common.GENRES_FRAGMENT:
			mCurrentFragmentLayout = mApp.getSharedPreferences().getInt(GENRES_FRAGMENT_LAYOUT, GRID_LAYOUT);
			bundle.putInt(Common.FRAGMENT_ID, Common.GENRES_FRAGMENT);
            bundle.putString(FRAGMENT_HEADER, mContext.getResources().getString(R.string.genres));
			break;
		case Common.FOLDERS_FRAGMENT:
			mCurrentFragmentLayout = mApp.getSharedPreferences().getInt(FOLDERS_FRAGMENT_LAYOUT, LIST_LAYOUT);
			bundle.putInt(Common.FRAGMENT_ID, Common.FOLDERS_FRAGMENT);
            bundle.putString(FRAGMENT_HEADER, mContext.getResources().getString(R.string.folders));
			break;
		}		
				
		//Return the correct layout fragment.
		if (mCurrentFragmentLayout==GRID_LAYOUT) {
			fragment = new GridViewFragment();
			fragment.setArguments(bundle);
		} else {
			fragment = new ListViewFragment();
			fragment.setArguments(bundle);
		}
		
		return fragment;
	}
	
	/**
	 * Loads the specified fragment into the target layout.
	 */
	public void switchContent(Fragment fragment) {
		getSupportFragmentManager().beginTransaction()
								   .replace(R.id.mainActivityContainer, fragment)
								   .commit();
		
		//Close the drawer(s).
		mDrawerLayout.closeDrawer(Gravity.START);

	}
	
	/**
	 * Loads the drawer fragments.
	 */
	private void loadDrawerFragments() {
		//Load the navigation drawer.
		getSupportFragmentManager().beginTransaction()
		   						   .replace(R.id.nav_drawer_container, new NavigationDrawerFragment())
		   						   .commit();
		
		//Load the current queue drawer.
        mQueueDrawerFragment = new QueueDrawerFragment();
		getSupportFragmentManager().beginTransaction()
		   						   .replace(R.id.current_queue_drawer_container, mQueueDrawerFragment)
		   						   .commit();
		
	}
	
	/**
	 * Called when the user taps on the "Play all" or "Shuffle all" action button.
	 */
	private void playAll(boolean shuffle) {
		
		//Mix it all up (or not).
		if (shuffle) {
			mApp.getSharedPreferences().edit().putBoolean("SHUFFLE_MODE", true).commit();
		}
		
		//Start the playback sequence.
		mApp.getPlaybackKickstarter().initPlayback(mContext, "", Common.SONGS_FRAGMENT, 0, true);
		
	}
	
	/**
	 * Initializes the ActionBar.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		//Inflate the menu.
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_activity, menu);
	    
	    //Set the ActionBar background
	    getActionBar().setBackgroundDrawable(UIElementsHelper.getGeneralActionBarBackground(mContext));
        getActionBar().setTitle(null);
	    getActionBar().setDisplayUseLogoEnabled(true);
        getActionBar().setLogo(R.drawable.text_logo);

		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * ActionBar item selection listener.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
	    }
		
		switch (item.getItemId()) {
		case R.id.action_search:
			//ArtistsFragment.showSearch();
			return true;
/*	    case R.id.action_settings:
	        Intent intent = new Intent(mContext, SettingsActivity.class);
	        intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	        startActivity(intent);
	        return true;*/
	    case R.id.action_queue_drawer:
	    	if (mDrawerLayout!=null && mCurrentQueueDrawerLayout!=null) {
		    	if (mDrawerLayout.isDrawerOpen(mCurrentQueueDrawerLayout)) {
		    		mDrawerLayout.closeDrawer(mCurrentQueueDrawerLayout);
		    	} else {
		    		mDrawerLayout.openDrawer(mCurrentQueueDrawerLayout);
		    	}
		    	
	    	}
	    	return true;
/*	    case R.id.action_play_all:
	    	playAll(false);
	    	return true;
	    case R.id.action_shuffle_all:
	    	playAll(true);
	    	return true;*/
	    default:
	        return super.onOptionsItemSelected(item);
	    }
		
	}
	
	@Override
	protected void onStart() {
	    super.onStart();
	    LocalBroadcastManager.getInstance(this)
	    					 .registerReceiver((mReceiver), new IntentFilter(Common.UPDATE_UI_BROADCAST));
	
	}

	@Override
	protected void onStop() {
	    LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
	    super.onStop();
	    
	}
	
	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }
	
	/**
	 * Getters/Setters.
	 */
	
	public int getCurrentFragmentId() {
		return mCurrentFragmentId;
	}
	
	public void setCurrentFragmentId(int id) {
		mCurrentFragmentId = id;
	}
	
}
