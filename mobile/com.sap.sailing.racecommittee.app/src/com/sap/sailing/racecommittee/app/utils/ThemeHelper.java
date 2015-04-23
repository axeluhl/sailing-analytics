package com.sap.sailing.racecommittee.app.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
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

    public static int getThemeOffset(Context context) {
        int offset = 0;
        if (AppConstants.LIGHT_THEME.equals(AppPreferences.on(context).getTheme())) {
            offset = 10;
        }
        return offset;
    }

    public static boolean setPickerTextColor(Context context, TimePicker timePicker, int color) {
        Resources system = Resources.getSystem();
        int hourId = system.getIdentifier("hour", "id", "android");
        int minuteId = system.getIdentifier("minute", "id", "android");
        int amPmId = system.getIdentifier("amPm", "id", "android");

        NumberPicker hourPicker = (NumberPicker) timePicker.findViewById(hourId);
        NumberPicker minutePicker = (NumberPicker) timePicker.findViewById(minuteId);
        NumberPicker amPmPicker = (NumberPicker) timePicker.findViewById(amPmId);

        if (hourPicker != null && setPickerTextColor(context, hourPicker, color)) {
            if (minutePicker != null && setPickerTextColor(context, minutePicker, color)) {
                return true;
            }
        }
        return false;
    }

    public static boolean setPickerTextColor(Context context, NumberPicker numberPicker, int color) {
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
                    return true;
                } catch (NoSuchFieldException e) {
                    ExLog.w(context, TAG, "NoSuchFieldException - " + e.getMessage());
                } catch (IllegalAccessException e) {
                    ExLog.w(context, TAG, "IllegalAccessException - " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    ExLog.w(context, TAG, "IllegalArgumentException - " + e.getMessage());
                }
            }
        }
        return false;
    }

    public static @ColorRes int getColor(Context context, @AttrRes int colorId) {
        int color = 0;
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        if (theme.resolveAttribute(colorId, typedValue, true)) {
            color = context.getResources().getColor(typedValue.resourceId);
        }
        return color;
    }
}
