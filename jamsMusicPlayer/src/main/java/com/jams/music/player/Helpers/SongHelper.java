package com.jams.music.player.Helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.R;
import com.jams.music.player.Utils.Common;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Helper class for the current song.
 * 
 * @author Saravan Pantham
 *
 */
public class SongHelper {

	private SongHelper mSongHelper;
	private Common mApp;
	private int mIndex;
	private boolean mIsCurrentSong = false;
	private boolean mIsAlbumArtLoaded = false;
	
	//Song parameters.
	private String mTitle;
	private String mArtist;
	private String mAlbum;
	private String mAlbumArtist;
	private long mDuration;
	private String mFilePath;
	private String mGenre;
	private String mId;
	private String mAlbumArtPath;
	private String mSource;
	private String mLocalCopyPath;
	private long mSavedPosition;
	private Bitmap mAlbumArt;
	
	private AlbumArtLoadedListener mAlbumArtLoadedListener;
	
	/**
	 * Interface that provides callbacks to the provided listener 
	 * once the song's album art has been loaded.
	 */
	public interface AlbumArtLoadedListener {
		
		/**
		 * Called once the album art bitmap is ready for use.
		 */
		public void albumArtLoaded();
	}
	
	/**
	 * Moves the specified cursor to the specified index and populates this 
	 * helper object with new song data.
	 * 
	 * @param context Context used to get a new Common object.
	 * @param index The index of the song.
	 */
	public void populateSongData(Context context, int index) {
		
		mSongHelper = this;
		mApp = (Common) context.getApplicationContext();
		mIndex = index;
		
		if (mApp.isServiceRunning()) {
			mApp.getService().getCursor().moveToPosition(mApp.getService().getPlaybackIndecesList().get(index));
			
			this.setId(mApp.getService().getCursor().getString(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.SONG_ID)));
			this.setTitle(mApp.getService().getCursor().getString(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.SONG_TITLE)));
			this.setAlbum(mApp.getService().getCursor().getString(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.SONG_ALBUM)));
			this.setArtist(mApp.getService().getCursor().getString(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.SONG_ARTIST)));
			this.setAlbumArtist(mApp.getService().getCursor().getString(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.SONG_ALBUM_ARTIST)));
			this.setGenre(mApp.getService().getCursor().getString(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.SONG_GENRE)));
			this.setDuration(mApp.getService().getCursor().getLong(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.SONG_DURATION)));
			
			this.setAlbumArtPath(mApp.getService().getCursor().getString(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.SONG_ALBUM_ART_PATH)));
			this.setFilePath(mApp.getService().getCursor().getString(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.SONG_FILE_PATH)));
			this.setSource(mApp.getService().getCursor().getString(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.SONG_SOURCE)));
			this.setLocalCopyPath(mApp.getService().getCursor().getString(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.LOCAL_COPY_PATH)));	
			this.setSavedPosition(mApp.getService().getCursor().getLong(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.SAVED_POSITION)));

            mApp.getPicasso().load(getAlbumArtPath()).into(imageLoadingTarget);
		}

	}

    /**
     * Moves the specified cursor to the specified index and populates this
     * helper object with new song data. Note that this method only laods
     * the song's title and artist. All other fields are set to null. To
     * retrieve all song data, see populateSongData().
     *
     * @param context Context used to get a new Common object.
     * @param index The index of the song.
     */
    public void populateBasicSongData(Context context, int index) {

        mSongHelper = this;
        mApp = (Common) context.getApplicationContext();
        mIndex = index;

        if (mApp.isServiceRunning()) {
            mApp.getService().getCursor().moveToPosition(mApp.getService().getPlaybackIndecesList().get(index));

            this.setId(mApp.getService().getCursor().getString(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.SONG_ID)));
            this.setTitle(mApp.getService().getCursor().getString(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.SONG_TITLE)));
            this.setAlbum(mApp.getService().getCursor().getString(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.SONG_ALBUM)));
            this.setArtist(mApp.getService().getCursor().getString(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.SONG_ARTIST)));
            this.setAlbumArtist(mApp.getService().getCursor().getString(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.SONG_ALBUM_ARTIST)));
            this.setGenre(mApp.getService().getCursor().getString(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.SONG_GENRE)));
            this.setDuration(mApp.getService().getCursor().getLong(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.SONG_DURATION)));

            this.setAlbumArtPath(mApp.getService().getCursor().getString(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.SONG_ALBUM_ART_PATH)));
            this.setFilePath(mApp.getService().getCursor().getString(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.SONG_FILE_PATH)));
            this.setSource(mApp.getService().getCursor().getString(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.SONG_SOURCE)));
            this.setLocalCopyPath(mApp.getService().getCursor().getString(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.LOCAL_COPY_PATH)));
            this.setSavedPosition(mApp.getService().getCursor().getLong(mApp.getService().getCursor().getColumnIndex(DBAccessHelper.SAVED_POSITION)));

        }

    }

    /**
	 * Sets this helper object as the current song. This method 
	 * will check if the song's album art has already been loaded. 
	 * If so, the updateNotification() and updateWidget() methods 
	 * will be called. If not, they'll be called as soon as the 
	 * album art is loaded.
	 */
	public void setIsCurrentSong() {
		mIsCurrentSong = true;
		//The album art has already been loaded.
		if (mIsAlbumArtLoaded) {
			mApp.getService().updateNotification(this);
			mApp.getService().updateWidgets();
		} else {
			/* 
			 * The album art isn't ready yet. The listener will call 
			 * the updateNotification() and updateWidgets() methods.
			 */
		}
		
	}
	
	/**
	 * Image loading listener to store the current song's album art.
	 */
    Target imageLoadingTarget = new Target() {

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mIsAlbumArtLoaded = true;
            setAlbumArt(bitmap);
            if (getAlbumArtLoadedListener()!=null)
                getAlbumArtLoadedListener().albumArtLoaded();

            if (mIsCurrentSong) {
                mApp.getService().updateNotification(mSongHelper);
                mApp.getService().updateWidgets();

            }

        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            Drawable defaultAlbumArtDrawable = mApp.getResources().getDrawable(R.drawable.default_album_art);
            Bitmap defaultAlbumArt = ((BitmapDrawable) defaultAlbumArtDrawable).getBitmap();
            setAlbumArt(defaultAlbumArt);
            onBitmapLoaded(mAlbumArt, null);

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            mIsAlbumArtLoaded = false;

        }

    };

    public int getSongIndex() {
        return mIndex;
    }

	public String getTitle() {
		return mTitle;
	}
	
	public void setTitle(String title) {
		mTitle = title;
	}
	
	public String getArtist() {
		return mArtist;
	}
	
	public void setArtist(String artist) {
		mArtist = artist;
	}
	
	public String getAlbum() {
		return mAlbum;
	}
	
	public void setAlbum(String album) {
		mAlbum = album;
	}
	
	public String getAlbumArtist() {
		return mAlbumArtist;
	}
	
	public void setAlbumArtist(String albumArtist) {
		mAlbumArtist = albumArtist;
	}
	
	public long getDuration() {
		return mDuration;
	}
	
	public void setDuration(long duration) {
		mDuration = duration;
	}
	
	public String getFilePath() {
		return mFilePath;
	}
	
	public String getLocalCopyPath() {
		return mLocalCopyPath;
	}
	
	public Bitmap getAlbumArt() {
		return mAlbumArt;
	}
	
	public void setFilePath(String filePath) {
		mFilePath = filePath;
	}
	
	public String getGenre() {
		return mGenre;
	}
	
	public void setGenre(String genre) {
		mGenre = genre;
	}
	
	public String getId() {
		return mId;
	}
	
	public void setId(String id) {
		mId = id;
	}
	
	public String getAlbumArtPath() {
		return mAlbumArtPath;
	}
	
	public void setAlbumArtPath(String albumArtPath) {
		mAlbumArtPath = albumArtPath;
	}
	
	public String getSource() {
		return mSource;
	}
	
	public void setSource(String source) {
		mSource = source;
	}

	public void setLocalCopyPath(String localCopyPath) {
		mLocalCopyPath = localCopyPath;
	}
	
	public void setAlbumArt(Bitmap albumArt) {
		mAlbumArt = albumArt;
	}
	
	public void setSavedPosition(long savedPosition) {
		mSavedPosition = savedPosition;
	}
	
	public long getSavedPosition() {
		return mSavedPosition;
	}
	
	public void setAlbumArtLoadedListener(AlbumArtLoadedListener listener) {
		mAlbumArtLoadedListener = listener;
	}
	
	public AlbumArtLoadedListener getAlbumArtLoadedListener() {
		return mAlbumArtLoadedListener;
	}
	
}
