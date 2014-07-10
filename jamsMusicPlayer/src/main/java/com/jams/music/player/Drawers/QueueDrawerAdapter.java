package com.jams.music.player.Drawers;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.Helpers.SongHelper;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.R;
import com.jams.music.player.Utils.Common;

import java.util.ArrayList;

public class QueueDrawerAdapter extends ArrayAdapter<Integer> {

    private Context mContext;
    private Common mApp;
    private String mCurrentTheme;
    private int[] mColors;

    public QueueDrawerAdapter(Context context, ArrayList<Integer> playbackIndecesList) {
        super(context, R.layout.queue_drawer_list_layout, playbackIndecesList);

        mContext = context;
        mApp = (Common) mContext.getApplicationContext();
        mCurrentTheme = mApp.getSharedPreferences().getString("SELECTED_THEME", "LIGHT_CARDS_THEME");
        mColors = UIElementsHelper.getQuickScrollColors(context);
    }

    public View getView(final int position, View convertView, ViewGroup parent){

        QueueDrawerHolder holder;
        if (convertView==null) {
            convertView = LayoutInflater.from(mContext)
                                        .inflate(R.layout.queue_drawer_list_layout, parent, false);

            holder = new QueueDrawerHolder();
            holder.songTitleText = (TextView) convertView.findViewById(R.id.queue_song_title);
            holder.artistText = (TextView) convertView.findViewById(R.id.queue_song_artist);
            holder.removeSong = (ImageView) convertView.findViewById(R.id.queue_remove_song);

            holder.songTitleText.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
            holder.songTitleText.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Light"));
            holder.songTitleText.setPaintFlags(holder.songTitleText.getPaintFlags()
                                               | Paint.ANTI_ALIAS_FLAG
                                               | Paint.SUBPIXEL_TEXT_FLAG);

            holder.artistText.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Regular"));
            holder.artistText.setPaintFlags(holder.artistText.getPaintFlags()
                                            | Paint.ANTI_ALIAS_FLAG
                                            | Paint.SUBPIXEL_TEXT_FLAG);

            convertView.setTag(holder);
        } else {
            holder = (QueueDrawerHolder) convertView.getTag();
        }

        //Get the song's basic info.
        SongHelper songHelper = new SongHelper();
        songHelper.populateBasicSongData(mContext, position);

        holder.songTitleText.setText(songHelper.getTitle());
        holder.artistText.setText(songHelper.getArtist());

        //Apply the item's colors.
        if (position==mApp.getService().getCurrentSongIndex()) {
            convertView.setBackgroundColor(mColors[0]);
            holder.songTitleText.setTextColor(mColors[2]);
            holder.artistText.setTextColor(mColors[2]);
            holder.removeSong.setImageResource(R.drawable.cross_light);

        } else if (mCurrentTheme.equals("LIGHT_CARDS_THEME") ||
                   mCurrentTheme.equals("LIGHT_THEME")) {
            convertView.setBackgroundColor(0xFFFFFFFF);
            holder.songTitleText.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
            holder.artistText.setTextColor(UIElementsHelper.getSmallTextColor(mContext));
            holder.removeSong.setImageResource(R.drawable.cross);

        } else if (mCurrentTheme.equals("DARK_CARDS_THEME") ||
                   mCurrentTheme.equals("DARK_THEME")) {
            convertView.setBackgroundColor(0xFF191919);
            holder.songTitleText.setTextColor(UIElementsHelper.getThemeBasedTextColor(mContext));
            holder.artistText.setTextColor(UIElementsHelper.getSmallTextColor(mContext));
            holder.removeSong.setImageResource(R.drawable.cross_light);

        }

        return convertView;
    }

    class QueueDrawerHolder {
        public TextView songTitleText;
        public TextView artistText;
        public ImageView removeSong;
    }

}
