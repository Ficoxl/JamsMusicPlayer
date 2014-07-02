package com.jams.music.player.AsyncTasks;

import java.io.File;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.jams.music.player.R;
import com.jams.music.player.FoldersFragment.FileExtensionFilter;
import com.jams.music.player.LauncherActivity.LauncherActivity;
import com.jams.music.player.MainActivity.MainActivity;
import com.jams.music.player.Services.BuildMusicLibraryService;
import com.jams.music.player.Utils.Common;

public class AsyncCacheLocalArtworkTask extends AsyncTask<String, String, String>{
	
	private Context mContext;
	private Common mApp;
	private WakeLock wakeLock;
	private PowerManager pm;
	private String currentTask = "";
	private int currentProgressValue = 0;
	
	public AsyncCacheLocalArtworkTask(Context context) {
		mContext = context;
		mApp = (Common) mContext;
		
	}
	
    @Override
    protected void onPreExecute() {
    	super.onPreExecute();
    	
    	mApp.setIsBuildingLibrary(true);
    	try {
    		LauncherActivity.buildingLibraryMainText.setText(R.string.jams_is_caching_artwork);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	//Acquire a wakelock to prevent the CPU from sleeping while the process is running.
    	pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
    	wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.jams.music.player.AsyncTasks.AsyncCacheLocalArtworkTask");
    	wakeLock.acquire();
    	
    	//Set the initial setting of the progressbar as indeterminate.
    	currentTask = "";
	
    }
	
	//@param preferThisSource specifies whether the user prefers this source for artwork.
	@Override
    protected String doInBackground(String... params) {
		
        
		
        return "";
    }
	
	//Retrieve artwork from folder.
	public String getArtworkFromFolder(String filePath) {
		
		File file = new File(filePath);
		if (!file.exists()) {
			if (mApp.getSharedPreferences().getInt("ALBUM_ART_SOURCE", 0)==1) {
				return getEmbeddedArtwork(filePath);
			} else {
				return "";
			}
			
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
				if (mApp.getSharedPreferences().getInt("ALBUM_ART_SOURCE", 0)==1) {
					return getEmbeddedArtwork(filePath);
				} else {
					return "";
				}
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
						//Skip the file if it's corrupted or unreadable.isScanFinished
						continue;
					}
    				
    			}
				
				if (mApp.getSharedPreferences().getInt("ALBUM_ART_SOURCE", 0)==1) {
					return getEmbeddedArtwork(filePath);
				} else {
					return "";
				}
				
			}
    		
    	}
		
	}
	
	//Hunt for embedded artwork. 
	public String getEmbeddedArtwork(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) {
			if (mApp.getSharedPreferences().getInt("ALBUM_ART_SOURCE", 0)==0) {
				return getArtworkFromFolder(filePath);
			} else {
				return "";
			}
			
		} else {
        	MediaMetadataRetriever mmdr = new MediaMetadataRetriever();
        	mmdr.setDataSource(filePath);
        	byte[] embeddedArt = mmdr.getEmbeddedPicture();
        	
        	if (embeddedArt!=null) {
        		return "byte://" + filePath;
        	} else {
    			if (mApp.getSharedPreferences().getInt("ALBUM_ART_SOURCE", 0)==0) {
    				return getArtworkFromFolder(filePath);
    			} else {
    				return "";
    			}
    			
        	}
        	
		}
		
	}
	
    @Override
    protected void onProgressUpdate(String... progressParams) {
    	super.onProgressUpdate(progressParams);
    	
    	if (progressParams.length > 0) {
    		//We're done caching artwork, so it's time to make the notification cancelable.
    		BuildMusicLibraryService.mBuilder.setTicker(mContext.getResources().getString(R.string.done_building_music_library));
    		BuildMusicLibraryService.mBuilder.setContentTitle(mContext.getResources().getString(R.string.done_building_music_library));
    		BuildMusicLibraryService.mBuilder.setContentText(null);
    		BuildMusicLibraryService.mBuilder.setContentInfo(null);
    		BuildMusicLibraryService.mBuilder.setProgress(0, 0, false);
    		
    		PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, new Intent(mContext, MainActivity.class)
    																				 .setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT), 
    																				 0);
    		BuildMusicLibraryService.mBuilder.setContentIntent(pendingIntent);
    		BuildMusicLibraryService.mNotification = BuildMusicLibraryService.mBuilder.build();
    		BuildMusicLibraryService.mNotification.flags = Notification.FLAG_AUTO_CANCEL;
    		BuildMusicLibraryService.mNotifyManager.notify(BuildMusicLibraryService.mNotificationId, 
    													   BuildMusicLibraryService.mNotification);
    	} else {
    		//Update the notification with the task's progress.
        	BuildMusicLibraryService.mBuilder.setTicker(null);
        	BuildMusicLibraryService.mBuilder.setContentTitle(mContext.getResources().getString(R.string.scanning_for_album_art));
        	BuildMusicLibraryService.mBuilder.setContentText(currentTask);
        	BuildMusicLibraryService.mBuilder.setContentInfo(null);
        	BuildMusicLibraryService.mBuilder.setProgress(1000000, currentProgressValue, false);
        	BuildMusicLibraryService.mNotification = BuildMusicLibraryService.mBuilder.build();
        	BuildMusicLibraryService.mNotifyManager.notify(BuildMusicLibraryService.mNotificationId, 
        												   BuildMusicLibraryService.mNotification);
    	}
    	
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        
        mApp.getSharedPreferences().edit().putBoolean("DONE_BUILDING_LIBRARY", true).commit();
        
        //If a rescan was required, reset the flag to remove the footer.
        mApp.getSharedPreferences().edit().putBoolean("RESCAN_REQUIRED", false).commit();
        
        //Set the flag to notify the app that the first run scan has been completed.
        mApp.getSharedPreferences().edit().putBoolean("FIRST_RUN", false).commit();
        mApp.getSharedPreferences().edit().putBoolean("BUILDING_LIBRARY", false).commit();
        
        //Stop the scanning service.
        mContext.stopService(new Intent(mContext, BuildMusicLibraryService.class));
        mApp.setIsScanFinished(true);
        
        //Release the wakelock.
    	wakeLock.release();
        mContext = null;
        mApp.setIsBuildingLibrary(false);
        
    }
 
}
