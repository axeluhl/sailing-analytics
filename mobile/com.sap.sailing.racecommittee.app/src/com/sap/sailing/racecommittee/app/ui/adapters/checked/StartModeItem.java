package com.sap.sailing.racecommittee.app.ui.adapters.checked;

import com.sap.sailing.domain.common.racelog.Flags;

public class StartModeItem extends CheckedItem {

    private Flags mFlag;

    public StartModeItem(Flags flag) {
        mFlag = flag;
    }

    public Flags getFlag() {
        return mFlag;
    }

    public String getFlagName() {
        return mFlag.toString();
    }

    @Override
    public String getText() {
        return getFlagName();
    }
}
