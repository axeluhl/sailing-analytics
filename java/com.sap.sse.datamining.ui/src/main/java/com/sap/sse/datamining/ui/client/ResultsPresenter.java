package com.sap.sse.datamining.ui.client;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.gwt.client.shared.components.Component;

public interface ResultsPresenter<SettingsType extends Settings> extends Component<SettingsType> {

    public QueryResultDTO<?> getCurrentResult();

    public void showResult(QueryResultDTO<?> result);

    public void showError(String error);

    public void showError(String mainError, Iterable<String> detailedErrors);

    public void showBusyIndicator();

}
