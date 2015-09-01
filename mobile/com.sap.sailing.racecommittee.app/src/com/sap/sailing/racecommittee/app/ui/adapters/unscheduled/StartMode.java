package com.sap.sailing.racecommittee.app.ui.adapters.unscheduled;

import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.ui.adapters.coursedesign.CheckedListItem;

public class StartMode extends CheckedListItem{
    
    private Flags mFlag;
    
    public StartMode(Flags flag) {
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
