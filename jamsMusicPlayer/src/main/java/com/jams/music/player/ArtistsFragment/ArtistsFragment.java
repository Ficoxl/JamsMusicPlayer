package com.jams.music.player.ArtistsFragment;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
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
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andraskindler.quickscroll.QuickScroll;
import com.andraskindler.quickscroll.QuickScrollGridView;
import com.jams.music.player.R;
import com.jams.music.player.ArtistsFlippedActivity.ArtistsFlippedActivity;
import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.Utils.Common;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;

/**
 * ArtistsFragment. Contained within MainActivity.
 * 
 * @author Saravan Pantham
 */
public class ArtistsFragment extends Fragment {
	
	private Context mContext;
	private ArtistsFragment mArtistsFragment;
	private Common mApp;
	private View mRootView;
	
	private QuickScrollGridView mQuickScrollGridView;
	private QuickScroll mQuickScrollListView;
	private boolean mLandscape = false;
	private int mSelectedLayout;
	
	private ArtistsGridViewAdapter mGridViewAdapter;
	private ArtistsListViewAdapter mListViewAdapter;
	private GridView mGridView;
	private ListView mListView;
	private TextView mEmptyTextView;
	
	private RelativeLayout mSearchLayout;
	private EditText mSearchEditText;
	
	public Handler mHandler = new Handler();
	private Cursor mCursor;
	private String mQuerySelection = "";
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_artists, container, false);
        mContext = getActivity().getApplicationContext();
	    mApp = (Common) mContext;
        mArtistsFragment = this;
        
	    mSelectedLayout = mApp.getSharedPreferences().getInt("ARTISTS_LAYOUT_PREF", 0);
	    
	    //Set the orientation.
        if (mApp.getOrientation()==Common.ORIENTATION_LANDSCAPE) {
        	mLandscape = true;
        } else {
        	mLandscape = false;
        }
        
	    //Init the search fields.
	    mSearchLayout = (RelativeLayout) mRootView.findViewById(R.id.search_layout);
	    mSearchEditText = (EditText) mRootView.findViewById(R.id.search_field);
	    
	    mSearchEditText.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Regular"));
	    mSearchEditText.setPaintFlags(mSearchEditText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
	    mSearchEditText.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
	    mSearchEditText.setFocusable(true);
	    mSearchEditText.setCursorVisible(true);
	    
	    //Initialize the layout based on the user's mQuerySelection.
        mQuickScrollGridView = (QuickScrollGridView) mRootView.findViewById(R.id.quickscrollgrid);
        mQuickScrollListView = (QuickScroll) mRootView.findViewById(R.id.quickscroll);
	    if (mSelectedLayout==0) {
 		    //Set the adapter for the outer gridview.
	        mGridView = (GridView) mRootView.findViewById(R.id.artistsGridView);
	        mGridView.setVerticalScrollBarEnabled(false);
	        
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
					mGridView.setNumColumns(3);
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
	            
	            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mSearchLayout.getLayoutParams();
	            layoutParams.setMargins(15, topPadding + 15, 15, 0);
	            mSearchLayout.setLayoutParams(layoutParams);
	            
	        }
			
	    } else {
		    //Set the adapter for the outer gridview.
	        mListView = (ListView) mRootView.findViewById(R.id.artistsListView);
	        mListView.setVerticalScrollBarEnabled(false);

			//Set the background color based on the theme.
			if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME")) {
				mRootView.setBackgroundColor(0xFFEEEEEE);
				mListView.setDivider(getResources().getDrawable(R.drawable.transparent_drawable));
				mListView.setDividerHeight(10);
				RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				layoutParams.setMargins(20, 20, 20, 0);
				mListView.setLayoutParams(layoutParams);

				RelativeLayout.LayoutParams searchParams = (RelativeLayout.LayoutParams) mSearchLayout.getLayoutParams();
				searchParams.setMargins(20, 20, 20, 0);
				mSearchLayout.setLayoutParams(searchParams);
				
			} else if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
				mRootView.setBackgroundColor(0xFF000000);
				mListView.setDivider(getResources().getDrawable(R.drawable.transparent_drawable));
				mListView.setDividerHeight(10);
				RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				layoutParams.setMargins(20, 20, 20, 0);
				mListView.setLayoutParams(layoutParams);

				RelativeLayout.LayoutParams searchParams = (RelativeLayout.LayoutParams) mSearchLayout.getLayoutParams();
				searchParams.setMargins(20, 20, 20, 0);
				mSearchLayout.setLayoutParams(searchParams);
				
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
	            
	            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mSearchLayout.getLayoutParams();
	            layoutParams.setMargins(20, topPadding + 20, 20, 0);
	            mSearchLayout.setLayoutParams(layoutParams);
	            
	        }
	        
	    }

        //Set the empty views.
        mEmptyTextView = (TextView) mRootView.findViewById(R.id.empty_view_text);
	    mEmptyTextView.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Light"));
	    mEmptyTextView.setPaintFlags(mEmptyTextView.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        
        //Create a set of options to optimize the bitmap memory usage.
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inJustDecodeBounds = false;
        options.inPurgeable = true;
	    
        mHandler.postDelayed(queryRunnable, 250);
        return mRootView;
    }
    
    /**
     * Query runnable.
     */
    public Runnable queryRunnable = new Runnable() {

		@Override
		public void run() {
			new AsyncRunQuery().execute();
			
		}
    	
    };
    
    /**
     * Displays the search field.
     */
    private void showSearch() {
    	mSearchLayout.setVisibility(View.VISIBLE);
    	final TranslateAnimation searchAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, 
    														   		 Animation.RELATIVE_TO_SELF, 0f, 
    														   		 Animation.RELATIVE_TO_SELF, -2f, 
    														   		 Animation.RELATIVE_TO_SELF, 0f);
    	searchAnim.setDuration(500l);
    	searchAnim.setInterpolator(new AccelerateDecelerateInterpolator());
    	
    	final TranslateAnimation gridListAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, 
		   		 													   Animation.RELATIVE_TO_SELF, 0f, 
		   		 													   Animation.RELATIVE_TO_SELF, 0f, 
		   		 													   Animation.RELATIVE_TO_SELF, 2f);

    	gridListAnim.setDuration(500l);
    	gridListAnim.setInterpolator(new LinearInterpolator());
    	
    	gridListAnim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				mGridView.setAdapter(null);
				
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				mSearchLayout.startAnimation(searchAnim);
				mSearchLayout.setVisibility(View.VISIBLE);
				
			}
    		
    	});
    	
    	searchAnim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				if (mSearchEditText.requestFocus()) {
				    mArtistsFragment.getActivity()
				    		.getWindow()
				    		.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
				}
				
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
    		
    	});
    	
    	mGridView.startAnimation(gridListAnim);
    	
    }
    
    /**
     * Item click listener for the GridView/ListView.
     */
    private OnItemClickListener onItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int index, long id) {
		    
			String currentArtist = (String) view.getTag(R.string.artist);
			
			//If the artist has artwork from Google Play Music, use that as header image path.
			String dataURI = "";
			String artSource = (String) view.getTag(R.string.song_source);
			
			if (artSource.equals(DBAccessHelper.GMUSIC)) {
				dataURI = (String) view.getTag(R.string.artist_art_path);
			} else {
				dataURI = (String) view.getTag(R.string.album_art);
			}
			
			Intent intent = new Intent(mContext, ArtistsFlippedActivity.class);
			intent.putExtra("ARTIST_NAME", currentArtist);
			intent.putExtra("HEADER_IMAGE_PATH", dataURI);
			intent.putExtra("ART_SOURCE", artSource);
			
			startActivity(intent);
			getActivity().overridePendingTransition(R.anim.fade_in, R.anim.scale_and_fade_out);
			
		}
    	
    };
    
    @Override
    public void onDestroyView() {
    	super.onDestroyView();
    	mRootView = null;
    	
    	if (mCursor!=null) {
        	mCursor.close();
        	mCursor = null;
    	}
    	
    	onItemClickListener = null;
    	mGridView = null;
    	mListView = null;
    	mGridViewAdapter = null;
    	mListViewAdapter = null;
    	mContext = null;
    	mHandler = null;
    	
    }
    
    public class AsyncRunQuery extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
	        mCursor = mApp.getDBAccessHelper().getFragmentCursor(mContext, mQuerySelection, Common.ARTISTS_FRAGMENT);
	        return null;
		}
    	
		@Override
		public void onPostExecute(Void result) {
			super.onPostExecute(result);
			
			TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, 
					  											  Animation.RELATIVE_TO_SELF, 0.0f, 
					  											  Animation.RELATIVE_TO_SELF, 2.0f, 
					  											  Animation.RELATIVE_TO_SELF, 0.0f);

			animation.setDuration(600);
			animation.setInterpolator(new AccelerateDecelerateInterpolator());
			
			if (mSelectedLayout==0) {
	        	mGridViewAdapter = new ArtistsGridViewAdapter(mContext, mArtistsFragment, mLandscape);
		        mGridView.setAdapter(mGridViewAdapter);
		        
		        mGridView.setOnItemClickListener(onItemClickListener);
		        PauseOnScrollListener listener = new PauseOnScrollListener(mApp.getImageLoader(), true, true);
		        mGridView.setOnScrollListener(listener);
		        
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
	        	mListViewAdapter = new ArtistsListViewAdapter(mContext, mArtistsFragment);
		        mListView.setAdapter(mListViewAdapter);
		        
		        PauseOnScrollListener listener = new PauseOnScrollListener(mApp.getImageLoader(), true, true);
		        mListView.setOnScrollListener(listener);
	        	//mListView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
	        	//mListView.setMultiChoiceModeListener(multiChoiceModeListener);
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
    
	public int getSelectedLayout() {
		return mSelectedLayout;
	}

	public ArtistsGridViewAdapter getGridViewAdapter() {
		return mGridViewAdapter;
	}

	public ArtistsListViewAdapter getListViewAdapter() {
		return mListViewAdapter;
	}

	public GridView getGridView() {
		return mGridView;
	}

	public ListView getListView() {
		return mListView;
	}

	public Cursor getCursor() {
		return mCursor;
	}

	/*
	 * Setter methods.
	 */
	
	public void setCursor(Cursor cursor) {
		this.mCursor = cursor;
	}

}
