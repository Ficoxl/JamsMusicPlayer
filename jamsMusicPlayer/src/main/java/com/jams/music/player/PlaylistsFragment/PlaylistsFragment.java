package com.jams.music.player.PlaylistsFragment;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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

import com.andraskindler.quickscroll.QuickScroll;
import com.jams.music.player.R;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.PlaylistsFlippedActivity.PlaylistsFlippedActivity;
import com.jams.music.player.Utils.Common;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

/**
 * PlaylistsFragment. Contained within MainActivity.
 * 
 * @author Saravan Pantham
 */
public class PlaylistsFragment extends Fragment {
	
	private Context mContext;
	private Common mApp;
	private PlaylistsFragment mPlaylistsFragment;
	private Cursor mCursor;
	
	private ListView mListView;
	private QuickScroll mQuickScrollListView;
	private PlaylistsListViewAdapter mListViewAdapter;
	private String mQuerySelection = "";
	public Handler mHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_playlists, container, false);

        mContext = getActivity().getApplicationContext();
        mPlaylistsFragment = this;
        mHandler = new Handler();
        mApp = (Common) mContext;
	    
        //Set the drawer backgrounds based on the theme.
        if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_THEME")) {
        	rootView.setBackgroundColor(0xFF191919);
        } else if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("LIGHT_THEME")) {
        	rootView.setBackgroundColor(0xFFFFFFFF);
        }
	    
        mQuickScrollListView = (QuickScroll) rootView.findViewById(R.id.quickscroll);
	    mListView = (ListView) rootView.findViewById(R.id.playlists_list_view);
        
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

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
				
				Intent intent = new Intent(mContext, PlaylistsFlippedActivity.class);
				if (index==0) {
					//Top 25 Played Songs.
					intent.putExtra("DEFAULT_PLAYLIST_TYPE", "TOP_25_PLAYED_SONGS");
				} else if (index==1) {
					//Top 25 Played Songs.
					intent.putExtra("DEFAULT_PLAYLIST_TYPE", "RECENTLY_ADDED");
				} else if (index==2) {
					//Top 25 Played Songs.
					intent.putExtra("DEFAULT_PLAYLIST_TYPE", "TOP_RATED");
				} else if (index==3) {
					//Top 25 Played Songs.
					intent.putExtra("DEFAULT_PLAYLIST_TYPE", "RECENTLY_PLAYED");
				} else {
					//Not a default playlist.
					intent.putExtra("DEFAULT_PLAYLIST_TYPE", "USER_PLAYLIST");
				}
				
				intent.putExtra("PLAYLIST_NAME", (String) view.getTag(R.string.playlist_name));
				intent.putExtra("PLAYLIST_FILE_PATH", (String) view.getTag(R.string.playlist_file_path));
				intent.putExtra("PLAYLIST_FOLDER_PATH", (String) view.getTag(R.string.playlist_folder_path));
				intent.putExtra("PLAYLIST_SOURCE", (String) view.getTag(R.string.playlist_source));
				intent.putExtra("PLAYLIST_ID", (String) view.getTag(R.string.playlist_id));
				
				startActivity(intent);
				getActivity().overridePendingTransition(R.anim.fade_in, R.anim.scale_and_fade_out);
				
			}
			
		});
		
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
				new AsyncLoadCursor().execute();
				
			}
        	
        }, 250);
		
        return rootView;
    }
    
    public void updatePlaylistsListView() {
/*		//Update the GridView.
    	if (mContext!=null) {
		    if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==false) {
		    	mQuerySelection = " AND " + DBAccessHelper.PLAYLIST_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'";
		    } else {
		    	mQuerySelection = "";
		    }
		    mCursor = mApp.getDBAccessHelper().getMusicLibraryPlaylistsDBHelper().getAllUniquePlaylists(mQuerySelection);
		    mListViewAdapter.changeCursor(mCursor);
	        mListView.setAdapter(mListViewAdapter);
			mListView.invalidate();
    	}*/
    	
    }
    
    @Override
    public void onDestroyView() {
    	super.onDestroyView();
    	
    	if (mListView!=null) {
        	mListView.setOnItemClickListener(null);
        	mListView.setAdapter(null);
        	mListView = null;
    	}
    	
    	if (mCursor!=null) {
    		mCursor.close();
    	}
    	
    	mCursor = null;
    	
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	if (mListView!=null) {
        	mListView.setOnItemClickListener(null);
        	mListView.setAdapter(null);
        	mListView = null;
    	}
    	
    	mContext = null;
    }
    
    class AsyncLoadCursor extends AsyncTask<Boolean, Boolean, Boolean> {

		@Override
		protected Boolean doInBackground(Boolean... arg0) {

/*			if (mApp.getSharedPreferences().getBoolean("GOOGLE_PLAY_MUSIC_ENABLED", false)==false) {
		    	mQuerySelection = " AND " + DBAccessHelper.PLAYLIST_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'";
		    } else {
		    	mQuerySelection = "";
		    }
			
		    mCursor = mApp.getDBAccessHelper().getMusicLibraryPlaylistsDBHelper().getAllUniquePlaylists(mQuerySelection); */
			return null;
		}
    	
		@Override
		public void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			
			TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, 
					  											  Animation.RELATIVE_TO_SELF, 0.0f, 
					  											  Animation.RELATIVE_TO_SELF, 2.0f, 
					  											  Animation.RELATIVE_TO_SELF, 0.0f);

			animation.setDuration(600);
			animation.setInterpolator(new AccelerateDecelerateInterpolator());
			
		    mListViewAdapter = new PlaylistsListViewAdapter(getActivity(), mPlaylistsFragment);
	        mListView.setAdapter(mListViewAdapter);
	        
	        PauseOnScrollListener listener = new PauseOnScrollListener(mApp.getImageLoader(), true, true);
	        mListView.setOnScrollListener(listener);
	        
	        //Init the quick scroll widget.
	        mQuickScrollListView.setVisibility(View.GONE);
	        mQuickScrollListView.init(QuickScroll.TYPE_INDICATOR_WITH_HANDLE, 
	        				 mListView, 
	        				 mListViewAdapter, 
	        				 QuickScroll.STYLE_HOLO);
	        
	        int[] quickScrollColors = UIElementsHelper.getQuickScrollColors(mContext);
	        mQuickScrollListView.setHandlebarColor(quickScrollColors[0], quickScrollColors[0], quickScrollColors[1]);
	        mQuickScrollListView.setIndicatorColor(quickScrollColors[1], quickScrollColors[0], quickScrollColors[2]);
	        mQuickScrollListView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 48);
	        
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

	public PlaylistsListViewAdapter getListViewAdapter() {
		return mListViewAdapter;
	}

	public String getQuerySelection() {
		return mQuerySelection;
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

	public void setListViewAdapter(PlaylistsListViewAdapter listViewAdapter) {
		this.mListViewAdapter = listViewAdapter;
	}

	public void setQuerySelection(String querySelection) {
		this.mQuerySelection = querySelection;
	}
	
}
