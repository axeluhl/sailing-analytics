package com.sap.sailing.racecommittee.app.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorRes;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TimePicker;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.R;

import java.lang.reflect.Field;

public class ThemeHelper {

    private static final String TAG = ThemeHelper.class.getName();

    public static void setTheme(Activity activity) {
        String theme = AppPreferences.on(activity).getTheme();
        if (AppConstants.LIGHT_THEME.equals(theme)) {
            activity.setTheme(R.style.AppTheme_Light);
        } else {
            activity.setTheme(R.style.AppTheme_Dark);
        }
    }

    public static void setPickerTextColor(Context context, TimePicker timePicker, @ColorRes int color) {
        NumberPicker hourPicker = getHourPicker(timePicker);
        if (hourPicker != null) {
            setPickerTextColor(context, hourPicker, color);
        }

        NumberPicker minutePicker = getMinutePicker(timePicker);
        if (minutePicker != null) {
            setPickerTextColor(context, minutePicker, color);
        }
    }

    public static void setPickerTextColor(Context context, NumberPicker numberPicker, @ColorRes int color) {
        final int count = numberPicker.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = numberPicker.getChildAt(i);
            if (child instanceof EditText) {
                try {
                    Field selectorWheelPaintField = numberPicker.getClass().getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);
                    ((Paint) selectorWheelPaintField.get(numberPicker)).setColor(color);
                    ((EditText) child).setTextColor(color);
                    numberPicker.invalidate();
                } catch (NoSuchFieldException e) {
                    ExLog.w(context, TAG, "NoSuchFieldException - " + e.getMessage());
                } catch (IllegalAccessException e) {
                    ExLog.w(context, TAG, "IllegalAccessException - " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    ExLog.w(context, TAG, "IllegalArgumentException - " + e.getMessage());
                }
            }
        }
    }

    public static void setPickerDividerColor(Context context, TimePicker timePicker, @ColorRes int color) {
        NumberPicker hourPicker = getHourPicker(timePicker);
        if (hourPicker != null) {
            setPickerDividerColor(context, hourPicker, color);
        }

        NumberPicker minutePicker = getMinutePicker(timePicker);
        if (minutePicker != null) {
            setPickerDividerColor(context, minutePicker, color);
        }
    }

    public static void setPickerDividerColor(Context context, NumberPicker numberPicker, @ColorRes int color) {
        try {
            ColorDrawable drawable = new ColorDrawable(color);
            Field selectionDivider = numberPicker.getClass().getDeclaredField("mSelectionDivider");
            selectionDivider.setAccessible(true);
            selectionDivider.set(numberPicker, drawable);
        } catch (NoSuchFieldException e) {
            ExLog.w(context, TAG, "NoSuchFieldException - " + e.getMessage());
        } catch (IllegalArgumentException e) {
            ExLog.w(context, TAG, "IllegalArgumentException - " + e.getMessage());
        } catch (IllegalAccessException e) {
            ExLog.w(context, TAG, "IllegalAccessException - " + e.getMessage());
        }
    }

    public static void setPickerColor(Context context, TimePicker timePicker, @ColorRes int textColor, @ColorRes int dividerColor) {
        setPickerTextColor(context, timePicker, textColor);
        setPickerDividerColor(context, timePicker, dividerColor);
    }

    public static void setPickerColor(Context context, NumberPicker numberPicker, @ColorRes int textColor, @ColorRes int dividerColor) {
        setPickerTextColor(context, numberPicker, textColor);
        setPickerDividerColor(context, numberPicker, dividerColor);
    }

    public static
    @ColorRes
    int getColor(Context context, @AttrRes int colorId) {
        int color = 0;
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        if (theme.resolveAttribute(colorId, typedValue, true)) {
            color = context.getResources().getColor(typedValue.resourceId);
        }
        return color;
    }

    private static NumberPicker getHourPicker(TimePicker timePicker) {
        Resources system = Resources.getSystem();
        int hourId = system.getIdentifier("hour", "id", "android");
        NumberPicker hourPicker = (NumberPicker) timePicker.findViewById(hourId);
        return hourPicker;
    }

    private static NumberPicker getMinutePicker(TimePicker timePicker) {
        Resources system = Resources.getSystem();
        int minuteId = system.getIdentifier("minute", "id", "android");
        NumberPicker minutePicker = (NumberPicker) timePicker.findViewById(minuteId);
        return minutePicker;
    }
}
