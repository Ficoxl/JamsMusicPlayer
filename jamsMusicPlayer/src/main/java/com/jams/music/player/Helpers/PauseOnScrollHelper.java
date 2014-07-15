package com.jams.music.player.Helpers;

import android.content.Context;
import android.util.Log;
import android.widget.AbsListView;
import com.squareup.picasso.Picasso;

public class PauseOnScrollHelper implements AbsListView.OnScrollListener {

    protected AbsListView.OnScrollListener delegate;
    protected Picasso picasso;
    private int previousScrollState = SCROLL_STATE_IDLE;
    private boolean scrollingFirstTime = true;
    private boolean pauseOnScroll = false;
    private boolean pauseOnFling = true;

    public PauseOnScrollHelper(Picasso picasso, AbsListView.OnScrollListener delegate,
                               boolean pauseOnScroll, boolean pauseOnFling) {
        this.delegate = delegate;
        this.picasso = picasso;
        this.pauseOnScroll = pauseOnScroll;
        this.pauseOnFling = pauseOnFling;
        picasso.continueDispatching();
    }

    public PauseOnScrollHelper(Picasso picasso, boolean pauseOnScroll, boolean pauseOnFling) {
        this(picasso, null, pauseOnScroll, pauseOnFling);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

        if (scrollingFirstTime) {
            picasso.continueDispatching();
            scrollingFirstTime = false;
        }

        //Intercept this method here if we don't need imagel loading to be paused while scrolling.
        if (scrollState==SCROLL_STATE_TOUCH_SCROLL && pauseOnScroll==false) {
            return;
        }

        //Intercept this method here if we don't need imagel loading to be paused while flinging.
        if (scrollState==SCROLL_STATE_FLING && pauseOnFling==false) {
            return;
        }

        if (!isScrolling(scrollState) && isScrolling(previousScrollState)) {
            picasso.continueDispatching();
        }

        if (isScrolling(scrollState) && !isScrolling(previousScrollState)) {
            picasso.interruptDispatching();
        }

        previousScrollState = scrollState;

        // Forward to the delegate
        if (delegate != null) {
            delegate.onScrollStateChanged(view, scrollState);
        }
    }

    protected boolean isScrolling(int scrollState) {
        return scrollState == SCROLL_STATE_FLING || scrollState == SCROLL_STATE_TOUCH_SCROLL;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                         int totalItemCount) {

        // Forward to the delegate
        if (delegate != null) {
            delegate.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }
}