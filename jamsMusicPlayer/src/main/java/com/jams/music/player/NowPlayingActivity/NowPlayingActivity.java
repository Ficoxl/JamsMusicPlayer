package com.jams.music.player.NowPlayingActivity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.jams.music.player.Animations.FadeAnimation;
import com.jams.music.player.Animations.TranslateAnimation;
import com.jams.music.player.AsyncTasks.AsyncRemovePinnedSongsTask;
import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.Dialogs.RepeatSongRangeDialog;
import com.jams.music.player.EqualizerAudioFXActivity.EqualizerFragment;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.R;
import com.jams.music.player.Services.AudioPlaybackService;
import com.jams.music.player.SettingsActivity.SettingsActivity;
import com.jams.music.player.Utils.Common;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.SimpleFloatViewManager;
import com.velocity.view.pager.library.VelocityViewPager;

import java.util.HashMap;

public class NowPlayingActivity extends FragmentActivity {

	private Context mContext;
	private Menu mMenu;
	private FragmentManager mFragmentManager;
	
	private DrawerLayout mDrawerLayout;
	private FrameLayout mDrawerParentLayout;
	private RelativeLayout mEqualizerLayout;
	
	//Current queue, only on sw600dp-land layouts.
	private RelativeLayout landscapeRootContainer;
    private DragSortListView currentQueueListView;
    private NowPlayingQueueListAdapter queueAdapter;
    private RelativeLayout currentQueueLayout;
    private ProgressBar loadingQueueProgress;
    private boolean tabletInLandscape = false;
	
	//Song info/seekbar elements.
    private RelativeLayout seekbarLayout;
    private TextView elapsedTime;
    private TextView remainingTime;
	private SeekBar mSeekbar;
	private ProgressBar mStreamingProgressBar;
	
	//Playback Controls.
	private RelativeLayout mControlsLayoutHeader;
	private ImageButton mPlayPauseButton;
	private ImageButton mNextButton;
	private ImageButton mPreviousButton;
	private ImageButton mShuffleButton;
	private ImageButton mRepeatButton;
	
	//Playlist pager.
    private PlaylistPagerAdapter mPlaylistPagerAdapter;
    private VelocityViewPager mPlaylistViewPager;
	
    //Handler object.
    private Handler mHandler = new Handler();
    
    //Differentiates between a user's scroll input and a programmatic scroll.
    private boolean USER_SCROLL = true;
    
    //Equalizer fragment.
    private EqualizerFragment mEqualizerFragment;
    private boolean mIsEqualizerVisible = false;
    
    //Global objects provider.
    private Common mApp;
    
    //HashMap that passes on song information to the "Download from Cloud" dialog.
    private HashMap<String, String> metadata;
    
    //Receiver to update the UI.
    private boolean mTrackComplete = false;
    
    //Interface instance and flags.
    private NowPlayingActivityListener mNowPlayingActivityListener;
    public static final String START_SERVICE = "StartService";
    
    @SuppressLint("NewApi")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
    	mContext = getApplicationContext();
    	mApp = (Common) getApplicationContext();
    	mApp.setNowPlayingActivity(this);
    	setNowPlayingActivityListener(mApp.getPlaybackKickstarter());
    	
    	//Set the UI theme.
        setTheme();
        
        super.onCreate(savedInstanceState);	
    	setContentView(R.layout.activity_now_playing);
    	
