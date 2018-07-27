package com.sap.sse.datamining.ui.client;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;

/**
 * Interface for a result presenter composing multiple inner result presenters.
 * 
 * @author Lennart Hensler
 */
public interface CompositeResultsPresenter<SettingsType extends Settings> extends ResultsPresenter<SettingsType> {

    /**
     * @return The identifier for the currently active presenter
     */
    String getCurrentPresenterId();
    
    /**
     * @return All ids of the currently contained presenters
     */
    Iterable<String> getPresenterIds();
    
    void showResult(String presenterId, QueryResultDTO<?> result);
    void showError(String presenterId, String error);
    void showError(String presenterId, String mainError, Iterable<String> detailedErrors);
    void showBusyIndicator(String presenterId);

    @Override
    default void showResult(QueryResultDTO<?> result) {
        showResult(getCurrentPresenterId(), result);
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