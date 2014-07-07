package com.jams.music.player.Helpers;

import android.content.Context;
import android.widget.AbsListView;
import com.squareup.picasso.Picasso;

public class PauseOnScrollHelper implements AbsListView.OnScrollListener {

    protected AbsListView.OnScrollListener delegate;
    protected Picasso picasso;
    private int previousScrollState = SCROLL_STATE_IDLE;
    private boolean scrollingFirstTime = true;

    public PauseOnScrollHelper(Picasso picasso, AbsListView.OnScrollListener delegate) {
        this.delegate = delegate;
        this.picasso = picasso;
        picasso.continueDispatching();
    }

    public PauseOnScrollHelper(Picasso picasso) {
        this(picasso, null);
    }

    public PauseOnScrollHelper(Context context) {
        this(context, null);
    }

    public PauseOnScrollHelper(Context context, AbsListView.OnScrollListener delegate) {
        this(Picasso.with(context), delegate);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

        if (scrollingFirstTime) {
            picasso.continueDispatching();
            scrollingFirstTime = false;
        }

        // TO the picasso staff
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