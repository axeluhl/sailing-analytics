package com.sap.sailing.dashboards.gwt.client.widgets.startanalysis.rankingtable;

import com.google.gwt.user.cellview.client.Column;

/**
 * @author Alexander Ries (D062114)
 *
 */

public abstract class StartAnalysisStartRankTableTextColumn<T> extends Column<T, String> {

    public StartAnalysisStartRankTableTextColumn() {
        super(new StartAnalysisStartRankTableTextCell());
    }

    @Override
    public String getValue(T object) {
        return null;
    }
}

