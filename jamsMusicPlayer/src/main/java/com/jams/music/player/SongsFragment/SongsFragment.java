package com.jams.music.player.SongsFragment;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andraskindler.quickscroll.QuickScroll;
import com.jams.music.player.R;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.Utils.Common;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;

/**
 * SongsFragment. Contained within MainActivity.
 * 
 * @author Saravan Pantham
 */
public class SongsFragment extends Fragment {

	private Context mContext;
	private SongsFragment mSongsFragment;
	private ViewGroup mRootView;
	private Cursor mCursor;
	private Common mApp;

	private ListView mListView;
	private QuickScroll mQuickScrollListView;
	private TextView emptyViewText;
	private SongsListViewAdapter mListViewAdapter;
	public Handler mHandler = new Handler();
	private String mQuerySelection = "";
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_songs, container, false);
        
        mContext = getActivity().getApplicationContext();
        mSongsFragment = this;
        mApp = (Common) mContext;
        
        //Set the drawer backgrounds based on the theme.
        if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_THEME")) {
        	mRootView.setBackgroundColor(0xFF191919);
        } else if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("LIGHT_THEME")) {
        	mRootView.setBackgroundColor(0xFFFFFFFF);
        }
        
	    mListView = (ListView) mRootView.findViewById(R.id.songsListView);
	    mQuickScrollListView = (QuickScroll) mRootView.findViewById(R.id.quickscroll);
	    
	    emptyViewText = (TextView) mRootView.findViewById(R.id.empty_view_text);
	    emptyViewText.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
	    emptyViewText.setPaintFlags(emptyViewText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        
		//Set the background color based on the theme.
		if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME")) {
			mRootView.setBackgroundColor(0xFFEEEEEE);
			mListView.setDivider(getResources().getDrawable(R.drawable.transparent_drawable));
			mListView.setDividerHeight(10);
			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mListView.getLayoutParams();
			layoutParams.setMargins(20, 20, 20, 20);
			mListView.setLayoutParams(layoutParams);
		} else if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
			mRootView.setBackgroundColor(0xFF121212);
			mListView.setDivider(getResources().getDrawable(R.drawable.transparent_drawable));
			mListView.setDividerHeight(10);
			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mListView.getLayoutParams();
			layoutParams.setMargins(20, 20, 20, 20);
			mListView.setLayoutParams(layoutParams);
		} else if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_THEME")) {
			mListView.setDivider(getResources().getDrawable(R.drawable.list_divider));
			mListView.setDividerHeight(1);
        } else {
        	mListView.setDivider(getResources().getDrawable(R.drawable.list_divider_light));
        	mListView.setDividerHeight(1);
		}
		
		//KitKat translucent navigation/status bar.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        	int topPadding = Common.getStatusBarHeight(mContext);

            //Calculate navigation bar height.
            int navigationBarHeight = 0;
            int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                navigationBarHeight = getResources().getDimensionPixelSize(resourceId);
            }
            
            mListView.setClipToPadding(false);
            mListView.setPadding(0, topPadding, 0, navigationBarHeight);
            mQuickScrollListView.setPadding(0, topPadding, 0, navigationBarHeight);
        }
        
        mHandler.postDelayed(queryRunnable, 250);
        return mRootView;
        
    }
	
	/**
	 * Query runnable.
	 */
	public Runnable queryRunnable = new Runnable() {

		@Override
		public void run() {
			new AsyncLoadCursor().execute();
			
		}
		
	};
	
	/**
	 * Item click listener for the ListView.
	 */
    private OnItemClickListener onItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
			mApp.getPlaybackKickstarter()
				.initPlayback(mContext, mQuerySelection, Common.SONGS_FRAGMENT, index, true);	
			
		}
    	
    };
    
    //IMPORTANT: Setting mRootView to null is *mandatory*. Not doing this will leak the parent Activity.
    @Override
    public void onDestroyView() {
    	super.onDestroyView();
    	mRootView = null;
    	
    	if (mCursor!=null) {
    		mCursor.close();
        	mCursor = null;
    	}
    	
    	if (mListView!=null) {
    		mListView.setOnItemClickListener(null);
        	mListView.setAdapter(null);
        	mListView = null;
    	}

    	mContext = null;

    }
    
    @Override
    public void onResume() {
    	super.onResume();
        
    }
    
    class AsyncLoadCursor extends AsyncTask<Boolean, Boolean, Boolean> {

    	@Override
    	protected Boolean doInBackground(Boolean... arg0) {
    		mCursor = mApp.getDBAccessHelper().getFragmentCursor(mContext, mQuerySelection, Common.SONGS_FRAGMENT);
    		return null;
    	}
    	
    	@Override
    	public void onPostExecute(Boolean result) {
    		super.onPostExecute(result);

            mListViewAdapter = new SongsListViewAdapter(getActivity(), mSongsFragment);
            mListView.setAdapter(mListViewAdapter);
            mListView.setVerticalScrollBarEnabled(false);
            
            PauseOnScrollListener listener = new PauseOnScrollListener(mApp.getImageLoader(), true, true);
            mListView.setOnScrollListener(listener);
            mListView.setOnItemClickListener(onItemClickListener);
            
            //Init the quick scroll widget.
            mQuickScrollListView.init(QuickScroll.TYPE_INDICATOR_WITH_HANDLE, 
            				 mListView, 
            				 mListViewAdapter, 
            				 QuickScroll.STYLE_HOLO);
            
            int[] quickScrollColors = UIElementsHelper.getQuickScrollColors(mContext);
            mQuickScrollListView.setHandlebarColor(quickScrollColors[0], quickScrollColors[0], quickScrollColors[1]);
            mQuickScrollListView.setIndicatorColor(quickScrollColors[1], quickScrollColors[0], quickScrollColors[2]);
            mQuickScrollListView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 48);
            
            TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, 
					  											  Animation.RELATIVE_TO_SELF, 0.0f, 
					  											  Animation.RELATIVE_TO_SELF, 2.0f, 
					  											  Animation.RELATIVE_TO_SELF, 0.0f);

            animation.setDuration(600);
            animation.setInterpolator(new AccelerateDecelerateInterpolator());
            
	        animation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationEnd(Animation arg0) {
					mQuickScrollListView.setVisibility(View.VISIBLE);
					
				}

				@Override
				public void onAnimationRepeat(Animation arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onAnimationStart(Animation arg0) {
					mListView.setVisibility(View.VISIBLE);
					
				}
	        	
	        });
	        
	        mListView.startAnimation(animation);
    		
    	}
    	
    }
    
    /*
     * Getter methods.
     */

	public Cursor getCursor() {
		return mCursor;
	}

	public ListView getListView() {
		return mListView;
	}

	public SongsListViewAdapter getListViewAdapter() {
		return mListViewAdapter;
	}

	/*
	 * Setter methods.
	 */
	
	public void setCursor(Cursor cursor) {
		this.mCursor = cursor;
	}

	public void setListView(ListView listView) {
		this.mListView = listView;
	}

	public void setListViewAdapter(SongsListViewAdapter listViewAdapter) {
		this.mListViewAdapter = listViewAdapter;
	}
    
}
