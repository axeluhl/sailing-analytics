package com.sap.sailing.server.operationaltransformation;

import java.util.logging.Logger;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.server.interfaces.RacingEventService;

public abstract class AbstractSeriesOperation<ResultType> extends AbstractRacingEventServiceOperation<ResultType> {
    private static final long serialVersionUID = -7786133902346491275L;
    private static final Logger logger = Logger.getLogger(AbstractSeriesOperation.class.getName());
    private final RegattaIdentifier regattaIdentifier;
    private final String seriesName;
    
    public AbstractSeriesOperation(RegattaIdentifier regattaIdentifier, String seriesName) {
        super();
        this.regattaIdentifier = regattaIdentifier;
        this.seriesName = seriesName;
    }

    protected RegattaIdentifier getRegattaIdentifier() {
        return regattaIdentifier;
    }

    protected String getSeriesName() {
        return seriesName;
    }
    
    protected Series getSeries(RacingEventService toState) {
        Series series = null;
        Regatta regatta = toState.getRegatta(getRegattaIdentifier());
        if (regatta != null) {
            series = regatta.getSeriesByName(getSeriesName());
            if (series == null) {
                logger.warning("series "+getSeriesName()+" not found in regatta "+regatta.getName());
            }
        } else {
            logger.warning("regatta "+getRegattaIdentifier()+" not found");
        }
        return series;
    }
}
