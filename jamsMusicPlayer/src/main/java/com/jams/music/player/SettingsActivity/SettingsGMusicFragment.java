package com.jams.music.player.SettingsActivity;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jams.music.player.Utils.Common;

/**
 * @author Saravan Pantham
 */
public class SettingsGMusicFragment extends Fragment {

    private Context mContext;
    private Common mApp;
    private View mRootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity().getApplicationContext();
        mApp = (Common) mContext;


        return mRootView;
    }

}
