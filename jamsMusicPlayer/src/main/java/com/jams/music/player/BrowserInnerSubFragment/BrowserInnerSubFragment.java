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
    private RelativeLayout mContentLayout;
    private RelativeLayout mBackgroundLayout;
    private RelativeLayout mDetailsLayout;
    private TextView mHeaderText;
    private TextView mHeaderSubText;
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
        mRootView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }

        });

        mHeaderImage = (ImageView) mRootView.findViewById(R.id.browser_inner_fragment_header_image);
        mContentLayout = (RelativeLayout) mRootView.findViewById(R.id.browser_inner_fragment_content);
        mBackgroundLayout = (RelativeLayout) mRootView.findViewById(R.id.browser_inner_fragment_background);
        mDetailsLayout = (RelativeLayout) mRootView.findViewById(R.id.browser_inner_fragment_details_layout);
        mHeaderText = (TextView) mRootView.findViewById(R.id.browser_inner_fragment_details_header_text);
        mHeaderSubText = (TextView) mRootView.findViewById(R.id.browser_inner_fragment_details_header_subtext);

        //Apply fonts and text rendering properties.
        mHeaderText.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Light"));
        mHeaderText.setPaintFlags(mHeaderText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);

        mHeaderSubText.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Regular"));
        mHeaderSubText.setPaintFlags(mHeaderSubText.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);

        //Set the background colors.
        mBackgroundLayout.setBackgroundColor(UIElementsHelper.getBackgroundColor(mContext));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            mDetailsLayout.setBackgroundDrawable(UIElementsHelper.getGeneralActionBarBackground(mContext));
        else
            mDetailsLayout.setBackground(UIElementsHelper.getGeneralActionBarBackground(mContext));

        //Init the interpolators.
        easeOutInterpolator = new EaseInOutInterpolator(EaseInOutInterpolator.EasingType.Type.OUT);
        easeInOutInterpolator = new EaseInOutInterpolator(EaseInOutInterpolator.EasingType.Type.INOUT);

       /*
        * Retrieve the data we need for the picture to display
        * and animate from.
        */
        Bundle bundle = getArguments();
        String artworkPath = bundle.getString("albumArtPath");
        final int thumbnailTop = bundle.getInt("top");
        final int thumbnailLeft = bundle.getInt("left");
        final int thumbnailWidth = bundle.getInt("width");
        final int thumbnailHeight = bundle.getInt("height");
        mOriginalOrientation = bundle.getInt("orientation");

        mApp.getPicasso().load(artworkPath).into(mHeaderImage);

        /*
         * Only run the animation if we're coming from the parent activity and not if
         * we were recreated automatically by the window manager (e.g., device rotation).
         */
        if (savedInstanceState==null) {
            ViewTreeObserver observer = mHeaderImage.getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    mHeaderImage.getViewTreeObserver().removeOnPreDrawListener(this);

                    /*
                     * Figure out where the thumbnail and full size versions
                     * are, relative to the screen and each other.
                     */
                    int[] screenLocation = new int[2];
                    mHeaderImage.getLocationOnScreen(screenLocation);
                    mLeftDelta = thumbnailLeft - screenLocation[0];
                    mTopDelta = thumbnailTop - screenLocation[1];

                    // Scale factors to make the large version the same size as the thumbnail
                    mWidthScale = (float) thumbnailWidth / mHeaderImage.getWidth();
                    mHeightScale = (float) thumbnailHeight / mHeaderImage.getHeight();
                    runEnterAnimation();

                    return true;
                }

            });

        }

        return mRootView;
    }

    /**
     * The enter animation scales the picture in from its previous thumbnail
     * size/location.
     */
    public void runEnterAnimation() {

        /*
         * Set starting values for properties we're going to animate. These
         * values scale and position the full size version down to the thumbnail
         * size/location, from which we'll animate it back up.
        */
        mHeaderImage.setPivotX(0);
        mHeaderImage.setPivotY(0);
        mHeaderImage.setScaleX(mWidthScale);
        mHeaderImage.setScaleY(mHeightScale);
        mHeaderImage.setTranslationX(mLeftDelta);
        mHeaderImage.setTranslationY(mTopDelta);

        //Animate scaling and translation to go from thumbnail to full size.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            mHeaderImage.animate()
                        .setDuration(ANIM_DURATION)
                        .translationX(0)
                        .scaleX(1)
                        .scaleY(1)
                        .setInterpolator(easeInOutInterpolator)
                        .setListener(new Animator.AnimatorListener() {

                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                animateContent();
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }

                        });

        } else {
            mHeaderImage.animate()
                        .setDuration(ANIM_DURATION)
                        .translationX(0)
                        .scaleX(1)
                        .scaleY(1)
                        .setInterpolator(easeInOutInterpolator)
                        .withEndAction(new Runnable() {

                            @Override
                            public void run() {
                                animateContent();

                            }

                        });

        }

        //Moves the header image up to the top.
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                mHeaderImage.animate()
                            .setDuration(300)
                            .translationY(0)
                            .setInterpolator(easeInOutInterpolator);
            }

        }, 100);



        //Dim the background view.
        FadeAnimation fadeIn = new FadeAnimation(mBackgroundLayout, 400, 0.0f, 0.8f,
                                                 new DecelerateInterpolator());
        fadeIn.animate();

    }

    /**
     * Slides down the content layout as a part of the transitional
     * animation sequence. Called right after the header image has
     * been scaled into place.
     */
    private void animateContent() {
        //Slide down the details pane.
        /*TranslateAnimation slideDownDetails = new TranslateAnimation(mContentLayout, 300,
                                                                     new DecelerateInterpolator(2.0f),
                                                                     View.VISIBLE,
                                                                     Animation.RELATIVE_TO_SELF, 0.0f,
                                                                     Animation.RELATIVE_TO_SELF, 0.0f,
                                                                     Animation.RELATIVE_TO_SELF, -0.5f,
                                                                     Animation.RELATIVE_TO_SELF, 0.0f);*/

        //Slide down the content view.
        TranslateAnimation slideDown = new TranslateAnimation(mContentLayout, 400,
                                                              new DecelerateInterpolator(2.0f),
                                                              View.VISIBLE,
                                                              Animation.RELATIVE_TO_SELF, 0.0f,
                                                              Animation.RELATIVE_TO_SELF, 0.0f,
                                                              Animation.RELATIVE_TO_SELF, -0.5f,
                                                              Animation.RELATIVE_TO_SELF, 0.0f);

        //slideDownDetails.animate();
        slideDown.animate();

    }

}
