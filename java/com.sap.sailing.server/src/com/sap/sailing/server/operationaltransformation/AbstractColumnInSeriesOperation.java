package com.sap.sailing.server.operationaltransformation;

import java.util.logging.Logger;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.server.RacingEventService;

public abstract class AbstractColumnInSeriesOperation<ResultType> extends AbstractSeriesOperation<ResultType> {
    private static final long serialVersionUID = -8985474505327170991L;
    private static final Logger logger = Logger.getLogger(AbstractColumnInSeriesOperation.class.getName());
    private final String columnName;

    public AbstractColumnInSeriesOperation(RegattaIdentifier regattaIdentifier, String seriesName, String columnName) {
        super(regattaIdentifier, seriesName);
        this.columnName = columnName;
    }

    protected String getColumnName() {
        return columnName;
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
