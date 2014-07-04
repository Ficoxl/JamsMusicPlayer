package com.jams.music.player.Helpers;

import android.graphics.Bitmap;

/**
 * Helper class used to animate a thumbnail to a
 * larger, scaled-in version during an activity
 * transition.
 *
 * @author Saravan Pantham
 */
public class ImageViewCoordHelper {

    public String mAlbumArtPath;
    public Bitmap mThumbnail;

    public ImageViewCoordHelper(String albumArtPath, Bitmap thumbnail) {
        mAlbumArtPath = albumArtPath;
        mThumbnail = thumbnail;

    }

}
