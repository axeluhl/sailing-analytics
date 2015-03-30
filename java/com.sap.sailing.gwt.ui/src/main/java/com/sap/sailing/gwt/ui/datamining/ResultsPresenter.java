package com.sap.sailing.gwt.ui.datamining;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.datamining.shared.QueryResult;

public interface ResultsPresenter<ResultType> {

    public Widget getWidget();
    
    public void showResult(QueryResult<ResultType> result);

    public void showError(String error);
    public void showError(String mainError, Iterable<String> detailedErrors);

    public void showBusyIndicator();

}
