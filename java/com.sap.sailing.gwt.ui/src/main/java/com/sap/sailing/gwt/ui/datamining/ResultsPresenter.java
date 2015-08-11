package com.sap.sailing.gwt.ui.datamining;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;

public interface ResultsPresenter<ResultType> {

    public Widget getWidget();
    
    public QueryResultDTO<ResultType> getCurrentResult();
    
    public void showResult(QueryResultDTO<ResultType> result);

    public void showError(String error);
    public void showError(String mainError, Iterable<String> detailedErrors);

    public void showBusyIndicator();

}
