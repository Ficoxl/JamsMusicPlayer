package com.jams.music.player.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.jams.music.player.R;
import com.jams.music.player.SettingsActivity.PreferenceDialogLauncherActivity;
import com.jams.music.player.Utils.Common;

public class ApplicationThemeDialog extends DialogFragment {

	private Activity parentActivity;
    private Common mApp;
	private int selectedThemeIndex;
	
	private static final String SELECTED_THEME = "SELECTED_THEME";
	private static final String DARK_THEME = "DARK_THEME";
	private static final String LIGHT_THEME = "LIGHT_THEME";
	private static final String DARK_CARDS_THEME = "DARK_CARDS_THEME";
	private static final String LIGHT_CARDS_THEME = "LIGHT_CARDS_THEME";
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

		parentActivity = getActivity();
        mApp = (Common) parentActivity.getApplicationContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        //Check which theme is currently selected and set the appropriate flag.
        if (mApp.getSharedPreferences().getString(SELECTED_THEME, LIGHT_CARDS_THEME).equals(LIGHT_THEME)) {
        	selectedThemeIndex = 0;
        } else if (mApp.getSharedPreferences().getString(SELECTED_THEME, LIGHT_CARDS_THEME).equals(DARK_THEME)) {
        	selectedThemeIndex = 1;
        } else if (mApp.getSharedPreferences().getString(SELECTED_THEME, LIGHT_CARDS_THEME).equals(LIGHT_CARDS_THEME)) {
        	selectedThemeIndex = 2;
        } else if (mApp.getSharedPreferences().getString(SELECTED_THEME, LIGHT_CARDS_THEME).equals(DARK_CARDS_THEME)) {
        	selectedThemeIndex = 3;
        }

        //Set the dialog title.
        builder.setTitle(R.string.app_theme);
        builder.setSingleChoiceItems(R.array.app_theme_choices, selectedThemeIndex, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				PreferenceDialogLauncherActivity activity = new PreferenceDialogLauncherActivity();
				if (which==0) {
					mApp.getSharedPreferences().edit().putString(SELECTED_THEME, LIGHT_THEME).commit();
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
					mApp.getSharedPreferences().edit().putString(SELECTED_THEME, LIGHT_CARDS_THEME).commit();
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
	
				} else if (which==2) {
					mApp.getSharedPreferences().edit().putString(SELECTED_THEME, LIGHT_CARDS_THEME).commit();
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
					
				} else if (which==3) {
					mApp.getSharedPreferences().edit().putString(SELECTED_THEME, DARK_CARDS_THEME).commit();
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
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		getActivity().finish();
		
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		getActivity().finish();
		
	}
	
}
