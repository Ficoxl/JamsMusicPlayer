package com.jams.music.player.MusicLibraryEditorActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Paint;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jams.music.player.R;
import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.Helpers.TypefaceHelper;

public class EditDeleteMusicLibraryAdapter extends SimpleCursorAdapter {
	
	private Context mContext;
	private SharedPreferences sharedPreferences;
    private LibrariesListViewHolder holder = null;
	
    public EditDeleteMusicLibraryAdapter(Context context, Cursor cursor) {
        super(context, -1, cursor, new String[] {}, new int[] {}, 0);
        mContext = context;
        sharedPreferences = mContext.getSharedPreferences("com.jams.music.player", Context.MODE_PRIVATE);
    }

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
        Cursor c = (Cursor) getItem(position);

		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.sliding_menu_list_layout, parent, false);
			holder = new LibrariesListViewHolder();
			holder.browserIcon = (ImageView) convertView.findViewById(R.id.sliding_menu_list_icon);
			holder.tagColor = (ImageView) convertView.findViewById(R.id.sliding_menu_libraries_icon);
			holder.title = (TextView) convertView.findViewById(R.id.sliding_menu_list_item);
			convertView.setTag(holder);
		} else {
		    holder = (LibrariesListViewHolder) convertView.getTag();
		}
		
		//Retrieve the library's parameters.
		String libraryName = c.getString(c.getColumnIndex(DBAccessHelper.LIBRARY_NAME));
		String libraryColorCode = c.getString(c.getColumnIndex(DBAccessHelper.LIBRARY_TAG));
		
		//Construct the library color tag drawable from the given color code string.
		int colorCodeDrawableID = mContext.getResources().getIdentifier(libraryColorCode, "drawable", mContext.getPackageName());
		
		//Set the tag for this child view. The key is required to be an application-defined key.
		convertView.setTag(R.string.library_name, libraryName);
		convertView.setTag(R.string.library_color_code, libraryColorCode);
		
		//Set the library name.
		holder.title.setText(libraryName);

		holder.title.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
		holder.title.setPaintFlags(holder.title.getPaintFlags() | 
								   Paint.SUBPIXEL_TEXT_FLAG | 
								   Paint.ANTI_ALIAS_FLAG);
		
		holder.tagColor.setVisibility(View.VISIBLE);
		holder.browserIcon.setVisibility(View.INVISIBLE);
		holder.tagColor.setImageResource(colorCodeDrawableID);
        
		return convertView;
	}

	static class LibrariesListViewHolder {
	    public ImageView tagColor;
	    public ImageView browserIcon;
	    public TextView title;
	}
	
}
