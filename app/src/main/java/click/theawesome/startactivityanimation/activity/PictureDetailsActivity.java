/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package click.theawesome.startactivityanimation.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import click.theawesome.startactivityanimation.BitmapUtils;
import click.theawesome.startactivityanimation.R;
import click.theawesome.startactivityanimation.ShadowLayout;

/**
 * This sub-activity shows a zoomed-in view of a specific photo, along with the
 * picture's text description. Most of the logic is for the animations that will
 * be run when the activity is being launched and exited. When launching,
 * the large version of the picture will resize from the thumbnail version in the
 * main activity, colorizing it from the thumbnail's grayscale version at the
 * same time. Meanwhile, the black background of the activity will fade in and
 * the description will eventually slide into place. The exit animation runs all
 * of this in reverse.
 */
public class PictureDetailsActivity extends Activity {

    private static final TimeInterpolator sDecelerator = new DecelerateInterpolator();
    private static final TimeInterpolator sAccelerator = new AccelerateInterpolator();
    private static final String PACKAGE_NAME = "click.theawesome.startactivityanimation";
    private static final int ANIM_DURATION = 500;

    private BitmapDrawable mBitmapDrawable;
    private ColorMatrix colorizerMatrix = new ColorMatrix();
    ColorDrawable mBackground;
    int mLeftDelta;
    int mTopDelta;
    float mWidthScale;
    float mHeightScale;
    private ImageView mImageView;
    private TextView mTextView;
    private FrameLayout mTopLevelLayout;
    private ShadowLayout mShadowLayout;
    private int mOriginalOrientation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picture_info);
        mImageView = (ImageView) findViewById(R.id.imageView);
        mTopLevelLayout = (FrameLayout) findViewById(R.id.topLevelLayout);
        mShadowLayout = (ShadowLayout) findViewById(R.id.shadowLayout);
        mTextView = (TextView) findViewById(R.id.description);

        // Retrieve the data we need for the picture/description to display and
        // the thumbnail to animate it from
        Bundle bundle = getIntent().getExtras();
        Bitmap bitmap = BitmapUtils.getBitmap(getResources(),
                bundle.getInt(PACKAGE_NAME + ".resourceId"));
        String description = bundle.getString(PACKAGE_NAME + ".description");
        final int thumbnailTop = bundle.getInt(PACKAGE_NAME + ".top");
        final int thumbnailLeft = bundle.getInt(PACKAGE_NAME + ".left");
        final int thumbnailWidth = bundle.getInt(PACKAGE_NAME + ".width");
        final int thumbnailHeight = bundle.getInt(PACKAGE_NAME + ".height");
        mOriginalOrientation = bundle.getInt(PACKAGE_NAME + ".orientation");

        mBitmapDrawable = new BitmapDrawable(getResources(), bitmap);
        mImageView.setImageDrawable(mBitmapDrawable);
        mTextView.setText(description);

        mBackground = new ColorDrawable(Color.WHITE);

        mTopLevelLayout.setBackgroundDrawable(mBackground);

