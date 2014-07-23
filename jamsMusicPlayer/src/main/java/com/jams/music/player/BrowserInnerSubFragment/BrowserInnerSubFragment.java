package com.jams.music.player.BrowserInnerSubFragment;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jams.music.player.Animations.FadeAnimation;
import com.jams.music.player.Animations.TranslateAnimation;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.R;
import com.jams.music.player.Utils.Common;
import com.jams.music.player.Utils.EaseInOutInterpolator;
import com.manuelpeinado.fadingactionbar.FadingActionBarHelper;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Callback;

/**
 * Used for inner/sub navigation screens such as browsing
 * an artist's albums, a genre's albums, etc. This fragment
 * should NOT be used for displaying individual songs. Use
 * VerticalListSubActivity instead.
 *
 * @author Saravan Pantham
 */
public class BrowserInnerSubFragment extends Fragment {

    //Context and common objects.
    private Context mContext;
    private Common mApp;

    //UI elements
    private ViewGroup mRootView;
    private ImageView mHeaderImage;
    private FadingActionBarHelper mFadingActionBarHelper;

    //Image scale/translate parameters.
    private static final int ANIM_DURATION = 300;
    private int mLeftDelta;
    private int mTopDelta;
    private float mWidthScale;
    private float mHeightScale;
    private int mOriginalOrientation;

    //Animation interpolators.
    private EaseInOutInterpolator easeOutInterpolator;
    private EaseInOutInterpolator easeInOutInterpolator;

    @Override
    public ViewGroup onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {

        mContext = getActivity().getApplicationContext();
        mApp = (Common) mContext;
        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_horizontal_list_sub, null);
        mRootView.setBackgroundColor(UIElementsHelper.getBackgroundColor(mContext));
        mRootView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }

        });

        //Init the interpolators.
        easeOutInterpolator = new EaseInOutInterpolator(EaseInOutInterpolator.EasingType.Type.OUT);
        easeInOutInterpolator = new EaseInOutInterpolator(EaseInOutInterpolator.EasingType.Type.INOUT);

        mHeaderImage = (ImageView) mRootView.findViewById(R.id.browser_inner_fragment_header_image);
        mHeaderImage.setVisibility(View.INVISIBLE);

        String headerImagePath = getArguments().getString("headerImagePath");
        mApp.getPicasso().load(headerImagePath).into(mHeaderImage, new Callback() {

            @Override
            public void onSuccess() {
                TranslateAnimation slideDown = new TranslateAnimation(mHeaderImage, 400, new DecelerateInterpolator(2.0f),
                                                                      View.VISIBLE, Animation.RELATIVE_TO_SELF,
                                                                      0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                                                                      Animation.RELATIVE_TO_SELF, -2.0f,
                                                                      Animation.RELATIVE_TO_SELF, 0.0f);

                slideDown.animate();
            }

            @Override
            public void onError() {

            }

        });

        return mRootView;
    }

    /**
     * Slides down the content layout as a part of the transitional
     * animation sequence. Called right after the header image has
     * been scaled into place.
     */
    private void animateContent() {


    }

}
