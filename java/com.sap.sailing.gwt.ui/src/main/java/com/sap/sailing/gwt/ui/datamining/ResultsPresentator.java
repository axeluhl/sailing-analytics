package com.sap.sailing.gwt.ui.datamining;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.datamining.shared.QueryResult;

public interface ResultsPresentator<ResultType> {
    
    public void showResult(QueryResult<ResultType> result);

    public Widget getWidget();

}
