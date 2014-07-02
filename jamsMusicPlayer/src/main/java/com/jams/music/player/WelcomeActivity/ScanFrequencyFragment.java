package com.jams.music.player.WelcomeActivity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.jams.music.player.R;
import com.jams.music.player.Helpers.TypefaceHelper;

public class ScanFrequencyFragment extends Fragment {
	
	private Activity parentActivity;
	private TextView welcomeHeader;
	private TextView welcomeText1;
	
	private RadioGroup radioGroup;
	private RadioButton scanManually;
	private RadioButton scanEveryStartup;
	private RadioButton scanEvery3Startups;
	private RadioButton scanEvery5Startups;
	private RadioButton scanEvery10Startups;
	private RadioButton scanEvery20Startups;
	
	private SharedPreferences sharedPreferences;
	private String SCAN_FREQUENCY = "SCAN_FREQUENCY";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		parentActivity =  getActivity();
		View rootView = (View) parentActivity.getLayoutInflater().inflate(R.layout.fragment_welcome_screen_3, null);	
		sharedPreferences = parentActivity.getSharedPreferences("com.jams.music.player", Context.MODE_PRIVATE);
		
		welcomeHeader = (TextView) rootView.findViewById(R.id.welcome_header);
		welcomeHeader.setTypeface(TypefaceHelper.getTypeface(getActivity(), "RobotoCondensed-Light"));
        welcomeHeader.setPaintFlags(welcomeHeader.getPaintFlags() 
        						   | Paint.ANTI_ALIAS_FLAG
        						   | Paint.SUBPIXEL_TEXT_FLAG);
		
		welcomeText1 = (TextView) rootView.findViewById(R.id.welcome_text_1);
		welcomeText1.setTypeface(TypefaceHelper.getTypeface(getActivity(), "RobotoCondensed-Light"));
        welcomeText1.setPaintFlags(welcomeText1.getPaintFlags() 
        						   | Paint.ANTI_ALIAS_FLAG
        						   | Paint.SUBPIXEL_TEXT_FLAG);
        
        radioGroup = (RadioGroup) rootView.findViewById(R.id.scan_frequency_radio_group);
        scanManually = (RadioButton) rootView.findViewById(R.id.scan_manually);
        scanEveryStartup = (RadioButton) rootView.findViewById(R.id.scan_every_startup);
        scanEvery3Startups = (RadioButton) rootView.findViewById(R.id.scan_every_3_startups);
        scanEvery5Startups = (RadioButton) rootView.findViewById(R.id.scan_every_5_startups);
        scanEvery10Startups = (RadioButton) rootView.findViewById(R.id.scan_every_10_startups);
        scanEvery20Startups = (RadioButton) rootView.findViewById(R.id.scan_every_20_startups);
        
		scanManually.setTypeface(TypefaceHelper.getTypeface(getActivity(), "RobotoCondensed-Light"));
        scanManually.setPaintFlags(scanManually.getPaintFlags() 
        						   | Paint.ANTI_ALIAS_FLAG
        						   | Paint.SUBPIXEL_TEXT_FLAG);
        
		scanEveryStartup.setTypeface(TypefaceHelper.getTypeface(getActivity(), "RobotoCondensed-Light"));
        scanEveryStartup.setPaintFlags(scanEveryStartup.getPaintFlags() 
        						   | Paint.ANTI_ALIAS_FLAG
        						   | Paint.SUBPIXEL_TEXT_FLAG);
        
		scanEvery3Startups.setTypeface(TypefaceHelper.getTypeface(getActivity(), "RobotoCondensed-Light"));
        scanEvery3Startups.setPaintFlags(scanEvery3Startups.getPaintFlags() 
        						   | Paint.ANTI_ALIAS_FLAG
        						   | Paint.SUBPIXEL_TEXT_FLAG);
        
		scanEvery5Startups.setTypeface(TypefaceHelper.getTypeface(getActivity(), "RobotoCondensed-Light"));
        scanEvery5Startups.setPaintFlags(scanEvery5Startups.getPaintFlags() 
        						   | Paint.ANTI_ALIAS_FLAG
        						   | Paint.SUBPIXEL_TEXT_FLAG);
        
		scanEvery10Startups.setTypeface(TypefaceHelper.getTypeface(getActivity(), "RobotoCondensed-Light"));
        scanEvery10Startups.setPaintFlags(scanEvery10Startups.getPaintFlags() 
        						   | Paint.ANTI_ALIAS_FLAG
        						   | Paint.SUBPIXEL_TEXT_FLAG);
        
		scanEvery20Startups.setTypeface(TypefaceHelper.getTypeface(getActivity(), "RobotoCondensed-Light"));
        scanEvery20Startups.setPaintFlags(scanEvery20Startups.getPaintFlags() 
        						   | Paint.ANTI_ALIAS_FLAG
        						   | Paint.SUBPIXEL_TEXT_FLAG);
        
        //Check which frequency is currently selected and set the appropriate flag.
        if (sharedPreferences.getInt(SCAN_FREQUENCY, 5)==0) {
        	scanEveryStartup.setChecked(true);
        } else if (sharedPreferences.getInt(SCAN_FREQUENCY, 5)==1) {
        	scanEvery3Startups.setChecked(true);
        } else if (sharedPreferences.getInt(SCAN_FREQUENCY, 5)==2) {
        	scanEvery5Startups.setChecked(true);
        } else if (sharedPreferences.getInt(SCAN_FREQUENCY, 5)==3) {
        	scanEvery10Startups.setChecked(true);
        } else if (sharedPreferences.getInt(SCAN_FREQUENCY, 5)==4) {
        	scanEvery20Startups.setChecked(true);
        } else if (sharedPreferences.getInt(SCAN_FREQUENCY, 5)==5) {
        	scanManually.setChecked(true);
        }
        
        radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch(checkedId) {
				case R.id.scan_every_startup:
					sharedPreferences.edit().putInt(SCAN_FREQUENCY, 0).commit();
					break;
				case R.id.scan_every_3_startups:
					sharedPreferences.edit().putInt(SCAN_FREQUENCY, 1).commit();
					break;
				case R.id.scan_every_5_startups:
					sharedPreferences.edit().putInt(SCAN_FREQUENCY, 2).commit();
					break;
				case R.id.scan_every_10_startups:
					sharedPreferences.edit().putInt(SCAN_FREQUENCY, 3).commit();
					break;
				case R.id.scan_every_20_startups:
					sharedPreferences.edit().putInt(SCAN_FREQUENCY, 4).commit();
					break;
				case R.id.scan_manually:
					sharedPreferences.edit().putInt(SCAN_FREQUENCY, 5).commit();
					break;
				}
				
			}
        	
        });

        return rootView;
    }
	
}

