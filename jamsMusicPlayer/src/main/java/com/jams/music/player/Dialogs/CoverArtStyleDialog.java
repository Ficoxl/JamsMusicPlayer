package com.jams.music.player.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.jams.music.player.R;

public class CoverArtStyleDialog extends DialogFragment {

	private Activity parentActivity;
	private int selectedThemeIndex;
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

		parentActivity = getActivity();
		
		final SharedPreferences sharedPreferences = parentActivity.
											  getSharedPreferences("com.jams.music.player", Context.MODE_PRIVATE);
		
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        //Check which style is currently selected and set the appropriate flag.
        if (sharedPreferences.getString("COVER_ART_STYLE", "CARD_STYLE").equals("CARD_STYLE")) {
        	selectedThemeIndex = 0;
        } else {
        	selectedThemeIndex = 1;
        }

        //Set the dialog title.
        builder.setTitle(R.string.cover_art_style);
        builder.setSingleChoiceItems(R.array.cover_art_style_choices, selectedThemeIndex, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				if (which==0) {
					sharedPreferences.edit().putString("COVER_ART_STYLE", "CARD_STYLE").commit();

				} else if (which==1) {
					sharedPreferences.edit().putString("COVER_ART_STYLE", "FILL_SCREEN").commit();

				}
				
				dialog.dismiss();
				getActivity().finish();
				
			}
        	
        });

        return builder.create();
    }
	
	@Override
	public void onPause() {
		super.onPause();
		
		getActivity().finish();
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		getActivity().finish();
		
	}
	
}
