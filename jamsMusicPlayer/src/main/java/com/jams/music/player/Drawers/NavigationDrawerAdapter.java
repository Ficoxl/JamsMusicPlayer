package com.jams.music.player.Drawers;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.jams.music.player.R;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.MainActivity.MainActivity;
import com.jams.music.player.Utils.Common;

public class NavigationDrawerAdapter extends ArrayAdapter<String> {

	private Context mContext;
	private SharedPreferences sharedPreferences;
	private ArrayList<String> mTitlesList;
   
    public NavigationDrawerAdapter(Context context, ArrayList<String> titlesList) {
    	super(context, R.layout.sliding_menu_browsers_layout, titlesList);
    	mContext = context;
    	mTitlesList = titlesList;
    	sharedPreferences = mContext.getSharedPreferences("com.jams.music.player", Context.MODE_PRIVATE);
    }
    
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
    	
    	SongsListViewHolder holder = null;
		if (convertView == null) {	
			convertView = LayoutInflater.from(mContext).inflate(R.layout.sliding_menu_browsers_layout, parent, false);
			holder = new SongsListViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.nav_drawer_item_title);
			convertView.setTag(holder);
		} else {
		    holder = (SongsListViewHolder) convertView.getTag();
		}

		holder.title.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Light"));
		holder.title.setPaintFlags(holder.title.getPaintFlags()
								   | Paint.ANTI_ALIAS_FLAG
								   | Paint.SUBPIXEL_TEXT_FLAG);		
		
		holder.title.setText(mTitlesList.get(position));
		holder.title.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
		
		//Highlight the current browser.
		int[] colors = UIElementsHelper.getQuickScrollColors(mContext);
		
		if (MainActivity.mCurrentFragmentId==Common.ARTISTS_FRAGMENT && 
			mTitlesList.get(position).equals(mContext.getResources().getString(R.string.artists))) {
			convertView.setBackgroundColor(colors[0]);
			holder.title.setTextColor(colors[2]);
		} else if (MainActivity.mCurrentFragmentId==Common.ALBUM_ARTISTS_FRAGMENT &&
				   mTitlesList.get(position).equals(mContext.getResources().getString(R.string.album_artists))) {
			convertView.setBackgroundColor(colors[0]);
			holder.title.setTextColor(colors[2]);
		} else if (MainActivity.mCurrentFragmentId==Common.ALBUMS_FRAGMENT &&
				   mTitlesList.get(position).equals(mContext.getResources().getString(R.string.albums))) {
			convertView.setBackgroundColor(colors[0]);
			holder.title.setTextColor(colors[2]);
		} else if (MainActivity.mCurrentFragmentId==Common.SONGS_FRAGMENT &&
				   mTitlesList.get(position).equals(mContext.getResources().getString(R.string.songs))) {
			convertView.setBackgroundColor(colors[0]);
			holder.title.setTextColor(colors[2]);
		} else if (MainActivity.mCurrentFragmentId==Common.PLAYLISTS_FRAGMENT &&
				   mTitlesList.get(position).equals(mContext.getResources().getString(R.string.playlists))) {
			convertView.setBackgroundColor(colors[0]);
			holder.title.setTextColor(colors[2]);
		} else if (MainActivity.mCurrentFragmentId==Common.GENRES_FRAGMENT &&
				   mTitlesList.get(position).equals(mContext.getResources().getString(R.string.genres))) {
			convertView.setBackgroundColor(colors[0]);
			holder.title.setTextColor(colors[2]);
		} else if (MainActivity.mCurrentFragmentId==Common.FOLDERS_FRAGMENT &&
				   mTitlesList.get(position).equals(mContext.getResources().getString(R.string.folders))) {
			convertView.setBackgroundColor(colors[0]);
			holder.title.setTextColor(colors[2]);
		}
		
		return convertView;

	}
    
	static class SongsListViewHolder {
	    public TextView title;
	}
   
}
