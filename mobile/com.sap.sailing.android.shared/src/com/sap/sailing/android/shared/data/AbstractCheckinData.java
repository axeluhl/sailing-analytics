package com.sap.sailing.android.shared.data;

public abstract class AbstractCheckinData {
    protected boolean update;

    public boolean isUpdate(){
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }
}
