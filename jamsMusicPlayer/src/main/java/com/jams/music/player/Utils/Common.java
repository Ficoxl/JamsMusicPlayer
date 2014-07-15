package com.jams.music.player.Utils;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MergeCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.images.ImageManager;
import com.jams.music.player.AsyncTasks.AsyncGoogleMusicAuthenticationTask;
import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.GMusicHelpers.GMusicClientCalls;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.NowPlayingActivity.NowPlayingActivity;
import com.jams.music.player.PlaybackKickstarter.PlaybackKickstarter;
import com.jams.music.player.R;
import com.jams.music.player.Services.AudioPlaybackService;
import com.jams.music.player.Services.PinGMusicSongsService;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Singleton class that provides access to common objects
 * and methods used in the application.
 * 
 * @author Saravan Pantham
 */
public class Common extends Application {

	//Context.
	private Context mContext;
	
	//Service reference and flags.
	private AudioPlaybackService mService;
	private boolean mIsServiceRunning = false;
	
	//Playback kickstarter object.
	private PlaybackKickstarter mPlaybackKickstarter;
	
	//NowPlayingActivity reference.
	private NowPlayingActivity mNowPlayingActivity;
	
	//SharedPreferences.
	private static SharedPreferences mSharedPreferences;
	
	//Database access helper object.
	private static DBAccessHelper mDBAccessHelper;

    //Picasso instance.
    private Picasso mPicasso;
	
	//Indicates if the library is currently being built.
	private boolean mIsBuildingLibrary = false;
	private boolean mIsScanFinished = false;
	
	//Google Play Music access object.
	private GMusicClientCalls mGMusicClientCalls;
	private boolean mIsGMusicLoggedIn = false;
	
	//ImageManager for asynchronous image downloading.
	private ImageManager mImageManager;
	
	//ImageLoader/ImageLoaderConfiguration objects for ListViews and GridViews.
	private ImageLoader mImageLoader;
	private ImageLoaderConfiguration mImageLoaderConfiguration;
	
	//Image display options.
	private DisplayImageOptions mDisplayImageOptions;
	
	//Cursor that stores the songs that are currently queued for download.
	private Cursor mPinnedSongsCursor;
	
	//Specifies whether the app is currently downloading pinned songs from the GMusic app.
	private boolean mIsFetchingPinnedSongs = false;
	
	public static final String uid4 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvFlvWGADp9cW2LPuOIjDPB";
	public static final String uid2 = "ormNR2mpS8HR8utvhNHKs2AJzV8GLPh35m3rE6GPND4GsOdrbySPETG4+0fvagBr5E";
	public static final String uid6 = "QgMR7z76DJlRqy+VyVzmx7cly2JiXo+ZnISYKKn71oP+Xw+dO/eRKFy3EFCO7khMxc";
	public static final String uid1 = "6QouPH11nHJPzXspzdkJbTcifIIGFtEkquXjA0y19Gouab7Gir8yLOA4V3m0URRivP";
	public static final String uid3 = "QeOx8JsY766F6FgU8uJABWRDZbqHEYRwT7iGmn7ukt7h5z+DOsYWSRmZxwJh3cpkGo";
	public static final String uid5 = "Vyqp4UZWnzGiiq/fWFKs5rrc+m3obsEpUxteGavKAhhXJZKgwAGFgkUQIDAQAB";
	
	//GAnalytics flag.
	private boolean mIsGAnalyticsEnabled = true;
	
	//Broadcast elements.
	private LocalBroadcastManager mLocalBroadcastManager;
	public static final String UPDATE_UI_BROADCAST = "com.jams.music.player.NEW_SONG_UPDATE_UI";
	
	//Update UI broadcast flags.
	public static final String SHOW_AUDIOBOOK_TOAST = "AudiobookToast";
	public static final String UPDATE_SEEKBAR_DURATION = "UpdateSeekbarDuration";
	public static final String UPDATE_PAGER_POSTIION = "UpdatePagerPosition";
	public static final String UPDATE_PLAYBACK_CONTROLS = "UpdatePlabackControls";
	public static final String SERVICE_STOPPING = "ServiceStopping";
	public static final String SHOW_STREAMING_BAR = "ShowStreamingBar";
	public static final String HIDE_STREAMING_BAR = "HideStreamingBar";
	public static final String UPDATE_BUFFERING_PROGRESS = "UpdateBufferingProgress";
	public static final String INIT_PAGER = "InitPager";
	public static final String NEW_QUEUE_ORDER = "NewQueueOrder";
	public static final String UPDATE_EQ_FRAGMENT = "UpdateEQFragment";
	
