package com.jams.music.player.PlaylistsFlippedActivity;

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
import com.jams.music.player.Utils.Common;

public class SmartPlaylistListViewAdapter extends SimpleCursorAdapter {
	
	private SharedPreferences sharedPreferences;
	private Common mApp;
	
    public SmartPlaylistListViewAdapter(Context context, Cursor cursor) {
        super(context, -1, cursor, new String[] {}, new int[] {}, 0);
        sharedPreferences = context.getSharedPreferences("com.jams.music.player", Context.MODE_PRIVATE);
        mApp = (Common) context.getApplicationContext();
    }

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
        Cursor c = (Cursor) getItem(position);
	    SongsListViewHolder holder = null;

		if (convertView == null) {
			if (sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME") ||
				sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.top_25_played_list_cards_layout, parent, false);
			} else {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.top_25_played_list_layout, parent, false);
			}

			holder = new SongsListViewHolder();
			holder.dimmer = (ImageView) convertView.findViewById(R.id.dimmer);
			holder.image = (ImageView) convertView.findViewById(R.id.songsListAlbumThumbnail);
			holder.title = (TextView) convertView.findViewById(R.id.songNameListView);
			holder.artist = (TextView) convertView.findViewById(R.id.artistNameSongListView);
			holder.duration = (TextView) convertView.findViewById(R.id.songDurationListView);
			holder.rank = (TextView) convertView.findViewById(R.id.top_25_played_rank_text);

			convertView.setTag(holder);
		} else {
		    holder = (SongsListViewHolder) convertView.getTag();
		}
		
		//Apply the card layout's background based on the color theme.
		if (sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME")) {
			convertView.setBackgroundResource(R.drawable.card_light);
		} else if (sharedPreferences.getString(Common.CURRENT_THEME, "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
			convertView.setBackgroundResource(R.drawable.card_dark);
		}
		
		String songSource = c.getString(c.getColumnIndex(DBAccessHelper.SONG_SOURCE));
		String songId = c.getString(c.getColumnIndex(DBAccessHelper.SONG_ID));
		String songAlbumArtPath = c.getString(c.getColumnIndex(DBAccessHelper.SONG_ALBUM_ART_PATH));
		
		//Set the child view's tags.
		convertView.setTag(R.string.title, c.getString(c.getColumnIndex(DBAccessHelper.SONG_TITLE)));
		convertView.setTag(R.string.artist, c.getString(c.getColumnIndex(DBAccessHelper.SONG_ARTIST)));
		convertView.setTag(R.string.album, c.getString(c.getColumnIndex(DBAccessHelper.SONG_ALBUM)));
		convertView.setTag(R.string.duration, c.getString(c.getColumnIndex(DBAccessHelper.SONG_DURATION)));
		convertView.setTag(R.string.song_file_path, c.getString(c.getColumnIndex(DBAccessHelper.SONG_FILE_PATH)));
		convertView.setTag(R.string.genre, c.getString(c.getColumnIndex(DBAccessHelper.SONG_GENRE)));
		convertView.setTag(R.string.song_source, songSource);
		convertView.setTag(R.string.song_id, songId);
		convertView.setTag(R.string.album_art, songAlbumArtPath);
		
		holder.title.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
		holder.artist.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
		holder.duration.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
		holder.rank.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Light"));
		
		holder.title.setPaintFlags(holder.title.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
		holder.artist.setPaintFlags(holder.artist.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
		holder.duration.setPaintFlags(holder.duration.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
		holder.rank.setPaintFlags(holder.duration.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
		
		//Set the song title.
		holder.title.setText(c.getString(c.getColumnIndex(DBAccessHelper.SONG_TITLE)));

		mApp.getImageLoader().displayImage(songAlbumArtPath, holder.image, SmartPlaylistFragment.displayImageOptions);
        
		//Set the artist.
        holder.artist.setText(c.getString(c.getColumnIndex(DBAccessHelper.SONG_ARTIST)));
        
        //Set the duration of the song.
        long songDurationInMillis = 0;
        try {
        	songDurationInMillis = Long.parseLong(c.getString(c.getColumnIndex(DBAccessHelper.SONG_DURATION)));
        } catch (Exception e) {
        	songDurationInMillis = 0;
        }
		holder.duration.setText(convertMillisToMinsSecs(songDurationInMillis));
		
		if (SmartPlaylistFragment.CURRENT_LOADER.equals("TOP_25_PLAYED")) {
			holder.rank.setText("#" + (position+1));
		} else {
			holder.dimmer.setVisibility(View.GONE);
			holder.rank.setText("");
		}
        
		return convertView;
	}
    
	//Convert millisseconds to hh:mm:ss format.
    private String convertMillisToMinsSecs(long milliseconds) {
    	
    	int secondsValue = (int) (milliseconds / 1000) % 60 ;
    	int minutesValue = (int) ((milliseconds / (1000*60)) % 60);
    	int hoursValue  = (int) ((milliseconds / (1000*60*60)) % 24);
    	
    	String seconds = "";
    	String minutes = "";
    	String hours = "";
    	
    	if (secondsValue < 10) {
    		seconds = "0" + secondsValue;
    	} else {
    		seconds = "" + secondsValue;
    	}

    	if (minutesValue < 10) {
    		minutes = "0" + minutesValue;
    	} else {
    		minutes = "" + minutesValue;
    	}
    	
    	if (hoursValue < 10) {
    		hours = "0" + hoursValue;
    	} else {
    		hours = "" + hoursValue;
    	}
    	
    	
    	String output = "";
    	
    	if (hoursValue!=0) {
    		output = hours + ":" + minutes + ":" + seconds;
    	} else {
    		output = minutes + ":" + seconds;
    	}
    	
    	return output;
    }
    
	class SongsListViewHolder {
	    public ImageView image;
	    public TextView title;
	    public TextView artist;
	    public TextView duration;
	    public TextView rank;
	    public ImageView dimmer;
	}
	
}
