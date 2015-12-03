package com.sap.sailing.android.tracking.app.ui.activities;

import android.os.Bundle;

import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.ui.fragments.EventFragment;
import com.sap.sailing.android.tracking.app.valueobjects.EventInfo;

public class EventActivity extends BaseActivity {

    public final static String EVENT = "event.info";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventInfo eventInfo = getIntent().getParcelableExtra(EVENT);

        setContentView(R.layout.activity_event);

        replaceFragment(R.id.container, EventFragment.newInstance(eventInfo));
    }
}
