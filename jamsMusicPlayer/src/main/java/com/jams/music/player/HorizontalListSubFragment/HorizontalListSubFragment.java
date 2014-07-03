package com.jams.music.player.HorizontalListSubFragment;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.Circle;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.R;
import com.jams.music.player.Utils.Common;
import com.mikhaellopez.circularimageview.CircularImageView;

/**
 * Used for inner/sub navigation screens such as browsing
 * an artist's albums, a genre's albums, etc. This fragment
 * should NOT be used for displaying individual songs. Use
 * VerticalListSubFragment instead.
 *
 * @author Saravan Pantham
 */
public class HorizontalListSubFragment extends Fragment {

    private Context mContext;
    private Common mApp;
    private ViewGroup mRootView;

    private TextView mHeaderText;
    private CircularImageView mProfileImage;
    private ImageView mHeaderImage;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_horizontal_list_sub, null);
        mContext = getActivity().getApplicationContext();
        mApp = (Common) mContext;

        mHeaderText = (TextView) mRootView.findViewById(R.id.horiz_list_sub_activity_header_text);
        mProfileImage = (CircularImageView) mRootView.findViewById(R.id.horiz_list_sub_activity_header_profile);
        mHeaderImage = (ImageView) mRootView.findViewById(R.id.horiz_list_sub_activity_header_image);

        mHeaderText.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Thin"));
        mHeaderText.setPaintFlags(mHeaderText.getPaintFlags()
                                  | Paint.ANTI_ALIAS_FLAG
                                  | Paint.SUBPIXEL_TEXT_FLAG);

        //Apply a subtle shadow to the circular profile image.
        mProfileImage.setBorderWidth(0);
        mProfileImage.addShadow();

        return mRootView;
    }

}
