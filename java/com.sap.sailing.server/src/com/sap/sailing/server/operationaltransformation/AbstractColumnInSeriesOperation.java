package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.common.RegattaIdentifier;

public abstract class AbstractColumnInSeriesOperation<ResultType> extends AbstractSeriesOperation<ResultType> {
    private static final long serialVersionUID = -8985474505327170991L;
    private final String columnName;

    public AbstractColumnInSeriesOperation(RegattaIdentifier regattaIdentifier, String seriesName, String columnName) {
        super(regattaIdentifier, seriesName);
        this.columnName = columnName;
    }

    protected String getColumnName() {
        return columnName;
    }
}
