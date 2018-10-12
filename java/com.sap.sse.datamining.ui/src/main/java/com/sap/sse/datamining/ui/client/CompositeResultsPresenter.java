package com.sap.sse.datamining.ui.client;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;

/**
 * Interface for a result presenter composing multiple inner result presenters.
 * 
 * @author Lennart Hensler
 */
public interface CompositeResultsPresenter<SettingsType extends Settings> extends ResultsPresenter<SettingsType> {
    
    @FunctionalInterface
    public interface CurrentPresenterChangedListener {
        
        void currentPresenterChanged(String presenterId);
        
    }

    /**
     * @return The identifier for the currently active presenter
     */
    String getCurrentPresenterId();
    
    /**
     * @return All ids of the currently contained presenters
     */
    Iterable<String> getPresenterIds();
    
    /**
     * @param presenterId The id of the presenter to check for
     * @return <code>true</code>, if a presenter with the given id exists
     */
    boolean containsPresenter(String presenterId);
    
    /**
     * @param presenterId The id of the presenter to get the results from
     * @return The results of the presenter with the given id or <code>null</code>,
     *         if no presenter for the id exists. 
     */
    QueryResultDTO<?> getResult(String presenterId);
    
    /**
     * @param presenterId The id of the presenter to get the query definition from
     * @return The query definition of the presenter with the given id or <code>null</code>,
     *         if no presenter for the id exists. 
     */
    StatisticQueryDefinitionDTO getQueryDefinition(String presenterId);
    
    /**
     * Displays the given result for the given query definition int the presenter for the given id.
     * Does nothing if no presenter for the given id exists. The given result may be <code>null</code>
     * to clear the presenter. The given query definition may be <code>null</code>, but this is
     * discouraged unless the result is also <code>null</code>.
     * 
     * @param presenterId The id of the presenter used to show the result
     * @param queryDefinition The query definition of the result to display
     * @param result The result to display
     */
    void showResult(String presenterId, StatisticQueryDefinitionDTO queryDefinition, QueryResultDTO<?> result);
    
    /**
     * Shows the given error in the presenter for the given id. Does nothing if no
     * presenter for the given id exists.
     * 
     * @param presenterId The id of the presenter used to show the error
     * @param error The error message
     */
    void showError(String presenterId, String error);
    
    /**
     * Shows the given error in the presenter for the given id. Does nothing if no
     * presenter for the given id exists.
     * 
     * @param presenterId The id of the presenter used to show the error
     * @param mainError The main error message
     * @param detailedErrors The detailed error messages
     */
    void showError(String presenterId, String mainError, Iterable<String> detailedErrors);
    
    /**
     * Shows the busy indicator of the presenter for the given id. Does nothing if no
     * presenter for the given id exists.
     * 
     * @param presenterId The id of the presenter used to show the busy indicator
     */
    void showBusyIndicator(String presenterId);
    
    void addCurrentPresenterChangedListener(CurrentPresenterChangedListener listener);
    void removeCurrentPresenterChangedListener(CurrentPresenterChangedListener listener);
    
    @Override
    default QueryResultDTO<?> getCurrentResult() {
        return getResult(getCurrentPresenterId());
    }
    
    @Override
    default StatisticQueryDefinitionDTO getCurrentQueryDefinition() {
        return getQueryDefinition(getCurrentPresenterId());
    }

    @Override
    default void showResult(StatisticQueryDefinitionDTO queryDefinition, QueryResultDTO<?> result) {
        showResult(getCurrentPresenterId(), queryDefinition, result);
    }

    @Override
    default void showError(String error) {
        showError(getCurrentPresenterId(), error);
    }

    @Override
    default void showError(String mainError, Iterable<String> detailedErrors) {
        showError(getCurrentPresenterId(), mainError, detailedErrors);
    }

    @Override
    default void showBusyIndicator() {
        showBusyIndicator(getCurrentPresenterId());
    }

}