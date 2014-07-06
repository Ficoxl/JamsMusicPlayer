package com.jams.music.player.Helpers;


import android.os.Handler;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.squareup.picasso.Picasso;

/**
 * Pauses image loading for various scroll events.
 */
public class PauseOnScrollHelper implements OnScrollListener {

    private Picasso picasso;
    private long delay;
    private final OnScrollListener externalListener;
    private Handler handler;

    public PauseOnScrollHelper(Picasso picasso, long delay) {
        this(picasso, delay, null);
    }

    public PauseOnScrollHelper(Picasso picasso, long delay, OnScrollListener customListener) {
        this.picasso = picasso;
        this.delay = delay;
        handler = new Handler();
        externalListener = customListener;
        Log.e("DEBUG", ">>>>INITING SCROLL LISTENER");
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        Log.e("DEBUG", ">>>>SCROLL STATE CHANGED");
        switch (scrollState) {
            case OnScrollListener.SCROLL_STATE_IDLE:
                Log.e("DEBUG", ">>>>RESUMING!");
                handler.postDelayed(resumeLoading, delay);
                break;
            case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                Log.e("DEBUG", ">>>>SCROLLING");
                picasso.interruptDispatching();
                break;
            case OnScrollListener.SCROLL_STATE_FLING:
                Log.e("DEBUG", ">>>>>FLINGING!");
                picasso.interruptDispatching();
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
            picasso.continueDispatching();

        }

    };

}
