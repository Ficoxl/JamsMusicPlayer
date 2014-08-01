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

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TextView;

import com.jams.music.player.R;

public class TrialExpiryDialog extends DialogFragment {

	private Activity parentActivity;
	private DialogFragment dialogFragment;
	private TextView trialExpiryText;
	private TextView daysTextView;
	private TextView numberOfDaysLeftTextView;
	private TextView trialCounterDummyInfoTextView;
	
	private SharedPreferences sharedPreferences;
	private boolean EXPIRED_FLAG = false;
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		parentActivity = getActivity();
		dialogFragment = this;
		
		View rootView = (View) parentActivity.getLayoutInflater().inflate(R.layout.trial_expiry_dialog, null);
		sharedPreferences = getActivity().getSharedPreferences("com.jams.music.player", Context.MODE_PRIVATE);
		
		//Check if the BETA version has expired.
        File file = new File(Environment.getExternalStorageDirectory() + "/.beta2");
        if (file.exists()) {
        	//The app has expired.
        	EXPIRED_FLAG = true;
        } else {
        	EXPIRED_FLAG = false;
        }
        
/*        //Change the dialog message based on the expiry status of the app.
        if (EXPIRED_FLAG==true) {
    		trialExpiryText = (TextView) rootView.findViewById(R.id.beta_version_will_expire);
    		trialExpiryText.setText(R.string.beta_has_expired);
    		trialExpiryText.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
    		trialExpiryText.setPaintFlags(trialExpiryText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    		
    		numberOfDaysLeftTextView = (TextView) rootView.findViewById(R.id.beta_expire_day_count);
    		numberOfDaysLeftTextView.setVisibility(View.GONE);
    		
    		daysTextView = (TextView) rootView.findViewById(R.id.beta_days_text);
    		daysTextView.setVisibility(View.GONE);
    		
    		trialCounterDummyInfoTextView = (TextView) rootView.findViewById(R.id.beta_trial_dummy_info_text);
    		trialCounterDummyInfoTextView.setVisibility(View.GONE);
    		
        } else {
    		trialExpiryText = (TextView) rootView.findViewById(R.id.beta_version_will_expire);
    		trialExpiryText.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
    		trialExpiryText.setPaintFlags(trialExpiryText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    		
    		numberOfDaysLeftTextView = (TextView) rootView.findViewById(R.id.beta_expire_day_count);
    		numberOfDaysLeftTextView.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
    		numberOfDaysLeftTextView.setPaintFlags(numberOfDaysLeftTextView.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    		
    		trialCounterDummyInfoTextView = (TextView) rootView.findViewById(R.id.beta_trial_dummy_info_text);
    		trialCounterDummyInfoTextView.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
    		trialCounterDummyInfoTextView.setPaintFlags(trialCounterDummyInfoTextView.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    		
    		//Calculate the number of days left.
    		Date date = new Date();
    		//1296000000 ms = 15 days.
    		long expiryDate = sharedPreferences.getLong("EXPIRY_DATE", date.getTime() + 1296000000);
    		long currentDate = date.getTime();
    		long millisLeft = expiryDate - currentDate;
    		long daysLeft = TimeUnit.MILLISECONDS.toDays(millisLeft);
    		numberOfDaysLeftTextView.setText("" + daysLeft);
    		
    		daysTextView = (TextView) rootView.findViewById(R.id.beta_days_text);
    		daysTextView.setTypeface(TypefaceHelper.getTypeface(parentActivity, "RobotoCondensed-Light"));
    		daysTextView.setPaintFlags(daysTextView.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        }*/
		
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //Set the dialog title.
        builder.setTitle(R.string.app_name);
        builder.setView(rootView);
        builder.setPositiveButton(R.string.ok, new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				dialogFragment.dismiss();
				
				if (EXPIRED_FLAG==true) {
					getActivity().finish();
				}
				
			}
        	
        });

        return builder.create();
    }
	
}
