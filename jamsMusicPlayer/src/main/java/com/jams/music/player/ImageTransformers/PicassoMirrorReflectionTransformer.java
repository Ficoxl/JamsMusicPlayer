package com.jams.music.player.ImageTransformers;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;

import com.squareup.picasso.Transformation;

/**
 * Returns a bitmap image that is reflected over
 * the Y axis.
 *
 */
public class PicassoMirrorReflectionTransformer implements Transformation {

    @Override
    public Bitmap transform(Bitmap bitmap) {
        return mirrorBitmap(bitmap);
    }

    public Bitmap mirrorBitmap(Bitmap image) {
        // The gap we want between the reflection and the original image
        final int reflectionGap = 0;
        Bitmap originalImage = image;

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        //Fip the image over the Y axis.
        Matrix matrix = new Matrix();
        matrix.preScale(1, -1);

        /*
         * Create a bitmap with the flip matix applied to it.
         * We only want the bottom half of the image
         */
        Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0,
                height / 2, width, height / 2, matrix, false);

        //Create a new bitmap with same width but taller to fit reflection
        Bitmap bitmapWithReflection = Bitmap.createBitmap(width,(height + height / 2), Bitmap.Config.ARGB_8888);

        /*
         * Create a new Canvas with the bitmap that's big enough for
         * the image, the gap, and the reflection.
         */
        Canvas canvas = new Canvas(bitmapWithReflection);

        //Draw in the original image.
        canvas.drawBitmap(originalImage, 0, 0, null);

        //Draw in the reflected image.
        canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);

        if (originalImage!=null && !originalImage.isRecycled())
            originalImage.recycle();

        if (image!=null && !image.isRecycled())
            image.recycle();

        return bitmapWithReflection;
    }

    @Override
    public String key() {
        return "mirror";
    }

}