    	//Set the volume stream for this activity.
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        
        //Drawer layout.
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_activity_drawer_root);
        mDrawerLayout.setDrawerListener(mDrawerListener);
        
        //Equalizer layout.
        mEqualizerLayout = (RelativeLayout) findViewById(R.id.equalizer_fragment_container);
        
        //ViewPager.
        mPlaylistViewPager = (VelocityViewPager) findViewById(R.id.nowPlayingPlaylistPager);
    	
    	//Playback Controls
        mControlsLayoutHeader = (RelativeLayout) findViewById(R.id.relativeLayout1);
    	mPlayPauseButton = (ImageButton) findViewById(R.id.playPauseButton);
    	mNextButton = (ImageButton) findViewById(R.id.nextButton);
    	mPreviousButton = (ImageButton) findViewById(R.id.previousButton);
    	mShuffleButton = (ImageButton) findViewById(R.id.shuffleButton);
    	mRepeatButton = (ImageButton) findViewById(R.id.repeatButton);
    	
    	//Song info/seekbar elements.
    	seekbarLayout = (RelativeLayout) findViewById(R.id.seekbar_progress_bar_parent);
    	mSeekbar = (SeekBar) findViewById(R.id.nowPlayingSeekBar);
    	elapsedTime = (TextView) findViewById(R.id.elapsedTime);
    	remainingTime = (TextView) findViewById(R.id.remainingTime);
    	mStreamingProgressBar = (ProgressBar) findViewById(R.id.startingStreamProgressBar);
    	mStreamingProgressBar.setVisibility(View.GONE);
    	
    	elapsedTime.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Regular"));
    	elapsedTime.setPaintFlags(elapsedTime.getPaintFlags() |
    							  Paint.ANTI_ALIAS_FLAG |
    							  Paint.SUBPIXEL_TEXT_FLAG);
    	
    	remainingTime.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Regular"));
    	remainingTime.setPaintFlags(remainingTime.getPaintFlags() |
    							    Paint.ANTI_ALIAS_FLAG |
    							    Paint.SUBPIXEL_TEXT_FLAG);
    	
    	//Tablet layout specific code.
    	if (getResources().getBoolean(R.bool.tablet_in_landscape) || 
            mApp.getOrientation()==Common.ORIENTATION_LANDSCAPE) {
            tabletInLandscape = true;
        } else {
            tabletInLandscape = false;
        }

        if (tabletInLandscape) {
            landscapeRootContainer = (RelativeLayout) findViewById(R.id.nowPlayingRootContainer);
            currentQueueListView = (DragSortListView) findViewById(R.id.queue_list_view);
            currentQueueLayout = (RelativeLayout) findViewById(R.id.now_playing_tablet_queue_layout);
            loadingQueueProgress = (ProgressBar) findViewById(R.id.now_playing_land_loading);

            //Set the drawer backgrounds based on the theme.
            if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME") ||
                mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_THEME")) {
                currentQueueLayout.setBackgroundColor(0xFF191919);
            } else {
                currentQueueLayout.setBackgroundColor(0xFFFFFFFF);
            }

        } else {
            //Initialize the Current Queue drawer.
            mDrawerParentLayout = (FrameLayout) findViewById(R.id.now_playing_drawer_frame_root);
            currentQueueLayout = (RelativeLayout) findViewById(R.id.main_activity_queue_drawer);
            currentQueueListView = (DragSortListView) findViewById(R.id.queue_list_view);

            currentQueueLayout.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View arg0, MotionEvent arg1) {
                    //Don't let touch events pass through the drawer.
                    return true;
                }

            });

            //Set the drawer backgrounds based on the theme.
            if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME") ||
                mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_THEME")) {
                currentQueueLayout.setBackgroundColor(0xFF191919);
                mDrawerLayout.setBackgroundColor(0xFF191919);
            } else {
                currentQueueLayout.setBackgroundColor(0xFFFFFFFF);
                mDrawerLayout.setBackgroundColor(0xFFFFFFFF);
            }

            initializeDrawer();
        }
    	
        //Set the theme for the control headers and seekbar background.
        mControlsLayoutHeader.setBackgroundResource(UIElementsHelper.getNowPlayingControlsBackground(mContext));
        seekbarLayout.setBackgroundResource(UIElementsHelper.getNowPlayingControlsBackground(mContext));
    	
    	try {
    		mSeekbar.setThumb(getResources().getDrawable(R.drawable.transparent_drawable));
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

    	mNextButton.setImageResource(UIElementsHelper.getIcon(mContext, "btn_playback_next"));
    	mPreviousButton.setImageResource(UIElementsHelper.getIcon(mContext, "btn_playback_previous"));
    	
    	if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME") ||
        	mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_THEME")) {
    		mNextButton.setAlpha(1f);
    		mPreviousButton.setAlpha(1f);
        }
    	
    	//KitKat specific layout code.
        setKitKatTranslucentBars();
    	
    	//Set the control buttons.
    	setPlayPauseButton();
        setShuffleButtonIcon();
        setRepeatButtonIcon();
    	
        //Set the listeners.
    	mSeekbar.setOnSeekBarChangeListener(seekBarChangeListener);
    	mNextButton.setOnClickListener(mOnClickNextListener);
    	mPreviousButton.setOnClickListener(mOnClickPreviousListener);
    	mPlayPauseButton.setOnClickListener(mOnClickPlayPauseListener);
    	mShuffleButton.setOnClickListener(shuffleButtonClickListener);
    	mRepeatButton.setOnClickListener(repeatButtonClickListener);
    	mRepeatButton.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View arg0) {
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				RepeatSongRangeDialog dialog = new RepeatSongRangeDialog();
				dialog.show(ft, "repeatSongRangeDialog");
				return false;
			}
    		
    	});
    	
    }
    
    /**
     * Updates this activity's UI elements based on the passed intent's 
     * update flag(s).
     */
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
    	
        @Override
        public void onReceive(Context context, Intent intent) {
        	
        	//Grab the bundle from the intent.
        	Bundle bundle = intent.getExtras();
        	
        	//Initializes the ViewPager.
        	if (intent.hasExtra(Common.INIT_PAGER) || 
        		intent.hasExtra(Common.NEW_QUEUE_ORDER))
        		initViewPager();
        	
        	//Updates the ViewPager's current page/position.
        	if (intent.hasExtra(Common.UPDATE_PAGER_POSTIION) && 
        		mPlaylistViewPager.getCurrentItem()!=mApp.getService().getCurrentSongIndex()) {
        		int position = Integer.parseInt(bundle.getString(Common.UPDATE_PAGER_POSTIION));
        		if (position==0) {
        			mPlaylistViewPager.setCurrentItem(position, false);
        		} else {
        			scrollViewPager(position, true, 1, false);
        		}
        	
        	}
        		
        	//Updates the playback control buttons.
        	if (intent.hasExtra(Common.UPDATE_PLAYBACK_CONTROLS)) {
        		setPlayPauseButton();
        		setRepeatButtonIcon();
        		setShuffleButtonIcon();
        		
        	}
        	
            //Displays the audibook toast.
        	if (intent.hasExtra(Common.SHOW_AUDIOBOOK_TOAST))
        		displayAudiobookToast(Long.parseLong(
        							  bundle.getString(
        							  Common.SHOW_AUDIOBOOK_TOAST)));
        	
        	//Updates the duration of the SeekBar.
        	if (intent.hasExtra(Common.UPDATE_SEEKBAR_DURATION))
        		setSeekbarDuration(Integer.parseInt(
        						   bundle.getString(
        						   Common.UPDATE_SEEKBAR_DURATION)));
        	
        	//Hides the seekbar and displays the streaming progress bar.
        	if (intent.hasExtra(Common.SHOW_STREAMING_BAR)) {
        		mSeekbar.setVisibility(View.INVISIBLE);
        		mStreamingProgressBar.setVisibility(View.VISIBLE);
        		mHandler.removeCallbacks(seekbarUpdateRunnable);
        		
        	}
        	
        	//Shows the seekbar and hides the streaming progress bar.
        	if (intent.hasExtra(Common.HIDE_STREAMING_BAR)) {
        		mSeekbar.setVisibility(View.VISIBLE);
        		mStreamingProgressBar.setVisibility(View.INVISIBLE);
        		mHandler.postDelayed(seekbarUpdateRunnable, 100);
        	}
        	
        	//Updates the buffering progress on the seekbar.
        	if (intent.hasExtra(Common.UPDATE_BUFFERING_PROGRESS))
        		mSeekbar.setSecondaryProgress(Integer.parseInt(
        									  bundle.getString(
        									  Common.UPDATE_BUFFERING_PROGRESS)));
        	
        	if (intent.hasExtra(Common.UPDATE_EQ_FRAGMENT) && 
        		mEqualizerFragment==null) {
        		//The equalizer fragment hasn't been initialized yet.
        		mFragmentManager = getSupportFragmentManager();
        		mEqualizerFragment = new EqualizerFragment();
        		mFragmentManager.beginTransaction()
        					    .add(R.id.equalizer_fragment_container, mEqualizerFragment, "EqualizerFragment")
        					    .commit();
        	}
        	
        	//Close this activity if the service is about to stop running.
        	if (intent.hasExtra(Common.SERVICE_STOPPING)) {
        		mHandler.removeCallbacks(seekbarUpdateRunnable);
        		finish();
        	}
        	
        }
        
    };
    
    /**
     * Sets the activity's theme based on user preferences.
     */
    private void setTheme() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
        	//Use the standard theme.
        	if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_THEME") ||
        		mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
        		this.setTheme(R.style.AppTheme);
        	} else {
        		this.setTheme(R.style.AppThemeLight);
        	}
        	
        } else {
        	//Use the theme without translucent nav bar.
        	if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_THEME") ||
        		mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
        		this.setTheme(R.style.AppThemeNoTranslucentNav);
        	} else {
        		this.setTheme(R.style.AppThemeNoTranslucentNavLight);
        	}
        	
        }
        
    }
    
    /**
     * Initializes the view pager.
     */
    private void initViewPager() {
    	mPlaylistViewPager.setVisibility(View.INVISIBLE);
    	mPlaylistPagerAdapter = new PlaylistPagerAdapter(getSupportFragmentManager());
    	mPlaylistViewPager.setAdapter(mPlaylistPagerAdapter);
    	mPlaylistViewPager.setOffscreenPageLimit(5);
    	mPlaylistViewPager.setOnPageChangeListener(mPageChangeListener);
    	mPlaylistViewPager.setCurrentItem(mApp.getService().getCurrentSongIndex(), false);

    	//Show the pager after a .1sec delay (keeps the animation smooth).
    	mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
                FadeAnimation fadeAnimation = new FadeAnimation(mPlaylistViewPager, 600, 0.0f,
                                                                1.0f, new DecelerateInterpolator(2.0f));

                fadeAnimation.animate();

			}

    	}, 200);

    }
    
    /**
     * Scrolls the ViewPager programmatically. If dispatchToListener 
     * is true, USER_SCROLL will be set to true.
     */
    private void scrollViewPager(int newPosition, 
    							 boolean smoothScroll, 
    							 int velocity, 
    							 boolean dispatchToListener) {
    	
    	USER_SCROLL = dispatchToListener;
    	mPlaylistViewPager.scrollToItem(newPosition, 
    									smoothScroll, 
    									velocity, 
    									dispatchToListener);
    	
    }
    
    /**
     * Sets the play/pause button states.
     */
    private void setPlayPauseButton() {
    	if (mApp.isServiceRunning())
	    	if (mApp.getService().isPlayingMusic()) {
	    		mPlayPauseButton.setImageResource(UIElementsHelper.getIcon(mContext, "pause"));
	    	} else {
	    		mPlayPauseButton.setImageResource(UIElementsHelper.getIcon(mContext, "play"));
	    	}
    	
    	else
    		mPlayPauseButton.setImageResource(UIElementsHelper.getIcon(mContext, "pause"));
    	
    }
    
    /**
     * Sets the repeat button icon based on the current repeat mode.
     */
    private void setRepeatButtonIcon() {
    	if (mApp.isServiceRunning())
	    	if (mApp.getService().getRepeatMode()== AudioPlaybackService.REPEAT_OFF) {
	    		mRepeatButton.setImageResource(UIElementsHelper.getIcon(mContext, "repeat"));
	    	} else if (mApp.getService().getRepeatMode()== AudioPlaybackService.REPEAT_PLAYLIST) {
	    		mRepeatButton.setImageResource(R.drawable.repeat_highlighted);
	    	} else if (mApp.getService().getRepeatMode()== AudioPlaybackService.REPEAT_SONG) {
	    		mRepeatButton.setImageResource(R.drawable.repeat_song);
	    	} else if (mApp.getService().getRepeatMode()== AudioPlaybackService.A_B_REPEAT) {
	    		mRepeatButton.setImageResource(R.drawable.repeat_song_range);
	    	}
    	
	    else
	    	mRepeatButton.setImageResource(UIElementsHelper.getIcon(mContext, "repeat"));
    	
    }
    
    /**
     * Sets the shuffle button icon based on the current shuffle mode.
     */
    private void setShuffleButtonIcon() {
    	if (mApp.isServiceRunning())
	        if (mApp.getService().isShuffleOn()==true) {
	        	mShuffleButton.setImageResource(R.drawable.shuffle_highlighted);
	        } else {
	        	mShuffleButton.setImageResource(UIElementsHelper.getIcon(mContext, "shuffle"));
	        }
    	
    	else
    		mShuffleButton.setImageResource(UIElementsHelper.getIcon(mContext, "shuffle"));
        
    }
    
    /**
     * Sets the seekbar's duration. Also updates the 
     * elapsed/remaining duration text.
     */
    private void setSeekbarDuration(int duration) {
    	mSeekbar.setMax(duration);
    	elapsedTime.setText("00:00");
    	remainingTime.setText("-" + convertMillisToMinsSecs(duration));
    	mHandler.postDelayed(seekbarUpdateRunnable, 100);
    }
    
    /**
     * Sets the KitKat translucent status/nav bar and adjusts 
     * the views' boundaries.
     */
    private void setKitKatTranslucentBars() {
    	//KitKat translucent status bar.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

        	//Set the window background.
        	getWindow().setBackgroundDrawable(UIElementsHelper.getGeneralActionBarBackground(mContext));
        	
        	//Set the background for the view.
        	RelativeLayout containerBackground = (RelativeLayout) findViewById(R.id.landscape_background);
        	if (containerBackground!=null) {
                if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME") ||
                	mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_THEME")) {
                	containerBackground.setBackgroundColor(0xFF191919);
                } else {
                	containerBackground.setBackgroundColor(0xFFFFFFFF);
                }
                
        	}
        	
    		int statusBarHeight = Common.getStatusBarHeight(mContext);
    		if (mDrawerLayout!=null) {
    			mDrawerLayout.setPadding(0, statusBarHeight, 0, 0);
    		}
    		
    		if (mEqualizerLayout!=null) {
    			mEqualizerLayout.setPadding(0, statusBarHeight, 0, 0);
    		}

            //Calculate ActionBar height.
            TypedValue tv = new TypedValue();
            int actionBarHeight = 0;
            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
            }
            
            if (landscapeRootContainer!=null) {
            	landscapeRootContainer.setPadding(0, statusBarHeight + actionBarHeight, 0, 0);
            	landscapeRootContainer.setClipToPadding(false);
            }
            
            if (mDrawerParentLayout!=null) {
            	mDrawerParentLayout.setPadding(0, actionBarHeight, 0, 0);
            	mDrawerParentLayout.setClipToPadding(false);
            }
            
            int orientation = mApp.getOrientation();
            if (orientation==Common.ORIENTATION_PORTRAIT) {
            	
                if (currentQueueListView!=null) {
                	currentQueueListView.setPadding(0, statusBarHeight, 0, 0);
                	currentQueueListView.setClipToPadding(false);
                }
                
            } else if (orientation==Common.ORIENTATION_LANDSCAPE) {
                
            	if (!getResources().getString(R.string.screen_type).equals("Phone")) {
                    
                    if (currentQueueListView!=null) {
                    	currentQueueListView.setPadding(0, statusBarHeight, 0, 0);
                    	currentQueueListView.setClipToPadding(false);
                    }
                    
            	}
                
            }

        }
        
    }
    
    /**
     * Seekbar change listener.
     */
    private OnSeekBarChangeListener seekBarChangeListener = new OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar seekBar, int seekBarPosition, boolean changedByUser) {
			
			//Check if the seekbar was scrolled while music was playing or if the user changed it.
			long currentSongDuration = mApp.getService().getCurrentMediaPlayer().getDuration();
			seekBar.setMax((int) currentSongDuration/1000);
			
			elapsedTime.setText(convertMillisToMinsSecs(seekBarPosition*1000));
			remainingTime.setText("-" + convertMillisToMinsSecs(currentSongDuration - (seekBarPosition*1000)));
			
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			mHandler.removeCallbacks(seekbarUpdateRunnable);
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			//Reinitiate the handler.
			mHandler.postDelayed(seekbarUpdateRunnable, 100);
			int seekBarPosition = seekBar.getProgress();
			mApp.getService().getCurrentMediaPlayer().seekTo(seekBarPosition*1000);
			
		}
		
	};
	
	/**
	 * Repeat button click listener.
	 */
	private View.OnClickListener repeatButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			
			mApp.getService().clearRepeatSongRange();
			if (mApp.getService().getRepeatMode()== AudioPlaybackService.REPEAT_OFF) {
				mRepeatButton.setImageResource(R.drawable.repeat_highlighted);
				mApp.getService().setRepeatMode(AudioPlaybackService.REPEAT_PLAYLIST);
				
			} else if (mApp.getService().getRepeatMode()== AudioPlaybackService.REPEAT_PLAYLIST) {
				mRepeatButton.setImageResource(R.drawable.repeat_song);
				mApp.getService().setRepeatMode(AudioPlaybackService.REPEAT_SONG);
				
			} else {
				mRepeatButton.setImageResource(UIElementsHelper.getIcon(mContext, "repeat"));
				mApp.getService().setRepeatMode(AudioPlaybackService.REPEAT_OFF);
				
			}
			
		}
		
	};
	
	/**
	 * Shuffle button click listener.
	 */
	private View.OnClickListener shuffleButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			//Toggle shuffle on/off.
			boolean shuffleOn = mApp.getService().toggleShuffleMode();
			
			if (shuffleOn)
				mShuffleButton.setImageResource(R.drawable.shuffle_highlighted);
			else
				mShuffleButton.setImageResource(UIElementsHelper.getIcon(mContext, "shuffle"));
			
		}
		
	};
    
	/**
	 * Click listener for the play/pause button.
	 */
    private View.OnClickListener mOnClickPlayPauseListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			boolean isPlaying = mApp.getService().togglePlaybackState();
			
			if (isPlaying)
				mHandler.post(seekbarUpdateRunnable);
			else
				mHandler.removeCallbacks(seekbarUpdateRunnable);
			
		}
		
	};
	
	/**
	 * Click listener for the previous button.
	 */
	private View.OnClickListener mOnClickPreviousListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			/*
			 * Scrolling the pager will automatically call the skipToTrack() method. 
			 * Since we're passing true for the dispatchToListener parameter, the 
			 * onPageSelected() listener will receive a callback once the scrolling 
			 * animation completes. This has the side-benefit of letting the animation 
			 * finish before starting playback (keeps the animation buttery smooth).
			 */
			int newPosition = mPlaylistViewPager.getCurrentItem() - 1;
			if (newPosition > -1) {
				scrollViewPager(newPosition, true, 1, true);
			} else {
				mPlaylistViewPager.setCurrentItem(0, false);
			}
			
		}
		
	};
    
	/**
	 * Click listener for the next button.
	 */
	private View.OnClickListener mOnClickNextListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			/*
			 * Scrolling the pager will automatically call the skipToTrack() method. 
			 * Since we're passing true for the dispatchToListener parameter, the 
			 * onPageSelected() listener will receive a callback once the scrolling 
			 * animation completes. This has the side-benefit of letting the animation 
			 * finish before starting playback (keeps the animation buttery smooth).
			 */
			int newPosition = mPlaylistViewPager.getCurrentItem() + 1;
			if (newPosition < mPlaylistPagerAdapter.getCount()) {
				scrollViewPager(newPosition, true, 1, true);
			} else {
				if (mApp.getService().getRepeatMode()== AudioPlaybackService.REPEAT_PLAYLIST)
					mPlaylistViewPager.setCurrentItem(0, false);
				else
					Toast.makeText(mContext, R.string.no_songs_to_skip_to, Toast.LENGTH_SHORT).show();
			}
				
			//mApp.getService().skipToNextTrack();
			
		}
		
	};
	
	/**
	 * Downloads a GMusic song for local playback.
	 */
    private void pinSong() {
    	
    	//Check if the app is getting pinned songs from the official GMusic app.
		if (mApp.isFetchingPinnedSongs()==false) {
			
			//Retrieve the name of the song.
			mApp.getService().getCursor().moveToPosition(mApp.getService().getPlaybackIndecesList().get(mApp.getService().getCurrentSongIndex()));
			
			//Get the song's ID/title.
			String songID = mApp.getService().getCursor().getString(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.SONG_ID));
			String songTitle = mApp.getService().getCursor().getString(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.SONG_TITLE));

    		//Check if a local copy of the song exists.
    		String localCopyPath = mApp.getDBAccessHelper().getLocalCopyPath(songID);
    		
			if (localCopyPath!=null) {
    			if (localCopyPath.isEmpty() || localCopyPath.equals("")) {
    				if (mMenu!=null) {
    					mMenu.findItem(R.id.action_pin).setIcon(R.drawable.pin_highlighted);
    				}
    				
    				//Get a mCursor with the current song and initiate the download process.
    				String selection = " AND " + DBAccessHelper.SONG_ID + "=" + "'" + songID + "'";
    				mApp.queueSongsToPin(false, false, selection);
    				String toastMessage = getResources().getString(R.string.downloading_no_dot) + " " + songTitle + ".";
					Toast.makeText(mContext, toastMessage, Toast.LENGTH_SHORT).show();
    			} else {
    				if (mMenu!=null) {
    					mMenu.findItem(R.id.action_pin).setIcon(R.drawable.pin_light);
    				}
    				
    	    		String selection = " AND " + DBAccessHelper.SONG_ID + "=" + "'" + songID + "'";
    	    		AsyncRemovePinnedSongsTask task = new AsyncRemovePinnedSongsTask(mContext, selection, null);
    	    		task.execute();
    			}
    		} else {
				if (mMenu!=null) {
					mMenu.findItem(R.id.action_pin).setIcon(R.drawable.pin_highlighted);
				}
    			
				//Get a mCursor with the current song and initiate the download process.
    			String selection = " AND " + DBAccessHelper.SONG_ID + "=" + "'" + songID + "'";
				mApp.queueSongsToPin(false, false, selection);
    		}
			
		} else {
			Toast.makeText(mContext, R.string.wait_until_pinning_complete, Toast.LENGTH_SHORT).show();
		}
		
    }
    
    /**
     * Initializes the current queue drawer for the first time.
     */
    private void initializeDrawer() {

		//Apply the card layout's background based on the color theme.
		if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME")) {
			currentQueueListView.setDivider(getResources().getDrawable(R.drawable.transparent_drawable));
			currentQueueListView.setDividerHeight(3);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(7, 3, 7, 3);
			currentQueueListView.setLayoutParams(layoutParams);
		} else if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
			currentQueueListView.setDivider(getResources().getDrawable(R.drawable.transparent_drawable));
			currentQueueListView.setDividerHeight(3);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(7, 3, 7, 3);
			currentQueueListView.setLayoutParams(layoutParams);
		}
		
		updateDrawerQueue(true, true);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
		
	}
    
    /**
     * Updates the queue's list.
     */
    private void updateDrawerQueue(boolean initAdapter, boolean setListViewPosition) {
		
		//If the service is running, load the queue. If not, show the empty text.
		if (mApp.getSharedPreferences().getBoolean("SERVICE_RUNNING", false)==true &&
			mApp.getService().getCurrentMediaPlayer()!=null && 
			mApp.getService().getPlaybackIndecesList()!=null &&
			mApp.getService().getPlaybackIndecesList().size() > 0) {
			
			//The service is running.
			currentQueueListView.setVisibility(View.VISIBLE);
			
			//Initialize the adapter (called the first time the drawer is initialized).
			if (initAdapter) {
				queueAdapter = new NowPlayingQueueListAdapter(mContext, 
															  mApp.getService().getPlaybackIndecesList());
			
				currentQueueListView.setAdapter(queueAdapter);
	    		currentQueueListView.setFastScrollEnabled(true);
	    		currentQueueListView.setDropListener(onDrop);
	    		currentQueueListView.setRemoveListener(onRemove);
	    		SimpleFloatViewManager simpleFloatViewManager = new SimpleFloatViewManager(currentQueueListView);
	    		simpleFloatViewManager.setBackgroundColor(Color.TRANSPARENT);
	    		currentQueueListView.setFloatViewManager(simpleFloatViewManager);
	    		
	    		//Set the listview dividers.
	    		if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME") ||
	            	mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_THEME")) {
	    			currentQueueListView.setDivider(getResources().getDrawable(R.drawable.list_divider));
	    			currentQueueListView.setDividerHeight(1);
	            } else {
	            	currentQueueListView.setDivider(getResources().getDrawable(R.drawable.list_divider_light));
	            	currentQueueListView.setDividerHeight(1);
	            }
	    		
	            currentQueueListView.setOnItemClickListener(new OnItemClickListener() {

	    			@Override
	    			public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
	    				
	    				//Change the mCursor position in the service to skip to the appropriate track.
	    				if (mApp.getService().getCurrentMediaPlayer()!=null && 
	    					mApp.getService().getMediaPlayer()!=null &&
	    					mApp.getService().getMediaPlayer2()!=null) {
	    					
	    					if (mApp.getService().getCursor().getCount() > index) {
	        					mApp.getService().skipToTrack(index);
	        					
	    					}
	    					
	    				}
	    				
	    			}
	            	
	            });
	    		
			} else {
				if (queueAdapter!=null) {
					queueAdapter.notifyDataSetChanged();
				}
				
			}
            
    		//Scroll down to the current song only if the drawer isn't already open.
    		if (setListViewPosition) {
    			if (!mDrawerLayout.isDrawerOpen(Gravity.END)) {
    				currentQueueListView.setSelection(mApp.getService().getCurrentSongIndex());
    			}
    			
    		}

		} else {
			//The service isn't running.
			currentQueueListView.setVisibility(View.GONE);
		}
		
	}
    
    /**
     * Drawer open/close listener.
     */
    private DrawerListener mDrawerListener = new DrawerListener() {

		@Override
		public void onDrawerClosed(View drawer) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onDrawerOpened(View drawer) {
			currentQueueListView.setSelection(mApp.getService().getCurrentSongIndex());
			
		}

		@Override
		public void onDrawerSlide(View drawer, float arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onDrawerStateChanged(int newState) {
			// TODO Auto-generated method stub
			
		}
    	
    };
    
    /**
     * 
     */
    private void initNowPlayingQueue(boolean initAdapter, boolean setListViewPosition) {
		
		//If the service is running, load the queue. If not, show the empty text.
		if (mApp.getSharedPreferences().getBoolean("SERVICE_RUNNING", false)==true &&
			mApp.getService().getCurrentMediaPlayer()!=null && 
			mApp.getService().getPlaybackIndecesList()!=null &&
			mApp.getService().getPlaybackIndecesList().size() > 0) {
			
			//The service is running.
			currentQueueListView.setVisibility(View.VISIBLE);
			
			//Initialize the adapter (called the first time the drawer is initialized).
			if (initAdapter) {
				queueAdapter = new NowPlayingQueueListAdapter(this, 
															  mApp.getService().getPlaybackIndecesList());
			
				currentQueueListView.setAdapter(queueAdapter);
	    		currentQueueListView.setFastScrollEnabled(true);
	    		currentQueueListView.setDropListener(onDrop);
	    		currentQueueListView.setRemoveListener(onRemove);
	    		SimpleFloatViewManager simpleFloatViewManager = new SimpleFloatViewManager(currentQueueListView);
	    		simpleFloatViewManager.setBackgroundColor(Color.TRANSPARENT);
	    		currentQueueListView.setFloatViewManager(simpleFloatViewManager);
	    		
	    		//Set the listview dividers.
	    		if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME") ||
	            	mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_THEME")) {
	    			currentQueueListView.setDivider(getResources().getDrawable(R.drawable.list_divider));
	    			currentQueueListView.setDividerHeight(1);
	            } else {
	            	currentQueueListView.setDivider(getResources().getDrawable(R.drawable.list_divider_light));
	            	currentQueueListView.setDividerHeight(1);
	            }
	    		
	            currentQueueListView.setOnItemClickListener(new OnItemClickListener() {

	    			@Override
	    			public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
	    				
	    				//Change the mCursor position in the service to skip to the appropriate track.
	    				if (mApp.getService().getCurrentMediaPlayer()!=null && 
	    					mApp.getService().getMediaPlayer()!=null &&
	    					mApp.getService().getMediaPlayer2()!=null) {
	    					
	    					if (mApp.getService().getCursor().getCount() > index) {
	        					mApp.getService().skipToTrack(index);
	        					
	    					}
	    					
	    					if (index < mPlaylistPagerAdapter.getCount()) {
	    						mPlaylistViewPager.setCurrentItem(index, true);
	    						try {
	    							queueAdapter.notifyDataSetChanged();
	    						} catch (Exception e) {
	    							e.printStackTrace();
	    						}
	    						
	    					}
	    					
	    				}
	    				
	    			}
	            	
	            });
	            
	            //Fade out the progressbar and fade in the queue list.
	            FadeAnimation fadeOutAnimation = new FadeAnimation(loadingQueueProgress, 400, 1.0f, 
	            												   0.0f, new LinearInterpolator());
	            fadeOutAnimation.animate();
	            
	            FadeAnimation fadeInAnimation = new FadeAnimation(currentQueueListView, 400, 0.0f, 
						   										  1.0f, new LinearInterpolator());
	            fadeInAnimation.animate();
	    		
			} else {
				try {
					queueAdapter.notifyDataSetChanged();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
            
    		//Scroll down to the current song only if the drawer isn't already open.
    		if (setListViewPosition) {
    			currentQueueListView.setSelection(mApp.getService().getCurrentSongIndex());
    		}

		} else {
			//The service isn't running.
			currentQueueListView.setVisibility(View.GONE);
		}
		
	}
    
    /**
     * Provides callback methods when the ViewPager's position/current page has changed.
     */
    private VelocityViewPager.OnPageChangeListener mPageChangeListener = new VelocityViewPager.OnPageChangeListener() {

		@Override
		public void onPageScrollStateChanged(int scrollState) {
			if (scrollState==VelocityViewPager.SCROLL_STATE_DRAGGING)
				USER_SCROLL = true;
			
		}

		@Override
		public void onPageScrolled(int pagerPosition, float swipeVelocity, int offsetFromCurrentPosition) {
			
			/* swipeVelocity determines whether the viewpager has finished scrolling or not.
			 * Throw in an if statement that only allows the track to change when
			 * swipeVelocity is 0 (which means the page is done scrolling). This ensures
			 * that the tracks don't jump around or get truncated while the user is 
			 * swiping between different pages.
			 */

			if (mApp.getService().getCursor().getCount()!=1) {
				
				/* Change tracks ONLY when the user has finished the swiping gesture (swipeVelocity will be zero).
				 * Also, don't skip tracks if the new pager position is the same as the current mCursor position (indicates 
				 * that the starting and ending position of the pager is the same).
				 */
				if (swipeVelocity==0.0f && pagerPosition!=mApp.getService().getCurrentSongIndex()) {
					if (USER_SCROLL) {
						mApp.getService().skipToTrack(pagerPosition);
					}
					
				}
				
			}
			
		}

		@Override
		public void onPageSelected(int arg0) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	/**
	 * Slides away the entire player screen to reveal the equalizer. 
	 * Redisplays the player screen if the equalizer is already being 
	 * shown.
	 */
	public void showHideEqualizer() {
		
		if (mIsEqualizerVisible==false) {
			//Slide down the entire player screen.
			TranslateAnimation slideDownAnimation = new TranslateAnimation(mDrawerLayout, 400, new AccelerateInterpolator(), 
																		   View.INVISIBLE, 
																		   Animation.RELATIVE_TO_SELF, 0.0f, 
																		   Animation.RELATIVE_TO_SELF, 0.0f, 
																		   Animation.RELATIVE_TO_SELF, 0.0f, 
																		   Animation.RELATIVE_TO_SELF, 2.0f);
			
			slideDownAnimation.animate();
			mIsEqualizerVisible = true;
			
		} else {
			//Slide up the entire player screen.
			TranslateAnimation slideUpAnimation = new TranslateAnimation(mDrawerLayout, 400, new DecelerateInterpolator(), 
																		 View.VISIBLE, 
																		 Animation.RELATIVE_TO_SELF, 0.0f, 
																		 Animation.RELATIVE_TO_SELF, 0.0f, 
																		 Animation.RELATIVE_TO_SELF, 2.0f, 
																		 Animation.RELATIVE_TO_SELF, 0.0f);

			slideUpAnimation.animate();
			mIsEqualizerVisible = false;
			
		}
		
		//Update the ActionBar.
		invalidateOptionsMenu();
		
	}
	
	/**
	 * Drop listener for the current queue list.
	 */
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
                	
                	queueReorderedInvalidatePager(from, to);
                	return;
                	
                } else if (from > mApp.getService().getCurrentSongIndex() && to <= mApp.getService().getCurrentSongIndex()) {
                	//One of the next songs was moved to a position before the current song. Move currentSongIndex forward by 1.
                	mApp.getService().setCurrentSongIndex(mApp.getService().getCurrentSongIndex()+1);
                	mApp.getService().setEnqueueReorderScalar(mApp.getService().getEnqueueReorderScalar()+1);
                	
                	//Check which mediaPlayer is currently playing, and prepare the other mediaPlayer.
                	mApp.getService().prepareAlternateMediaPlayer();
                	
                	queueReorderedInvalidatePager(from, to);
                	return;
                	
                } else if (from < mApp.getService().getCurrentSongIndex() && to==mApp.getService().getCurrentSongIndex()) {
                	/* One of the previous songs was moved to the current song's position (visually speaking, 
                	 * the new song will look like it was placed right after the current song.
                	 */
                	mApp.getService().setCurrentSongIndex(mApp.getService().getCurrentSongIndex()-1);
                	mApp.getService().setEnqueueReorderScalar(mApp.getService().getEnqueueReorderScalar()-1);
                	
                	//Check which mediaPlayer is currently playing, and prepare the other mediaPlayer.
                	mApp.getService().prepareAlternateMediaPlayer();
                	
                	queueReorderedInvalidatePager(from, to);
                	return;
                
            	} else if (from < mApp.getService().getCurrentSongIndex() && to > mApp.getService().getCurrentSongIndex()) {
                	//One of the previous songs was moved to a position after the current song. Move currentSongIndex back by 1.
            		mApp.getService().setCurrentSongIndex(mApp.getService().getCurrentSongIndex()-1);
                	mApp.getService().setEnqueueReorderScalar(mApp.getService().getEnqueueReorderScalar()-1);
                	
                	//Check which mediaPlayer is currently playing, and prepare the other mediaPlayer.
                	mApp.getService().prepareAlternateMediaPlayer();
                	
                	queueReorderedInvalidatePager(from, to);
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
            
            queueReorderedInvalidatePager(from, to);
        }
        
    };
    
    /**
     * Invalidates the ViewPager if the queue is reordered.
     * 
     * @param from The index from which the item was moved.
     * @param to The index to which the item was moved.
     */
    public void queueReorderedInvalidatePager(int from, int to) {
        if (Math.abs(mPlaylistViewPager.getCurrentItem()-from) <= 10 ||
        	Math.abs(mPlaylistViewPager.getCurrentItem()-to) <= 10) {

        	//Invalidate the pager and redraw it.
        	mPlaylistViewPager.setAdapter(null);
        	mPlaylistPagerAdapter = null;
        	mPlaylistPagerAdapter = new PlaylistPagerAdapter(getSupportFragmentManager());
        	mPlaylistViewPager.setAdapter(mPlaylistPagerAdapter);
            mPlaylistPagerAdapter.notifyDataSetChanged();
            mPlaylistViewPager.invalidate();
        	mPlaylistViewPager.setCurrentItem(mApp.getService().getCurrentSongIndex(), false);
        }
        
    }
    
    /**
     * Remove interface for the queue list.
     */
    private DragSortListView.RemoveListener onRemove = new DragSortListView.RemoveListener() {
    	
        @Override
        public void remove(int which) {

        	//Stop the service if we just removed the last (and only) song.
        	if (mApp.getService().getPlaybackIndecesList().size()==1) {
        		finish();
        		mContext.stopService(new Intent(mContext, AudioPlaybackService.class));
        		return;
        	}
        	
            //If the song that was removed is the next song, reload it.
            if (mApp.getService().getPlaybackIndecesList().size() > (mApp.getService().getCurrentSongIndex()+1)) {
                if (queueAdapter.getItem(which)==mApp.getService().getPlaybackIndecesList().get(mApp.getService().getCurrentSongIndex()+1)) {

                	//Check which mediaPlayer is currently playing, and prepare the other mediaPlayer.
                	mApp.getService().prepareAlternateMediaPlayer();
                	
                } else if (queueAdapter.getItem(which)==mApp.getService().getPlaybackIndecesList().get(mApp.getService().getCurrentSongIndex())) {
                	mApp.getService().setCurrentSongIndex(mApp.getService().getCurrentSongIndex()+1);
                	mApp.getService().prepareMediaPlayer(mApp.getService().getCurrentSongIndex());
                	mApp.getService().setCurrentSongIndex(mApp.getService().getCurrentSongIndex()-1);
                } else if (queueAdapter.getItem(which) < mApp.getService().getPlaybackIndecesList().get(mApp.getService().getCurrentSongIndex())) {
                	mApp.getService().setCurrentSongIndex(mApp.getService().getCurrentSongIndex()-1);
                }
                
            } else if (which==(mApp.getService().getPlaybackIndecesList().size()-1) &&
      		   	   	   mApp.getService().getCurrentSongIndex()==(mApp.getService().getPlaybackIndecesList().size()-1)) {	
            	//The current song was the last one and it was removed. Time to back up to the previous song.
            	mApp.getService().setCurrentSongIndex(mApp.getService().getCurrentSongIndex()-1);
            	mApp.getService().prepareMediaPlayer(mApp.getService().getCurrentSongIndex());
            } else {
            	//Check which mediaPlayer is currently playing, and prepare the other mediaPlayer.
            	mApp.getService().prepareAlternateMediaPlayer();
            	
            }
            
            //Remove the item from the adapter.
            queueAdapter.remove(queueAdapter.getItem(which));
            mPlaylistPagerAdapter.notifyDataSetChanged();

            if (Math.abs(mPlaylistViewPager.getCurrentItem()-which) <= 2) {
            	mPlaylistPagerAdapter = null;
                mPlaylistPagerAdapter= new PlaylistPagerAdapter(getSupportFragmentManager());
                
                //Set the ViewPager's adapter and the current song.
                mPlaylistPagerAdapter.notifyDataSetChanged();
                mPlaylistViewPager.setAdapter(mPlaylistPagerAdapter);
                mPlaylistViewPager.setOffscreenPageLimit(10);
            	mPlaylistViewPager.setCurrentItem(mApp.getService().getCurrentSongIndex(), false);
            }
           
        }
        
    };
    
    /**
     * @deprecated
     * Applies the correct transformer effect to the ViewPager.
     */
    @SuppressWarnings("unused")
	private void setPlaylistPagerAnimation() {
    	if (mApp.getSharedPreferences().getInt("TRACK_CHANGE_ANIMATION", 0)==0) {
    		//Don't set a transformer.
    	} else if (mApp.getSharedPreferences().getInt("TRACK_CHANGE_ANIMATION", 0)==1) {
    		//mPlaylistViewPager.setPageTransformer(true, new ZoomOutPageTransformer(0.85f));
    	} else if (mApp.getSharedPreferences().getInt("TRACK_CHANGE_ANIMATION", 0)==2) {
    		//mPlaylistViewPager.setPageTransformer(true, new DepthPageTransformer());
    	}
    	
    }
	
    /**
     * Create a new Runnable to update the seekbar and time every 100ms.
     */
    public Runnable seekbarUpdateRunnable = new Runnable() {
    	
    	public void run() {
    		
    		try {
    			if (mApp.getService().isPlayingMusic()) {
            		long currentPosition = mApp.getService().getCurrentMediaPlayer().getCurrentPosition();
            		int currentPositionInSecs = (int) currentPosition/1000;

            		mSeekbar.setProgress(currentPositionInSecs);
            		mHandler.postDelayed(seekbarUpdateRunnable, 100);
        		}
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    		
    	}
    	
    };
    
    public class PlaylistPagerAdapter extends FragmentStatePagerAdapter {

        public PlaylistPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
          
        }

        //This method controls the layout that is shown on each screen.
        @Override
        public Fragment getItem(int position) {

        	/* PlaylistPagerFragment.java will be shown on every pager screen. However, 
        	 * the fragment will check which screen (position) is being shown, and will
        	 * update its TextViews and ImageViews to match the song that's being played. */
    		Fragment fragment = new PlaylistPagerFragment();
    		
    		Bundle bundle = new Bundle();
    		bundle.putInt("POSITION", position);
    		fragment.setArguments(bundle);
    		return fragment;

        }

        @Override
        public int getCount() {
        	
        	try {
            	if (mApp.getService().getPlaybackIndecesList()!=null) {
            		return mApp.getService().getPlaybackIndecesList().size();
            	} else {
            		mApp.getService().stopSelf();
            		return 0;
            	}
        	} catch (Exception e) {
        		e.printStackTrace();
        		return 0;
        	}

        }

    }
	
	/**
	 * Displays the "Resuming from xx:xx" toast.
	 */
	public void displayAudiobookToast(long resumePlaybackPosition) {
		try {
			String resumingFrom = mContext.getResources().getString(R.string.resuming_from) 
								+ " " + convertMillisToMinsSecs(resumePlaybackPosition) + ".";
			
			Toast.makeText(mContext, resumingFrom, Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Converts millis to mins and secs and returns a 
	 * formatted string.
	 */
    private String convertMillisToMinsSecs(long milliseconds) {
    	
    	int secondsValue = (int) (milliseconds / 1000) % 60 ;
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

    	if (minutesValue < 10) {
    		minutes = "0" + minutesValue;
    	} else {
    		minutes = "" + minutesValue;
    	}
    	
    	if (hoursValue < 10) {
    		hours = "0" + hoursValue;
    	} else {
    		hours = "" + hoursValue;
    	}
    	
    	String output = "";
    	
    	if (hoursValue!=0) {
    		output = hours + ":" + minutes + ":" + seconds;
    	} else {
    		output = minutes + ":" + seconds;
    	}
    	
    	return output;
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.action_pin:
			pinSong();
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
	    case R.id.action_equalizer:
	    	showHideEqualizer();
	    	return true;
	    case R.id.action_settings:
	    	Intent intent = new Intent(mContext, SettingsActivity.class);
	    	startActivity(intent);
	    	return true;
	    case R.id.action_done:
	    	mEqualizerFragment.buildApplyToDialog().show();
	    	return true;
	    default:
	    	//Return false to allow the fragment to handle the item click.
	        return false;
	    }

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.now_playing, menu);
	    mMenu = menu;
	    
	    //Set the ActionBar title text color.
	    int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
		TextView abTitle = (TextView) findViewById(titleId);
		
		if (mApp.getSharedPreferences().getString("NOW_PLAYING_COLOR", "BLUE").equals("WHITE")) 
			abTitle.setTextColor(0xFF444444);
		else
			abTitle.setTextColor(0xFFFFFFFF);

	    //Show the appropriate ActionBar.
		if (mIsEqualizerVisible) {
			getActionBar().setTitle(R.string.equalizer);
			showEqualizerActionBar(menu);
		} else {
			getActionBar().setTitle(R.string.now_playing);
			showNowPlayingActionBar(menu);
		}
			
		return super.onCreateOptionsMenu(menu);
	}
	
	/**
	 * Loads the ActionBar for the player screen.
	 */
	private void showNowPlayingActionBar(Menu menu) { 
	    
	    ActionBar actionBar = getActionBar();
	    if (actionBar!=null) {
		    actionBar.setTitle(getResources().getString(R.string.now_playing));
		    actionBar.setHomeButtonEnabled(true);
		    actionBar.setDisplayHomeAsUpEnabled(true);
		    actionBar.setBackgroundDrawable(UIElementsHelper.getGeneralActionBarBackground(getApplicationContext()));
	    }
	    
	    //Hide the EQ toggle and "done" icon.
	    menu.findItem(R.id.action_equalizer_toggle).setVisible(false);
	    menu.findItem(R.id.action_done).setVisible(false);
	    
		//Update the player screen's ActionBar icon.
		try {
			actionBar.setIcon(getResources().getDrawable(R.drawable.ic_launcher));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Hide the queue button if we're in landscape mode (on a tablet).
		if (tabletInLandscape) {
			if (menu!=null) {
				menu.findItem(R.id.action_queue_drawer).setVisible(false);
			}
			
		}
		
		if (menu!=null && mApp.getSharedPreferences().getString("NOW_PLAYING_COLOR", "BLUE").equals("WHITE")) {
			MenuItem pinIcon = menu.findItem(R.id.action_pin);
			MenuItem equalizerIcon = menu.findItem(R.id.action_equalizer);
			MenuItem queueIcon = menu.findItem(R.id.action_queue_drawer);
			
			if (pinIcon!=null)
				pinIcon.setIcon(getResources().getDrawable(R.drawable.pin));
			
			if (equalizerIcon!=null)
				equalizerIcon.setIcon(getResources().getDrawable(R.drawable.equalizer));
		
			if (queueIcon!=null)
				queueIcon.setIcon(getResources().getDrawable(R.drawable.queue_drawer));
			
		}
		
	}
	
	/**
	 * Loads the ActionBar for the equalizer fragment.
	 */
	private void showEqualizerActionBar(Menu menu) {
		
		//Hide all menu items except the toggle button and "done" icon.
		menu.findItem(R.id.action_equalizer).setVisible(false);
		menu.findItem(R.id.action_pin).setVisible(false);
		menu.findItem(R.id.action_queue_drawer).setVisible(false);
		menu.findItem(R.id.action_settings).setVisible(false);
		menu.findItem(R.id.action_done).setVisible(true);
		
		/**
		 * The Toggle button in the actionbar doesn't work at this point. The setChecked() 
		 * method doesn't do anything, so there's no way to programmatically set the 
		 * switch to its correct position when the equalizer fragment is first shown. 
		 * Users will just have to rely on the "Reset" button in the equalizer fragment 
		 * to effectively switch off the equalizer.
		 */
		menu.findItem(R.id.action_equalizer_toggle).setVisible(false); //Hide the toggle for now.
		
		/*//Set the toggle listener.
		ToggleButton equalizerToggle = (ToggleButton) menu.findItem(R.id.action_equalizer_toggle)
									 		  			  .getActionView()
									 		  			  .findViewById(R.id.actionbar_toggle_switch);
		
		//Set the current state of the toggle.
		boolean toggleSetting = true;
		if (mApp.isEqualizerEnabled())
			toggleSetting = true;
		else
			toggleSetting = false;
		
		equalizerToggle.setChecked(toggleSetting);
		equalizerToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean state) {
				mApp.setIsEqualizerEnabled(state);
				
				if (state==true)
					mEqualizerFragment.applyCurrentEQSettings();
				
			}
			
		});*/
		
		getActionBar().setHomeButtonEnabled(false);
	    getActionBar().setDisplayHomeAsUpEnabled(false);
		
		if (menu!=null && mApp.getSharedPreferences().getString("NOW_PLAYING_COLOR", "BLUE").equals("WHITE")) {
			menu.findItem(R.id.action_done).setIcon(R.drawable.checkmark);
			
		}
		
	}

	/*
	 * Getter and setter methods.
	 */
	
	public SeekBar getSeekbar() {
		return mSeekbar;
	}
	
	public ProgressBar getStreamingProgressBar() {
		return mStreamingProgressBar;
	}
	
	public VelocityViewPager getPlaylistViewPager() {
		return mPlaylistViewPager;
	}
	
	public ImageButton getPlayPauseButton() {
		return mPlayPauseButton;
	}
 	
	public boolean getTrackComplete() {
		return mTrackComplete;
	}
	
	public EqualizerFragment getEqualizerFragment() {
		return mEqualizerFragment;
	}
	
	public NowPlayingActivityListener getNowPlayingActivityListener() {
		return mNowPlayingActivityListener;
	}
	
	public void setNowPlayingActivityListener(NowPlayingActivityListener listener) {
		mNowPlayingActivityListener = listener;
	}
	
	/**
	 * Interface that provides callbacks once this activity is 
	 * up and running.
	 */
	public interface NowPlayingActivityListener {
		
		/**
		 * Called once this activity's onResume() method finishes 
		 * executing.
		 */
		public void onNowPlayingActivityReady();
		
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();

	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (getIntent().hasExtra(START_SERVICE) && 
			getNowPlayingActivityListener()!=null) {
			getNowPlayingActivityListener().onNowPlayingActivityReady();
			
			/**
			 * To prevent the service from being restarted every time this 
			 * activity is resume, we're gonna have to remove the "START_SERVICE" 
			 * extra from the intent.
			 */
			getIntent().removeExtra(START_SERVICE);
			
		}
	}
	
	@Override
	public void onBackPressed() {

		if (mIsEqualizerVisible) {
			//Reset the EQ and show the player screen.
			mEqualizerFragment.new AsyncInitSlidersTask().execute();
			showHideEqualizer();
		} else {
			super.onBackPressed();
		}
		
	}
	
    @Override
    public void onStart() {
    	super.onStart();
    	//Initialize the broadcast manager that will listen for track changes.
    	LocalBroadcastManager.getInstance(mContext)
		 					 .registerReceiver((mReceiver), new IntentFilter(Common.UPDATE_UI_BROADCAST));
    	
    	/* Check if the service is up and running. If so, send out a broadcast message 
    	 * that will initialize this activity fully. This code block is what will 
    	 * initialize this activity fully if it is opened after the service is already 
    	 * up and running (the onServiceRunning() callback isn't available at this point).
    	 */
    	if (mApp.isServiceRunning() && mApp.getService().getCursor()!=null) {
    		String[] updateFlags = new String[] { Common.UPDATE_PAGER_POSTIION, 
    											  Common.UPDATE_SEEKBAR_DURATION, 
    											  Common.HIDE_STREAMING_BAR, 
    											  Common.INIT_PAGER, 
    											  Common.UPDATE_PLAYBACK_CONTROLS, 
    											  Common.UPDATE_EQ_FRAGMENT };
    		
        	String[] flagValues = new String[] { "" + mApp.getService().getCurrentSongIndex(), 
        										 "" + mApp.getService().getCurrentMediaPlayer().getDuration(), 
        										 "", "", "", "" };
        	mApp.broadcastUpdateUICommand(updateFlags, flagValues);
    	}
    	
    }
    
    @Override
    public void onStop() {
    	//Unregister the broadcast receivers.
    	LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReceiver);
    	super.onStop();
    	
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	super.onSaveInstanceState(savedInstanceState);
    	savedInstanceState.putBoolean("CALLED_FROM_FOOTER", true);
    	savedInstanceState.putBoolean("CALLED_FROM_NOTIF", true);
    }
	
}
