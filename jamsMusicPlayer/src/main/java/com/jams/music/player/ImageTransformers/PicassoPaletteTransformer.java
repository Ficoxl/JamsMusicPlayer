package com.jams.music.player.ImageTransformers;

import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
//import android.support.v7.graphics.Palette;
import android.view.View;

import com.squareup.picasso.Transformation;

public class PicassoPaletteTransformer implements Transformation {

    View mView;
    ColorDrawable bgDrawable;
    String mId;

    public PicassoPaletteTransformer(View view, String id) {
        mView = view;
        mId = id;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        //try {
            //Palette palette = Palette.generate(source);
            //if (mId.equals(mView.getTag()))
                //onGenerated(palette);
        //} catch (Exception e) {
            //e.printStackTrace();
            //mView.setBackgroundColor(0x99555555);
        //}

        return source;
    }

    @Override
    public String key() {
        return "trans#";
    }

    public void onGenerated(/* Palette palette */) {
        //try {
            //bgDrawable = new ColorDrawable(palette.getVibrantColor().getRgb());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                mView.setBackground(bgDrawable);
            else
                mView.setBackgroundDrawable(bgDrawable);
        //} catch (Exception e) {
            //e.printStackTrace();
            //mView.setBackgroundColor(0x99555555);
        //}

    }

}
