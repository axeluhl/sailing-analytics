package com.sap.sailing.racecommittee.app.ui.adapters.checked;

import com.sap.sailing.domain.common.racelog.RacingProcedureType;

public class StartProcedureItem extends CheckedItem {

    private RacingProcedureType mProcedureType;

    public StartProcedureItem(RacingProcedureType procedureType) {
        mProcedureType = procedureType;
    }

    public RacingProcedureType getProcedureType() {
        return mProcedureType;
    }

    @Override
    public String getText() {
        return mProcedureType.toString();
    }
}