	//Contants for identifying each fragment/activity.
	public static final String FRAGMENT_ID = "FragmentId";
	public static final int ARTISTS_FRAGMENT = 0;
	public static final int ALBUM_ARTISTS_FRAGMENT = 1;
	public static final int ALBUMS_FRAGMENT = 2;
	public static final int SONGS_FRAGMENT = 3;
	public static final int PLAYLISTS_FRAGMENT = 4;
	public static final int GENRES_FRAGMENT = 5;
	public static final int FOLDERS_FRAGMENT = 6;
	public static final int ARTISTS_FLIPPED_FRAGMENT = 7;
	public static final int ARTISTS_FLIPPED_SONGS_FRAGMENT = 8;
	public static final int ALBUM_ARTISTS_FLIPPED_FRAGMENT = 9;
	public static final int ALBUM_ARTISTS_FLIPPED_SONGS_FRAGMENT = 10;
	public static final int ALBUMS_FLIPPED_FRAGMENT = 11;
	public static final int GENRES_FLIPPED_FRAGMENT = 12;
	public static final int GENRES_FLIPPED_SONGS_FRAGMENT = 13;
	
	//Device orientation constants.
	public static final int ORIENTATION_PORTRAIT = 0;
	public static final int ORIENTATION_LANDSCAPE = 1;

    //Miscellaneous flags/identifiers.
    public static final String SONG_ID = "SongId";
    public static final String SONG_TITLE = "SongTitle";
    public static final String SONG_ALBUM = "SongAlbum";
    public static final String SONG_ARTIST = "SongArtist";
    public static final String ALBUM_ART = "AlbumArt";
    public static final String CURRENT_THEME = "CurrentTheme";
    public static final int DARK_THEME = 0;
    public static final int LIGHT_THEME = 1;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		//Application context.
		mContext = getApplicationContext();
		
		//SharedPreferences.
		mSharedPreferences = this.getSharedPreferences("com.jams.music.player", Context.MODE_PRIVATE);
		
		//Init the database.
		mDBAccessHelper = new DBAccessHelper(mContext);
    	
    	//Playback kickstarter.
    	mPlaybackKickstarter = new PlaybackKickstarter();

        //Picasso.
        mPicasso = new Picasso.Builder(mContext).build();

    	//ImageLoader.
    	mImageLoader = ImageLoader.getInstance();
    	mImageLoaderConfiguration = new ImageLoaderConfiguration.Builder(getApplicationContext())
    														   .memoryCache(new WeakMemoryCache())
    														   .memoryCacheSizePercentage(13)
    														   .imageDownloader(new ByteArrayUniversalImageLoader(mContext))
    														   .build();
    	mImageLoader.init(mImageLoaderConfiguration);
    	
        //Init DisplayImageOptions.
        initDisplayImageOptions();
    	
