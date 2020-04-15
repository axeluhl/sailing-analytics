package com.sap.sailing.racecommittee.app.utils;

import java.util.Calendar;
import java.util.Locale;

import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.racecommittee.app.R;

import android.content.Context;
import android.support.annotation.Nullable;
import android.widget.NumberPicker;
import android.widget.TimePicker;

public class TimeUtils extends com.sap.sailing.android.shared.util.TimeUtils {

    public static void initTimePickerWithSeconds(Context context, final Calendar time,
            @Nullable final TimePicker timePicker, @Nullable NumberPicker secondPicker) {
        int textColor = ThemeHelper.getColor(context, R.attr.white);
        int dividerColor = ThemeHelper.getColor(context, R.attr.sap_yellow_1);
        if (timePicker != null) {
            ViewHelper.disableSave(timePicker);
            ThemeHelper.setPickerColor(context, timePicker, textColor, dividerColor);
            timePicker.setIs24HourView(true);
            timePicker.setCurrentHour(time.get(Calendar.HOUR_OF_DAY));
        }
        if (secondPicker != null) {
            ViewHelper.disableSave(secondPicker);
            ThemeHelper.setPickerColor(context, secondPicker, textColor, dividerColor);
            secondPicker.setMinValue(0);
            secondPicker.setMaxValue(59);
            secondPicker.setFormatter(new NumberPicker.Formatter() {
                @Override
                public String format(int value) {
                    return String.format(Locale.US, "%02d", value);
                }
            });
            secondPicker.setValue(time.get(Calendar.SECOND));
            secondPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    if (timePicker != null) {
                        Calendar pickerTime = (Calendar) time.clone();
                        pickerTime.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                        pickerTime.set(Calendar.MINUTE, timePicker.getCurrentMinute());
                        if (oldVal == 0 && newVal == 59) {
                            pickerTime.add(Calendar.MINUTE, -1);
                        }
                        if (oldVal == 59 && newVal == 0) {
                            pickerTime.add(Calendar.MINUTE, 1);
                        }
                        timePicker.setCurrentHour(pickerTime.get(Calendar.HOUR_OF_DAY));
                        timePicker.setCurrentMinute(pickerTime.get(Calendar.MINUTE));
                    }
                }
            });
        }
    }
}
