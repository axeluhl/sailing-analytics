package com.sap.sailing.android.shared.ui.customviews;

public enum GPSQuality {
    noSignal(0), poor(2), good(3), great(4);

    private final int mQuality;

    GPSQuality(int quality) {
        mQuality = quality;
    }

    public int toInt() {
        return this.mQuality;
    }

    public static GPSQuality getValue(int value) {
        GPSQuality result = null;
        for (GPSQuality item : GPSQuality.values()) {
            if (item.toInt() == value) {
                result = item;
                break;
            }
        }
        return result;
    }
}