package com.sap.sailing.android.shared.ui.utils;

import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

import android.content.Context;

/**
 * Factory for creating a tracker to be associated with a new barcode. The multi-processor uses this factory to create
 * barcode trackers as needed -- one for each barcode.
 */
public class BarcodeTrackerFactory implements MultiProcessor.Factory<Barcode> {
    private Context mContext;

    public BarcodeTrackerFactory(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public Tracker<Barcode> create(Barcode barcode) {
        return new BarcodeGraphicTracker(mContext);
    }
}
