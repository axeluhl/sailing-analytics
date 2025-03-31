package com.sap.sailing.racecommittee.app.utils;

import android.text.InputFilter;
import android.text.Spanned;

public class RangeInputFilter implements InputFilter {

    private double mMin;
    private double mMax;

    public RangeInputFilter(double min, double max) {
        mMin = min;
        mMax = max;
    }

    public RangeInputFilter(String min, String max) {
        mMin = Double.parseDouble(min);
        mMax = Double.parseDouble(max);
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            double input = Double.parseDouble(dest.toString() + source.toString());
            if (isInRange(mMin, mMax, input))
                return null;
        } catch (NumberFormatException nfe) {
        }
        return "";
    }

    private boolean isInRange(double a, double b, double input) {
        return b > a ? input >= a && input <= b : input >= b && input <= a;
    }
}