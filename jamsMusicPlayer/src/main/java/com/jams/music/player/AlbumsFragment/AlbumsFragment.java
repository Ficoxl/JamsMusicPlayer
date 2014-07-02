package com.jams.music.player.AlbumsFragment;

import android.app.Activity;
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
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andraskindler.quickscroll.QuickScroll;
import com.andraskindler.quickscroll.QuickScrollGridView;
import com.jams.music.player.R;
import com.jams.music.player.AlbumsFlippedActivity.AlbumsFlippedActivity;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.Utils.Common;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;

/**
 * AlbumsFragment. Contained within MainActivity.
 * 
 * @author Saravan Pantham
 */
public class AlbumsFragment extends Fragment {
	
	private Context mContext;
	private AlbumsFragment mAlbumsFragment;
	private Common mApp;
	private View mRootView;
	private Cursor mCursor;
	
	private int mSelectedLayout;
	private boolean mLandscape = false;
	
	private GridView mGridView;
	private ListView mListView;
	private AlbumsGridViewAdapter mGridViewAdapter;
	private AlbumsListViewAdapter mListViewAdapter;
	private TextView mEmptyViewText;
	
	private QuickScrollGridView mQuickScrollGridView;
	private QuickScroll mQuickScrollListView;
	
	private int mContextMenuItemIndex;
	public Handler mHandler = new Handler();
	private String mQuerySelection = "";
	private boolean DONE_EDITING_TAGS = false;
	private String mHeaderImagePath = null;
	private int index;
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_albums, container, false);
        mContext = getActivity().getApplicationContext();
        mApp = (Common) mContext;
        mAlbumsFragment = this;
        mSelectedLayout = mApp.getSharedPreferences().getInt("ALBUMS_LAYOUT_PREF", 0);
        
	    //Set the orientation.
        if (mApp.getOrientation()==Common.ORIENTATION_LANDSCAPE) {
        	mLandscape = true;
        } else {
        	mLandscape = false;
        }
        
        //Initialize the layout based on the user's mQuerySelection.
        mQuickScrollGridView = (QuickScrollGridView) mRootView.findViewById(R.id.quickscrollgrid);
        mQuickScrollListView = (QuickScroll) mRootView.findViewById(R.id.quickscroll);
	    if (mSelectedLayout==0) {
		    //Set the adapter for the outer gridview.
	        mGridView = (GridView) mRootView.findViewById(R.id.albumsGridView);
	        mGridView.setFastScrollEnabled(true);

			//Limit the number of gridview columns to two if we're dealing with a cards theme. Also set the background color based on the theme.
			if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME")) {
				//Set the number of gridview columns based on the orientation.
				if (mLandscape==false) {
					mGridView.setNumColumns(2);
				} else {
					mGridView.setNumColumns(4);
				}
				
				mRootView.setBackgroundColor(0xFFEEEEEE);
				RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				layoutParams.setMargins(10, 10, 10, 10);
				mGridView.setLayoutParams(layoutParams);
			} else if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
				//Set the number of gridview columns based on the orientation.
				if (mLandscape==false) {
					mGridView.setNumColumns(2);
				} else {
					mGridView.setNumColumns(4);
				}
				
				mRootView.setBackgroundColor(0xFF111111);
				RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				layoutParams.setMargins(10, 10, 10, 10);
				mGridView.setLayoutParams(layoutParams);
			} else {
				//Set the number of gridview columns based on the orientation.
				if (mLandscape==false) {
					mGridView.setNumColumns(2);
				} else {
					mGridView.setNumColumns(4);
				}
				
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
	            
	            mGridView.setClipToPadding(false);
	            mGridView.setPadding(0, topPadding, 0, navigationBarHeight);
	            mQuickScrollGridView.setPadding(0, topPadding, 0, navigationBarHeight);
	        }

	    } else {
		    //Set the adapter for the outer gridview.
	        mListView = (ListView) mRootView.findViewById(R.id.albumsListView);
	        mListView.setFastScrollEnabled(true);

			//Set the background color based on the theme.
			if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME")) {
				mRootView.setBackgroundColor(0xFFEEEEEE);
				mListView.setDivider(getResources().getDrawable(R.drawable.transparent_drawable));
				mListView.setDividerHeight(10);
				RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				layoutParams.setMargins(20, 20, 20, 20);
				mListView.setLayoutParams(layoutParams);
			} else if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
				mRootView.setBackgroundColor(0xFF000000);
				mListView.setDivider(getResources().getDrawable(R.drawable.transparent_drawable));
				mListView.setDividerHeight(10);
				RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
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
	        
	    }
        
        mEmptyViewText = (TextView) mRootView.findViewById(R.id.empty_view_text);
        mEmptyViewText.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
	    mEmptyViewText.setPaintFlags(mEmptyViewText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
	    
        mHandler.postDelayed(queryRunnable, 300);
        return mRootView;
    }
    
    /**
     * Runnable that handles the query.
     */
    public Runnable queryRunnable = new Runnable() {

		@Override
		public void run() {
			new AsyncRunQuery().execute();
			
		}
    	
    };
    
    /**
     * Item click listener for GridView/ListView.
     */
    private OnItemClickListener onItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
			
			//Get the album+artist pair from the child view.
			String albumName = (String) view.getTag(R.string.album);
			String artistName = (String) view.getTag(R.string.artist);
		    String songSource = (String) view.getTag(R.string.song_source);
		    String albumArtPath = (String) view.getTag(R.string.album_art);
		    
		    Intent intent = new Intent(mContext, AlbumsFlippedActivity.class);
		    intent.putExtra("ALBUM_NAME", albumName);
		    intent.putExtra("ARTIST_NAME", artistName);
		    intent.putExtra("ART_SOURCE", songSource);
		    intent.putExtra("HEADER_IMAGE_PATH", albumArtPath);
		    
		    startActivity(intent);
		    getActivity().overridePendingTransition(R.anim.fade_in, R.anim.scale_and_fade_out);
		   
		}
    	
    };
    
    private Runnable reloadCursorLoaderRunnable = new Runnable() {

		@Override
		public void run() {
			
			if (DONE_EDITING_TAGS==true) {
				mGridViewAdapter.notifyDataSetChanged();
				mGridView.invalidate();
				mGridView.setAdapter(mGridViewAdapter);
				
				//Rest the flag and keep listening for any further updates.
				DONE_EDITING_TAGS = false;
				mHandler.postDelayed(reloadCursorLoaderRunnable, 100);

			} else {
				mHandler.postDelayed(reloadCursorLoaderRunnable, 100);
			}
			
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
    	
    	if (mGridView!=null) {
        	mGridView = null;
        	mGridViewAdapter = null;
    	}
    	
    	mListView = null;
    	onItemClickListener = null;
    	mContext = null;
    	
    }

    public class AsyncRunQuery extends AsyncTask<Boolean, Boolean, Boolean> {

		@Override
		protected Boolean doInBackground(Boolean... params) {
			mCursor = mApp.getDBAccessHelper().getFragmentCursor(mContext, mQuerySelection, Common.ALBUMS_FRAGMENT);
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
			
	        if (mSelectedLayout==0) {
	        	mGridViewAdapter = new AlbumsGridViewAdapter(mContext, mAlbumsFragment, mLandscape);
		        mGridView.setAdapter(mGridViewAdapter);

		        mGridView.setOnItemClickListener(onItemClickListener);
		        
		        //Init the quick scroll widget.
		        mQuickScrollListView.setVisibility(View.GONE);
		        mQuickScrollGridView.init(QuickScrollGridView.TYPE_INDICATOR_WITH_HANDLE, 
		        				 	 mGridView, 
		        				 	 mGridViewAdapter, 
		        				 	 QuickScrollGridView.STYLE_HOLO);
		        
		        int[] quickScrollColors = UIElementsHelper.getQuickScrollColors(mContext);
		        mQuickScrollGridView.setHandlebarColor(quickScrollColors[0], quickScrollColors[0], quickScrollColors[1]);
		        mQuickScrollGridView.setIndicatorColor(quickScrollColors[1], quickScrollColors[0], quickScrollColors[2]);
		        mQuickScrollGridView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 48);
		        
		        animation.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationEnd(Animation arg0) {
						mQuickScrollGridView.setVisibility(View.VISIBLE);
						
					}

					@Override
					public void onAnimationRepeat(Animation arg0) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onAnimationStart(Animation arg0) {
						mGridView.setVisibility(View.VISIBLE);
						
					}
		        	
		        });
		        
		        mGridView.startAnimation(animation);
		        
	        } else {
	        	mListViewAdapter = new AlbumsListViewAdapter(mContext, mAlbumsFragment);
		        mListView.setAdapter(mListViewAdapter);
		        
		        PauseOnScrollListener listener = new PauseOnScrollListener(mApp.getImageLoader(), true, true);
		        mListView.setOnScrollListener(listener);
		        mListView.setOnItemClickListener(onItemClickListener);
		        
		        //Init the quick scroll widget.
		        mQuickScrollGridView.setVisibility(View.GONE);
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
    	
    }

    /*
     * Getter methods.
     */
    
	public Cursor getCursor() {
		return mCursor;
	}

	public int getSelectedLayout() {
		return mSelectedLayout;
	}

	public boolean ismLandscape() {
		return mLandscape;
	}

	public GridView getGridView() {
		return mGridView;
	}

	public ListView getListView() {
		return mListView;
	}

	public AlbumsGridViewAdapter getGridViewAdapter() {
		return mGridViewAdapter;
	}

	public AlbumsListViewAdapter getListViewAdapter() {
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

	public void setLandscape(boolean landscape) {
		this.mLandscape = landscape;
	}

	public void setAlbumsGridViewAdapter(AlbumsGridViewAdapter gridViewAdapter) {
		this.mGridViewAdapter = gridViewAdapter;
	}

	public void setListViewAdapter(AlbumsListViewAdapter listViewAdapter) {
		this.mListViewAdapter = listViewAdapter;
	}

	public void setQuerySelection(String querySelection) {
		this.mQuerySelection = querySelection;
	}

}
