package com.jams.music.player.ArtistsFlippedActivity;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.jams.music.player.R;
import com.jams.music.player.AsyncTasks.AsyncAddToQueueTask;
import com.jams.music.player.AsyncTasks.AsyncDeleteAlbumArtTask;
import com.jams.music.player.AsyncTasks.AsyncGetAlbumArtTask;
import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.Dialogs.AddToPlaylistDialog;
import com.jams.music.player.Dialogs.CautionEditAlbumsDialog;
import com.jams.music.player.Dialogs.ID3sAlbumEditorDialog;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.Utils.Common;

public class ArtistsFlippedListViewAdapter extends SimpleCursorAdapter {
	
	private Context mContext;
	private Common mApp;
	public static DBAccessHelper musicLibraryDBHelper;
	public static Cursor cursor;
	private String mArtistName = "";
	private String mAlbumName = "";
	
    public ArtistsFlippedListViewAdapter(Context context, Cursor cursor) {
        super(context, -1, cursor, new String[] {}, new int[] {}, 0);
        mContext = context;
        mApp = (Common) mContext.getApplicationContext();
    
    }

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
        Cursor c = (Cursor) getItem(position);
	    ArtistsFlippedListViewHolder holder = null;

		if (convertView == null) {
			
			if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME") || 
				mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.artists_flipped_albums_cards_list_layout, parent, false);
			} else {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.artists_flipped_albums_list_layout, parent, false);
			}

			holder = new ArtistsFlippedListViewHolder();
			holder.image = (ImageView) convertView.findViewById(R.id.artistsFlippedAlbumThumbnail);
			holder.title = (TextView) convertView.findViewById(R.id.artistsFlippedAlbumName);
			holder.albumYear = (TextView) convertView.findViewById(R.id.artistsFlippedAlbumYear);
			holder.numberOfSongs = (TextView) convertView.findViewById(R.id.artistsFlippedNumberOfSongs);
			
			holder.overflowButton = (ImageButton) convertView.findViewById(R.id.overflow_icon);
			holder.overflowButton.setOnClickListener(overflowClickListener);
			holder.overflowButton.setFocusable(false);
			holder.overflowButton.setFocusableInTouchMode(false);
			
			holder.title.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Light"));
			holder.albumYear.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
			holder.numberOfSongs.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
			
			holder.title.setPaintFlags(holder.title.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
			holder.albumYear.setPaintFlags(holder.albumYear.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
			holder.numberOfSongs.setPaintFlags(holder.numberOfSongs.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
			
			convertView.setTag(holder);
			
		} else {
		    holder = (ArtistsFlippedListViewHolder) convertView.getTag();
		}

		//Apply the card layout's background based on the color theme.
		if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME")) {
			convertView.setBackgroundResource(R.drawable.card_light);
		} else if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
			convertView.setBackgroundResource(R.drawable.card_dark);
		}
		
		String songSource = c.getString(c.getColumnIndex(DBAccessHelper.SONG_SOURCE));
		String songFilePath = c.getString(c.getColumnIndex(DBAccessHelper.SONG_FILE_PATH));
		String albumArtPath = c.getString(c.getColumnIndex(DBAccessHelper.SONG_ALBUM_ART_PATH));
		String artist = c.getString(c.getColumnIndex(DBAccessHelper.SONG_ARTIST));
		String album = c.getString(c.getColumnIndex(DBAccessHelper.SONG_ALBUM));
		String albumYear = c.getString(c.getColumnIndex(DBAccessHelper.SONG_YEAR));
		
		//Set the tag properties for this child view.
		convertView.setTag(R.string.artist, artist);
		convertView.setTag(R.string.album, album);
		convertView.setTag(R.string.song_source, songSource);
		convertView.setTag(R.string.song_file_path, songFilePath);
		convertView.setTag(R.string.album_art, albumArtPath);
		
		holder.overflowButton.setTag(R.string.artist, artist);
		holder.overflowButton.setTag(R.string.album, album);
		
		if (album.contains("'")) {
			album = album.replace("'", "''");
		}
		
		//Get the number of songs in the album.
		String where = DBAccessHelper.SONG_ALBUM + "=" + "'" + album + "'";
		musicLibraryDBHelper = new DBAccessHelper(mContext);
		cursor = musicLibraryDBHelper.getReadableDatabase().query(DBAccessHelper.MUSIC_LIBRARY_TABLE, 
														 new String[] { DBAccessHelper.SONG_TITLE }, 
														 where, 
														 null, 
														 null, 
														 null, 
														 null);
		
		album = album.replace("''", "'");
		
		int numberOfSongs = 0;
		if (cursor!=null) {
			numberOfSongs = cursor.getCount();
		}
		
		//Set the Artist name in the GridView.
		holder.title.setText(c.getString(c.getColumnIndex(DBAccessHelper.SONG_ALBUM)));
		
		mApp.getImageLoader().displayImage(albumArtPath, holder.image, ArtistsFlippedFragment.displayImageOptions);
        
        //Set the album year.
		if (albumYear==null || albumYear.length()==0) {
			albumYear = mContext.getResources().getString(R.string.unknown_year);
		}
        holder.albumYear.setText(albumYear);
        
        //Set the number of songs in the album.
        if (numberOfSongs==1) {
            holder.numberOfSongs.setText(numberOfSongs + " song");
        } else {
            holder.numberOfSongs.setText(numberOfSongs + " songs");
        }
		
		return convertView;
	}
    
    private OnClickListener overflowClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			PopupMenu menu = new PopupMenu(mContext, v);
			menu.inflate(R.menu.album_overflow_menu);
			menu.setOnMenuItemClickListener(popupMenuItemClickListener);
			mAlbumName = (String) v.getTag(R.string.album);
			mArtistName = (String) v.getTag(R.string.artist);
		    menu.show();
			
		}
    	
    };
    
    private OnMenuItemClickListener popupMenuItemClickListener = new OnMenuItemClickListener() {

		@Override
		public boolean onMenuItemClick(MenuItem item) {
			
            switch(item.getItemId()) {
        	case R.id.edit_album_tags:
        		//Edit Album Tags.
        		if (mApp.getSharedPreferences().getBoolean("SHOW_ALBUM_EDIT_CAUTION", true)==true) {
            		FragmentTransaction transaction = ArtistsFlippedFragment.fragment.getFragmentManager().beginTransaction();
            		Bundle bundle = new Bundle();
            		bundle.putString("EDIT_TYPE", "ALBUM");
            		bundle.putString("ARTIST", mArtistName);
            		bundle.putString("ALBUM", mAlbumName);
            		bundle.putString("CALLING_FRAGMENT", "ARTISTS_FLIPPED_FRAGMENT");
            		CautionEditAlbumsDialog dialog = new CautionEditAlbumsDialog();
            		dialog.setArguments(bundle);
            		dialog.show(transaction, "cautionArtistsDialog");
        		} else {
        			FragmentTransaction ft = ArtistsFlippedFragment.fragment.getFragmentManager().beginTransaction();
    				Bundle bundle = new Bundle();
    				bundle.putString("EDIT_TYPE", "ALBUM");
    				bundle.putString("ARTIST", mArtistName);
    				bundle.putString("ALBUM", mAlbumName);
    				bundle.putString("CALLING_FRAGMENT", "ARTISTS_FLIPPED_FRAGMENT");
    				ID3sAlbumEditorDialog dialog = new ID3sAlbumEditorDialog();
    				dialog.setArguments(bundle);
    				dialog.show(ft, "id3EditorDialog");
        		}
        		
        		break;
        	case R.id.add_to_queue:
        		//Add to queue.
        		AsyncAddToQueueTask addToQueueTask = new AsyncAddToQueueTask(mContext,
        														   ArtistsFlippedFragment.fragment,
        														   "ALBUM",
        														   mArtistName,
        														   mAlbumName,
        														   null, 
        														   null, 
        														   null,
        														   null,
        														   null);
        		addToQueueTask.execute();
        		break;
        	case R.id.play_next:
        		//Play next.
        		AsyncAddToQueueTask playNextTask = new AsyncAddToQueueTask(mContext,
		        														   ArtistsFlippedFragment.fragment,
		        														   "ALBUM",
		        														   mArtistName,
		        														   mAlbumName,
		        														   null, 
		        														   null, 
		        														   null,
		        														   null,
		        														   null);
        		playNextTask.execute(new Boolean[] { true });
        		break;
        	case R.id.add_to_playlist:
        		//Add to Playlist
        		FragmentTransaction ft = ArtistsFlippedFragment.fragment.getFragmentManager().beginTransaction();
				AddToPlaylistDialog dialog = new AddToPlaylistDialog();
				Bundle bundle = new Bundle();
				bundle.putString("ADD_TYPE", "ALBUM");
				bundle.putString("ARTIST", mArtistName);
				bundle.putString("ALBUM", mAlbumName);
				dialog.setArguments(bundle);
				dialog.show(ft, "AddToPlaylistDialog");
        		break;
        	case R.id.get_album_art: 
        		//Download Album Art.
        		AsyncGetAlbumArtTask getAlbumArtTask = new AsyncGetAlbumArtTask(mContext, 
        																		null, 
        																		R.id.albumsGridViewImage);
        		String[] metadata = { mArtistName, mAlbumName };
        		getAlbumArtTask.execute(metadata);
        		break;
        	case R.id.remove_album_art:
        		//Remove Album Art.
        		AsyncDeleteAlbumArtTask task = new AsyncDeleteAlbumArtTask(mContext, 
        																   null, 
        																   R.id.albumsGridViewImage, 
        																   ArtistsFlippedFragment.fragment.getActivity(), 
        																   "ARTISTS_FLIPPED_FRAGMENT");
        		String[] metadata2 = { mArtistName, mAlbumName };
        		task.execute(metadata2);
        		break;
        	case R.id.blacklist_album:
        		//Blacklist Album.
        		mApp.getDBAccessHelper().setBlacklistForAlbum(mAlbumName, mArtistName, true);
        		Toast.makeText(mContext, R.string.album_blacklisted, Toast.LENGTH_SHORT).show();
        		
        		//Update the GridView.
				ArtistsFlippedFragment.getCursor();
				ArtistsFlippedFragment.artistsFlippedListViewAdapter.notifyDataSetChanged();
        		break;
            	
            }
			
			return false;
		}
    	
    };

	static class ArtistsFlippedListViewHolder {
	    public ImageView image;
	    public TextView title;
	    public TextView albumYear;
	    public TextView numberOfSongs;
	    public ImageButton overflowButton;
	}
	
}
