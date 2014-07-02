package com.jams.music.player.Helpers;


import android.os.Handler;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Pauses image loading for various scroll events.
 */
public class PauseOnScrollHelper implements OnScrollListener {

    private ImageLoader imageLoader;
    private long delay;
    private final boolean pauseOnScroll;
    private final boolean pauseOnFling;
    private final OnScrollListener externalListener;
    private Handler handler;

    /**
     * Constructor
     *
     * @param imageLoader   {@linkplain ImageLoader} instance for controlling
     * @param pauseOnScroll Whether {@linkplain ImageLoader#pause() pause ImageLoader} during touch scrolling
     * @param pauseOnFling  Whether {@linkplain ImageLoader#pause() pause ImageLoader} during fling
     * @param delay			The delay, in milliseconds, after which image loading resumes after a scroll event.
     */
    public PauseOnScrollHelper(ImageLoader imageLoader, boolean pauseOnScroll,
                               boolean pauseOnFling, long delay) {
        this(imageLoader, pauseOnScroll, pauseOnFling, delay, null);
    }

    /**
     * Constructor
     *
     * @param imageLoader    {@linkplain ImageLoader} instance for controlling
     * @param pauseOnScroll  Whether {@linkplain ImageLoader#pause() pause ImageLoader} during touch scrolling
     * @param pauseOnFling   Whether {@linkplain ImageLoader#pause() pause ImageLoader} during fling
     * @param customListener Your custom {@link OnScrollListener} for {@linkplain AbsListView list view} which also will
     *                       be get scroll events
     * @param delay			 The delay, in milliseconds, after which image loading resumes after a scroll event.
     */
    public PauseOnScrollHelper(ImageLoader imageLoader, boolean pauseOnScroll,
                               boolean pauseOnFling, long delay, OnScrollListener customListener) {
        this.imageLoader = imageLoader;
        this.pauseOnScroll = pauseOnScroll;
        this.pauseOnFling = pauseOnFling;
        this.delay = delay;
        handler = new Handler();
        externalListener = customListener;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
            case OnScrollListener.SCROLL_STATE_IDLE:
                //handler.postDelayed(resumeLoading, delay);
                imageLoader.resume();
                break;
            case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                if (pauseOnScroll)
                    imageLoader.pause();
                break;
            case OnScrollListener.SCROLL_STATE_FLING:
                if (pauseOnFling)
                    imageLoader.pause();
                break;
        }

        if (externalListener != null)
            externalListener.onScrollStateChanged(view, scrollState);

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (externalListener != null)
            externalListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);

    }

    /**
     * Runnable that resumes image loading after the specified delay.
     */
    Runnable resumeLoading = new Runnable() {

        @Override
        public void run() {
            imageLoader.resume();

        }

    };

}
