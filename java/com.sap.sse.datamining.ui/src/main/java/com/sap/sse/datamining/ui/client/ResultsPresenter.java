package com.sap.sse.datamining.ui.client;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.gwt.client.shared.components.Component;

public interface ResultsPresenter<SettingsType extends Settings> extends Component<SettingsType> {

    QueryResultDTO<?> getCurrentResult();

    void showResult(QueryResultDTO<?> result);
    void showError(String error);
    void showError(String mainError, Iterable<String> detailedErrors);
    void showBusyIndicator();

}
