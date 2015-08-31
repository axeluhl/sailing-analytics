package com.sap.sailing.racecommittee.app.ui.adapters.unscheduled;

import com.sap.sailing.domain.common.racelog.RacingProcedureType;

public class StartProcedure {

    private Boolean mChecked;
    private RacingProcedureType mProcedureType;

    public StartProcedure(RacingProcedureType procedureType, Boolean checked) {
        mChecked = checked;
        mProcedureType = procedureType;
    }

    public RacingProcedureType getProcedureType() {
        return mProcedureType;
    }

    public Boolean isChecked() {
        return mChecked;
    }
    
    public void setChecked(Boolean check) {
        mChecked = check;
    }
    
    @Override
    public String toString() {
        return mProcedureType.toString();
    }
}
