package com.sap.sailing.racecommittee.app.ui.adapters.unscheduled;

import com.sap.sailing.domain.common.racelog.Flags;

public class StartMode {
    
    private Flags mFlag;
    private Boolean mChecked = false;
    
    public StartMode(Flags flag) {
        this(flag, false);
    }
    
    public StartMode(Flags flag, Boolean checked) {
        mFlag = flag;
        mChecked = checked;
    }
    
    public Flags getFlag() {
        return mFlag;
    }
    
    public String getFlagName() {
        return mFlag.toString();
    }
    
    public Boolean isChecked() {
        return mChecked;
    }
    
    public void setChecked(Boolean checked) {
        mChecked = checked;
    }
}
