package com.sap.sailing.server.operationaltransformation;

import java.util.logging.Logger;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;

public class RemoveSeries extends AbstractRacingEventServiceOperation<Void> {
    private static final long serialVersionUID = -2232723085937305299L;
    private static final Logger logger = Logger.getLogger(RemoveSeries.class.getName());
    private final RegattaIdentifier regattaIdentifier;
    private final String seriesName;
    
    public RemoveSeries(RegattaIdentifier identifier, String seriesName) {
        super();
        this.regattaIdentifier = identifier;
        this.seriesName = seriesName;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        Regatta regatta = toState.getRegatta(regattaIdentifier);
        if (regatta != null) {
            Series series = regatta.getSeriesByName(seriesName);
            if (series != null) {
                toState.removeSeries(series);
            } else {
                logger.warning("Could not find series " + seriesName);
            }
        } else {
            logger.warning("Couldn't find regatta "+regattaIdentifier);
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
