package com.jams.music.player.MiscFragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jams.music.player.R;
import com.jams.music.player.AsyncTasks.AsyncBuildLibraryTask.OnBuildLibraryProgressUpdate;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.MainActivity.MainActivity;
import com.jams.music.player.Utils.Common;

public class BuildingLibraryProgressFragment extends Fragment implements OnBuildLibraryProgressUpdate {
	
	private Context mContext;
	private Common mApp;
	private View mRootView;
	private RelativeLayout mProgressElementsContainer;
	private TextView mCurrentTaskText;
	private ProgressBar mProgressBar;
	private Animation mFadeInAnimation;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		mContext = getActivity().getApplicationContext();
		mApp = (Common) mContext;
		mRootView = (View) getActivity().getLayoutInflater().inflate(R.layout.fragment_building_library_progress, null);
		
		mProgressElementsContainer = (RelativeLayout) mRootView.findViewById(R.id.progress_elements_container);
		mProgressElementsContainer.setVisibility(View.INVISIBLE);
		
		mCurrentTaskText = (TextView) mRootView.findViewById(R.id.building_library_task);
		mCurrentTaskText.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Light"));
        mCurrentTaskText.setPaintFlags(mCurrentTaskText.getPaintFlags() 
        						       | Paint.ANTI_ALIAS_FLAG
        						       | Paint.SUBPIXEL_TEXT_FLAG);
        
        mProgressBar = (ProgressBar) mRootView.findViewById(R.id.building_library_progress);
        mProgressBar.setMax(1000000); 
        
        mFadeInAnimation = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
        mFadeInAnimation.setAnimationListener(fadeInListener);
        mFadeInAnimation.setDuration(700);
        
        return mRootView;
    }
	
	/**
	 * Fade in animation listener.
	 */
	private AnimationListener fadeInListener = new AnimationListener() {

		@Override
		public void onAnimationEnd(Animation arg0) {
			mProgressElementsContainer.setVisibility(View.VISIBLE);
			
		}

		@Override
		public void onAnimationRepeat(Animation arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onAnimationStart(Animation arg0) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	@Override
	public void onStartBuildingLibrary() {
		mProgressElementsContainer.startAnimation(mFadeInAnimation);
		
	}

	@Override
	public void onProgressUpdate(String mCurrentTask, int overallProgress,
			int maxProgress) {
		//mCurrentTaskText.setText(mCurrentTask);
		mProgressBar.setProgress(overallProgress);
		
	}

	@Override
	public void onFinishBuildingLibrary() {
		Intent intent = new Intent(mContext, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mContext.startActivity(intent);
		
		mApp.getSharedPreferences().edit().putBoolean("FIRST_RUN", false).commit();
		
	}
	
}

