package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;

public class MoveColumnInSeriesDown extends AbstractColumnInSeriesOperation<Void> {
    private static final long serialVersionUID = 1055871052852494542L;

    public MoveColumnInSeriesDown(RegattaIdentifier regattaIdentifier, String seriesName, String columnName) {
        super(regattaIdentifier, seriesName, columnName);
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        Series series = getSeries(toState);
        if (series != null) {
            series.moveRaceColumnDown(getColumnName());
            if (series.getRegatta().isPersistent()) {
                toState.updateStoredRegatta(series.getRegatta());
            }
        }
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        // TODO Auto-generated method stub
        return null;
    }

}
