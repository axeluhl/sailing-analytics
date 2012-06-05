package com.sap.sailing.server.operationaltransformation;

import java.util.logging.Logger;

import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class RenameColumnInSeries extends AbstractColumnInSeriesOperation<Void> {
    private static final long serialVersionUID = -6772077267645894432L;
    private final static Logger logger = Logger.getLogger(RenameColumnInSeries.class.getName());
    private final String newColumnName;
    
    public RenameColumnInSeries(RegattaIdentifier regattaIdentifier, String seriesName, String columnName, String newColumnName) {
        super(regattaIdentifier, seriesName, columnName);
        this.newColumnName = newColumnName;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        Series series = getSeries(toState);
        if (series != null) {
            RaceColumnInSeries raceColumn = series.getRaceColumnByName(getColumnName());
            if (raceColumn != null) {
                RaceColumnInSeries existingRaceColumnWithNewName = series.getRaceColumnByName(newColumnName);
                if (existingRaceColumnWithNewName != null) {
                    logger.info("Not renaming column "+getColumnName()+" in series "+series.getName()+" in regatta "+series.getRegatta().getName()+
                            " to "+newColumnName+" because a column of that name already exists");
                } else {
                    raceColumn.setName(newColumnName);
                    if (series.getRegatta().isPersistent()) {
                        // TODO update regatta in DB
                    }
                }
            } else {
                logger.warning("Didn't find race column " + getColumnName() + " in series " + series.getName()
                        + " in regatta " + series.getRegatta().getName());
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
