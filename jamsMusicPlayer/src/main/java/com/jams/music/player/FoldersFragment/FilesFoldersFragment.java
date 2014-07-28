package com.jams.music.player.FoldersFragment;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.NameFileComparator;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andraskindler.quickscroll.QuickScroll;
import com.jams.music.player.Helpers.PauseOnScrollHelper;
import com.jams.music.player.ListViewFragment.ListViewCardsAdapter;
import com.jams.music.player.R;
import com.jams.music.player.AsyncTasks.AsyncDeleteTask;
import com.jams.music.player.AsyncTasks.AsyncMoveTask;
import com.jams.music.player.AsyncTasks.AsyncPasteTask;
import com.jams.music.player.AsyncTasks.AsyncPlayFolderRecursiveTask;
import com.jams.music.player.Dialogs.InvalidFileDialog;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.NowPlayingActivity.NowPlayingActivity;
import com.jams.music.player.Utils.Common;

/**
 * FilesFoldersFragment. Contained within MainActivity.
 * 
 * @author Saravan Pantham
 */
public class FilesFoldersFragment extends Fragment {

	//Context.
	private Context mContext;
	private FilesFoldersFragment mFilesFoldersFragment;
	private Common mApp;
	
	//UI Elements.
	private ListView listView;
	private View foldersViewLayout;
	private TextView landscapeFolderName;
	private TextView landscapeFreeSpaceText;
	private TextView landscapeFreeSpaceValue;
	private TextView landscapeItemsText;
	private TextView landscapeItemsValue;
	private TextView landscapeLastModifiedText;
	private TextView landscapeLastModifiedValue;
	
	//Folder parameter ArrayLists.
	private String rootDir;
	public static String currentDir;
	private List<String> fileFolderNameList = null; 
	private List<String> fileFolderPathList = null;
	private List<String> fileFolderSizeList = null;
	private List<Integer> fileFolderTypeList = null;
	
	//File size/unit dividers
	private final long kiloBytes = 1024;
	private final long megaBytes = kiloBytes * kiloBytes;
	private final long gigaBytes = megaBytes * kiloBytes;
	private final long teraBytes = gigaBytes * kiloBytes;
	
	//Temp variable that stores the file/folder path of the long-pressed item.
	private String contextMenuItemPath;
	
	//List of audio file paths within a specific folder.
	private ArrayList<String> audioFilePathsInFolder = new ArrayList<String>();
	
	//List of subdirectories within a directory (Used by "Play Folder Recursively").
	private ArrayList<String> subdirectoriesList = new ArrayList<String>();
	
	//Flag that determines whether hidden files are displayed or not.
	private boolean SHOW_HIDDEN_FILES = false;
	
	//Temp variables that store the source file during a copy/move operation.
	private File sourceFile = null;
	private File targetFile = null;
	
	//Flags that indicate that a user has selected a file/folder to copy/move but hasn't actually copied/moved it yet.
	public static boolean COPY_OPERATION_PENDING = false;
	public static boolean MOVE_OPERATION_PENDING = false;
	
	//Temp file that stores the files that needs to be moved.
	private File moveSourceFile = null;
	
	//Handler and flag to update the folder hierarchy once a CRUD operation is finished.
	private Handler mHandler = new Handler();
	public static boolean REFRESH_REQUIRED = false;
	private String currentFolderPath;

