package com.jams.music.player.AsyncTasks;

import java.io.File;

import org.apache.commons.io.FileUtils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.widget.Toast;

import com.jams.music.player.R;
import com.jams.music.player.FoldersFragment.FilesFoldersFragment;

public class AsyncMoveTask extends AsyncTask<String, Void, Boolean> {
	
    private Context mContext;
    private String mFolderName;
    private ProgressDialog pd;
	boolean dialogVisible = true;
	
	private File sourceDir;
	private File targetLocation;
	private String mSourceType;
    
    public AsyncMoveTask(Context context,
    					  File source,
    					  File destinationDirectory,
    					  String sourceType) {
    	
    	mContext = context;
    	sourceDir = source;
    	targetLocation = destinationDirectory;
    	mSourceType = sourceType;
    }
    
    protected void onPreExecute() {
		pd = new ProgressDialog(mContext);
		pd.setCancelable(false);
		pd.setIndeterminate(false);
		pd.setTitle(R.string.move);
		pd.setMessage(mContext.getResources().getString(R.string.moving_file));
		pd.setButton(DialogInterface.BUTTON_NEUTRAL, mContext.getResources()
															 .getString(R.string.run_in_background), 
															 new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				pd.dismiss();
				
			}
			
		});
		
		pd.show();
    	
    }
 
    @Override
    protected Boolean doInBackground(String... params) {
    	
    	if (mSourceType.equals("DIRECTORY")) {
    		
    		try {
				FileUtils.moveDirectoryToDirectory(sourceDir, targetLocation, true);
			} catch (Exception e) {
				return false;
			}
    		
    	} else if (mSourceType.equals("FILE")) {
    		
    		try {
    			FileUtils.moveFileToDirectory(sourceDir, targetLocation, true);
    		} catch (Exception e) {
    			return false;
    		}
    		
    	}
    	return true;
    }

    @Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		
    	pd.dismiss();
    	FilesFoldersFragment.REFRESH_REQUIRED = true;
    	FilesFoldersFragment.MOVE_OPERATION_PENDING = false;
    	
    	if (result==true) {
        	Toast.makeText(mContext, R.string.done_move, Toast.LENGTH_SHORT).show();
    	} else {
        	Toast.makeText(mContext, R.string.file_could_not_be_written_new_location, Toast.LENGTH_LONG).show();
    	}
		
	}

}