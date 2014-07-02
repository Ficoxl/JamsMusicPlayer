package com.jams.music.player.NowPlayingActivity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.jams.music.player.R;
import com.jams.music.player.AlbumArtistsFlippedActivity.AlbumArtistsFlippedActivity;
import com.jams.music.player.AlbumsFlippedActivity.AlbumsFlippedActivity;
import com.jams.music.player.Animations.TranslateAnimation;
import com.jams.music.player.ArtistsFlippedActivity.ArtistsFlippedActivity;
import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.FoldersFragment.FileExtensionFilter;
import com.jams.music.player.GenresFlippedActivity.GenresFlippedActivity;
import com.jams.music.player.Helpers.SongHelper;
import com.jams.music.player.Helpers.SongHelper.AlbumArtLoadedListener;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.Utils.Common;
import com.nostra13.universalimageloader.core.assist.ImageSize;

public class PlaylistPagerFragment extends Fragment implements AlbumArtLoadedListener {

	private Context mContext;
	private Common mApp;
	private ViewGroup mRootView;
	private int mPosition;
	private SongHelper mSongHelper;
	private PopupMenu popup;
	
	private TextView songNameTextView;
	private TextView artistAlbumNameTextView;
	private RelativeLayout songInfoLayout;
	private ImageView coverArt;
	private ImageView overflowIcon;
	
	private boolean mAreLyricsVisible = false;
	private ScrollView mLyricsScrollView;
	private TextView mLyricsTextView;
	private TextView mLyricsEmptyTextView;
	
	private Bitmap bitmapWithReflection;
	private Bitmap reflectionImage;
	private Bitmap bm;

	private Matrix matrix;
	private Canvas canvas;
	private Paint paint;
	private LinearGradient shader;
	private BroadcastReceiver receiver;
	
	private int screenWidth;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        mContext = getActivity();
        mApp = (Common) mContext.getApplicationContext();
        
