package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.startphase;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.sap.sailing.racecommittee.app.ui.fragments.panels.BasePanelFragment;

public class CheckedItemListAdapter extends ArrayAdapter<BasePanelFragment> {

    public CheckedItemListAdapter(Context context, int resource) {
        super(context, resource);
    }
}