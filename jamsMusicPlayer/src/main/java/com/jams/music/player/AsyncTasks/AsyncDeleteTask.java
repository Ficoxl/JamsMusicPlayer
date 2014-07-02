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

public class AsyncDeleteTask extends AsyncTask<String, Void, Boolean> {
    private Context mContext;
    private ProgressDialog pd;
	boolean dialogVisible = true;
	
	private File sourceDir;
	private String mSourceType;
    
    public AsyncDeleteTask(Context context,
    					   File source,
    					   String sourceType) {
    	
    	mContext = context;
    	sourceDir = source;
    	mSourceType = sourceType;
    }
    
    protected void onPreExecute() {
		pd = new ProgressDialog(mContext);
		pd.setCancelable(false);
		pd.setIndeterminate(false);
		pd.setTitle(R.string.delete);
		pd.setMessage(mContext.getResources().getString(R.string.deleting_files));
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
				FileUtils.deleteDirectory(sourceDir);
			} catch (Exception e) {
				return false;
			}
    		
    	} else if (mSourceType.equals("FILE")) {
    		
    		try {
    			boolean status = sourceDir.delete();
    			
    			if (status==true) {
    				return true;
    			} else {
    				return false;
    			}
    			
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
    	
    	if (result==true) {
        	Toast.makeText(mContext, R.string.file_deleted, Toast.LENGTH_SHORT).show();
    	} else {
        	Toast.makeText(mContext, R.string.file_could_not_be_deleted, Toast.LENGTH_LONG).show();
    	}
		
	}

}