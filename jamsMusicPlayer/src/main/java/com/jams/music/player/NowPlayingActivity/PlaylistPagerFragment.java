package com.jams.music.player.NowPlayingActivity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.jams.music.player.AlbumArtistsFlippedActivity.AlbumArtistsFlippedActivity;
import com.jams.music.player.AlbumsFlippedActivity.AlbumsFlippedActivity;
import com.jams.music.player.Animations.TranslateAnimation;
import com.jams.music.player.ArtistsFlippedActivity.ArtistsFlippedActivity;
import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.Dialogs.RepeatSongRangeDialog;
import com.jams.music.player.GenresFlippedActivity.GenresFlippedActivity;
import com.jams.music.player.Helpers.SongHelper;
import com.jams.music.player.Helpers.SongHelper.AlbumArtLoadedListener;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.ImageTransformers.PicassoBlurTransformer;
import com.jams.music.player.ImageTransformers.PicassoMirrorReflectionTransformer;
import com.jams.music.player.R;
import com.jams.music.player.Utils.Common;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;

public class PlaylistPagerFragment extends Fragment implements AlbumArtLoadedListener {

	private Context mContext;
	private Common mApp;
	private ViewGroup mRootView;
	private int mPosition;
	private SongHelper mSongHelper;
	private PopupMenu popup;
	
	private TextView songNameTextView;
	private TextView artistAlbumNameTextView;
	private ImageView coverArt;
	private ImageView overflowIcon;
	
