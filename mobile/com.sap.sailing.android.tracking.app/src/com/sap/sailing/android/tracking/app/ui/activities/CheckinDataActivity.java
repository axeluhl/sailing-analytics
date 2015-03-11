package com.sap.sailing.android.tracking.app.ui.activities;

import com.sap.sailing.android.tracking.app.utils.CheckinManager;
import com.sap.sailing.android.tracking.app.valueobjects.CheckinData;

public abstract class CheckinDataActivity extends BaseActivity implements CheckinManager.CheckinDataHandler{

    @Override
    public abstract void onCheckinDataAvailable(CheckinData data);
}
