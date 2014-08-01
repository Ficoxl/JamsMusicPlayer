/*
 * Copyright (C) 2014 Saravan Pantham
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jams.music.player.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.jams.music.player.R;
import com.jams.music.player.AsyncTasks.AsyncApplyEQToAlbumTask;
import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.EqualizerActivity.EqualizerActivity;
import com.jams.music.player.NowPlayingActivity.NowPlayingActivity;
import com.jams.music.player.Utils.Common;

public class EQAlbumsListDialog extends DialogFragment {

	private Common mApp;
	private EqualizerActivity mEqualizerFragment;
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

		mApp = (Common) getActivity().getApplicationContext();
		mEqualizerFragment = (EqualizerActivity) getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //Get a cursor with the list of all albums.
        final Cursor cursor = mApp.getDBAccessHelper().getAllAlbumsOrderByName();
        
        //Set the dialog title.
        builder.setTitle(R.string.apply_to);
        builder.setCursor(cursor, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				cursor.moveToPosition(which);
				String albumName = cursor.getString(cursor.getColumnIndex(DBAccessHelper.SONG_ALBUM));
				AsyncApplyEQToAlbumTask task = new AsyncApplyEQToAlbumTask(getActivity(), 
																		   albumName, 
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
				
				//Hide the equalizer fragment.
				getActivity().finish();
				
			}
			
		}, DBAccessHelper.SONG_ALBUM);

        return builder.create();
    }
	
}