		//Log the user into Google Play Music only if the account is currently set up and active.
		if (mSharedPreferences.getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==true) {
			
			//Create a temp WebView to retrieve the user agent string.
			String userAgentString = "";
			if (mSharedPreferences.getBoolean("GOT_USER_AGENT", false)==false) {
				WebView webView = new WebView(getApplicationContext());
				webView.setVisibility(View.GONE);
				webView.loadUrl("http://www.google.com");
				userAgentString = webView.getSettings().getUserAgentString();
				mSharedPreferences.edit().putBoolean("GOT_USER_AGENT", true).commit();
				mSharedPreferences.edit().putString("USER_AGENT", userAgentString).commit();
				webView = null;
			}
			
			setGMusicClientCalls(GMusicClientCalls.getInstance(getApplicationContext()));
			GMusicClientCalls.setWebClientUserAgent(userAgentString);
			String accountName = mSharedPreferences.getString("GOOGLE_PLAY_MUSIC_ACCOUNT", "");
			
			//Authenticate with Google.
			AsyncGoogleMusicAuthenticationTask task = new AsyncGoogleMusicAuthenticationTask(mContext, false, accountName);
			task.execute();
			
		}
		
	}

    /**
     * Initializes a DisplayImageOptions object. The drawable shown
     * while an image is loading is based on the current theme.
     */
    public void initDisplayImageOptions() {

        //Create a set of options to optimize the bitmap memory usage.
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inJustDecodeBounds = false;
        options.inPurgeable = true;

        int emptyColorPatch = UIElementsHelper.getEmptyColorPatch(this);
        mDisplayImageOptions = null;
        mDisplayImageOptions = new DisplayImageOptions.Builder()
                                                      .showImageForEmptyUri(emptyColorPatch)
                                                      .showImageOnFail(emptyColorPatch)
                                                      .showImageOnLoading(emptyColorPatch)
                                                      .cacheInMemory(true)
                                                      .cacheOnDisc(true)
                                                      .decodingOptions(options)
                                                      .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                                                      .bitmapConfig(Bitmap.Config.ARGB_4444)
                                                      .delayBeforeLoading(400)
                                                      .displayer(new FadeInBitmapDisplayer(200))
                                                      .build();

    }

	/**
	 * Sends out a local broadcast that notifies all receivers to update 
	 * their respective UI elements.
	 */
	public void broadcastUpdateUICommand(String[] updateFlags, String[] flagValues) {
		Intent intent = new Intent(UPDATE_UI_BROADCAST);
		for (int i=0; i < updateFlags.length; i++) {
			intent.putExtra(updateFlags[i], flagValues[i]);
		}
		
		mLocalBroadcastManager = LocalBroadcastManager.getInstance(mContext);
		mLocalBroadcastManager.sendBroadcast(intent);
		
	}
	
	/**
	 * Toggles the equalizer.
	 */
	public void toggleEqualizer() {
		if (isEqualizerEnabled()) {
			getSharedPreferences().edit().putBoolean("EQUALIZER_ENABLED", true).commit();
			
			if (isServiceRunning()) {
				try {
					getService().getEqualizerHelper().getEqualizer().setEnabled(false);
					getService().getEqualizerHelper().getEqualizer2().setEnabled(false);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		} else {
			getSharedPreferences().edit().putBoolean("EQUALIZER_ENABLED", true).commit();
			
			if (isServiceRunning()) {
				try {
					getService().getEqualizerHelper().getEqualizer().setEnabled(true);
					getService().getEqualizerHelper().getEqualizer2().setEnabled(true);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		}
		
		//Reload the EQ settings.
		if (isServiceRunning()) {
			try {
				getService().getEqualizerHelper().releaseEQObjects();
				getService().initAudioFX();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	
 	/**
	 * Used to downsample a bitmap that's been downloaded from the internet.
	 */
	public Bitmap getDownsampledBitmap(Context ctx, URL url, int targetWidth, int targetHeight) {
        Bitmap bitmap = null;
        try {
            BitmapFactory.Options outDimens = getBitmapDimensions(url);

            int sampleSize = calculateSampleSize(outDimens.outWidth, outDimens.outHeight, targetWidth, targetHeight);

            bitmap = downsampleBitmap(url, sampleSize);

        } catch (Exception e) {
            //handle the exception(s)
        }

        return bitmap;
    }

	/**
	 * Retrieves the image dimensions of the input file.
	 * 
	 * @param url Url of the input file.
	 * @return A BitmapFactory.Options object with the output image dimensions.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
    public BitmapFactory.Options getBitmapDimensions(URL url) throws FileNotFoundException, IOException {
        BitmapFactory.Options outDimens = new BitmapFactory.Options();
        outDimens.inJustDecodeBounds = true; // the decoder will return null (no bitmap)

        InputStream is = url.openStream();
        // if Options requested only the size will be returned
        BitmapFactory.decodeStream(is, null, outDimens);
        is.close();

        return outDimens;
    }
    
    /**
     * Resamples a resource image to avoid OOM errors.
     * 
     * @param resID Resource ID of the image to be downsampled.
     * @param reqWidth Width of output image.
     * @param reqHeight Height of output image.
     * 
     * @return A bitmap of the resampled image.
     */
    public Bitmap decodeSampledBitmapFromResource(int resID, int reqWidth, int reqHeight) {

	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
	    options.inJustDecodeBounds = false;
	    options.inPurgeable = true;
	
	    return BitmapFactory.decodeResource(mContext.getResources(), resID, options);
    }
    
    /**
     * Resamples the specified input image file to avoid OOM errors.
     * 
     * @param inputFile Input file to be downsampled
     * @param reqWidth Width of the output file.
     * @param reqHeight Height of the output file.
     * @return The downsampled bitmap.
     */
    public Bitmap decodeSampledBitmapFromFile(File inputFile, int reqWidth, int reqHeight) {
    	
    	InputStream is = null;
        try {
        	
        	try {
				is = new FileInputStream(inputFile);
			} catch (Exception e) {
				//Return a null bitmap if there's an error reading the file.
				return null;
			}

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;
            options.inPurgeable = true;

            try {
				is = new FileInputStream(inputFile);
			} catch (FileNotFoundException e) {
				//Return a null bitmap if there's an error reading the file.
				return null;
			}
            
            return BitmapFactory.decodeStream(is, null, options);
        } finally {
            try {
            	if (is!=null) {
            		is.close();
            	}
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }
        
    }

    /**
     * Calculates the sample size for the resampling process.
     * 
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return The sample size.
     */
	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
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

    /**
     * Calculates the sample size for the resampling process.
	 *
     * @return The sample size.
     */
    public int calculateSampleSize(int width, int height, int targetWidth, int targetHeight) {
        float bitmapWidth = width;
        float bitmapHeight = height;

        int bitmapResolution = (int) (bitmapWidth * bitmapHeight);
        int targetResolution = targetWidth * targetHeight;

        int sampleSize = 1;

        if (targetResolution == 0) {
            return sampleSize;
        }

        for (int i = 1; (bitmapResolution / i) > targetResolution; i *= 2) {
            sampleSize = i;
        }

        return sampleSize;
    }

    /**
     * 
     * @param url
     * @param sampleSize
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public Bitmap downsampleBitmap(URL url, int sampleSize) throws FileNotFoundException, IOException {
        Bitmap resizedBitmap;
        BitmapFactory.Options outBitmap = new BitmapFactory.Options();
        outBitmap.inJustDecodeBounds = false; // the decoder will return a bitmap
        outBitmap.inSampleSize = sampleSize;

        InputStream is = url.openStream();
        resizedBitmap = BitmapFactory.decodeStream(is, null, outBitmap);
        is.close();

        return resizedBitmap;
    }
    
    /*
     * UI Elements Provider.
     */
    public static int getStatusBarHeight(Context context) {
    	int result = 0;
    	int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
    	if (resourceId > 0) {
    		result = context.getResources().getDimensionPixelSize(resourceId);
    	}
    	
    	return result;
    }
	
    /*
     * Download Manager implementation for pinning songs.
     */
    public void queueSongsToPin(boolean getAllPinnedSongs, boolean pinPlaylist, String selection) {
    	//If the current cursor is empty or null, retrieve the cursor using the selection parameter.
    	if (mPinnedSongsCursor==null || mPinnedSongsCursor.getCount()<=0) {
    		
    		if (getAllPinnedSongs==true) {
    			mPinnedSongsCursor = null;
    			mIsFetchingPinnedSongs = true;
    			Toast.makeText(mContext, R.string.getting_pinned_songs, Toast.LENGTH_LONG).show();
    		} else if (pinPlaylist==true) {
    			//Pinning from a playlist, so we'll need to use a db call that utilizes a JOIN.
    			mPinnedSongsCursor = mDBAccessHelper.getAllSongsInPlaylistSearchable(selection);
    		} else {
    			//Check if we're pinning a smart playlist.
    			if (selection.equals("TOP_25_PLAYED_SONGS")) {
    	            mPinnedSongsCursor = mDBAccessHelper.getTop25PlayedTracks(selection);
    	        } else if (selection.equals("RECENTLY_ADDED")) {
    	        	mPinnedSongsCursor = mDBAccessHelper.getRecentlyAddedSongs(selection);
    	        } else if (selection.equals("TOP_RATED")) {
    	        	mPinnedSongsCursor = mDBAccessHelper.getTopRatedSongs(selection);
    	        } else if (selection.equals("RECENTLY_PLAYED")) {
    	        	mPinnedSongsCursor = mDBAccessHelper.getRecentlyPlayedSongs(selection);
    	        } else {
    	        	//Not playing from a smart playlist. Just use a regular db query that searches songs.
        			mPinnedSongsCursor = mDBAccessHelper.getAllSongsSearchable(selection);
    	        }
    			
    		}

    		Intent intent = new Intent(this, PinGMusicSongsService.class);
    		startService(intent);
    		
    	} else {
    		//mPinnedSongsCursor already has songs queued, so append a new intermCursor;
    		Cursor intermCursor = null;
    		if (getAllPinnedSongs==true) {
    			Toast.makeText(mContext, R.string.wait_until_pinning_complete, Toast.LENGTH_SHORT).show();
    			return;
    		} else if (pinPlaylist==true) {
    			//Pinning from a playlist, so we'll need to use a db call that utilizes a JOIN.
    			intermCursor = mDBAccessHelper.getAllSongsInPlaylistSearchable(selection);
    		} else {
    			//Check if we're pinning a smart playlist.
    			if (selection.equals("TOP_25_PLAYED_SONGS")) {
    				intermCursor = mDBAccessHelper.getTop25PlayedTracks(selection);
    	        } else if (selection.equals("RECENTLY_ADDED")) {
    	        	intermCursor = mDBAccessHelper.getRecentlyAddedSongs(selection);
    	        } else if (selection.equals("TOP_RATED")) {
    	        	intermCursor = mDBAccessHelper.getTopRatedSongs(selection);
    	        } else if (selection.equals("RECENTLY_PLAYED")) {
    	        	intermCursor = mDBAccessHelper.getRecentlyPlayedSongs(selection);
    	        } else {
    	        	//Not playing from a smart playlist. Just use a regular db query that searches songs.
    	        	intermCursor = mDBAccessHelper.getAllSongsSearchable(selection);
    	        }

    		}

    		Cursor[] cursorArray = { mPinnedSongsCursor, intermCursor };
     		MergeCursor mergeCursor = new MergeCursor(cursorArray);
     		mPinnedSongsCursor = (Cursor) mergeCursor;
     		
    	}
    	
    }
    
    /**
     * Rebuilds the service cursor.
     * 
     * @param context The context to use for this operation.
     */
/*    public static void rebuildServiceCursor(Context context) {
		AsyncBuildServiceCursorTask task = new AsyncBuildServiceCursorTask(context, 
																		   Common.callingFragment, 
																		   Common.bundle, 
																		   Common.currentLibrary, 
																		   Common.audioFilePathsInFolder, 
																		   Common.songTitle, 
																		   Common.songArtist, 
																		   Common.songAlbum, 
																		   Common.songGenre, 
																		   Common.songAlbumArtist);
		task.execute();
		
    }*/
    
    /**
     * Converts dp unit to equivalent pixels, depending on device density. 
     * 
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public float convertDpToPixels(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    /**
     * Converts device specific pixels to density independent pixels.
     * 
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public float convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }
    
    /**
     * Returns the orientation of the device.
     */
    public int getOrientation() {
        if (getResources().getDisplayMetrics().widthPixels > 
           getResources().getDisplayMetrics().heightPixels) { 
            return ORIENTATION_LANDSCAPE;
        } else {
            return ORIENTATION_PORTRAIT;
        }     
        
    }

    //Resamples an input stream bitmap to avoid OOM errors.
    public Bitmap decodeSampledBitmapFromInputStream(InputStream is, int reqWidth, int reqHeight) {
        try {

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

    /**
     * Converts milliseconds to hh:mm:ss format.
     */
    public String convertMillisToMinsSecs(long milliseconds) {

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
    
    /*
     * Getter methods.
     */
    
    public boolean isGoogleAnalyticsEnabled() {
    	return mIsGAnalyticsEnabled;
    }
    
    public DBAccessHelper getDBAccessHelper() {
    	return DBAccessHelper.getInstance(mContext);
    }
    
    public SharedPreferences getSharedPreferences() {
    	return mSharedPreferences;
    }
    
	public GMusicClientCalls getGMusicClientCalls() {
		return mGMusicClientCalls;
	}

    public Picasso getPicasso() {
        return mPicasso;
    }
	
	public ImageManager getImageManager() {
		return mImageManager;
	}
	
	public boolean isBuildingLibrary() {
		return mIsBuildingLibrary;
	}
	
	public boolean isScanFinished() {
		return mIsScanFinished;
	}
	
	public boolean isGMusicLoggedIn() {
		return mIsGMusicLoggedIn;
	}
	
    public Cursor getPinnedSongsCursor() {
    	return mPinnedSongsCursor;
    }
    
    public boolean isFetchingPinnedSongs() {
    	return mIsFetchingPinnedSongs;
    }
    
    public AudioPlaybackService getService() {
    	return mService;
    }
    
    public NowPlayingActivity getNowPlayingActivity() {
    	return mNowPlayingActivity;
    }
    
    public DisplayImageOptions getDisplayImageOptions() {
    	return mDisplayImageOptions;
    }
    
    public ImageLoader getImageLoader() {
    	return mImageLoader;
    }

    public int getCurrentTheme() {
        return getSharedPreferences().getInt(CURRENT_THEME, DARK_THEME);
    }
    
    public boolean isServiceRunning() {
    	return mIsServiceRunning;
    }
    
    public boolean isEqualizerEnabled() {
    	return getSharedPreferences().getBoolean("EQUALIZER_ENABLED", true);
    }
    
    public boolean isCrossfadeEnabled() {
    	return getSharedPreferences().getBoolean("CROSSFADE_ENABLED", false);
    }
    
    public boolean isGooglePlayMusicEnabled() {
    	return getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false);
    }
    
    public String getCurrentLibrary() {
    	return getSharedPreferences().getString("CURRENT_LIBRARY", mContext.getResources()
    																	   .getString(R.string.all_libraries));
    }
	
    public String getCurrentLibraryNormalized() {
    	return getCurrentLibrary().replace("'", "''");
    }
    
    public PlaybackKickstarter getPlaybackKickstarter() {
    	return mPlaybackKickstarter;
    }
    
	/*
	 * Setter methods.
	 */
	
	public void setIsBuildingLibrary(boolean isBuildingLibrary) {
		mIsBuildingLibrary = isBuildingLibrary;
	}
	
	public void setIsScanFinished(boolean isScanFinished) {
		mIsScanFinished = isScanFinished;
	}
	
	public void setIsGMusicLoggedIn(boolean isGMusicLoggedIn) {
		mIsGMusicLoggedIn = isGMusicLoggedIn;
	}
	
	public void setService(AudioPlaybackService service) {
		mService = service;
	}
	
	public void setNowPlayingActivity(NowPlayingActivity activity) {
		mNowPlayingActivity = activity;
	}
	
	public void setIsServiceRunning(boolean running) {
		mIsServiceRunning = running;
	}
	
	public void setIsEqualizerEnabled(boolean isEnabled) {
		getSharedPreferences().edit().putBoolean("EQUALIZER_ENABLED", isEnabled).commit();
		
		//Reload the EQ settings.
		if (isServiceRunning()) {
			try {
				getService().getEqualizerHelper().releaseEQObjects();
				getService().initAudioFX();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}

	public void setGMusicClientCalls(GMusicClientCalls gMusicClientCalls) {
		this.mGMusicClientCalls = gMusicClientCalls;
	}
	
	public void setPinnedSongsCursor(Cursor cursor) {
		this.mPinnedSongsCursor = cursor;
	}
	
	public void setIsFetchingPinnedSongs(boolean fetching) {
		this.mIsFetchingPinnedSongs = fetching;
	}
	
	public void setIsGoogleAnalyticsEnabled(boolean enabled) {
		this.mIsGAnalyticsEnabled = enabled;
	}
	
}
