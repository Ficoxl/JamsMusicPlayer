package com.jams.music.player.HorizListSubFragment;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.jams.music.player.Animations.FadeAnimation;
import com.jams.music.player.Animations.TranslateAnimation;
import com.jams.music.player.R;
import com.jams.music.player.Utils.Common;
import com.jams.music.player.Utils.EaseInOutInterpolator;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

/**
 * Used for inner/sub navigation screens such as browsing
 * an artist's albums, a genre's albums, etc. This fragment
 * should NOT be used for displaying individual songs. Use
 * VerticalListSubActivity instead.
 *
 * @author Saravan Pantham
 */
public class HorizListSubFragment extends Fragment {

    //Context and common objects.
    private Context mContext;
    private Common mApp;

    //UI elements
    private ViewGroup mRootView;
    private CircularImageView mCircularActionButton;
    private ImageView mHeaderImage;
    private RelativeLayout mContentLayout;
    private RelativeLayout mBackgroundLayout;

    //Image scale/translate parameters.
    private static final int ANIM_DURATION = 225;
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

        mCircularActionButton = (CircularImageView) mRootView.findViewById(R.id.horiz_list_sub_activity_circular_action);
        mHeaderImage = (ImageView) mRootView.findViewById(R.id.horiz_list_sub_activity_header_image);
        mContentLayout = (RelativeLayout) mRootView.findViewById(R.id.horiz_list_sub_activity_content);
        mBackgroundLayout = (RelativeLayout) mRootView.findViewById(R.id.horiz_list_sub_activity_background);

        //Apply a subtle shadow to the circular action button.
        mCircularActionButton.setBorderWidth(0);
        mCircularActionButton.addShadow();

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

        mApp.getImageLoader().loadImage(artworkPath, mApp.getDisplayImageOptions(), new ImageLoadingListener() {

            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                mHeaderImage.setImageBitmap(bitmap);
                mCircularActionButton.setImageBitmap(bitmap);

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

                            /* Figure out where the thumbnail and full size versions
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

            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }

        });

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

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    mHeaderImage.animate()
                                .setDuration(300)
                                .translationY(0)
                                .setInterpolator(easeInOutInterpolator);
                }

            }, 100);

        }

        //Dim the background view.
        FadeAnimation fadeIn = new FadeAnimation(mBackgroundLayout, 400, 0.0f, 0.8f,
                                                 new DecelerateInterpolator());
        fadeIn.animate();

    }

    /**
     * Scales in the main action button. Also slides down the
     * content layout as a part of the transitional animation
     * sequence. Called right after the header image has been
     * scaled into place.
     */
    private void animateContent() {
        //Scale in the action button.
        int pivotX = mCircularActionButton.getWidth()/2;
        int pivotY = mCircularActionButton.getHeight()/2;
        final ScaleAnimation scaleIn = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
                                                          pivotX, pivotY);
        scaleIn.setDuration(300);
        scaleIn.setAnimationListener(scaleInListener);
        scaleIn.setInterpolator(easeOutInterpolator);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                mCircularActionButton.startAnimation(scaleIn);
            }

        }, 500);

        //Slide down the content view.
        TranslateAnimation slideDown = new TranslateAnimation(mContentLayout, 400,
                                                              new DecelerateInterpolator(2.0f),
                                                              View.VISIBLE,
                                                              Animation.RELATIVE_TO_SELF, 0.0f,
                                                              Animation.RELATIVE_TO_SELF, 0.0f,
                                                              Animation.RELATIVE_TO_SELF, -0.5f,
                                                              Animation.RELATIVE_TO_SELF, 0.0f);

        slideDown.animate();

    }

    /**
     * Action button scale animation listener.
     */
    private Animation.AnimationListener scaleInListener = new Animation.AnimationListener() {

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mCircularActionButton.setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }

    };

}
