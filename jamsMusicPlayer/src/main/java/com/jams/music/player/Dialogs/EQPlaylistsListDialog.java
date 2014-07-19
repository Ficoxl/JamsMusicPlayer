package com.jams.music.player.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.jams.music.player.EqualizerActivity.EqualizerActivity;
import com.jams.music.player.Utils.Common;

public class EQPlaylistsListDialog extends DialogFragment {

	private Common mApp;
	private Activity parentActivity;
	private EqualizerActivity mFragment;
	
	public EQPlaylistsListDialog() {
		super();
	}
	
	public EQPlaylistsListDialog(EqualizerActivity fragment) {
		mFragment = fragment;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

		mApp = (Common) getActivity().getApplicationContext();
		parentActivity = getActivity();		
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        /*//Get a cursor with the list of all user-created playlists.
        final Cursor cursor = mApp.getDBAccessHelper().getAllUniqueUserPlaylists();
        
        //Set the dialog title.
        builder.setTitle(R.string.apply_to);
        builder.setCursor(cursor, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				cursor.moveToPosition(which);
				AsyncApplyEQToPlaylistTask task = new AsyncApplyEQToPlaylistTask(parentActivity, mFragment, cursor.getString(
																								 			cursor.getColumnIndex(
																								 			DBAccessHelper.PLAYLIST_NAME)));
				
				task.execute(new String[] { "" + which });
				
			}
			
		}, DBAccessHelper.PLAYLIST_NAME);
*/
        return builder.create();
    }
	
}
