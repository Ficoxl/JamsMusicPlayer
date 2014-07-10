package com.jams.music.player.Drawers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.R;
import com.jams.music.player.Services.AudioPlaybackService;
import com.jams.music.player.Utils.Common;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.SimpleFloatViewManager;

public class QueueDrawerFragment extends Fragment {
	
	private Context mContext;
	private Common mApp;

	private DragSortListView mListView;
    private QueueDrawerAdapter mListViewAdapter;
    private TextView mEmptyText;
    private TextView mEmptyInfoText;

    private boolean mInitListViewParams = true;
    private boolean mDrawerOpen = false;

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		mContext = getActivity();
		mApp = (Common) mContext.getApplicationContext();

		View rootView = inflater.inflate(R.layout.fragment_queue_drawer, null);
		if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("LIGHT_CARDS_THEME") ||
			mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_THEME").equals("LIGHT_THEME")) {
			rootView.setBackgroundColor(0xFFFFFFFF);
		} else {
			rootView.setBackgroundColor(0xFF191919);
		}

		mListView = (DragSortListView) rootView.findViewById(R.id.queue_drawer_list_view);
        mEmptyText = (TextView) rootView.findViewById(R.id.queue_drawer_empty_text);
        mEmptyInfoText = (TextView) rootView.findViewById(R.id.queue_drawer_empty_text_info);

		mEmptyText.setTypeface(TypefaceHelper.getTypeface(getActivity(), "Roboto-Light"));
		mEmptyText.setPaintFlags(mEmptyText.getPaintFlags()
                                 | Paint.ANTI_ALIAS_FLAG
                                 | Paint.SUBPIXEL_TEXT_FLAG);

        mEmptyInfoText.setTypeface(TypefaceHelper.getTypeface(getActivity(), "RobotoCondensed-Regular"));
        mEmptyInfoText.setPaintFlags(mEmptyInfoText.getPaintFlags()
                                     | Paint.ANTI_ALIAS_FLAG
                                     | Paint.SUBPIXEL_TEXT_FLAG);

        //Restrict all touch events to this fragment.
        rootView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }

        });

		return rootView;
	}

    /**
     * Helper method that checks whether the audio playback service
     * is running or not.
     */
    private void checkServiceRunning() {
        if (mApp.isServiceRunning() && mApp.getService().getCursor()!=null)
            initListViewAdapter(mInitListViewParams);
        else
            showEmptyTextView();

    }

    /**
     * Initializes the drag sort list view.
     *
     * @param initViewParams Pass true if the ListView is being
     *                       initialized for the very first time
     *                       (dividers, background colors and other
     *                       layout settings will be applied). Pass
     *                       false if the list just needs to be updated
     *                       with the current song.
     */
	public void initListViewAdapter(boolean initViewParams) {

        if (initViewParams) {
            //Reset the initialization flag.
            mInitListViewParams = false;

            if (mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_THEME") ||
                mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME").equals("DARK_CARDS_THEME")) {
                mListView.setDivider(mContext.getResources().getDrawable(R.drawable.list_divider));
            } else {
                mListView.setDivider(mContext.getResources().getDrawable(R.drawable.list_divider_light));
            }

            mListView.setDividerHeight(1);
            mListView.setFastScrollEnabled(true);

            //KitKat ListView margins.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

                //Calculate navigation bar height.
                int navigationBarHeight = 0;
                int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
                if (resourceId > 0) {
                    navigationBarHeight = getResources().getDimensionPixelSize(resourceId);
                }

                mListView.setClipToPadding(false);
                mListView.setPadding(0, 0, 0, navigationBarHeight);

            }

        }

        mListViewAdapter = new QueueDrawerAdapter(mContext, mApp.getService().getPlaybackIndecesList());
        mListView.setAdapter(mListViewAdapter);
        mListView.setOnItemClickListener(onClick);
        mListView.setDropListener(onDrop);
        mListView.setRemoveListener(onRemove);

        SimpleFloatViewManager simpleFloatViewManager = new SimpleFloatViewManager(mListView);
        simpleFloatViewManager.setBackgroundColor(Color.TRANSPARENT);
        mListView.setFloatViewManager(simpleFloatViewManager);

        mListView.setVisibility(View.VISIBLE);
        mEmptyText.setVisibility(View.INVISIBLE);
        mEmptyInfoText.setVisibility(View.INVISIBLE);

        /*
         * If the drawer is open, the user is probably scrolling through
         * the list already, so don't move the list to the new position.
         */
        if (!isDrawerOpen())
            mListView.setSelection(mApp.getService().getCurrentSongIndex());

    }

    /**
     * Called if the audio playback service is not running.
     */
    public void showEmptyTextView() {
        mListView.setVisibility(View.INVISIBLE);
        mEmptyText.setVisibility(View.VISIBLE);
        mEmptyInfoText.setVisibility(View.VISIBLE);

    }

    /**
     * Click listener for the ListView.
     */
    private AdapterView.OnItemClickListener onClick = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mApp.isServiceRunning())
                mApp.getService().skipToTrack(position);

        }

    };

    /**
     * Drag and drop interface for the ListView.
     */
    private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {

        @Override
        public void drop(int from, int to) {
            if (from!=to) {
                int fromItem = mListViewAdapter.getItem(from);
                int toItem = mListViewAdapter.getItem(to);
                mListViewAdapter.remove(fromItem);
                mListViewAdapter.insert(fromItem, to);

                //If the current song was reordered, change currentSongIndex and update the next song.
                if (from==mApp.getService().getCurrentSongIndex()) {
                    mApp.getService().setCurrentSongIndex(to);

                    //Check which mediaPlayer is currently playing, and prepare the other mediaPlayer.
                    mApp.getService().prepareAlternateMediaPlayer();
                    return;

                } else if (from > mApp.getService().getCurrentSongIndex() && to <= mApp.getService().getCurrentSongIndex()) {
                    //One of the next songs was moved to a position before the current song. Move currentSongIndex forward by 1.
                    mApp.getService().incrementCurrentSongIndex();
                    mApp.getService().incrementEnqueueReorderScalar();

                    //Check which mediaPlayer is currently playing, and prepare the other mediaPlayer.
                    mApp.getService().prepareAlternateMediaPlayer();
                    return;

                } else if (from < mApp.getService().getCurrentSongIndex() && to==mApp.getService().getCurrentSongIndex()) {
                	/* One of the previous songs was moved to the current song's position (visually speaking,
                	 * the new song will look like it was placed right after the current song.
                	 */
                    mApp.getService().decrementCurrentSongIndex();
                    mApp.getService().decrementEnqueueReorderScalar();

                    //Check which mediaPlayer is currently playing, and prepare the other mediaPlayer.
                    mApp.getService().prepareAlternateMediaPlayer();
                    return;

                } else if (from < mApp.getService().getCurrentSongIndex() && to > mApp.getService().getCurrentSongIndex()) {
                    //One of the previous songs was moved to a position after the current song. Move currentSongIndex back by 1.
                    mApp.getService().decrementCurrentSongIndex();
                    mApp.getService().decrementEnqueueReorderScalar();

                    //Check which mediaPlayer is currently playing, and prepare the other mediaPlayer.
                    mApp.getService().prepareAlternateMediaPlayer();
                    return;

                }

                //If the next song was reordered, reload it with the new index.
                if (mApp.getService().getPlaybackIndecesList().size() > (mApp.getService().getCurrentSongIndex()+1)) {
                    if (fromItem==mApp.getService().getPlaybackIndecesList().get(mApp.getService().getCurrentSongIndex()+1) ||
                            toItem==mApp.getService().getPlaybackIndecesList().get(mApp.getService().getCurrentSongIndex()+1)) {

                        //Check which mediaPlayer is currently playing, and prepare the other mediaPlayer.
                        mApp.getService().prepareAlternateMediaPlayer();

                    }

                } else {
                    //Check which mediaPlayer is currently playing, and prepare the other mediaPlayer.
                    mApp.getService().prepareAlternateMediaPlayer();

                }

            }

            //Fire a broadcast that notifies all listeners that the current queue order has changed.
            String[] updateFlags = { Common.NEW_QUEUE_ORDER };
            String[] flagValues = { "" };
            mApp.broadcastUpdateUICommand(updateFlags, flagValues);

        }

    };

    /**
     * Click remove interface for the ListView.
     */
    private DragSortListView.RemoveListener onRemove = new DragSortListView.RemoveListener() {

        @Override
        public void remove(int which) {

            //Stop the service if we just removed the last (and only) song.
            if (mApp.getService().getPlaybackIndecesList().size()==1) {
                mContext.stopService(new Intent(mContext, AudioPlaybackService.class));
                return;
            }

            //If the song that was removed is the next song, reload it.
            if (mApp.getService().getPlaybackIndecesList().size() > (mApp.getService().getCurrentSongIndex()+1)) {
                if (mListViewAdapter.getItem(which)==mApp.getService().getPlaybackIndecesList().get(mApp.getService().getCurrentSongIndex()+1)) {

                    //Check which mediaPlayer is currently playing, and prepare the other mediaPlayer.
                    mApp.getService().prepareAlternateMediaPlayer();

                } else if (mListViewAdapter.getItem(which)==mApp.getService().getPlaybackIndecesList().get(mApp.getService().getCurrentSongIndex())) {
                    mApp.getService().incrementCurrentSongIndex();
                    mApp.getService().prepareMediaPlayer(mApp.getService().getCurrentSongIndex());
                    mApp.getService().decrementCurrentSongIndex();
                } else if (mListViewAdapter.getItem(which) < mApp.getService().getPlaybackIndecesList().get(mApp.getService().getCurrentSongIndex())) {
                    mApp.getService().decrementCurrentSongIndex();
                }

            } else if (which==(mApp.getService().getPlaybackIndecesList().size()-1) &&
                    mApp.getService().getCurrentSongIndex()==(mApp.getService().getPlaybackIndecesList().size()-1)) {
                //The current song was the last one and it was removed. Time to back up to the previous song.
                mApp.getService().decrementCurrentSongIndex();
                mApp.getService().prepareMediaPlayer(mApp.getService().getCurrentSongIndex());
            } else {
                //Check which mediaPlayer is currently playing, and prepare the other mediaPlayer.
                mApp.getService().prepareAlternateMediaPlayer();

            }

            //Remove the item from the adapter.
            mListViewAdapter.remove(mListViewAdapter.getItem(which));

        }

    };

    @Override
    public void onResume() {
        super.onResume();
        checkServiceRunning();

    }

    public boolean isDrawerOpen() {
        return mDrawerOpen;
    }

    public void setIsDrawerOpen(boolean isOpen) {
        mDrawerOpen = isOpen;
    }

}
