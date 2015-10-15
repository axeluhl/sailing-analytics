package com.sap.sailing.racecommittee.app.ui.utils;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.utils.BitmapHelper;

public class BuoyHelper {

    public static LayerDrawable getBuoy(Context context, MarkType type, String color, String shape, String pattern) {
        LayerDrawable drawable = null;
        ArrayList<Drawable> layers = new ArrayList<>();
        Drawable shadowDrawable;
        Drawable outlineDrawable;
        Drawable colorDrawable;

        if (TextUtils.isEmpty(shape) && type == MarkType.BUOY) { // default buoy
            shadowDrawable = ContextCompat.getDrawable(context, R.drawable.buoy_shadow);

            int outlineColor = context.getResources().getIdentifier("buoy_outline_" + color, "color", context.getPackageName());
            if (outlineColor == 0) {
                outlineColor = R.color.buoy_outline;
            }
            outlineDrawable = BitmapHelper.getTintedDrawable(context, R.drawable.buoy_outline, context.getResources().getColor(outlineColor));

            int colorColor = context.getResources().getIdentifier("buoy_color_" + color, "color", context.getPackageName());
            if (colorColor == 0) {
                colorColor = R.color.buoy_color;
            }
            colorDrawable = BitmapHelper.getTintedDrawable(context, R.drawable.buoy_color, context.getResources().getColor(colorColor));

            layers.add(shadowDrawable);
            layers.add(outlineDrawable);
            layers.add(colorDrawable);

            drawable = new LayerDrawable(layers.toArray(new Drawable[layers.size()]));
        }

        return drawable;
    }
}
