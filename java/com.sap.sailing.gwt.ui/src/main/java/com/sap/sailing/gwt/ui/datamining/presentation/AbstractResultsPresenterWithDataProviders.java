package com.sap.sailing.gwt.ui.datamining.presentation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.datamining.presentation.dataproviders.AbstractResultDataProvider;
import com.sap.sailing.gwt.ui.datamining.presentation.dataproviders.DistanceDataProvider;
import com.sap.sailing.gwt.ui.datamining.presentation.dataproviders.NumberDataProvider;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;

public abstract class AbstractResultsPresenterWithDataProviders extends AbstractResultsPresenter<Object> {
    
    private final NumberDataProvider numberDataProvider;
    private final Map<String, AbstractResultDataProvider<? extends Object>> dataProviders;
    private AbstractResultDataProvider<? extends Object> currentDataProvider;

    public AbstractResultsPresenterWithDataProviders(StringMessages stringMessages) {
        super(stringMessages);
        numberDataProvider = new NumberDataProvider();
        dataProviders = new HashMap<>();
        AbstractResultDataProvider<Distance> distanceDataProvider = new DistanceDataProvider();
        dataProviders.put(distanceDataProvider.getResultType().getName(), distanceDataProvider);
    }
    
    protected void internalShowResults(QueryResultDTO<Object> result) {
        currentDataProvider = selectCurrentDataProvider();
        updateDataSelectionListBox();
        if (currentDataProvider != null) {
            Map<GroupKey, Number> resultValues = currentDataProvider.getData(getCurrentResult(), dataSelectionListBox.getValue());
            internalShowNumberResult(resultValues);
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override
                public void execute() {
                    presentationPanel.onResize();
                }
            });
        } else {
            showError(getStringMessages().cantDisplayDataOfType(getCurrentResult().getResultType()));
        }
    }
    
    @Override
    protected void onDataSelectionValueChange() {
        Map<GroupKey, Number> resultValues = currentDataProvider.getData(getCurrentResult(), dataSelectionListBox.getValue());
        internalShowNumberResult(resultValues);
    }
    
    private AbstractResultDataProvider<? extends Object> selectCurrentDataProvider() {
        if (numberDataProvider.acceptsResultsOfType(getCurrentResult().getResultType())) {
            return numberDataProvider;
        }
        return dataProviders.get(getCurrentResult().getResultType());
    }
    
    private void updateDataSelectionListBox() {
        if (currentDataProvider == null) {
            dataSelectionListBox.setAcceptableValues(Collections.<String>emptyList());
        } else {
            Collection<String> dataKeys = currentDataProvider.getDataKeys();
            String keyToSelect = currentDataProvider.getDefaultDataKeyFor(getCurrentResult());
            dataSelectionListBox.setValue(keyToSelect, false);
            dataSelectionListBox.setAcceptableValues(dataKeys);
        }
    }

    protected abstract void internalShowNumberResult(Map<GroupKey, Number> resultValues);

}
