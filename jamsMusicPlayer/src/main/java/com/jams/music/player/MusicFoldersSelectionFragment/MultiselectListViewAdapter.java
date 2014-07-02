package com.jams.music.player.MusicFoldersSelectionFragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.jams.music.player.R;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.Utils.Common;

public class MultiselectListViewAdapter extends ArrayAdapter<String> {

	private Context mContext;
	private Common mApp;
	private MusicFoldersSelectionFragment mFragment;
	private boolean mDirChecked;
	private boolean mWelcomeSetup;
   
    public MultiselectListViewAdapter(Context context, 
    								  MusicFoldersSelectionFragment fragment, 
    								  boolean welcomeSetup,
    								  boolean dirChecked) {
    	
    	super(context, R.id.file_folder_title, fragment.getFileFolderNamesList());
    	
    	mContext = context;
    	mApp = (Common) mContext.getApplicationContext();
    	mFragment = fragment;
    	mDirChecked = dirChecked; //Indicates if this entire dir is a music folder.
    	mWelcomeSetup = welcomeSetup;
    	
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
		FoldersMultiselectHolder holder = null;

		if (convertView==null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.folder_view_layout_multiselect, parent, false);

			holder = new FoldersMultiselectHolder();
			holder.fileFolderNameText = (TextView) convertView.findViewById(R.id.file_folder_title_multiselect);
			holder.fileFoldersCheckbox = (CheckBox) convertView.findViewById(R.id.music_folder_select_checkbox);
			holder.fileFoldersImage = (ImageView) convertView.findViewById(R.id.file_folder_icon);
			holder.fileFolderSizeText = (TextView) convertView.findViewById(R.id.file_folder_size_multiselect);

			//Apply the card layout's background based on the color theme.
			if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME") ||
				mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("LIGHT_THEME") || 
				mWelcomeSetup==true) {
				convertView.setBackgroundResource(R.drawable.card_light);
				holder.fileFolderNameText.setTextColor(Color.parseColor("#2F2F2F"));
			} else if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME") ||
					   mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_THEME")) {
				convertView.setBackgroundResource(R.drawable.card_dark);
				holder.fileFolderNameText.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
			}
			
			holder.fileFolderNameText.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Light"));
			holder.fileFolderNameText.setPaintFlags(holder.fileFolderNameText.getPaintFlags()
											 		| Paint.ANTI_ALIAS_FLAG
											 		| Paint.SUBPIXEL_TEXT_FLAG);		
			
			holder.fileFolderSizeText.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Regular"));
			holder.fileFolderSizeText.setPaintFlags(holder.fileFolderSizeText.getPaintFlags()
											 		| Paint.ANTI_ALIAS_FLAG
											 		| Paint.SUBPIXEL_TEXT_FLAG);
			
			holder.fileFoldersImage.setImageResource(R.drawable.folder);
			convertView.setTag(holder);
			
		} else {
		    holder = (FoldersMultiselectHolder) convertView.getTag();
		}
		
		try {
			holder.fileFolderNameText.setText(mFragment.getFileFolderNamesList().get(position));
			holder.fileFolderSizeText.setText(mFragment.getFileFolderSizesList().get(position));
			
			//Set the corresponding path of the checkbox as it's tag.
			holder.fileFoldersCheckbox.setTag(mFragment.getFileFolderPathsList().get(position));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Set the checkbox status.
		String folderPath = mFragment.getFileFolderPathsList().get(position);
		if (mDirChecked) {
			holder.fileFoldersCheckbox.setChecked(true);
			if (mFragment.getMusicFoldersHashMap().get(folderPath)!=null && 
				mFragment.getMusicFoldersHashMap().get(folderPath)==false) {
				holder.fileFoldersCheckbox.setChecked(false);
			}
			
		} else {
			holder.fileFoldersCheckbox.setChecked(false);
			if (mFragment.getMusicFoldersHashMap().get(folderPath)!=null && 
				mFragment.getMusicFoldersHashMap().get(folderPath)==true) {
				holder.fileFoldersCheckbox.setChecked(true);
			}
			
		}
		
		holder.fileFoldersCheckbox.setOnCheckedChangeListener(checkChangeListener);
		return convertView;
	}
    
    /**
	 * Checkbox status listener.
     */
    private OnCheckedChangeListener checkChangeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton checkBox, boolean isChecked) {
			
			//Only respond to user presses.
			if (checkBox.isPressed()) {
				String filePath = (String) checkBox.getTag();
				mFragment.getMusicFoldersHashMap().put(filePath, isChecked);
				
			}
			
		}
    	
    };
    
    static class FoldersMultiselectHolder {
		public TextView fileFolderNameText;
		public TextView fileFolderSizeText;
		public CheckBox fileFoldersCheckbox;
		public ImageView fileFoldersImage;
    }
   
}
