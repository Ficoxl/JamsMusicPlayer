package com.jams.music.player.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;

import com.jams.music.player.R;
import com.jams.music.player.SettingsActivity.PreferenceDialogLauncherActivity;
import com.jams.music.player.Utils.Common;

public class ApplicationThemeDialog extends DialogFragment {

	private Activity parentActivity;
    private Common mApp;
	private int selectedThemeIndex;
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

		parentActivity = getActivity();
        mApp = (Common) parentActivity.getApplicationContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        //Check which theme is currently selected and set the appropriate flag.
        if (mApp.getCurrentTheme()==Common.LIGHT_THEME) {
        	selectedThemeIndex = 0;
        } else if (mApp.getCurrentTheme()==Common.DARK_THEME) {
        	selectedThemeIndex = 1;
        }

        //Set the dialog title.
        builder.setTitle(R.string.app_theme);
        builder.setSingleChoiceItems(R.array.app_theme_choices, selectedThemeIndex, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				PreferenceDialogLauncherActivity activity = new PreferenceDialogLauncherActivity();
				if (which==0) {
					mApp.getSharedPreferences().edit().putInt(Common.CURRENT_THEME, Common.LIGHT_THEME).commit();
                    mApp.initDisplayImageOptions();
					
					Intent i = parentActivity.getBaseContext()
											 .getPackageManager()
											 .getLaunchIntentForPackage(parentActivity.getBaseContext()
													 								  .getPackageName());
					
					i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
							   Intent.FLAG_ACTIVITY_NEW_TASK | 
							   Intent.FLAG_ACTIVITY_CLEAR_TASK);
					activity.finish();
					startActivity(i);
					
				} else if (which==1) {
					mApp.getSharedPreferences().edit().putInt(Common.CURRENT_THEME, Common.DARK_THEME).commit();
                    mApp.initDisplayImageOptions();
					
					Intent i = parentActivity.getBaseContext()
											 .getPackageManager()
											 .getLaunchIntentForPackage(parentActivity.getBaseContext()
													 								  .getPackageName());
	
					i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
							   Intent.FLAG_ACTIVITY_NEW_TASK | 
							   Intent.FLAG_ACTIVITY_CLEAR_TASK);
					activity.finish();
					startActivity(i);
	
				}
				
			}
        	
        });

        return builder.create();
    }
	
}
