package com.jams.music.player.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.jams.music.player.R;
import com.jams.music.player.AsyncTasks.AsyncApplyEQToGenreTask;
import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.EqualizerAudioFXActivity.EqualizerFragment;
import com.jams.music.player.NowPlayingActivity.NowPlayingActivity;
import com.jams.music.player.Utils.Common;

public class EQGenresListDialog extends DialogFragment {

	private Common mApp;
	private EqualizerFragment mEqualizerFragment;
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

		mApp = (Common) getActivity().getApplicationContext();
		mEqualizerFragment = ((NowPlayingActivity) getActivity()).getEqualizerFragment();	
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //Get a cursor with the list of all the unique genres.
        final Cursor cursor = mApp.getDBAccessHelper().getAllUniqueGenres("");
        
        //Set the dialog title.
        builder.setTitle(R.string.apply_to);
        builder.setCursor(cursor, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				cursor.moveToPosition(which);
				String genreName = cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_GENRE));
				AsyncApplyEQToGenreTask task = new AsyncApplyEQToGenreTask(getActivity(), 
																		   genreName, 
																		   mEqualizerFragment.getFiftyHertzLevel(), 
														  				   mEqualizerFragment.getOneThirtyHertzLevel(), 
														  				   mEqualizerFragment.getThreeTwentyHertzLevel(), 
														  				   mEqualizerFragment.getEightHundredHertzLevel(), 
														  				   mEqualizerFragment.getTwoKilohertzLevel(), 
														  				   mEqualizerFragment.getFiveKilohertzLevel(), 
														  				   mEqualizerFragment.getTwelvePointFiveKilohertzLevel(), 
														  				   (short) mEqualizerFragment.getVirtualizerSeekBar().getProgress(), 
														  				   (short) mEqualizerFragment.getBassBoostSeekBar().getProgress(), 
														  			       (short) mEqualizerFragment.getReverbSpinner().getSelectedItemPosition());
				
				task.execute(new String[] { "" + which });
				
				if (cursor!=null)
					cursor.close();
				
			}
			
		}, DBAccessHelper.SONG_GENRE);

        return builder.create();
    }
	
}
							