package com.sap.sailing.racecommittee.app.ui.adapters.unscheduled;

public class StartMode {
    
    private String mFlag;
    private Boolean mChecked = false;
    
    public StartMode(String flag) {
        this(flag, false);
    }
    
    public StartMode(String flag, Boolean checked) {
        mFlag = flag;
        mChecked = checked;
    }
    
    public String getFlag() {
        return mFlag;
    }
    
    public Boolean isChecked() {
        return mChecked;
    }
    
    public void setChecked(Boolean checked) {
        mChecked = checked;
    }
}
