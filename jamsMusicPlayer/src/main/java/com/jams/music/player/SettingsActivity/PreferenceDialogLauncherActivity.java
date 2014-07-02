package com.jams.music.player.SettingsActivity;


import main.java.de.psdev.licensesdialog.LicensesDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.jams.music.player.R;
import com.jams.music.player.Dialogs.AddMusicLibraryDialog;
import com.jams.music.player.Dialogs.AlbumArtSourceDialog;
import com.jams.music.player.Dialogs.ApplicationThemeDialog;
import com.jams.music.player.Dialogs.BlacklistedElementsDialog;
import com.jams.music.player.Dialogs.CoverArtStyleDialog;
import com.jams.music.player.Dialogs.CustomizeScreensDialog;
import com.jams.music.player.Dialogs.EditDeleteMusicLibraryDialog;
import com.jams.music.player.Dialogs.GooglePlayMusicAuthenticationDialog;
import com.jams.music.player.Dialogs.NowPlayingColorSchemesDialog;
import com.jams.music.player.Dialogs.ScanFrequencyDialog;

/*************************************************************************
 * This class displays a dummy activity which fires up a FragmentDialog 
 * from PreferencesActivity.
 * 
 * @author Saravan Pantham
 *************************************************************************/
public class PreferenceDialogLauncherActivity extends FragmentActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		SharedPreferences sharedPreferences = this.getSharedPreferences("com.jams.music.player", Context.MODE_PRIVATE);
		
		if (sharedPreferences.getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_THEME") ||
			sharedPreferences.getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
			this.setTheme(R.style.AppThemeTransparent);
		} else {
			this.setTheme(R.style.AppThemeTransparentLight);
		}
		
		super.onCreate(savedInstanceState);
		
		//Get the index that specifies which dialog to launch.
		int index = getIntent().getExtras().getInt("INDEX");
		
		if (index==0) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	        ApplicationThemeDialog appThemeDialog = new ApplicationThemeDialog();
	        appThemeDialog.show(ft, "appThemeDialog");
		} else if (index==1) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	        NowPlayingColorSchemesDialog appThemeDialog = new NowPlayingColorSchemesDialog();
	        appThemeDialog.show(ft, "colorSchemesDialog");
		} else if (index==2) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	        CustomizeScreensDialog screensDialog = new CustomizeScreensDialog();
	        screensDialog.show(ft, "customizeScreensDialog");
		} else if (index==3) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	        CoverArtStyleDialog coverArtStyleDialog = new CoverArtStyleDialog();
	        coverArtStyleDialog.show(ft, "coverArtStyleDialog");
		} else if (index==4) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	        AlbumArtSourceDialog albumArtSourceDialog = new AlbumArtSourceDialog();
	        albumArtSourceDialog.show(ft, "albumArtSourceDialog");
		} else if (index==5) {
			/*FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			Bundle bundle = new Bundle();
			bundle.putBoolean("CALLED_FROM_WELCOME", false);
	        MusicFoldersSelectionDialog foldersDialog = new MusicFoldersSelectionDialog();
	        foldersDialog.setArguments(bundle);
	        foldersDialog.show(ft, "foldersDialog");*/
			
		} else if (index==6) {	
			//Seting the "REBUILD_LIBRARY" flag to true will force MainActivity to rescan the folders.
			sharedPreferences.edit().putBoolean("REBUILD_LIBRARY", true).commit();
	    	
			//Restart the app.
			Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			finish();
			startActivity(i);
							
		} else if (index==7) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			Bundle bundle = new Bundle();
			bundle.putBoolean("CALLED_FROM_WELCOME", false);
	        ScanFrequencyDialog scanFrequencyDialog = new ScanFrequencyDialog();
	        scanFrequencyDialog.setArguments(bundle);
	        scanFrequencyDialog.show(ft, "scanFrequencyDialog");
	        
		} else if (index==8) {
			
/*			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			BlacklistManagerDialog blacklistDialog = new BlacklistManagerDialog();
			blacklistDialog.show(ft, "blacklistManagerDialog");*/
			
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			BlacklistedElementsDialog dialog = new BlacklistedElementsDialog();
			Bundle bundle = new Bundle();
			bundle.putString("MANAGER_TYPE", "ARTISTS");
			dialog.setArguments(bundle);
			dialog.show(ft, "blacklistedElementsDialog");
	        
		} else if (index==9) { 
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			Bundle bundle = new Bundle();
			bundle.putBoolean("CALLED_FROM_WELCOME", false);
	        ScanFrequencyDialog scanFrequencyDialog = new ScanFrequencyDialog();
	        scanFrequencyDialog.setArguments(bundle);
	        scanFrequencyDialog.show(ft, "scanFrequencyDialog");
	        
		} else if (index==12) {
			/*FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	        LicensesDialog appThemeDialog = new LicensesDialog();
	        appThemeDialog.show(ft, "licensesDialog");*/
			
			new LicensesDialog(this, R.raw.notices, false, false).show();
	        
		} else if (index==13) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			AddMusicLibraryDialog addMusicLibraryDialog = new AddMusicLibraryDialog();
			addMusicLibraryDialog.show(ft, "addMusicLibraryDialog");
		
		} else if (index==14) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			EditDeleteMusicLibraryDialog deleteMusicLibraryDialog = new EditDeleteMusicLibraryDialog();
			Bundle bundle = getIntent().getExtras();
			deleteMusicLibraryDialog.setArguments(bundle);
			deleteMusicLibraryDialog.show(ft, "editDeleteMusicLibraryDialog");
			
		} else if (index==15) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			GooglePlayMusicAuthenticationDialog dialog = new GooglePlayMusicAuthenticationDialog();
			Bundle bundle = new Bundle();
			bundle.putBoolean("FIRST_RUN", false);
			dialog.setArguments(bundle);
			dialog.show(ft, "gMusicAuthDialog");
			
		}

	}
	
}
