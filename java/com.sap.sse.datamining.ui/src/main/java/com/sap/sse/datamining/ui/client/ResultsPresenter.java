package com.sap.sse.datamining.ui.client;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.gwt.client.shared.components.Component;

public interface ResultsPresenter<SettingsType extends Settings> extends Component<SettingsType> {

    /**
     * @return The currently displayed result or <code>null</code>, if no result is shown.
     */
    QueryResultDTO<?> getCurrentResult();
    
    /**
     * @return The query definition of the currently displayed result or <code>null</code>,
     *         if not result is shown or no query definition has been set.
     */
    StatisticQueryDefinitionDTO getCurrentQueryDefinition();

    /**
     * Displays the given result for the given query definition. The given result may be
     * <code>null</code> to clear the presenter. The given query definition may be <code>null</code>,
     * but this is discouraged unless the result is also <code>null</code>.
     * 
     * @param queryDefinition The query definition of the results to display
     * @param result The results to display
     */
    void showResult(StatisticQueryDefinitionDTO queryDefinition, QueryResultDTO<?> result);
    
    void showError(String error);
    void showError(String mainError, Iterable<String> detailedErrors);
    void showBusyIndicator();

}
