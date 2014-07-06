package com.jams.music.player.GenresFragment;

import android.content.Context;
import android.content.Intent;
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
import android.view.ViewGroup.LayoutParams;
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
import com.jams.music.player.GenresFlippedActivity.GenresFlippedActivity;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.Utils.Common;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

/**
 * GenresFragment. Contained within MainActivity.
 * 
 * @author Saravan Pantham
 */
public class GenresFragment extends Fragment {
	
	private Context mContext;
	private GenresFragment mGenresFragment;
	private String mQuerySelection = "";
	private Cursor mCursor;
	private Common mApp;
	
	private int mIndex;
	private View mChildView;
	
	private ListView mListView;
	private QuickScroll mQuickScrollListView;
	private TextView mEmptyTextView;
	private GenresListViewAdapter mListViewAdapter;
	private Handler mHandler;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_genres, container, false);

        mContext = getActivity().getApplicationContext();
        mHandler = new Handler();
        mApp = (Common) mContext;
        mGenresFragment = this;
        
        //Set the drawer backgrounds based on the theme.
        if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_THEME")) {
        	rootView.setBackgroundColor(0xFF191919);
        } else if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("LIGHT_THEME")) {
        	rootView.setBackgroundColor(0xFFFFFFFF);
        }
        
	    //Set the adapter for the outer gridview.
        mListView = (ListView) rootView.findViewById(R.id.genresListView);
        mQuickScrollListView = (QuickScroll) rootView.findViewById(R.id.quickscroll);
        mEmptyTextView = (TextView) rootView.findViewById(R.id.empty_view_text);
        mEmptyTextView.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Light"));
	    mEmptyTextView.setPaintFlags(mEmptyTextView.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
	    
		//Set the background color based on the theme.
		if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME")) {
			rootView.setBackgroundColor(0xFFEEEEEE);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(20, 20, 20, 20);
			mListView.setLayoutParams(layoutParams);
			mListView.setDivider(getResources().getDrawable(R.drawable.transparent_drawable));
			mListView.setDividerHeight(10);
		} else if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
			rootView.setBackgroundColor(0xFF111111);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(20, 20, 20, 20);
			mListView.setLayoutParams(layoutParams);
			mListView.setDivider(getResources().getDrawable(R.drawable.transparent_drawable));
			mListView.setDividerHeight(10);
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
        }

        mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				new AsyncRunQuery().execute();
				
			}
        	
        }, 250);
        
        return rootView;
    }
    
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		
    	if (mListView!=null) {
    		mListView.setOnItemClickListener(null);
    		mListView.setAdapter(null);
    		mListView = null;
    	}
		
    	mContext = null;
	}
	
    class AsyncRunQuery extends AsyncTask<Boolean, Boolean, Boolean> {

    	@Override
    	protected Boolean doInBackground(Boolean... arg0) {
           	mCursor = mApp.getDBAccessHelper().getFragmentCursor(mContext, mQuerySelection, Common.GENRES_FRAGMENT);
    		return null;
    	}
    	
    	@Override
    	public void onPostExecute(Boolean result) {
    		super.onPostExecute(result);

		    mListViewAdapter = new GenresListViewAdapter(getActivity(), mGenresFragment);
	        mListView.setAdapter(mListViewAdapter);
	        
	        PauseOnScrollListener listener = new PauseOnScrollListener(mApp.getImageLoader(), true, true);
	        mListView.setOnScrollListener(listener);
	        
	        mListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
					
					Intent intent = new Intent(mContext, GenresFlippedActivity.class);
					intent.putExtra("GENRE_NAME", (String) view.getTag(R.string.genre));
					intent.putExtra("SONG_SOURCE", (String) view.getTag(R.string.song_source));
					intent.putExtra("SONG_FILE_PATH", (String) view.getTag(R.string.song_file_path));
					intent.putExtra("ALBUM_ART_PATH", (String) view.getTag(R.string.album_art));
					
					startActivity(intent);
					getActivity().overridePendingTransition(R.anim.fade_in, R.anim.scale_and_fade_out);
					
				}
	        	
	        });
    		
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

	public String getQuerySelection() {
		return mQuerySelection;
	}

	public Cursor getCursor() {
		return mCursor;
	}

	public ListView getListView() {
		return mListView;
	}

	public GenresListViewAdapter getListViewAdapter() {
		return mListViewAdapter;
	}
	
	/*
	 * Setter methods.
	 */

	public void setQuerySelection(String querySelection) {
		this.mQuerySelection = querySelection;
	}

	public void setCursor(Cursor cursor) {
		this.mCursor = cursor;
	}

	public void setListView(ListView listView) {
		this.mListView = listView;
	}

	public void setListViewAdapter(GenresListViewAdapter listViewAdapter) {
		this.mListViewAdapter = listViewAdapter;
	}
	
}
