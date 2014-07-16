package com.jams.music.player.Drawers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.R;
import com.jams.music.player.FoldersFragment.FilesFoldersFragment;
import com.jams.music.player.GenresFragment.GenresFragment;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.MainActivity.MainActivity;
import com.jams.music.player.PlaylistsFragment.PlaylistsFragment;
import com.jams.music.player.SettingsActivity.SettingsActivity;
import com.jams.music.player.Utils.Common;

public class NavigationDrawerFragment extends Fragment {
	
	private Context mContext;
	private Common mApp;
	
	private ListView browsersListView;
	private ListView librariesListView;
	private TextView librariesHeaderText;
	
	private Cursor cursor;
	private NavigationDrawerLibrariesAdapter mLibrariesAdapter;
	private NavigationDrawerAdapter mBrowsersAdapter;
	private Handler mHandler;

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		mContext = getActivity();
		mApp = (Common) mContext.getApplicationContext();
		mHandler = new Handler();

		View rootView = inflater.inflate(R.layout.navigation_drawer_layout, null);
		rootView.setBackgroundColor(UIElementsHelper.getBackgroundColor(mContext));

		browsersListView = (ListView) rootView.findViewById(R.id.browsers_list_view);
		librariesListView = (ListView) rootView.findViewById(R.id.libraries_list_view);
		librariesHeaderText = (TextView) rootView.findViewById(R.id.libraries_header_text);
		
		//Set the header text fonts/colors.
		librariesHeaderText.setTypeface(TypefaceHelper.getTypeface(getActivity(), "Roboto-Regular"));
		
		//Apply the Browser ListView's adapter.
		List<String> titles = Arrays.asList(getActivity().getResources().getStringArray(R.array.sliding_menu_array));
		mBrowsersAdapter = new NavigationDrawerAdapter(getActivity(), new ArrayList<String>(titles));
		browsersListView.setAdapter(mBrowsersAdapter);
		browsersListView.setOnItemClickListener(browsersClickListener);
		setListViewHeightBasedOnChildren(browsersListView);
		
		//Apply the Libraries ListView's adapter.
        cursor = mApp.getDBAccessHelper().getAllUniqueLibraries();
        mLibrariesAdapter = new NavigationDrawerLibrariesAdapter(getActivity(), cursor);
        librariesListView.setAdapter(mLibrariesAdapter);
        librariesListView.setOnItemClickListener(librariesClickListener);
        setListViewHeightBasedOnChildren(librariesListView);

        browsersListView.setDividerHeight(0);
        librariesListView.setDividerHeight(0);

/*        //KitKat padding.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int topPadding = Common.getStatusBarHeight(mContext);

            //Calculate navigation bar height.
            int navigationBarHeight = 0;
            int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                navigationBarHeight = getResources().getDimensionPixelSize(resourceId);
            }

            browsersListView.setClipToPadding(false);
            browsersListView.setPadding(0, topPadding, 0, navigationBarHeight);

        }*/

		return rootView;
	}
	
	private OnItemClickListener librariesClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int position, long dbID) {
			mApp.getSharedPreferences().edit().putString("CURRENT_LIBRARY", (String) view.getTag(R.string.library_name)).commit();
			librariesListView.setAdapter(mLibrariesAdapter);
			librariesListView.invalidate();
			
			//Update the fragment.
			((MainActivity) getActivity()).loadFragment(null);
			
			//Reset the ActionBar after 500ms.
			mHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					getActivity().invalidateOptionsMenu();
					
				}
				
			}, 500);
			
		}
		
	};
	
	private OnItemClickListener browsersClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int position, long dbID) {
			switch (position) {
			case 0:
				((MainActivity) getActivity()).setCurrentFragmentId(Common.ARTISTS_FRAGMENT);
				break;
			case 1:
				((MainActivity) getActivity()).setCurrentFragmentId(Common.ALBUM_ARTISTS_FRAGMENT);
				break;
			case 2:
				((MainActivity) getActivity()).setCurrentFragmentId(Common.ALBUMS_FRAGMENT);
				break;
			case 3:
				((MainActivity) getActivity()).setCurrentFragmentId(Common.SONGS_FRAGMENT);
				break;
			case 4:
				((MainActivity) getActivity()).setCurrentFragmentId(Common.PLAYLISTS_FRAGMENT);
				break;
			case 5:
				((MainActivity) getActivity()).setCurrentFragmentId(Common.GENRES_FRAGMENT);
				break;
			case 6:
				((MainActivity) getActivity()).setCurrentFragmentId(Common.FOLDERS_FRAGMENT);
				break;
            case 7:
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                break;
			}
			
			//Update the adapter to reflect the new fragment.
			List<String> titles = Arrays.asList(getActivity().getResources().getStringArray(R.array.sliding_menu_array));
			mBrowsersAdapter = new NavigationDrawerAdapter(getActivity(), new ArrayList<String>(titles));
			browsersListView.setAdapter(mBrowsersAdapter);
			
			//Update the fragment.
			((MainActivity) getActivity()).loadFragment(null);
			
			//Reset the ActionBar after 500ms.
			mHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					getActivity().invalidateOptionsMenu();
					
				}
				
			}, 500);

		}
		
	};
	
	/**
	 * Clips ListViews to fit within the drawer's boundaries.
	 */
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter(); 
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
    
    @Override
    public void onDestroyView() {
    	super.onDestroyView();
    	
    	if (cursor!=null) {
    		cursor.close();
    		cursor = null;
    	}
		
    }
	
}
