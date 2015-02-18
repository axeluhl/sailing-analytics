package com.sap.sailing.dashboards.gwt.client.startanalysis.rankingtable;

import com.google.gwt.user.cellview.client.Column;
import com.sap.sse.common.Util.Pair;

public abstract class StartAnalysisStartRankTableTeamCollumn<T> extends Column<T, Pair<String, String>> {

    public StartAnalysisStartRankTableTeamCollumn() {
        super(new StartAnalysisStartRankTableTeamCell());
    }

    @Override
    public Pair<String, String> getValue(T object) {
        return null;
    }
}