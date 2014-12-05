package com.sap.sailing.racecommittee.app.ui.adapters.unscheduled;

public class StartProcedure {

    private Boolean mChecked;
    private String mClassName;
    private String mStartProcdure;

    public StartProcedure(String startProcedure, Boolean checked, String className) {
        mChecked = checked;
        mClassName = className;
        mStartProcdure = startProcedure;
    }

    public String getClassName() {
        return mClassName;
    }

    public Boolean isChecked() {
        return mChecked;
    }
    
    public void setChecked(Boolean check) {
        mChecked = check;
    }
    
    @Override
    public String toString() {
        return mStartProcdure;
    }
}
