package com.jams.music.player.Drawers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Paint;
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

import com.jams.music.player.R;
import com.jams.music.player.AlbumArtistsFragment.AlbumArtistsFragment;
import com.jams.music.player.AlbumsFragment.AlbumsFragment;
import com.jams.music.player.ArtistsFragment.ArtistsFragment;
import com.jams.music.player.FoldersFragment.FilesFoldersFragment;
import com.jams.music.player.GenresFragment.GenresFragment;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.MainActivity.MainActivity;
import com.jams.music.player.PlaylistsFragment.PlaylistsFragment;
import com.jams.music.player.SongsFragment.SongsFragment;
import com.jams.music.player.Utils.Common;

public class NavigationDrawerFragment extends Fragment {
	
	private Context mContext;
	private Common mApp;
	
	private ListView browsersListView;
	private ListView librariesListView;
	private TextView browsersHeaderText;
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
		if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME") || 
			mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_THEME").equals("LIGHT_THEME")) {
			rootView.setBackgroundColor(0xFFFFFFFF);
		} else {
			rootView.setBackgroundColor(0xFF191919);
		}
		
		browsersListView = (ListView) rootView.findViewById(R.id.browsers_list_view);
		librariesListView = (ListView) rootView.findViewById(R.id.libraries_list_view);
		browsersHeaderText = (TextView) rootView.findViewById(R.id.browsers_header_text);
		librariesHeaderText = (TextView) rootView.findViewById(R.id.libraries_header_text);
		
		//Set the header text fonts/colors.
		browsersHeaderText.setTypeface(TypefaceHelper.getTypeface(getActivity(), "RobotoCondensed-Bold"));
		librariesHeaderText.setTypeface(TypefaceHelper.getTypeface(getActivity(), "RobotoCondensed-Bold"));
		
		browsersHeaderText.setPaintFlags(browsersHeaderText.getPaintFlags() 
										 | Paint.ANTI_ALIAS_FLAG 
										 | Paint.FAKE_BOLD_TEXT_FLAG 
										 | Paint.SUBPIXEL_TEXT_FLAG);
		
		librariesHeaderText.setPaintFlags(librariesHeaderText.getPaintFlags() 
										  | Paint.ANTI_ALIAS_FLAG 
										  | Paint.FAKE_BOLD_TEXT_FLAG 
										  | Paint.SUBPIXEL_TEXT_FLAG);
		
		//Apply the Browser ListView's adapter.
		List<String> titles = Arrays.asList(getActivity().getResources().getStringArray(R.array.sliding_menu_array));
		mBrowsersAdapter = new NavigationDrawerAdapter(getActivity(), new ArrayList<String>(titles));
		browsersListView.setAdapter(mBrowsersAdapter);
		browsersListView.setOnItemClickListener(browsersClickListener);
		setListViewHeightBasedOnChildren(browsersListView);
		
		//Apply the Libraries ListView's adapter.
		if (mApp.getSharedPreferences().getBoolean("BUILDING_LIBRARY", false)==false) {
			cursor = mApp.getDBAccessHelper().getAllUniqueLibraries();
			mLibrariesAdapter = new NavigationDrawerLibrariesAdapter(getActivity(), cursor);
			librariesListView.setAdapter(mLibrariesAdapter);
			librariesListView.setOnItemClickListener(librariesClickListener);
			setListViewHeightBasedOnChildren(librariesListView);
			
		}

		return rootView;
	}
	
	private OnItemClickListener librariesClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int position, long dbID) {
			mApp.getSharedPreferences().edit().putString("CURRENT_LIBRARY", (String) view.getTag(R.string.library_name)).commit();
			librariesListView.setAdapter(mLibrariesAdapter);
			librariesListView.invalidate();
			
			//Refresh the activity to reflect the new library.
			String currentLibrary = mApp.getSharedPreferences().getString("CURRENT_LIBRARY", mContext.getResources().getString(R.string.all_libraries));
			currentLibrary = currentLibrary.replace("'", "''");
			
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
