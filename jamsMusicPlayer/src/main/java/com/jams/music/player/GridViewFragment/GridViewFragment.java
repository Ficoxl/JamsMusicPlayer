package com.jams.music.player.GridViewFragment;

import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andraskindler.quickscroll.QuickScrollGridView;
import com.jams.music.player.Helpers.ImageViewCoordHelper;
import com.jams.music.player.HorizListSubFragment.HorizListSubFragment;
import com.jams.music.player.R;
import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.Utils.Common;

/**
 * Generic, multipurpose GridView fragment.
 * 
 * @author Saravan Pantham
 */
public class GridViewFragment extends Fragment {
	
	private Context mContext;
	private GridViewFragment mFragment;
	private Common mApp;
	private View mRootView;
	private int mFragmentId;
	
	private QuickScrollGridView mQuickScroll;
	private BaseAdapter mGridViewAdapter;
	private HashMap<Integer, String> mDBColumnsMap;
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
        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_grid_view, container, false);
        mContext = getActivity().getApplicationContext();
	    mApp = (Common) mContext;
        mFragment = this;
        
        //Grab the fragment. This will determine which data to load into the cursor.
        mFragmentId = getArguments().getInt(Common.FRAGMENT_ID);
        mDBColumnsMap = new HashMap<Integer, String>();
        
	    //Init the search fields.
	    mSearchLayout = (RelativeLayout) mRootView.findViewById(R.id.search_layout);
	    mSearchEditText = (EditText) mRootView.findViewById(R.id.search_field);
	    
	    mSearchEditText.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Regular"));
	    mSearchEditText.setPaintFlags(mSearchEditText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
	    mSearchEditText.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
	    mSearchEditText.setFocusable(true);
	    mSearchEditText.setCursorVisible(true);
	    
        mQuickScroll = (QuickScrollGridView) mRootView.findViewById(R.id.quickscrollgrid);

		    //Set the adapter for the outer gridview.
        mGridView = (GridView) mRootView.findViewById(R.id.generalGridView);
        mGridView.setVerticalScrollBarEnabled(false);
        
		//Limit the number of gridview columns to two if we're dealing with a cards theme. Also set the background color based on the theme.
		if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME")) {
			
			//Set the number of gridview columns based on the orientation.
			if (mApp.getOrientation()==Common.ORIENTATION_PORTRAIT) {
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
			if (mApp.getOrientation()==Common.ORIENTATION_PORTRAIT) {
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
			if (mApp.getOrientation()==Common.ORIENTATION_PORTRAIT) {
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
            mQuickScroll.setPadding(0, topPadding, 0, navigationBarHeight);
            
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mSearchLayout.getLayoutParams();
            layoutParams.setMargins(15, topPadding + 15, 15, 0);
            mSearchLayout.setLayoutParams(layoutParams);
            
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
				    mFragment.getActivity()
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

            //Grab a handle on the album art ImageView within the grid.
            ImageView albumArt = (ImageView) view.findViewById(R.id.artistsGridViewImage);

            //Grab the album art image data.
            int[] screenLocation = new int[2];
            albumArt.getLocationOnScreen(screenLocation);
            String albumArtPath = (String) view.getTag(R.string.album_art);
            Bitmap thumbnail = ((BitmapDrawable) albumArt.getDrawable()).getBitmap();
            ImageViewCoordHelper info = new ImageViewCoordHelper(albumArtPath, thumbnail);

            //Pass on the album art view info to the new fragment.
            Bundle bundle = new Bundle();
            int orientation = getResources().getConfiguration().orientation;

            bundle.putInt("orientation", orientation);
            bundle.putString("albumArtPath", info.mAlbumArtPath);
            bundle.putInt("left", screenLocation[0]);
            bundle.putInt("top", screenLocation[1]);
            bundle.putInt("width", albumArt.getWidth());
            bundle.putInt("height", albumArt.getHeight());

            //Fire up the new fragment.
            HorizListSubFragment fragment = new HorizListSubFragment();
            fragment.setArguments(bundle);

            FragmentManager manager = getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(R.id.mainActivityContainer2, fragment, "horizListSubFragment")
                       .addToBackStack("horizListSubFragment")
                       .commit();
			
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
    	mContext = null;
    	mHandler = null;
    	
    }
    
    /**
     * Runs the correct DB query based on the passed in fragment id and 
     * displays the GridView.
     * 
     * @author Saravan Pantham
     */
    public class AsyncRunQuery extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
	        mCursor = mApp.getDBAccessHelper().getFragmentCursor(mContext, mQuerySelection, mFragmentId);
	        loadDBColumnNames();
	        
	        return null;
		}
		
		/**
		 * Populates the DB column names based on the specifed fragment id.
		 */
		private void loadDBColumnNames() {
			
			switch (mFragmentId) {
			case Common.ARTISTS_FRAGMENT:
				mDBColumnsMap.put(GridViewCardsAdapter.TITLE_TEXT, DBAccessHelper.SONG_ARTIST);
				mDBColumnsMap.put(GridViewCardsAdapter.SOURCE, DBAccessHelper.SONG_SOURCE);
				mDBColumnsMap.put(GridViewCardsAdapter.FILE_PATH, DBAccessHelper.SONG_FILE_PATH);
				mDBColumnsMap.put(GridViewCardsAdapter.ARTWORK_PATH, DBAccessHelper.SONG_ALBUM_ART_PATH);
				break;
			case Common.ALBUM_ARTISTS_FRAGMENT:
				mDBColumnsMap.put(GridViewCardsAdapter.TITLE_TEXT, DBAccessHelper.SONG_ALBUM_ARTIST);
				mDBColumnsMap.put(GridViewCardsAdapter.SOURCE, DBAccessHelper.SONG_SOURCE);
				mDBColumnsMap.put(GridViewCardsAdapter.FILE_PATH, DBAccessHelper.SONG_FILE_PATH);
				mDBColumnsMap.put(GridViewCardsAdapter.ARTWORK_PATH, DBAccessHelper.SONG_ALBUM_ART_PATH);
				break;
			case Common.ALBUMS_FRAGMENT:
				mDBColumnsMap.put(GridViewCardsAdapter.TITLE_TEXT, DBAccessHelper.SONG_ALBUM);
				mDBColumnsMap.put(GridViewCardsAdapter.SOURCE, DBAccessHelper.SONG_SOURCE);
				mDBColumnsMap.put(GridViewCardsAdapter.FILE_PATH, DBAccessHelper.SONG_FILE_PATH);
				mDBColumnsMap.put(GridViewCardsAdapter.ARTWORK_PATH, DBAccessHelper.SONG_ALBUM_ART_PATH);
				break;
			case Common.PLAYLISTS_FRAGMENT:
				break;
			case Common.GENRES_FRAGMENT:
				break;
			case Common.FOLDERS_FRAGMENT:
				break;
			}
			
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
			
        	mGridViewAdapter = new GridViewCardsAdapter(mContext, mFragment, 
        										  		mApp.getOrientation()==Common.ORIENTATION_LANDSCAPE, 
        										  		mDBColumnsMap);
        	
	        mGridView.setAdapter(mGridViewAdapter);
	        mGridView.setOnItemClickListener(onItemClickListener);
	        
	       /* SwingBottomInAnimationAdapter animationAdapter = new SwingBottomInAnimationAdapter(mGridViewAdapter);
	        animationAdapter.setShouldAnimate(true);
	        animationAdapter.setShouldAnimateFromPosition(0);
	        animationAdapter.setAbsListView(mGridView);
	        mGridView.setAdapter(animationAdapter);*/

	        //Init the quick scroll widget.
	        mQuickScroll.init(QuickScrollGridView.TYPE_INDICATOR_WITH_HANDLE, 
	        				 	 	  mGridView, 
	        				 	 	  (GridViewCardsAdapter) mGridViewAdapter, 
	        				 	 	  QuickScrollGridView.STYLE_HOLO);
	        
	        int[] quickScrollColors = UIElementsHelper.getQuickScrollColors(mContext);
	        mQuickScroll.setHandlebarColor(quickScrollColors[0], quickScrollColors[0], quickScrollColors[1]);
	        mQuickScroll.setIndicatorColor(quickScrollColors[1], quickScrollColors[0], quickScrollColors[2]);
	        mQuickScroll.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 48);
	        
	        animation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationEnd(Animation arg0) {
					mQuickScroll.setVisibility(View.VISIBLE);
					
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
			
		}
		
    }

    /*
     * Getter methods.
     */

	public GridViewCardsAdapter getGridViewAdapter() {
		return (GridViewCardsAdapter) mGridViewAdapter;
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
