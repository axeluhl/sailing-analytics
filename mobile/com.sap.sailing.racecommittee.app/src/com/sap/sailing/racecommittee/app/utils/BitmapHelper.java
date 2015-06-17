package com.sap.sailing.racecommittee.app.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.util.TypedValue;
import android.view.View;

public class BitmapHelper {

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static Bitmap decodeSampleBitmapFromFile(String fileName, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fileName, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(fileName, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    // @SuppressWarnings, but it is handled correctly
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Drawable getAttrDrawable(Context context, @AttrRes int attrRes) {
        Drawable drawable = null;
        TypedValue value = new TypedValue();
        if (context.getTheme().resolveAttribute(attrRes, value, true)) {
            String[] data = String.valueOf(value.string).split("/");
            int resId = context.getResources().getIdentifier(data[2].substring(0, data[2].length() - 4), "drawable", context.getPackageName());
            if (resId != 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    drawable = context.getDrawable(resId);
                } else {
                    drawable = context.getResources().getDrawable(resId);
                }
            }
        }
        return drawable;
    }

    public static Drawable getAttrDrawable(Context context, String attr) {
        int attrRes = context.getResources().getIdentifier(attr, "attr", context.getPackageName());
        if (attrRes != 0) {
            return getAttrDrawable(context, attrRes);
        }
        return null;
    }

    // @SuppressWarnings, but it is handled correctly
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setBackground(View view, Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(drawable);
        }
        else{
            view.setBackgroundDrawable(drawable);
        }
    }

    // @SuppressWarnings, but it is handled correctly
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Drawable getDrawable(Context context, @DrawableRes int id) {
        Drawable drawable;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawable = context.getDrawable(id);
        } else {
            drawable = context.getResources().getDrawable(id);
        }
        return drawable;
    }

    public static Drawable getTintedDrawable(Context context, @DrawableRes int drawableResId, @ColorRes int color) {
        Drawable drawable = BitmapHelper.getDrawable(context, drawableResId);
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        return drawable;
    }
}
