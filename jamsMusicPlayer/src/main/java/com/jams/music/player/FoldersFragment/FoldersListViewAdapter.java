package com.jams.music.player.FoldersFragment;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jams.music.player.R;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.Utils.Common;

public class FoldersListViewAdapter extends ArrayAdapter<String> {

	private Context mContext;
    private Common mApp;
	private List<String> mFileFolderNameList;
	private List<Integer> mFileFolderTypeList;
	private List<String> mFileFolderSizeList;
	private List<String> mFileFolderPathsList;
   
    public FoldersListViewAdapter(Context context,
    							  List<String> nameList, 
    							  List<Integer> fileFolderTypeList,
    							  List<String> sizeList, 
    							  List<String> fileFolderPathsList) {
    	
    	super(context, -1, nameList);
    	
    	mContext = context;
        mApp = (Common) mContext.getApplicationContext();
    	mFileFolderNameList = nameList;
    	mFileFolderTypeList = fileFolderTypeList;
    	mFileFolderSizeList = sizeList;
    	mFileFolderPathsList = fileFolderPathsList;
    	
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
    	
    	FoldersViewHolder holder = null;
		if (convertView == null) {

            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_view_item, parent, false);
            ListView.LayoutParams params = (ListView.LayoutParams) convertView.getLayoutParams();
            params.height = (int) mApp.convertDpToPixels(72.0f, mContext);
            convertView.setLayoutParams(params);

			holder = new FoldersViewHolder();
			holder.fileFolderIcon = (ImageView) convertView.findViewById(R.id.listViewLeftIcon);
			holder.fileFolderSizeText = (TextView) convertView.findViewById(R.id.listViewSubText);
			holder.fileFolderNameText = (TextView) convertView.findViewById(R.id.listViewTitleText);
            holder.overflowButton = (ImageButton) convertView.findViewById(R.id.listViewOverflow);
            holder.rightSubText = (TextView) convertView.findViewById(R.id.listViewRightSubText);

            holder.fileFolderIcon.setScaleX(0.5f);
            holder.fileFolderIcon.setScaleY(0.55f);
            holder.rightSubText.setVisibility(View.INVISIBLE);

			holder.fileFolderNameText.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
			holder.fileFolderNameText.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));
			
			holder.fileFolderSizeText.setTextColor(UIElementsHelper.getSmallTextColor(mContext));
			holder.fileFolderSizeText.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));

            holder.overflowButton.setImageResource(UIElementsHelper.getIcon(mContext, "ic_action_overflow"));
            holder.overflowButton.setFocusable(false);
            holder.overflowButton.setFocusableInTouchMode(false);
			
			convertView.setTag(holder);
		} else {
		    holder = (FoldersViewHolder) convertView.getTag();
		}
		
		holder.fileFolderNameText.setText(mFileFolderNameList.get(position));
		holder.fileFolderSizeText.setText(mFileFolderSizeList.get(position));
		
		//Set the icon based on whether the item is a folder or a file.
		if (mFileFolderTypeList.get(position)==FilesFoldersFragment.FOLDER) {
			holder.fileFolderIcon.setImageResource(R.drawable.icon_folderblue);
			convertView.setTag(R.string.folder_list_item_type, FilesFoldersFragment.FOLDER);
			convertView.setTag(R.string.folder_path, mFileFolderPathsList.get(position));

		} else if (mFileFolderTypeList.get(position)==FilesFoldersFragment.AUDIO_FILE) {
			holder.fileFolderIcon.setImageResource(R.drawable.icon_mp3);
			convertView.setTag(R.string.folder_list_item_type, FilesFoldersFragment.AUDIO_FILE);
			convertView.setTag(R.string.folder_path, mFileFolderPathsList.get(position));

		} else if (mFileFolderTypeList.get(position)==FilesFoldersFragment.PICTURE_FILE) {
			holder.fileFolderIcon.setImageResource(R.drawable.icon_png);
			convertView.setTag(R.string.folder_list_item_type, FilesFoldersFragment.PICTURE_FILE);
			convertView.setTag(R.string.folder_path, mFileFolderPathsList.get(position));

		} else if (mFileFolderTypeList.get(position)==FilesFoldersFragment.VIDEO_FILE) {
			holder.fileFolderIcon.setImageResource(R.drawable.icon_avi);
			convertView.setTag(R.string.folder_list_item_type, FilesFoldersFragment.VIDEO_FILE);
			convertView.setTag(R.string.folder_path, mFileFolderPathsList.get(position));

		} else {
			holder.fileFolderIcon.setImageResource(R.drawable.icon_default);
			convertView.setTag(R.string.folder_list_item_type, FilesFoldersFragment.FILE);
			convertView.setTag(R.string.folder_path, mFileFolderPathsList.get(position));

		}
    	
    	return convertView;
	}
    
    static class FoldersViewHolder {
    	public TextView fileFolderNameText;
    	public TextView fileFolderSizeText;
    	public ImageView fileFolderIcon;
        public ImageButton overflowButton;
        public TextView rightSubText;
    }
   
}
