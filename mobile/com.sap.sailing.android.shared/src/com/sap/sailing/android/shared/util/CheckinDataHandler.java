package com.sap.sailing.android.shared.util;

import com.sap.sailing.android.shared.data.BaseCheckinData;

public interface CheckinDataHandler<C extends BaseCheckinData> {
    public void onCheckinDataAvailable(C data);
}
