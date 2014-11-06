package com.sap.sailing.dashboards.gwt.client.startanalysis.rankingtable;

import com.google.gwt.user.cellview.client.Column;

public abstract class StartAnalysisStartRankTableRankAtFirstMarkColumn<T> extends Column<T, String> {

    public StartAnalysisStartRankTableRankAtFirstMarkColumn() {
        super(new StartAnalysisStartRankTableRankAtFirstMarkCell());
    }

    @Override
    public String getValue(T object) {
        return null;
    }
}
