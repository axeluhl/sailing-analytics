package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;

public class AddColumnToSeries extends AbstractColumnInSeriesOperation<RaceColumnInSeries> {
    private static final long serialVersionUID = 8987540636040301063L;
    
    /**
     * -1 means append
     */
    private final int insertIndex;
    
    public AddColumnToSeries(RegattaIdentifier regattaIdentifier, String seriesName, String columnName) {
        this(/* insertIndex==-1 means append */ -1, regattaIdentifier, seriesName, columnName);
    }
    
    public AddColumnToSeries(int insertIndex, RegattaIdentifier regattaIdentifier, String seriesName, String columnName) {
        super(regattaIdentifier, seriesName, columnName);
        this.insertIndex = insertIndex;
    }

    @Override
    public RaceColumnInSeries internalApplyTo(RacingEventService toState) throws Exception {
        RaceColumnInSeries result = null;
        Series series = getSeries(toState);
        if (series != null) {
            if (insertIndex == -1) {
                result = series.addRaceColumn(getColumnName(), toState);
            } else {
                result = series.addRaceColumn(insertIndex, getColumnName(), toState);
            }
            if (result != null && series.getRegatta().isPersistent()) {
                toState.updateStoredRegatta(series.getRegatta());
            }
        }
        return result;
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
