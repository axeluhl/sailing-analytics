package com.sap.sailing.android.shared.util;

import com.sap.sailing.android.shared.data.AbstractCheckinData;

public interface CheckinDataHandler {
    public void onCheckinDataAvailable(AbstractCheckinData data);
}