    public static final int FOLDER = 0;
    public static final int FILE = 1;
    public static final int AUDIO_FILE = 3;
    public static final int PICTURE_FILE = 4;
    public static final int VIDEO_FILE = 5;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_folders, container, false);
        mContext = getActivity().getApplicationContext();
        mFilesFoldersFragment = this;
        mApp = (Common) mContext;
        
        //Set the hidden files flag.
        SHOW_HIDDEN_FILES = mApp.getSharedPreferences().getBoolean("SHOW_HIDDEN_FILES", false);

        listView = (ListView) rootView.findViewById(R.id.folders_list_view);
        listView.setFastScrollEnabled(true);
        listView.setVisibility(View.INVISIBLE);
        
		//Set the background color based on the theme.
        rootView.setBackgroundColor(UIElementsHelper.getBackgroundColor(mContext));

        //Apply the ListView params.
        //Apply the ListViews' dividers.
        if (mApp.getCurrentTheme()==Common.DARK_THEME) {
            listView.setDivider(mContext.getResources().getDrawable(R.drawable.icon_list_divider));
        } else {
            listView.setDivider(mContext.getResources().getDrawable(R.drawable.icon_list_divider_light));
        }

        listView.setDividerHeight(1);

        //Initialize landscape view's side pane.
		if (mApp.getOrientation()==Common.ORIENTATION_LANDSCAPE) {
			try {
				landscapeFolderName = (TextView) rootView.findViewById(R.id.landscape_folder_name);
				landscapeFreeSpaceText = (TextView) rootView.findViewById(R.id.landscape_size_text);
				landscapeFreeSpaceValue = (TextView) rootView.findViewById(R.id.landscape_size_value);
				landscapeItemsText = (TextView) rootView.findViewById(R.id.landscape_items_text);
				landscapeItemsValue = (TextView) rootView.findViewById(R.id.landscape_items_value);
				landscapeLastModifiedText = (TextView) rootView.findViewById(R.id.landscape_last_modified_text);
				landscapeLastModifiedValue = (TextView) rootView.findViewById(R.id.landscape_last_modified_value);
				
				landscapeFolderName.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
				landscapeFolderName.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));
				
				landscapeFreeSpaceText.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
				landscapeFreeSpaceText.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));
				
				landscapeFreeSpaceValue.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
				landscapeFreeSpaceValue.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));
				
				landscapeItemsText.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
				landscapeItemsText.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));
				
				landscapeItemsValue.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
				landscapeItemsValue.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));
				
				landscapeLastModifiedText.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
				landscapeLastModifiedText.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));
				
				landscapeLastModifiedValue.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
				landscapeLastModifiedValue.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));
				
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		
		if (mApp.getOrientation()==Common.ORIENTATION_LANDSCAPE) {
			foldersViewLayout = (LinearLayout) rootView.findViewById(R.id.folders_view_layout);
		}
		
		//KitKat translucent navigation/status bar.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        	int topPadding = Common.getStatusBarHeight(mContext);

            //Calculate navigation bar height.
            int navigationBarHeight = 0;
            int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                navigationBarHeight = getResources().getDimensionPixelSize(resourceId);
            }
            
            if (rootView!=null) {
            	rootView.setPadding(0, topPadding, 0, 0);
            }
            
            listView.setClipToPadding(false);
            listView.setPadding(0, 0, 0, navigationBarHeight);
        }

        rootDir = mApp.getSharedPreferences().getString("DEFAULT_FOLDER", "/");
        currentDir = rootDir;
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                slideUpListView();
            }

        }, 250);
        return rootView;
    }

    /**
     * Slides in the ListView.
     */
    private void slideUpListView() {

        getDir(rootDir);

        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                                                              Animation.RELATIVE_TO_SELF, 0.0f,
                                                              Animation.RELATIVE_TO_SELF, 2.0f,
                                                              Animation.RELATIVE_TO_SELF, 0.0f);

        animation.setDuration(600);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationEnd(Animation arg0) {

            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationStart(Animation arg0) {
               listView.setVisibility(View.VISIBLE);

            }

        });

        listView.startAnimation(animation);
    }
    
    /**
     * Retrieves the folder hierarchy for the specified folder.
     */
    private void getDir(String dirPath) {
    	
		getActivity().invalidateOptionsMenu();
		fileFolderNameList = new ArrayList<String>();
		fileFolderPathList = new ArrayList<String>();
		fileFolderSizeList = new ArrayList<String>();
		fileFolderTypeList = new ArrayList<Integer>();
		
		File f = new File(dirPath);
		File[] files = f.listFiles();
		 
		if (files!=null) {
			
			//Sort the files by name.
			Arrays.sort(files, NameFileComparator.NAME_INSENSITIVE_COMPARATOR);
			
			for(int i=0; i < files.length; i++) {
				
				File file = files[i];
				if(file.isHidden()==SHOW_HIDDEN_FILES && file.canRead()) {
					
					if (file.isDirectory()) {

                        /*
						 * Starting with Android 4.2, /storage/emulated/legacy/...
						 * is a symlink that points to the actual directory where
						 * the user's files are stored. We need to detect the
						 * actual directory's file path here.
						 */
                        String filePath;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                            filePath = getRealFilePath(file.getAbsolutePath());
                        else
                            filePath = file.getAbsolutePath();

                        fileFolderPathList.add(filePath);
						fileFolderNameList.add(file.getName());
						File[] listOfFiles = file.listFiles();
						
						if (listOfFiles!=null) {
							fileFolderTypeList.add(FOLDER);
							if (listOfFiles.length==1) {
								fileFolderSizeList.add("" + listOfFiles.length + " item");
							} else {
								fileFolderSizeList.add("" + listOfFiles.length + " items");
							}
							
						} else {
							fileFolderTypeList.add(FOLDER);
							fileFolderSizeList.add("Unknown items");
						}
						
					} else {
						
						try {
							String path = file.getCanonicalPath();
							fileFolderPathList.add(path);
						} catch (IOException e) {
							continue;
						}
						
						fileFolderNameList.add(file.getName());
						String fileName = "";
						try {
							fileName = file.getCanonicalPath();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						//Add the file element to fileFolderTypeList based on the file type.
						if (getFileExtension(fileName).equalsIgnoreCase("mp3") ||
							getFileExtension(fileName).equalsIgnoreCase("3gp") ||
							getFileExtension(fileName).equalsIgnoreCase("mp4") ||
							getFileExtension(fileName).equalsIgnoreCase("m4a") ||
							getFileExtension(fileName).equalsIgnoreCase("aac") ||
							getFileExtension(fileName).equalsIgnoreCase("ts") ||
							getFileExtension(fileName).equalsIgnoreCase("flac") ||
							getFileExtension(fileName).equalsIgnoreCase("mid") ||
							getFileExtension(fileName).equalsIgnoreCase("xmf") ||
							getFileExtension(fileName).equalsIgnoreCase("mxmf") ||
							getFileExtension(fileName).equalsIgnoreCase("midi") ||
							getFileExtension(fileName).equalsIgnoreCase("rtttl") ||
							getFileExtension(fileName).equalsIgnoreCase("rtx") ||
							getFileExtension(fileName).equalsIgnoreCase("ota") ||
							getFileExtension(fileName).equalsIgnoreCase("imy") ||
							getFileExtension(fileName).equalsIgnoreCase("ogg") ||
							getFileExtension(fileName).equalsIgnoreCase("mkv") ||
							getFileExtension(fileName).equalsIgnoreCase("wav")) {
							
							//The file is an audio file.
							fileFolderTypeList.add(AUDIO_FILE);
							fileFolderSizeList.add("" + getFormattedFileSize(file.length()));
							
						} else if (getFileExtension(fileName).equalsIgnoreCase("jpg") ||
								   getFileExtension(fileName).equalsIgnoreCase("gif") ||
								   getFileExtension(fileName).equalsIgnoreCase("png") ||
								   getFileExtension(fileName).equalsIgnoreCase("bmp") ||
								   getFileExtension(fileName).equalsIgnoreCase("webp")) {
							
							//The file is a picture file.
							fileFolderTypeList.add(PICTURE_FILE);
							fileFolderSizeList.add("" + getFormattedFileSize(file.length()));
							
						} else if (getFileExtension(fileName).equalsIgnoreCase("3gp") ||
								   getFileExtension(fileName).equalsIgnoreCase("mp4") ||
								   getFileExtension(fileName).equalsIgnoreCase("3gp") ||
								   getFileExtension(fileName).equalsIgnoreCase("ts") ||
								   getFileExtension(fileName).equalsIgnoreCase("webm") ||
								   getFileExtension(fileName).equalsIgnoreCase("mkv")) {
							
							//The file is a video file.
							fileFolderTypeList.add(VIDEO_FILE);
							fileFolderSizeList.add("" + getFormattedFileSize(file.length()));
							
						} else {
							
							//We don't have an icon for this file type so give it the generic file flag.
							fileFolderTypeList.add(FILE);
							fileFolderSizeList.add("" + getFormattedFileSize(file.length()));
							
						}

					}
					
				} 
			
			}
			
		}
		
		FoldersListViewAdapter foldersListViewAdapter = new FoldersListViewAdapter(getActivity(),
																				   fileFolderNameList,
																				   fileFolderTypeList, 
																				   fileFolderSizeList, 
																				   fileFolderPathList);
		
		listView.setAdapter(foldersListViewAdapter);
		foldersListViewAdapter.notifyDataSetChanged();

        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
                String newPath = fileFolderPathList.get(index);
                if ((Integer) view.getTag(R.string.folder_list_item_type)==FOLDER)
                    currentDir = newPath;

                //Indicates that the selected item is the "Back to Parent Directory" button.
                if (fileFolderTypeList.get(index)==null) {
                    getDir(newPath);
                } else {

                    //Check if the selected item is a folder or a file.
                    if (fileFolderTypeList.get(index).equals(FOLDER)) {
                        getDir(newPath);
                    } else {
                        playFile(index);
                    }

                }

            }

        });
		
		if (mApp.getOrientation()==Common.ORIENTATION_LANDSCAPE) {
			try {
				String folderName = f.getName();
				if (folderName.isEmpty()) {
					folderName = "Root Directory";
				}
				
				long millis = f.lastModified();
				int numberItems = fileFolderNameList.size();
				long size = f.getFreeSpace();
				
				if (size==0) {
					landscapeFreeSpaceValue.setText(getFormattedFileSize(size));
				} else {
					landscapeFreeSpaceValue.setText("Unknown");
				}

				landscapeFolderName.setText(folderName);
				
				Date date = new Date(millis);
				DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getActivity().getApplicationContext());
				String s = dateFormat.format(date);
				DateFormat formatter = new SimpleDateFormat("hh:mm aa");
				String dateFormatted = formatter.format(date);
				landscapeLastModifiedValue.setText(s + " " + dateFormatted);
				
				if (numberItems==1) {
					landscapeItemsValue.setText(numberItems + " item");
				} else {
					landscapeItemsValue.setText(numberItems + " items");
				}
	
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
    }

    /**
     * Resolves the /storage/emulated/legacy paths to
     * their true folder path representations. Required
     * for Nexuses and other devices with no SD card.
     */
    @SuppressLint("SdCardPath")
    private String getRealFilePath(String filePath) {

        if (filePath.equals("/storage/emulated/0") ||
                filePath.equals("/storage/emulated/0/") ||
                filePath.equals("/storage/emulated/legacy") ||
                filePath.equals("/storage/emulated/legacy/") ||
                filePath.equals("/storage/sdcard0") ||
                filePath.equals("/storage/sdcard0/") ||
                filePath.equals("/sdcard") ||
                filePath.equals("/sdcard/") ||
                filePath.equals("/mnt/sdcard") ||
                filePath.equals("/mnt/sdcard/")) {

            return Environment.getExternalStorageDirectory().toString();
        }

        return filePath;
    }

    /**
     * Calculates the parent dir of the current dir and calls getDir().
     * Returns true if the parent dir is the rootDir
     */
    public boolean getParentDir() {

        if (currentDir.equals("/"))
            return true;

        //Get the current folder's parent folder.
        File currentFolder = new File(currentDir);
        String parentFolder = "";
        try {
            parentFolder = currentFolder.getParentFile().getCanonicalPath();
        } catch (Exception e) {
            e.printStackTrace();
        }

        FilesFoldersFragment.currentDir = parentFolder;
        getDir(parentFolder);
        return false;

    }

    /**
     * Takes in a file size value and formats it.
     */
    public String getFormattedFileSize(final long value) {
    	
    	final long[] dividers = new long[] { teraBytes, gigaBytes, megaBytes, kiloBytes, 1 };
        final String[] units = new String[] { "TB", "GB", "MB", "KB", "bytes" };
        
        if(value < 1) {
        	return "";
        }
        
        String result = null;
        for(int i = 0; i < dividers.length; i++) {
            final long divider = dividers[i];
            if(value >= divider) {
                result = format(value, divider, units[i]);
                break;
            }
            
        }
        
        return result;
    }

    public String format(final long value, final long divider, final String unit) {
        final double result = divider > 1 ? (double) value / (double) divider : (double) value;
        
        return new DecimalFormat("#,##0.#").format(result) + " " + unit;
    }
    
    public String getFileExtension(String fileName) {
        String fileNameArray[] = fileName.split("\\.");
        String extension = fileNameArray[fileNameArray.length-1];

        return extension;
        
    }
    
    //Extracts specific ID3 metadata from an audio file and returns them in an ArrayList.
    public ArrayList<Object> extractFileMetadata(String filePath) {
    	ArrayList<Object> metadata = new ArrayList<Object>();
    	
    	MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
    	mediaMetadataRetriever.setDataSource(filePath);
    	
    	metadata.add(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
    	metadata.add(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
    	metadata.add(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
    	metadata.add(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
    	metadata.add(mediaMetadataRetriever.getEmbeddedPicture());
    	
    	return metadata;
    	
    }
    
    //Stores an ArrayList of all the audio files' paths within the specified folder.
    public void getAudioFilePathsInFolder(String folderPath) {
    	
    	//We'll use a filter to retrieve a list of all files with a matching extension.
    	File file = new File(folderPath);
    	FileExtensionFilter AUDIO_FILES_FILTER = new FileExtensionFilter(new String[] {".mp3", ".3gp", ".mp4",
    																				   ".m4a", ".aac", ".ts", 
    																				   ".flac", ".mid", ".xmf", 
    																				   ".mxmf", ".midi", ".rtttl", 
    																				   ".rtx", ".ota", "imy", ".ogg", 
    																				   ".mkv", ".wav" });
    	
    	File[] filesInFolder = file.listFiles(AUDIO_FILES_FILTER);
    	
    	//Loop through the list of files and add their file paths to the corresponding ArrayList.
    	for (int i=0; i < filesInFolder.length; i++) {
    		
    		try {
				audioFilePathsInFolder.add(filesInFolder[i].getCanonicalPath());
			} catch (IOException e) {
				//Skip any corrupt audilo files.
				continue;
			}
    		
    	}
    	
    }
    
    /**
     * This method goes through a folder recursively and saves all its
     * subdirectories to an ArrayList (subdirectoriesList). 
     */
    public void iterateThruFolder(String path) {

        File root = new File(path);
        File[] list = root.listFiles();

        if (list==null) {
        	return;
        }

        for (File f : list) {
        	
            if (f.isDirectory()) {
                iterateThruFolder(f.getAbsolutePath());
                
                if (!subdirectoriesList.contains(f.getPath())) {
                	subdirectoriesList.add(f.getPath());
                }
                    
            }
            
        }
        
    }
    
    /**
     * Plays the specified file.
     */
    public void playFile(int index) {

		//Check to make sure that the file is in a valid format.
		String fileName = fileFolderNameList.get(index);
		if (getFileExtension(fileName).equalsIgnoreCase("mp3") ||
			getFileExtension(fileName).equalsIgnoreCase("3gp") ||
			getFileExtension(fileName).equalsIgnoreCase("mp4") ||
			getFileExtension(fileName).equalsIgnoreCase("m4a") ||
			getFileExtension(fileName).equalsIgnoreCase("aac") ||
			getFileExtension(fileName).equalsIgnoreCase("ts") ||
			getFileExtension(fileName).equalsIgnoreCase("flac") ||
			getFileExtension(fileName).equalsIgnoreCase("mid") ||
			getFileExtension(fileName).equalsIgnoreCase("xmf") ||
			getFileExtension(fileName).equalsIgnoreCase("mxmf") ||
			getFileExtension(fileName).equalsIgnoreCase("midi") ||
			getFileExtension(fileName).equalsIgnoreCase("rtttl") ||
			getFileExtension(fileName).equalsIgnoreCase("rtx") ||
			getFileExtension(fileName).equalsIgnoreCase("ota") ||
			getFileExtension(fileName).equalsIgnoreCase("imy") ||
			getFileExtension(fileName).equalsIgnoreCase("ogg") ||
			getFileExtension(fileName).equalsIgnoreCase("mkv") ||
			getFileExtension(fileName).equalsIgnoreCase("wav")) {

            //Build the query's selection clause.
            Log.e("DEBUG", ">>>>CURRENT DIR: " + currentDir);
            String querySelection = MediaStore.Audio.Media.DATA + " LIKE "
                                  + "'" + currentDir.replace("'", "''") + "/%'";

            //Exclude all subfolders from this playback sequence.
            for (int i=0; i < fileFolderPathList.size(); i++) {
                if (fileFolderTypeList.get(i)==FOLDER)
                    querySelection += " AND " + MediaStore.Audio.Media.DATA + " NOT LIKE "
                                   + "'" + fileFolderPathList.get(i).replace("'", "''") + "/%'";

            }

            mApp.getPlaybackKickstarter().initPlayback(mContext,
                                                       querySelection,
                                                       Common.PLAY_ALL_IN_FOLDER,
                                                       index,
                                                       true, false);

		} else {
            Toast.makeText(mContext, R.string.cant_play_this_file, Toast.LENGTH_SHORT).show();
			
		}
		
    }
    
    //Plays the specified folder (recursively, if set).
    public void playFolder(String folderPath, boolean recursive) {
    	
    	//Retrieve the list of audio files within the folder (and subfolders if recursive is true).
    	if (recursive==false) {
        	getAudioFilePathsInFolder(folderPath);
        	
        	/* Now that we have a list of audio files within the folder, pass them
        	 * on to NowPlayingActivity (which will assemble the files into a cursor for the service. */
    		
        	//Check if the list is empty. If it is, show a Toast message to the user.
        	if (audioFilePathsInFolder.size() > 0) {
        		
        		//Extract the metadata from the first audio file (if any).
        		ArrayList<Object> metadata = new ArrayList<Object>();
        		metadata = extractFileMetadata(audioFilePathsInFolder.get(0));
        		
        		//Check if the audio file has a title. If not, use the file name.
        		String title = "";
        		if (metadata.get(0)==null) {
        			title = audioFilePathsInFolder.get(0);
        		} else {
        			title = (String) metadata.get(0);
        		}
        		
        		Intent intent = new Intent(mContext, NowPlayingActivity.class);
        		intent.putExtra("DURATION", (String) metadata.get(3));
        		intent.putExtra("SONG_NAME", title);
        		intent.putExtra("NUMBER_SONGS", 1);
        		
        		if (metadata.get(1)==null) {
        			intent.putExtra("ARTIST", "Unknown Artist");
        		} else {
        			intent.putExtra("ARTIST", (String) metadata.get(1));
        		}
        		
        		if (metadata.get(2)==null) {
        			intent.putExtra("ALBUM", "Unknown Album");
        		} else {
        			intent.putExtra("ALBUM", (String) metadata.get(2));
        		}
        		
        		if (metadata.get(3)==null) {
        			intent.putExtra("SELECTED_SONG_DURATION", 0);
        		} else {
        			intent.putExtra("SELECTED_SONG_DURATION", (String) metadata.get(3));
        		}
        		
        		intent.putExtra("DATA_URI", audioFilePathsInFolder.get(0));
        		
        		if (metadata.get(4)==null) {
        			intent.putExtra("EMBEDDED_ART", (byte[]) null);
        		} else {
        			intent.putExtra("EMBEDDED_ART", (byte[]) metadata.get(4));
        		}

        		intent.putExtra("NEW_PLAYLIST", true);
        		intent.putExtra("CALLED_FROM_FOOTER", false);
        		intent.putExtra("CALLED_FROM_FOLDERS", true);
        		intent.putExtra("CALLING_FRAGMENT", "FOLDERS_FRAGMENT");
        		
        		//We're dealing with the first audio file in the list, so just use zero for SONG_SELECTED_INDEX.
        		intent.putExtra("SONG_SELECTED_INDEX", 0);
        		
        		//Pass on the list of file paths to NowPlayingActivity (which will assemble them into a cursor).
        		intent.putStringArrayListExtra("FOLDER_AUDIO_FILE_PATHS", audioFilePathsInFolder);
        		
        		startActivity(intent);
        		getActivity().overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
        		
        	} else {
        		Toast.makeText(mContext, R.string.no_audio_files_found, Toast.LENGTH_LONG).show();
        	}
        	
    	} else {
    		//Run the AsyncTask that will excute the recursive methods.
    		File file = new File(folderPath);
    		AsyncPlayFolderRecursiveTask task = new AsyncPlayFolderRecursiveTask(getActivity(), file.getName());
    		String[] params = { folderPath };
    		task.execute(params);
    		
    		file = null;
    		
    	}
    	
    	//Clean up the ArrayLists.
    	audioFilePathsInFolder.clear();
    	subdirectoriesList.clear();
    	
    }
    
    private Runnable refreshFolderHierarchy = new Runnable() {

		@Override
		public void run() {
			
			if (REFRESH_REQUIRED==true) {

				try {
					File file = new File(currentFolderPath);
					
					if (file!=null) {
						String parentFile = file.getCanonicalPath();
						
						if (!parentFile.equals("/") && parentFile!=null) {
							getDir(file.getCanonicalPath());
							REFRESH_REQUIRED = false;
						}
						
					}

				} catch (Exception e) {
					return;
				}
			} else {
				mHandler.postDelayed(refreshFolderHierarchy, 100);
			}
			
		}
    	
    };
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
    	contextMenuItemPath = (String) info.targetView.getTag(R.string.folder_path);
    	if (info.targetView.getTag(R.string.folder_list_item_type).equals(FOLDER) 
    		&& v.getId()==R.id.folders_list_view) {
    		//Long-pressed a folder.
    		menu.setHeaderTitle(R.string.folder_actions);
    		
    		//Load the appropriate context menu items (Move vs Move Here).
    		String[] menuItems;
    		if (MOVE_OPERATION_PENDING==true) {
        		menuItems = getResources().getStringArray(R.array.folder_view_move_active_context_menu_items);
    		} else {
        		menuItems = getResources().getStringArray(R.array.folder_view_context_menu_items);
    		}

    
    		for (int i=0; i < menuItems.length; i++) {
    			menu.add(9868, i, i, menuItems[i]);
    		}
    		
    	} else {
    		//Long-pressed a file.
    		menu.setHeaderTitle(R.string.file_actions);
    		String[] menuItems = getResources().getStringArray(R.array.file_view_context_menu_items);
    
    		for (int i=0; i < menuItems.length; i++) {
    			menu.add(8689, i, i, menuItems[i]);
    		}
    		
    	}
    	
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	//Get the parameters of the ListView item we're dealing with.
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	int index = info.position;
    	
    	if (item.getGroupId()==9868) {
    		//Folder context menu actions.
        	switch(item.getItemId()) {
        	case 0:
        		//Play Folder.
        		
        		//Reset the shuffle function if it's currently enabled.
        		mApp.getSharedPreferences().edit().putBoolean(Common.SHUFFLE_ON, false).commit();
        		
        		playFolder(contextMenuItemPath, false);
        		contextMenuItemPath = "";
        		break;
        	case 1:
        		//Play Folder Recursively.
        		
        		//Reset the shuffle function if it's currently enabled.
        		mApp.getSharedPreferences().edit().putBoolean(Common.SHUFFLE_ON, false).commit();
        		
        		playFolder(contextMenuItemPath, true);
        		contextMenuItemPath = "";
        		break;
        	case 2:
        		//Copy.
        		sourceFile = new File(contextMenuItemPath);
        		final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
		        alertDialog.setTitle(R.string.copy);
		        alertDialog.setMessage(mContext.getResources().getString(R.string.copy_instructions));
		        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,
		        					  mContext.getResources().getString(R.string.ok), 
		        					  new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int which) {
											alertDialog.dismiss();
											
										}
										
									});
		        
		        alertDialog.show();
		        COPY_OPERATION_PENDING = true;
        		break;
        	case 3:
        		//Paste.
        		targetFile = new File(contextMenuItemPath);
        		if (COPY_OPERATION_PENDING==true) {
        			
        			//Check if the source and destination dirs are the same.
        			try {
						if (targetFile.getCanonicalPath().equals(sourceFile.getCanonicalPath())) {
							Toast.makeText(mContext, R.string.source_target_same, Toast.LENGTH_LONG).show();
							break;
						} else {
							
							AsyncPasteTask task = null;
							if (targetFile.isDirectory()) {
								task = new AsyncPasteTask(getActivity(), sourceFile, targetFile, "DIRECTORY");
							} else {
								task = new AsyncPasteTask(getActivity(), sourceFile, targetFile, "FILE");
							}
							
		        			task.execute();
		        			mHandler.postDelayed(refreshFolderHierarchy, 100);
		        			break;
						}
					} catch (IOException e) {
						Toast.makeText(mContext, R.string.error_occurred, Toast.LENGTH_LONG).show();
						break;
					}

        		} else {
        			Toast.makeText(mContext, R.string.no_file_copied, Toast.LENGTH_LONG).show();
        			break;
        		}
        		
        	case 4:
        		//Move/Move Here.
        		if (MOVE_OPERATION_PENDING==true) {
        			//Start the move operation.
        			String sourceType = "";
        			if (moveSourceFile.isDirectory()) {
        				sourceType = "DIRECTORY";
        			} else {
        				sourceType = "FILE";
        			}
        			
        			File destinationFolder = new File(contextMenuItemPath);
        			AsyncMoveTask moveTask = new AsyncMoveTask(getActivity(), 
        													   moveSourceFile, 
        													   destinationFolder, 
        													   sourceType);
        			moveTask.execute();	
        			break;
        		} else {
        			//Show the move info dialog.
        			moveSourceFile = new File(contextMenuItemPath);
            		final AlertDialog moveAlertDialog = new AlertDialog.Builder(getActivity()).create();
    		        moveAlertDialog.setTitle(R.string.move);
    		        moveAlertDialog.setMessage(mContext.getResources().getString(R.string.move_instructions));
    		        moveAlertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,
    		        					  mContext.getResources().getString(R.string.ok), 
    		        					  new DialogInterface.OnClickListener() {
    										
    										@Override
    										public void onClick(DialogInterface dialog, int which) {
    											moveAlertDialog.dismiss();
    											mHandler.postDelayed(refreshFolderHierarchy, 100);
    											
    										}
    										
    									});
    		        
    		        moveAlertDialog.show();
    		        MOVE_OPERATION_PENDING = true;
            		try {
    					currentFolderPath = moveSourceFile.getParentFile().getCanonicalPath();
    				} catch (IOException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
    		        break;
        		}
        		
        	case 5:
        		//Rename.
        		final File renameFile = new File(contextMenuItemPath);
        		final AlertDialog renameAlertDialog = new AlertDialog.Builder(getActivity()).create();
        		final EditText fileEditText = new EditText(getActivity());
        		fileEditText.setHint(R.string.file_name);
        		fileEditText.setSingleLine(true);
        		fileEditText.setText(renameFile.getName());
        		renameAlertDialog.setView(fileEditText);
        		renameAlertDialog.setTitle(R.string.rename);
        		renameAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, 
        								    mContext.getResources().getString(R.string.cancel), 
        								    new DialogInterface.OnClickListener() {
					
											@Override
											public void onClick(DialogInterface dialog, int which) {
												renameAlertDialog.dismiss();
												mHandler.removeCallbacks(refreshFolderHierarchy);
											}
												
										});
        		
        		renameAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE, 
										    mContext.getResources().getString(R.string.rename), 
										    new DialogInterface.OnClickListener() {
					
											@Override
											public void onClick(DialogInterface dialog, int which) {
												
												//Check if the new file name is empty.
												if (fileEditText.getText().toString().isEmpty()) {
													Toast.makeText(getActivity(), R.string.enter_a_name_for_folder, Toast.LENGTH_LONG).show();
												} else {

													File newNameFile = null;
													try {
														newNameFile = new File(renameFile.getParentFile().getCanonicalPath() + "/" + fileEditText.getText().toString());
													} catch (IOException e) {
														Toast.makeText(getActivity(), R.string.folder_could_not_be_renamed, Toast.LENGTH_LONG).show();
														return;
													}
													
													try {
														FileUtils.moveFile(renameFile, newNameFile);
													} catch (IOException e) {
														Toast.makeText(getActivity(), R.string.folder_could_not_be_renamed, Toast.LENGTH_LONG).show();
														return;
													}
													
									        		Toast.makeText(getActivity(), R.string.folder_renamed, Toast.LENGTH_SHORT).show();
									        		REFRESH_REQUIRED = true;
													renameAlertDialog.dismiss();
													
												}
												
											}
												
										});
					        		
        		renameAlertDialog.show();
        		mHandler.postDelayed(refreshFolderHierarchy, 100);
        		try {
					currentFolderPath = renameFile.getParentFile().getCanonicalPath();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		break;
        	case 6:
        		//Delete.
        		final File deleteFile = new File(contextMenuItemPath);
        		final AlertDialog deleteAlertDialog = new AlertDialog.Builder(getActivity()).create();
		        deleteAlertDialog.setTitle(R.string.delete);
		        deleteAlertDialog.setMessage(mContext.getResources().getString(R.string.delete_folder_confirmation));
		        deleteAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
			        					  	mContext.getResources().getString(R.string.cancel), 
			        					  	new DialogInterface.OnClickListener() {
											
												@Override
												public void onClick(DialogInterface dialog, int which) {
													deleteAlertDialog.dismiss();
												
												}
											
											});
		        
		        deleteAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
					  					  	mContext.getResources().getString(R.string.delete), 
					  					  	new DialogInterface.OnClickListener() {
												
												@Override
												public void onClick(DialogInterface dialog, int which) {
									        		AsyncDeleteTask task;
								        			task = new AsyncDeleteTask(getActivity(), deleteFile, "DIRECTORY");
								        			task.execute();
													deleteAlertDialog.dismiss();
													mHandler.postDelayed(refreshFolderHierarchy, 100);
													
												}
												
											});
		        
		        deleteAlertDialog.show();
        		try {
					currentFolderPath = deleteFile.getParentFile().getCanonicalPath();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		break;	
        		
        	case 7:
        		mApp.getSharedPreferences().edit().putString("DEFAULT_FOLDER", contextMenuItemPath).commit();
        		String confirmation = contextMenuItemPath + " " + getResources().getString(R.string.is_now_default_folder);
        		Toast.makeText(mContext, confirmation, Toast.LENGTH_LONG).show();
        		break;
            }
            
    	} else if (item.getGroupId()==8689) {
    		//File context menu actions.
    		switch(item.getItemId()) {
        	case 0:
        		//Play File.
        		playFile(index);
        		break;
        	case 1:
        		//Copy.
        		sourceFile = new File(contextMenuItemPath);
        		final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
		        alertDialog.setTitle(R.string.copy);
		        alertDialog.setMessage(mContext.getResources().getString(R.string.copy_instructions));
		        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,
		        					  mContext.getResources().getString(R.string.ok), 
		        					  new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int which) {
											alertDialog.dismiss();
											
										}
										
									});
		        
		        alertDialog.show();
		        COPY_OPERATION_PENDING = true;
        		break;
        	case 2:
        		//Move.
    			moveSourceFile = new File(contextMenuItemPath);
        		final AlertDialog moveAlertDialog = new AlertDialog.Builder(getActivity()).create();
		        moveAlertDialog.setTitle(R.string.move);
		        moveAlertDialog.setMessage(mContext.getResources().getString(R.string.move_instructions));
		        moveAlertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,
		        					  mContext.getResources().getString(R.string.ok), 
		        					  new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int which) {
											moveAlertDialog.dismiss();
											
										}
										
									});
		        
		        moveAlertDialog.show();
		        MOVE_OPERATION_PENDING = true;
        		break;
        	case 3:
        		//Rename.
        		final File renameFile = new File(contextMenuItemPath);
        		final AlertDialog renameAlertDialog = new AlertDialog.Builder(getActivity()).create();
        		final EditText fileEditText = new EditText(getActivity());
        		fileEditText.setHint(R.string.file_name);
        		fileEditText.setSingleLine(true);
        		fileEditText.setPadding(20, 20, 20, 20);
        		fileEditText.setText(renameFile.getName());
        		renameAlertDialog.setView(fileEditText);
        		renameAlertDialog.setTitle(R.string.rename);
        		renameAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, 
        								    mContext.getResources().getString(R.string.cancel), 
        								    new DialogInterface.OnClickListener() {
					
											@Override
											public void onClick(DialogInterface dialog, int which) {
												renameAlertDialog.dismiss();
												mHandler.removeCallbacks(refreshFolderHierarchy);
											}
												
										});
        		
        		renameAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE, 
										    mContext.getResources().getString(R.string.rename), 
										    new DialogInterface.OnClickListener() {
					
											@Override
											public void onClick(DialogInterface dialog, int which) {
												
												//Check if the new file name is empty.
												if (fileEditText.getText().toString().isEmpty()) {
													Toast.makeText(getActivity(), R.string.enter_a_name_for_file, Toast.LENGTH_LONG).show();
												} else {

													File newNameFile = null;
													try {
														newNameFile = new File(renameFile.getParentFile().getCanonicalPath() + "/" + fileEditText.getText().toString());
													} catch (IOException e) {
														Toast.makeText(getActivity(), R.string.file_could_not_be_renamed, Toast.LENGTH_LONG).show();
														return;
													}
													
													try {
														FileUtils.moveFile(renameFile, newNameFile);
													} catch (IOException e) {
														Toast.makeText(getActivity(), R.string.file_could_not_be_renamed, Toast.LENGTH_LONG).show();
														return;
													}
													
									        		Toast.makeText(getActivity(), R.string.file_renamed, Toast.LENGTH_SHORT).show();
									        		REFRESH_REQUIRED = true;
													renameAlertDialog.dismiss();
													
												}
												
											}
												
										});
					        		
        		renameAlertDialog.show();
        		try {
					currentFolderPath = renameFile.getParentFile().getCanonicalPath();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		
        		mHandler.postDelayed(refreshFolderHierarchy, 100);
        		break;
        	case 4:
        		//Delete.
        		final File deleteFile = new File(contextMenuItemPath);
        		final AlertDialog deleteAlertDialog = new AlertDialog.Builder(getActivity()).create();
		        deleteAlertDialog.setTitle(R.string.delete);
		        deleteAlertDialog.setMessage(mContext.getResources().getString(R.string.delete_file_confirmation));
		        deleteAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
			        					  	mContext.getResources().getString(R.string.cancel), 
			        					  	new DialogInterface.OnClickListener() {
											
												@Override
												public void onClick(DialogInterface dialog, int which) {
													deleteAlertDialog.dismiss();
												
												}
											
											});
		        
		        deleteAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
					  					  	mContext.getResources().getString(R.string.delete), 
					  					  	new DialogInterface.OnClickListener() {
												
												@Override
												public void onClick(DialogInterface dialog, int which) {
									        		AsyncDeleteTask task;
								        			task = new AsyncDeleteTask(getActivity(), deleteFile, "FILE");
								        			task.execute();
													deleteAlertDialog.dismiss();
													mHandler.postDelayed(refreshFolderHierarchy, 100);
													
												}
												
											});
		        
		        deleteAlertDialog.show();
        		try {
					currentFolderPath = deleteFile.getParentFile().getCanonicalPath();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		
        		break;
    		}
    		
    	}

        return super.onContextItemSelected(item);
    }
	
    @Override
    public void onDestroyView() {
    	super.onDestroyView();
    	mContext = null;
    	listView = null;

    }
    
    /*
     * Getter methods. 
     */
    public String getCurrentDir() {
    	return currentDir;
    }
     
    /*
     * Setter methods.
     */
    public void setCurrentDir(String currentDir) {
    	this.currentDir = currentDir;
    }
    
}
