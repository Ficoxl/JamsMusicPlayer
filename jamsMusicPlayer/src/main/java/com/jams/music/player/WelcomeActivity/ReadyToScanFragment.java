package com.jams.music.player.WelcomeActivity;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jams.music.player.R;
import com.jams.music.player.Helpers.TypefaceHelper;

public class ReadyToScanFragment extends Fragment {
	
	private Context mContext;
	private TextView welcomeHeader;
	private TextView welcomeText1;
	private TextView swipeLeftToContinue;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		mContext = getActivity().getApplication();
		View rootView = (View) getActivity().getLayoutInflater().inflate(R.layout.fragment_welcome_screen_6, null);		
		
		welcomeHeader = (TextView) rootView.findViewById(R.id.welcome_header);
		welcomeHeader.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Light"));
		
		welcomeText1 = (TextView) rootView.findViewById(R.id.welcome_text_1);
		welcomeText1.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));
        
		swipeLeftToContinue = (TextView) rootView.findViewById(R.id.swipe_left_to_continue);
		swipeLeftToContinue.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));

        return rootView;
    }
	
}