    	//Inflate the correct layout based on the user's selected Cover Art style.
    	if (mApp.getSharedPreferences().getString("COVER_ART_STYLE", "FILL_SCREEN").equals("CARD_STYLE")) {
    		mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_playlist_pager_card, container, false);
    	} else {
    		mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_playlist_pager_fill, container, false);
    	}
        
        mPosition = getArguments().getInt("POSITION");

        overflowIcon = (ImageView) mRootView.findViewById(R.id.now_playing_overflow_icon);
    	coverArt = (ImageView) mRootView.findViewById(R.id.coverArt);
        songNameTextView = (TextView) mRootView.findViewById(R.id.songName);
    	artistAlbumNameTextView = (TextView) mRootView.findViewById(R.id.artistAlbumName);
    	songInfoLayout = (RelativeLayout) mRootView.findViewById(R.id.song_artist_album_layout);
    	
    	mLyricsScrollView = (ScrollView) mRootView.findViewById(R.id.lyrics_scroll_view);
    	mLyricsTextView = (TextView) mRootView.findViewById(R.id.lyrics);
    	mLyricsEmptyTextView = (TextView) mRootView.findViewById(R.id.lyrics_empty);
        
    	mLyricsTextView.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Light"));
    	mLyricsTextView.setPaintFlags(mLyricsTextView.getPaintFlags() |
    								  Paint.ANTI_ALIAS_FLAG |
    								  Paint.SUBPIXEL_TEXT_FLAG);
    	
    	mLyricsEmptyTextView.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Light"));
    	mLyricsEmptyTextView.setPaintFlags(mLyricsEmptyTextView.getPaintFlags() |
    								  	   Paint.ANTI_ALIAS_FLAG |
    								  	   Paint.SUBPIXEL_TEXT_FLAG);
    	
    	songNameTextView.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Light"));
    	songNameTextView.setPaintFlags(songNameTextView.getPaintFlags() |
    								   Paint.ANTI_ALIAS_FLAG |
    								   Paint.SUBPIXEL_TEXT_FLAG);
    	
    	artistAlbumNameTextView.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Regular"));
    	artistAlbumNameTextView.setPaintFlags(artistAlbumNameTextView.getPaintFlags() |
    								   		  Paint.ANTI_ALIAS_FLAG |
    								   		  Paint.SUBPIXEL_TEXT_FLAG);
    	
    	//Initialize the pop up menu.
    	popup = new PopupMenu(getActivity(), overflowIcon);
		popup.getMenuInflater().inflate(R.menu.now_playing_overflow_menu, popup.getMenu());
    	
    	//Get the screen's parameters.
	    DisplayMetrics displaymetrics = new DisplayMetrics();
	    getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
	    screenWidth = displaymetrics.widthPixels;
        
    	//Set the layout's background based on the selected color scheme.
    	songInfoLayout.setBackgroundResource(UIElementsHelper.getNowPlayingInfoBackground(mContext));
    	
    	songNameTextView.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
    	artistAlbumNameTextView.setTextColor(UIElementsHelper.getSmallTextColor(mContext));
		
        mSongHelper = new SongHelper();
        mSongHelper.populateSongData(mContext, mPosition);
        mSongHelper.setAlbumArtLoadedListener(this);
        
		overflowIcon.setImageResource(UIElementsHelper.getIcon(mContext, "ic_action_overflow"));
    	initOverflowMenu(mSongHelper.getSavedPosition(),
				  		 mSongHelper.getId(),
				  		 mSongHelper.getArtist(),
				  		 mSongHelper.getAlbum(),
				  		 mSongHelper.getSource(),
				  		 mSongHelper.getAlbumArtist(),
				  		 mSongHelper.getGenre());
		
    	songNameTextView.setText(mSongHelper.getTitle());
    	artistAlbumNameTextView.setText(mSongHelper.getAlbum() + " - " + mSongHelper.getArtist());
    	
        return mRootView;
    }
    
    private void initOverflowMenu(final long finalLastPlaybackPosition,
    							  final String songId,
    							  final String finalSongArtist,
    							  final String finalSongAlbum,
    							  final String finalSongSource,
    							  final String finalSongAlbumArtist,
    							  final String finalSongGenre) {
    	
    	overflowIcon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				final long finalLastPlaybackPosition = mApp.getService().getCurrentSong().getSavedPosition();
				if (finalLastPlaybackPosition==-1) {
					popup.getMenu().findItem(R.id.save_clear_current_position).setTitle(R.string.save_current_position);
				} else {
					popup.getMenu().findItem(R.id.save_clear_current_position).setTitle(R.string.clear_saved_position);
				}

	            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {  
	            	
	            	@Override
	            	public boolean onMenuItemClick(MenuItem item) {  
	            		
	            		switch(item.getItemId()) {
	            		case R.id.save_clear_current_position:
	            			if (finalLastPlaybackPosition==-1) {
	            				//Save the current track position.
		            			try {
		            				long currentPlaybackPosition = mApp.getService().getCurrentMediaPlayer().getCurrentPosition();
		            				mApp.getDBAccessHelper().setLastPlaybackPosition(songId, currentPlaybackPosition);
		            				String confirmationToast = getActivity().getResources().getString(R.string.track_will_resume_from)
		            										 + " " + convertMillisToMinsSecs(currentPlaybackPosition) + " "
		            										 + getActivity().getResources().getString(R.string.next_time_you_play_it);
		            				
		            				//Rebuild the cursor to reflect the new changes.
		            				/**************************************************************************************
		            				 * Common.rebuildServiceCursor(getActivity().getApplicationContext()); *
		            				 **************************************************************************************/
		            				
		            				Toast.makeText(mContext, confirmationToast, Toast.LENGTH_LONG).show();
		            			} catch (Exception e) {
		            				e.printStackTrace();
		            				Toast.makeText(mContext, R.string.unable_to_save_playback_position, Toast.LENGTH_LONG).show();
		            			}
		            			
		            			//Update the menu.
		            			popup.getMenu().findItem(R.id.save_clear_current_position).setTitle(R.string.clear_saved_position);
		            			
	            			} else {
	            				//Reset the saved track position.
		            			try {
		            				DBAccessHelper dbHelper = new DBAccessHelper(mContext);
			            			dbHelper.setLastPlaybackPosition(songId, -1);
			            			if (dbHelper!=null) {
			            				dbHelper.close();
			            				dbHelper = null;
			            			}
			            			
			            			Toast.makeText(mContext, R.string.track_start_from_beginning_next_time_play, Toast.LENGTH_LONG).show();
			            			
		            				//Rebuild the cursor to reflect the new changes.
		            				/**************************************************************************************
		            				 * Common.rebuildServiceCursor(getActivity().getApplicationContext()); *
		            				 **************************************************************************************/
		            				
			            	    	initOverflowMenu(finalLastPlaybackPosition,
			   					  		 			 songId,
			   					  		 			 finalSongArtist,
			   					  		 			 finalSongAlbum,
			   					  		 			 finalSongSource,
			   					  		 			 finalSongAlbumArtist,
			   					  		 			 finalSongGenre);
			            	    	
		            			} catch (Exception e) {
		            				e.printStackTrace();
		            			}
		            			
		            			//Update the menu.
		            			popup.getMenu().findItem(R.id.save_clear_current_position).setTitle(R.string.save_current_position);
		            			
	            			}
	            			break;
	            		case R.id.show_embedded_lyrics:
	            			if (mAreLyricsVisible==true) {
	            				TranslateAnimation slideDownAnimation = new TranslateAnimation(coverArt, 400, new AccelerateInterpolator(), 
										 													   View.VISIBLE, 
										 													   Animation.RELATIVE_TO_SELF, 0.0f, 
										 													   Animation.RELATIVE_TO_SELF, 0.0f, 
										 													   Animation.RELATIVE_TO_SELF, -2.0f, 
										 													   Animation.RELATIVE_TO_SELF, 0.0f);

	            				slideDownAnimation.animate();
	            				popup.getMenu().findItem(R.id.show_embedded_lyrics).setTitle(R.string.show_embedded_lyrics);
	            				mAreLyricsVisible = false;
	            			} else {
		            			AsyncLoadLyricsTask task = new AsyncLoadLyricsTask();
		            			task.execute();
		            			popup.getMenu().findItem(R.id.show_embedded_lyrics).setTitle(R.string.hide_lyrics);
		            			mAreLyricsVisible = true;
	            			}

	            			break;
	            		case R.id.go_to:
	            			
	            			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
	            			builder.setTitle(R.string.go_to);
	            			builder.setSingleChoiceItems(R.array.show_more_menu, -1, new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									
				            		Intent intent = null;
									switch (which) {
									case 0:
				            			intent = new Intent(getActivity(), ArtistsFlippedActivity.class);
				            			intent.putExtra("ARTIST_NAME", finalSongArtist);
				            			intent.putExtra("HEADER_IMAGE_PATH", mSongHelper.getAlbumArtPath());
				            			intent.putExtra("ART_SOURCE", finalSongSource);
				            			getActivity().startActivity(intent);
				            			getActivity().finish();
				            			break;
				            		case 1:
				            			intent = new Intent(getActivity(), AlbumArtistsFlippedActivity.class);
				            			intent.putExtra("ALBUM_ARTIST_NAME", finalSongAlbumArtist);
				            			intent.putExtra("HEADER_IMAGE_PATH", mSongHelper.getAlbumArtPath());
				            			intent.putExtra("ART_SOURCE", finalSongSource);
				            			getActivity().startActivity(intent);
				            			getActivity().finish();
				            			break;
				            		case 2:
				            			intent = new Intent(getActivity(), AlbumsFlippedActivity.class);
				            			intent.putExtra("ARTIST_NAME", finalSongArtist);
				            			intent.putExtra("ALBUM_NAME", finalSongAlbum);
				            			intent.putExtra("HEADER_IMAGE_PATH", mSongHelper.getAlbumArtPath());
				            			intent.putExtra("ART_SOURCE", finalSongSource);
				            			getActivity().startActivity(intent);
				            			getActivity().finish();
				            			break;
				            		case 3:
				            			intent = new Intent(getActivity(), GenresFlippedActivity.class);
				            			intent.putExtra("GENRE_NAME", finalSongGenre);
				            			intent.putExtra("ALBUM_ART_PATH", mSongHelper.getAlbumArtPath());
				            			intent.putExtra("ART_SOURCE", finalSongSource);
				            			getActivity().startActivity(intent);
				            			getActivity().finish();
				            			break;
										
									}
									
									dialog.dismiss();
									
								}
	            				
	            			});
	            			
	            			builder.create().show();
	            			break;
	            		}

	            		return true;  
	            	}  
	            	
	            });  
	  
	            popup.show();
			}
    		
    	});
    	
    }
    
    /**
     * In order to save as much memory as possible, we'll be loading trivial bitmap data 
     * (such as height and width) into temporary variables and nullifying the original 
     * bitmaps along the way. 
     */
    public void createReflectedImage(Context context) {
    	
    	//If anything goes wrong during the decoding process, we'll just return an empty bitmap.
    	try {
    		//We don't need space between the actual bitmap and the reflected image.
            final int reflectionGap = 0;
            
    	    int width = bm.getWidth();
    	    int height = bm.getHeight();
    	
    	    //Flip the bitmap on the Y axis.
    	    matrix = new Matrix();
    	    matrix.preScale(1, -1);
    	     
    	    //Create a new bitmap using the flipped matrix. We only need half of the original bitmap for the reflection.
    	    reflectionImage = Bitmap.createBitmap(bm, 0, height/2, width, height/2, matrix, false);
    	     
    	    //Create a new bitmap that will encompass the original bitmap and it's reflection.
    	    bitmapWithReflection = Bitmap.createBitmap(width, (height + height/2), Config.ARGB_8888);
            
            //Create a new cavas that will encompass the new bitmap we just created above.
            canvas = new Canvas(bitmapWithReflection);
            //Draw in the original image.
            canvas.drawBitmap(bm, 0, 0, null);
            
            //After this point, we only need the height attribute of originalImage.
            int originalImageHeight = bm.getHeight();
            bm = null;
            
            //Draw in the gap.
            Paint defaultPaint = new Paint();
            canvas.drawRect(0, height, width, height + reflectionGap, defaultPaint);
            //Draw in the reflection.
            canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);
             
            //We're done with reflectionImage, so go ahead and nullify it.
            reflectionImage = null;
            
            //Create a shader that is a linear gradient that covers the reflection
            paint = new Paint();
            shader = new LinearGradient(0, originalImageHeight, 0,
            								bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff, 0x00ffffff,
            								TileMode.CLAMP);
            //Set the paint to use this shader (linear gradient)
            paint.setShader(shader);
            //Set the Transfer mode to be porter duff and destination in.
            paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
            
            //Draw a rectangle using the paint with the linear gradient.
            canvas.drawRect(0, height, width, bitmapWithReflection.getHeight() + reflectionGap, paint);
            
    	} catch (Exception e) {
    		bitmapWithReflection = null;
    		return;
    	}

    }
    
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
    
    //Resamples a resource image to avoid OOM errors.
    public Bitmap decodeSampledBitmapFromResource(int resID, int reqWidth, int reqHeight) {

	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
	    options.inJustDecodeBounds = false;
	    options.inPurgeable = true;
	
	    return BitmapFactory.decodeResource(mContext.getResources(), resID, options);
    }
    
	//Convert millisseconds to hh:mm:ss format.
    public static String convertMillisToMinsSecs(long milliseconds) {
    	
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
    public void onPause() {
    	super.onPause();
    	
    	bitmapWithReflection = null;
    	reflectionImage = null;
    	bm = null;
    	
    	matrix = null;
    	canvas = null;
    	paint = null;
    	shader = null;
    	
    }
	
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	bitmapWithReflection = null;
    	reflectionImage = null;
    	bm = null;
    	
    	matrix = null;
    	canvas = null;
    	paint = null;
    	shader = null;
    	
    }
    
    @Override
    public void onDestroyView() {
    	super.onDestroyView();
    	
    	mRootView = null;
		bm = null;
		bitmapWithReflection = null;
		reflectionImage = null;
    }
    
	@Override
	public void onStart() {
	    super.onStart();
	    LocalBroadcastManager.getInstance(mContext)
	    					 .registerReceiver(receiver, new IntentFilter("com.jams.music.player.PAGER_RECEIVER"));
	
	}

	@Override
	public void onStop() {
	    LocalBroadcastManager.getInstance(mContext).unregisterReceiver(receiver);
	    super.onStop();
	    
	}
	
	public class AsyncPlaylistPagerFragmentTask extends AsyncTask<String, Integer, Boolean> {
	    private Context mContext;
	    String songTitle;
	    String songArtist;
	    String songAlbum;
	    String artistAlbum;
	    boolean mCancelled = false;
	    
	    public AsyncPlaylistPagerFragmentTask(Context context) {
	    	mContext = context;
	    	
	    }
	 
	    @Override
	    protected Boolean doInBackground(String... params) {
	    	
	    	/* We're gonna have to surround this entire method in a try/catch block because we can't
	    	 * detect when the user closes the activity and potentially recycles the bitmaps used in
	    	 * this method. Accessing these recycled bitmaps will cause the app to crash.
	    	 */
	    	ImageSize imageSize = new ImageSize((int) ((2)*(screenWidth/3)), (int) ((2)*(screenWidth/3)));
	    	if (mCancelled) {
	    		return false;
	    	}
	    	
	    	if (mSongHelper.getAlbumArtPath()==null || mSongHelper.getAlbumArtPath().equals("") || mSongHelper.getAlbumArtPath().equals(" ")) {
    	    	bm = decodeSampledBitmapFromResource(R.drawable.app_icon_xlarge, (int) screenWidth/2, (int) screenWidth/2);
    	    } else {
    	    	bm = mApp.getImageLoader().loadImageSync(mSongHelper.getAlbumArtPath(), imageSize, mApp.getDisplayImageOptions());
    	    }
	    	    
    	    if (bm==null) {
    	    	bm = decodeSampledBitmapFromResource(R.drawable.app_icon_xlarge, (int) screenWidth/2, (int) screenWidth/2);
    	    }
 
    	    if (mApp.getSharedPreferences().getString("COVER_ART_STYLE", "FILL_SCREEN").equals("CARD_STYLE")) {
				createReflectedImage(mContext);
				publishProgress(new Integer[] {1});
			} else {
				publishProgress(new Integer[] {2});
			}
			
			//Fade in the album art image.
			publishProgress(new Integer[] {0});

	    	return true;
	    }
	    
	    public String getEmbeddedArtwork(String filePath) {
			File file = new File(filePath);
			if (!file.exists()) {
				return getArtworkFromFolder(filePath);
			} else {
	        	MediaMetadataRetriever mmdr = new MediaMetadataRetriever();
	        	mmdr.setDataSource(filePath);
	        	byte[] embeddedArt = mmdr.getEmbeddedPicture();
	        	
	        	if (embeddedArt!=null) {
	        		return "byte://" + filePath;
	        	} else {
	    			return getArtworkFromFolder(filePath);
	        	}
	        	
			}
			
		}
	    
	    public String getArtworkFromFolder(String filePath) {
			
			File file = new File(filePath);
			if (!file.exists()) {
				return "";	
			} else {
				//Create a File that points to the parent directory of the album.
				File directoryFile = file.getParentFile();
				
				//Get a list of images in the album's folder.
				FileExtensionFilter IMAGES_FILTER = new FileExtensionFilter(new String[] {".jpg", ".jpeg", 
																						  ".png", ".gif"});
				File[] folderList = directoryFile.listFiles(IMAGES_FILTER);
				
				//Check if any image files were found in the folder.
				if (folderList.length==0) {
					//No images found.
					return "";
				} else {
					
					//Loop through the list of image files. Use the first jpeg file if it's found.
					for (int i=0; i < folderList.length; i++) {
						
						try {
							if (folderList[i].getCanonicalPath().contains("jpg") ||
								folderList[i].getCanonicalPath().contains("jpeg")) {
								return folderList[i].getCanonicalPath();
							}
							
						} catch (Exception e) {
							//Skip the file if it's corrupted or unreadable.
							continue;
						}
						
					}
					
					//If an image was not found, check for gif or png files (lower priority).
					for (int i=0; i < folderList.length; i++) {
	    				
	    				try {
							if (folderList[i].getCanonicalPath().contains("png") ||
								folderList[i].getCanonicalPath().contains("gif")) {
								return folderList[i].getCanonicalPath();
							}
							
						} catch (Exception e) {
							//Skip the file if it's corrupted or unreadable.
							continue;
						}
	    				
	    			}
					
					return "";
				}
	    		
	    	}
			
		}
	    
	    @Override
	    protected void onCancelled() {
	    	super.onCancelled();
	    	mCancelled = true;
	    }
	    
	    @Override
	    protected void onProgressUpdate(Integer... params) {
	    	super.onProgressUpdate(params);
	    	
	    	//Perform the requested action on the UI thread.
	    	switch(params[0]) {
	    	case 0:
	    		coverArt.setVisibility(View.VISIBLE);
	    		break;
	    	case 1:
	    		coverArt.setImageBitmap(bitmapWithReflection);
	    		break;
	    	case 2:
	    		coverArt.setImageBitmap(bm);
	    		break;
	    	case 3:
				coverArt.setScaleX(0.5f);
				coverArt.setScaleY(0.5f);
				coverArt.setScaleType(ScaleType.FIT_CENTER);
	    		break;
	    	case 4:
				coverArt.setScaleX(1.0f);
				coverArt.setScaleY(1.0f);
				coverArt.setScaleType(ScaleType.FIT_CENTER);
	    		break;
	    	}
	    	
	    }

	    @Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			
		}

	}

	@Override
	public void albumArtLoaded() {
		coverArt.setImageBitmap(mSongHelper.getAlbumArt());
		
	}
	
	class AsyncLoadLyricsTask extends AsyncTask<Boolean, Boolean, Boolean> {

		String mLyrics = "";
		
		@Override
		protected Boolean doInBackground(Boolean... arg0) {
			String songFilePath = mApp.getService().getCurrentSong().getFilePath();
			AudioFile audioFile = null;
			Tag tag = null;
			try {
				audioFile = AudioFileIO.read(new File(songFilePath));
				
				if (audioFile!=null)
					tag = audioFile.getTag();
				else
					return false;
				
				if (tag!=null)
					mLyrics = tag.getFirst(FieldKey.LYRICS);
				else
					return false;
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return true;
		}
		
		@Override
		public void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			
			if (mLyrics!=null && !mLyrics.isEmpty()) {
				mLyricsTextView.setText(mLyrics);
				mLyricsTextView.setVisibility(View.VISIBLE);
				mLyricsEmptyTextView.setVisibility(View.INVISIBLE);
			} else {
				mLyrics = mContext.getResources().getString(R.string.no_embedded_lyrics_found);
				mLyricsTextView.setText(mLyrics);
				mLyricsTextView.setVisibility(View.INVISIBLE);
				mLyricsEmptyTextView.setVisibility(View.VISIBLE);
			}
			
			//Slide up the album art to show the lyrics.
	    	TranslateAnimation slideUpAnimation = new TranslateAnimation(coverArt, 400, new AccelerateInterpolator(), 
					 													 View.INVISIBLE, 
					 													 Animation.RELATIVE_TO_SELF, 0.0f, 
					 													 Animation.RELATIVE_TO_SELF, 0.0f, 
					 													 Animation.RELATIVE_TO_SELF, 0.0f, 
					 													 Animation.RELATIVE_TO_SELF, -2.0f);

	    	slideUpAnimation.animate();
	    	
		}
		
	}
	
}
