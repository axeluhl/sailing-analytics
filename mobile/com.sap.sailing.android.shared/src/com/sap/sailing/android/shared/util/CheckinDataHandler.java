package com.sap.sailing.android.shared.util;

import com.sap.sailing.android.shared.data.BaseCheckinData;

public interface CheckinDataHandler {
    public void onCheckinDataAvailable(BaseCheckinData data);
}
