package com.jams.music.player.FoldersFragment;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jams.music.player.R;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.Utils.Common;

public class FoldersListViewAdapter extends ArrayAdapter<String> {

	private Context mContext;
	private SharedPreferences sharedPreferences;
	private boolean mFromSaveSongFromCloud = false;
	private List<String> mFileFolderNameList;
	private List<String> mFileFolderTypeList;
	private List<String> mFileFolderSizeList;
	private List<String> mFileFolderPathsList;
   
    public FoldersListViewAdapter(Context context, 
    							  boolean fromSaveSongFromCloud, 
    							  List<String> nameList, 
    							  List<String> fileFolderTypeList, 
    							  List<String> sizeList, 
    							  List<String> fileFolderPathsList) {
    	
    	super(context, -1, nameList);
    	
    	mContext = context;
    	sharedPreferences = mContext.getSharedPreferences("com.jams.music.player", Context.MODE_PRIVATE);
    	mFromSaveSongFromCloud = fromSaveSongFromCloud;
    	mFileFolderNameList = nameList;
    	mFileFolderTypeList = fileFolderTypeList;
    	mFileFolderSizeList = sizeList;
    	mFileFolderPathsList = fileFolderPathsList;
    	
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
    	
    	FoldersViewHolder holder = null;
		if (convertView == null) {
			
			if (mFromSaveSongFromCloud==false) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.folder_view_cards_layout, parent, false);
			} else {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.folder_view_layout, parent, false);
			}

			holder = new FoldersViewHolder();
			holder.fileFolderIcon = (ImageView) convertView.findViewById(R.id.file_folder_icon);
			holder.fileFolderSizeText = (TextView) convertView.findViewById(R.id.file_folder_size);
			holder.fileFolderNameText = (TextView) convertView.findViewById(R.id.file_folder_title);

			holder.fileFolderNameText.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
			holder.fileFolderNameText.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Light"));
			holder.fileFolderNameText.setPaintFlags(holder.fileFolderNameText.getPaintFlags()
											 | Paint.ANTI_ALIAS_FLAG
											 | Paint.SUBPIXEL_TEXT_FLAG);		
			
			holder.fileFolderSizeText.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
			holder.fileFolderSizeText.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
			holder.fileFolderSizeText.setPaintFlags(holder.fileFolderSizeText.getPaintFlags()
											 | Paint.ANTI_ALIAS_FLAG
											 | Paint.SUBPIXEL_TEXT_FLAG);
			
			convertView.setTag(holder);
		} else {
		    holder = (FoldersViewHolder) convertView.getTag();
		}
			
		if (sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME")) {
			convertView.setBackgroundResource(R.drawable.card_light);
		} else if (sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
			convertView.setBackgroundResource(R.drawable.card_dark);
		}
		
		holder.fileFolderNameText.setText(mFileFolderNameList.get(position));
		holder.fileFolderSizeText.setText(mFileFolderSizeList.get(position));
		
		//Set the icon based on whether the item is a folder or a file.	
		//Set the tags on each individual ListView item.
		if (mFileFolderTypeList.get(position).equals("FOLDER")) {
			holder.fileFolderIcon.setImageResource(R.drawable.folder);
			convertView.setTag(R.string.folder_list_item_type, "FOLDER");
			convertView.setTag(R.string.folder_path, mFileFolderPathsList.get(position));
		} else if (mFileFolderTypeList.get(position).equals("AUDIO")) {
			holder.fileFolderIcon.setImageResource(UIElementsHelper.getIcon(mContext, "file_music"));
			convertView.setTag(R.string.folder_list_item_type, "AUDIO");
			convertView.setTag(R.string.folder_path, mFileFolderPathsList.get(position));
		} else if (mFileFolderTypeList.get(position).equals("PICTURE")) {
			holder.fileFolderIcon.setImageResource(UIElementsHelper.getIcon(mContext, "file_image"));
			convertView.setTag(R.string.folder_list_item_type, "PICTURE");
			convertView.setTag(R.string.folder_path, mFileFolderPathsList.get(position));
		} else if (mFileFolderTypeList.get(position).equals("VIDEO")) {
			holder.fileFolderIcon.setImageResource(UIElementsHelper.getIcon(mContext, "file_video"));
			convertView.setTag(R.string.folder_list_item_type, "VIDEO");
			convertView.setTag(R.string.folder_path, mFileFolderPathsList.get(position));
		} else {
			holder.fileFolderIcon.setImageResource(UIElementsHelper.getIcon(mContext, "file_generic"));
			convertView.setTag(R.string.folder_list_item_type, "GENERIC_FILE");
			convertView.setTag(R.string.folder_path, mFileFolderPathsList.get(position));
		}
    	
    	return convertView;
	}
    
    static class FoldersViewHolder {
    	public TextView fileFolderNameText;
    	public TextView fileFolderSizeText;
    	public ImageView fileFolderIcon;
    }
   
}
