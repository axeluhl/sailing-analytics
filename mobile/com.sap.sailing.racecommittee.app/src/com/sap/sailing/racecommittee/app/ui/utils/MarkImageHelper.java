package com.sap.sailing.racecommittee.app.ui.utils;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.MarkType;

public abstract class MarkImageHelper {
    protected MarkImageDescriptor defaultCourseMarkDescriptor;
    List<MarkImageDescriptor> markImageDescriptors;

    public LayerDrawable resolveMarkImage(Context context, Mark mark) {
        LayerDrawable drawable;
        MarkImageDescriptor result = defaultCourseMarkDescriptor;
        int highestCompatibilityLevel = -1;

        for (MarkImageDescriptor imageDescriptor : markImageDescriptors) {
            int compatibilityLevel = imageDescriptor.getCompatibilityLevel(mark.getType(), mark.getColor(), mark.getShape(), mark.getPattern());
            if (compatibilityLevel > highestCompatibilityLevel) {
                result = imageDescriptor;
                highestCompatibilityLevel = compatibilityLevel;
                if (highestCompatibilityLevel == 3) {
                    break;
                }
            }
        }

        if (result.getDrawableId() != 0) {
            drawable = new LayerDrawable(new Drawable[] { ContextCompat.getDrawable(context, result.getDrawableId()) });
        } else {
            drawable = result.getDrawable();
        }
        return drawable;
    }

    protected MarkImageDescriptor createMarkImageDescriptor(Context context, @DrawableRes int drawableId, MarkType type, String color, String shape,
        String pattern) {
        MarkImageDescriptor markIconDescriptor = new MarkImageDescriptor(context, drawableId, type, color, shape, pattern);
        markImageDescriptors.add(markIconDescriptor);

        return markIconDescriptor;
    }

}
