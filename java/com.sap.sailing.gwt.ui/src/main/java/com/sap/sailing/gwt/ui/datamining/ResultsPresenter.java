package com.sap.sailing.gwt.ui.datamining;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.gwt.client.shared.components.Component;

public interface ResultsPresenter<ResultType, SettingsType extends Settings> extends Component<SettingsType> {

    public QueryResultDTO<?> getCurrentResult();
    
    public void showResult(QueryResultDTO<ResultType> result);

    public void showError(String error);
    public void showError(String mainError, Iterable<String> detailedErrors);

    public void showBusyIndicator();

}