	private boolean mAreLyricsVisible = false;
	private ScrollView mLyricsScrollView;
	private TextView mLyricsTextView;
	private TextView mLyricsEmptyTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        mContext = getActivity();
        mApp = (Common) mContext.getApplicationContext();

        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_playlist_pager_fill, container, false);
        mPosition = getArguments().getInt("POSITION");

        overflowIcon = (ImageView) mRootView.findViewById(R.id.now_playing_overflow_icon);
    	coverArt = (ImageView) mRootView.findViewById(R.id.coverArt);
        songNameTextView = (TextView) mRootView.findViewById(R.id.songName);
    	artistAlbumNameTextView = (TextView) mRootView.findViewById(R.id.artistAlbumName);
    	
    	mLyricsScrollView = (ScrollView) mRootView.findViewById(R.id.lyrics_scroll_view);
    	mLyricsTextView = (TextView) mRootView.findViewById(R.id.lyrics);
    	mLyricsEmptyTextView = (TextView) mRootView.findViewById(R.id.lyrics_empty);
        
    	mLyricsTextView.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));
    	mLyricsEmptyTextView.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));
    	songNameTextView.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));
    	artistAlbumNameTextView.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Regular"));

        //Allow the TextViews to scroll if they extend beyond the layout margins.
        songNameTextView.setSelected(true);
        artistAlbumNameTextView.setSelected(true);
    	
    	//Initialize the pop up menu.
    	popup = new PopupMenu(getActivity(), overflowIcon);
		popup.getMenuInflater().inflate(R.menu.now_playing_overflow_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(menuItemClickListener);

        mSongHelper = new SongHelper();
        mSongHelper.setAlbumArtLoadedListener(this);
        mSongHelper.populateSongData(mContext, mPosition, new PicassoMirrorReflectionTransformer());
		
    	songNameTextView.setText(mSongHelper.getTitle());
    	artistAlbumNameTextView.setText(mSongHelper.getAlbum() + " - " + mSongHelper.getArtist());
        overflowIcon.setOnClickListener(overflowClickListener);
    	
        return mRootView;
    }

    /**
     * Overflow button click listener.
     */
    private OnClickListener overflowClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            popup.show();
        }

    };

    /**
     * Menu item click listener for the overflow pop up menu.
     */
    private PopupMenu.OnMenuItemClickListener menuItemClickListener = new PopupMenu.OnMenuItemClickListener() {

        @Override
        public boolean onMenuItemClick(MenuItem item) {

            switch (item.getItemId()) {
                case R.id.save_clear_current_position:
                    if (item.getTitle().equals(mContext.getResources().getString(R.string.save_current_position))) {
                        item.setTitle(R.string.clear_saved_position);
                    } else {
                        item.setTitle(R.string.save_current_position);
                    }

                    break;
                case R.id.show_embedded_lyrics:
                    if (item.getTitle().equals(mContext.getResources().getString(R.string.show_embedded_lyrics))) {
                        AsyncLoadLyricsTask task = new AsyncLoadLyricsTask();
                        task.execute();
                        item.setTitle(R.string.hide_lyrics);
                    } else {
                        hideLyrics();
                        item.setTitle(R.string.show_embedded_lyrics);
                    }

                    break;
                case R.id.a_b_repeat:
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    RepeatSongRangeDialog dialog = new RepeatSongRangeDialog();
                    dialog.show(ft, "repeatSongRangeDialog");
                    break;
                case R.id.current_queue:
                    ((NowPlayingActivity) getActivity()).toggleCurrentQueueDrawer();
                    break;
                case R.id.go_to:
                    PopupMenu goToPopupMenu = new PopupMenu(getActivity(), overflowIcon);
                    goToPopupMenu.inflate(R.menu.show_more_menu);
                    goToPopupMenu.setOnMenuItemClickListener(goToMenuClickListener);
                    goToPopupMenu.show();
                    break;
            }

            return false;
        }

    };

    /**
     * "Go to" popup menu item click listener.
     */
    private PopupMenu.OnMenuItemClickListener goToMenuClickListener = new PopupMenu.OnMenuItemClickListener() {

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.go_to_this_artist:
                    break;
                case R.id.go_to_this_album_artist:
                    break;
                case R.id.go_to_this_album:
                    break;
                case R.id.go_to_this_genre:
                    break;
            }

            return false;
        }

    };

    /**
     * Callback method for album art loading.
     */
	@Override
	public void albumArtLoaded() {
		coverArt.setImageBitmap(mSongHelper.getAlbumArt());
	}

    /**
     * Reads lyrics from the audio file's tag and displays them.
     */
	class AsyncLoadLyricsTask extends AsyncTask<Boolean, Boolean, Boolean> {

		String mLyrics = "";
		
		@Override
		protected Boolean doInBackground(Boolean... arg0) {
			String songFilePath = mApp.getService().getCurrentSong().getFilePath();
			AudioFile audioFile = null;
			Tag tag = null;
			try {
				audioFile = AudioFileIO.read(new File(songFilePath));
				
				if (audioFile!=null)
					tag = audioFile.getTag();
				else
					return false;
				
				if (tag!=null)
					mLyrics = tag.getFirst(FieldKey.LYRICS);
				else
					return false;
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return true;
		}
		
		@Override
		public void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			
			if (mLyrics!=null && !mLyrics.isEmpty()) {
				mLyricsTextView.setText(mLyrics);
				mLyricsTextView.setVisibility(View.VISIBLE);
				mLyricsEmptyTextView.setVisibility(View.INVISIBLE);
			} else {
				mLyrics = mContext.getResources().getString(R.string.no_embedded_lyrics_found);
				mLyricsTextView.setText(mLyrics);
				mLyricsTextView.setVisibility(View.INVISIBLE);
				mLyricsEmptyTextView.setVisibility(View.VISIBLE);
			}
			
			//Slide up the album art to show the lyrics.
	    	TranslateAnimation slideUpAnimation = new TranslateAnimation(coverArt, 400, new AccelerateInterpolator(), 
					 													 View.INVISIBLE,
					 													 Animation.RELATIVE_TO_SELF, 0.0f, 
					 													 Animation.RELATIVE_TO_SELF, 0.0f, 
					 													 Animation.RELATIVE_TO_SELF, 0.0f, 
					 													 Animation.RELATIVE_TO_SELF, -2.0f);

	    	slideUpAnimation.animate();
	    	
		}
		
	}

    /**
     * Slides down the album art to hide lyrics.
     */
    private void hideLyrics() {
        TranslateAnimation slideDownAnimation = new TranslateAnimation(coverArt, 400, new DecelerateInterpolator(2.0f),
                                                                       View.VISIBLE,
                                                                       Animation.RELATIVE_TO_SELF, 0.0f,
                                                                       Animation.RELATIVE_TO_SELF, 0.0f,
                                                                       Animation.RELATIVE_TO_SELF, -2.0f,
                                                                       Animation.RELATIVE_TO_SELF, 0.0f);

        slideDownAnimation.animate();
    }

}
