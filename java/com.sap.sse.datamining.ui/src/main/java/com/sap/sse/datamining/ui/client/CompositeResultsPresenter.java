package com.sap.sse.datamining.ui.client;

import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;

/**
 * Interface for a result presenter composing multiple inner result presenters. Presenters have a {@link String} ID by
 * which they can be found and addressed. Only one result presenter is shown at a time; this is the "current"
 * result presenter (see {@link #getCurrentPresenterId()}).
 * 
 * @author Lennart Hensler
 */
public interface CompositeResultsPresenter<SettingsType extends Settings> extends ResultsPresenter<SettingsType> {
    
    /**
     * @see CompositeResultsPresenter#addCurrentPresenterChangedListener(CurrentPresenterChangedListener)
     */
    @FunctionalInterface
    public interface CurrentPresenterChangedListener {
        /**
         * Invoked when a different result presented has been selected by the user and has now become visible.
         * 
         * @param presenterId
         *            the ID of the presenter now visible
         */
        void currentPresenterChanged(String presenterId);
    }
    
    /**
     * @see CompositeResultsPresenter#addPresenterRemovedListener(PresenterRemovedListener)
     */
    @FunctionalInterface
    public interface PresenterRemovedListener {
        /**
         * Invoked when a presenter is removed from this composite results presenter.
         */
        void onPresenterRemoved(String presenterId, int presenterIndex, StatisticQueryDefinitionDTO queryDefinition);
    }

    /**
     * @return The identifier for the currently active presenter
     */
    String getCurrentPresenterId();
    
    /**
     * @return The index of the currently active presenter
     */
    int getCurrentPresenterIndex();
    
    /**
     * @return All ids of the currently contained presenters
     */
    Iterable<String> getPresenterIds();
    
    /**
     * @param presenterId
     *            The id of the presenter to check for
     * @return <code>true</code>, if a presenter with the given id exists
     */
    boolean containsPresenter(String presenterId);
    
    /**
     * @param presenterId
     *            The id of the presenter to the the index of
     * @return The index of the presenter with the given id or -1, if no presenter for the id exists.
     */
    int getPresenterIndex(String presenterId);
    
    /**
     * @param presenterId
     *            The id of the presenter to get the results from
     * @return The results of the presenter with the given id or <code>null</code>, if no presenter for the id exists.
     */
    QueryResultDTO<?> getResult(String presenterId);
    
    /**
     * @param presenterId
     *            The id of the presenter to get the query definition from
     * @return The query definition of the presenter with the given id or <code>null</code>, if no presenter for the id
     *         exists.
     */
    StatisticQueryDefinitionDTO getQueryDefinition(String presenterId);
    
    /**
     * Displays the given result for the given query definition in the presenter for the given id. Does nothing if no
     * presenter for the given id exists. The given result may be <code>null</code> to clear the presenter. The given
     * query definition may be <code>null</code>, but this is discouraged unless the result is also <code>null</code>.
     * 
     * @param presenterId
     *            The id of the presenter used to show the result
     * @param queryDefinition
     *            The query definition of the result to display
     * @param result
     *            The result to display
     */
    void showResult(String presenterId, StatisticQueryDefinitionDTO queryDefinition, QueryResultDTO<?> result);
    
    /**
     * Display the given query result pairs. Any results that are currently displayed will be overridden and additional
     * child presenters will be created if necessary. No {@link CurrentPresenterChangedListener} or
     * {@link PresenterRemovedListener} registered through
     * {@link #addCurrentPresenterChangedListener(CurrentPresenterChangedListener)} and
     * {@link #addPresenterRemovedListener(PresenterRemovedListener)}, respectively, will be notified by calling this
     * method. It is assumed that the caller is entirely replacing all results at once and not incrementally
     * manipulating, e.g., a report.
     * 
     * @param results
     *            The query result pairs to display
     */
    void showResults(Iterable<Pair<StatisticQueryDefinitionDTO, QueryResultDTO<?>>> results);
    
    /**
     * Shows the given error in the presenter for the given id. Does nothing if no presenter for the given id exists.
     * 
     * @param presenterId
     *            The id of the presenter used to show the error
     * @param queryDefinition TODO
     * @param error
     *            The error message
     */
    void showError(String presenterId, StatisticQueryDefinitionDTO queryDefinition, String error);
    
    /**
     * Shows the given error in the presenter for the given id. Does nothing if no presenter for the given id exists.
     * 
     * @param presenterId
     *            The id of the presenter used to show the error
     * @param queryDefinition TODO
     * @param mainError
     *            The main error message
     * @param detailedErrors
     *            The detailed error messages
     */
    void showError(String presenterId, StatisticQueryDefinitionDTO queryDefinition, String mainError, Iterable<String> detailedErrors);
    
    /**
     * Shows the busy indicator of the presenter for the given id. Does nothing if no presenter for the given id exists.
     * 
     * @param presenterId
     *            The id of the presenter used to show the busy indicator
     */
    void showBusyIndicator(String presenterId);
    
    void addCurrentPresenterChangedListener(CurrentPresenterChangedListener listener);
    void removeCurrentPresenterChangedListener(CurrentPresenterChangedListener listener);
    
    void addPresenterRemovedListener(PresenterRemovedListener listener);
    void removePresenterRemovedListener(PresenterRemovedListener listener);
    
    @Override
    default QueryResultDTO<?> getCurrentResult() {
        return getResult(getCurrentPresenterId());
    }
    
    @Override
    default StatisticQueryDefinitionDTO getCurrentQueryDefinition() {
        return getQueryDefinition(getCurrentPresenterId());
    }

    /**
     * Shows the result in the {@link #getCurrentPresenterId() currently active presenter}.
     * 
     * @see #showResult(String, StatisticQueryDefinitionDTO, QueryResultDTO)
     */
    @Override
    default void showResult(StatisticQueryDefinitionDTO queryDefinition, QueryResultDTO<?> result) {
        showResult(getCurrentPresenterId(), queryDefinition, result);
    }

    @Override
    default void showError(StatisticQueryDefinitionDTO queryDefinition, String error) {
        showError(getCurrentPresenterId(), queryDefinition, error);
    }

    @Override
    default void showError(String mainError, Iterable<String> detailedErrors, StatisticQueryDefinitionDTO queryDefinition) {
        showError(getCurrentPresenterId(), queryDefinition, mainError, detailedErrors);
    }

    @Override
    default void showBusyIndicator() {
        showBusyIndicator(getCurrentPresenterId());
    }

}