//         Only run the animation if we're coming from the parent activity, not if
//         we're recreated automatically by the window manager (e.g., device rotation)
        if (savedInstanceState == null) {
            ViewTreeObserver observer = mImageView.getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    mImageView.getViewTreeObserver().removeOnPreDrawListener(this);

                    // Figure out where the thumbnail and full size versions are, relative
                    // to the screen and each other
                    int[] screenLocation = new int[2];
                    mImageView.getLocationOnScreen(screenLocation);
                    mLeftDelta = thumbnailLeft - screenLocation[0];
                    mTopDelta = thumbnailTop - screenLocation[1];

                    // Scale factors to make the large version the same size as the thumbnail
                    mWidthScale = (float) thumbnailWidth / mImageView.getWidth();
                    mHeightScale = (float) thumbnailHeight / mImageView.getHeight();

                    runEnterAnimation();

                    return true;
                }
            });
        }
    }

    /**
     * The enter animation scales the picture in from its previous thumbnail
     * size/location, colorizing it in parallel. In parallel, the background of the
     * activity is fading in. When the pictue is in place, the text description
     * drops down.
     */
    public void runEnterAnimation() {
        final long duration = (long) (ANIM_DURATION * MainActivity.sAnimatorScale);

        // Set starting values for properties we're going to animate. These
        // values scale and position the full size version down to the thumbnail
        // size/location, from which we'll animate it back up
        mImageView.setPivotX(0);
        mImageView.setPivotY(0);
        mImageView.setScaleX(mWidthScale);
        mImageView.setScaleY(mHeightScale);
        mImageView.setTranslationX(mLeftDelta);
        mImageView.setTranslationY(mTopDelta);

        // We'll fade the text in later
        mTextView.setAlpha(0);

        ViewPropertyAnimator viewPropertyAnimator =  mImageView.animate().setDuration(duration).
                scaleX(1).scaleY(1).
                translationX(0).translationY(0).
                setInterpolator(sDecelerator);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            viewPropertyAnimator.setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    startAnimation();
                }
            });
        } else {
            viewPropertyAnimator.withEndAction(new Runnable() {
                @Override
                public void run() {
                    startAnimation();
                }
            });
        }

        // Fade in the black background
        ObjectAnimator bgAnim = ObjectAnimator.ofInt(mBackground, "alpha", 0, 255);
        bgAnim.setDuration(duration);
        bgAnim.start();

        // Animate a color filter to take the image from grayscale to full color.
        // This happens in parallel with the image scaling and moving into place.
        ObjectAnimator colorizer = ObjectAnimator.ofFloat(PictureDetailsActivity.this,
                "saturation", 0, 1);
        colorizer.setDuration(duration);
        colorizer.start();

        // Animate a drop-shadow of the image
        ObjectAnimator shadowAnim = ObjectAnimator.ofFloat(mShadowLayout, "shadowDepth", 0, 1);
        shadowAnim.setDuration(duration);
        shadowAnim.start();
    }

    private void startAnimation() {
        new Runnable() {
            public void run() {
                mTextView.animate().setDuration(ANIM_DURATION/2).
                        translationY(0).alpha(1).
                        setInterpolator(sDecelerator);
            }
        }.run();
    }

    public void runExitAnimation() {
        final long duration = (long) (ANIM_DURATION * MainActivity.sAnimatorScale);

        ViewPropertyAnimator viewPropertyAnimator = mTextView.animate().translationY(-mTextView.getHeight()).alpha(0).
                setDuration(duration / 2).setInterpolator(sAccelerator);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            viewPropertyAnimator.setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    endAnimation();
                }
            });
        } else {
            viewPropertyAnimator.withEndAction(new Runnable() {
                @Override
                public void run() {
                    endAnimation();
                }
            });
        }

    }

    private void endAnimation() {
        final boolean fadeOut;
        if (getResources().getConfiguration().orientation != mOriginalOrientation) {
            mImageView.setPivotX(mImageView.getWidth() / 2);
            mImageView.setPivotY(mImageView.getHeight() / 2);
            mLeftDelta = 0;
            mTopDelta = 0;
            fadeOut = true;
        } else {
            fadeOut = false;
        }

        new Runnable() {
            public void run() {

                ViewPropertyAnimator viewPropertyAnimator = mImageView.animate().setDuration(ANIM_DURATION).
                        scaleX(mWidthScale).scaleY(mHeightScale).
                        translationX(mLeftDelta).translationY(mTopDelta);

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    viewPropertyAnimator.setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            finish();
                        }
                    });
                } else {
                    viewPropertyAnimator.withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    });
                }
                if (fadeOut) {
                    mImageView.animate().alpha(0);
                }
                // Fade out background
                ObjectAnimator bgAnim = ObjectAnimator.ofInt(mBackground, "alpha", 0);
                bgAnim.setDuration(ANIM_DURATION);
                bgAnim.start();

                // Animate the shadow of the image
                ObjectAnimator shadowAnim = ObjectAnimator.ofFloat(mShadowLayout,
                        "shadowDepth", 1, 0);
                shadowAnim.setDuration(ANIM_DURATION);
                shadowAnim.start();

                // Animate a color filter to take the image back to grayscale,
                // in parallel with the image scaling and moving into place.
                ObjectAnimator colorizer =
                        ObjectAnimator.ofFloat(PictureDetailsActivity.this,
                                "saturation", 1, 0);
                colorizer.setDuration(ANIM_DURATION);
                colorizer.start();
            }
        }.run();
    }

    /**
     * Overriding this method allows us to run our exit animation first, then exiting
     * the activity when it is complete.
     */
    @Override
    public void onBackPressed() {
        runExitAnimation();
    }

    /**
     * This is called by the colorizing animator. It sets a saturation factor that is then
     * passed onto a filter on the picture's drawable.
     *
     * @param value
     */
    public void setSaturation(float value) {
        colorizerMatrix.setSaturation(value);
        ColorMatrixColorFilter colorizerFilter = new ColorMatrixColorFilter(colorizerMatrix);
        mBitmapDrawable.setColorFilter(colorizerFilter);
    }

    @Override
    public void finish() {
        super.finish();

        // override transitions to skip the standard window animations
        overridePendingTransition(0, 0);
    }
}
