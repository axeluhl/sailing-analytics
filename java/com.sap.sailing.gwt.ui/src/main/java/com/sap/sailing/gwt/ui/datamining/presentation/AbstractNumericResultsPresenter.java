package com.sap.sailing.gwt.ui.datamining.presentation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.ValueListBox;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.AbstractObjectRenderer;
import com.sap.sailing.gwt.ui.datamining.presentation.dataproviders.AbstractNumericDataProvider;
import com.sap.sailing.gwt.ui.datamining.presentation.dataproviders.BearingDataProvider;
import com.sap.sailing.gwt.ui.datamining.presentation.dataproviders.DistanceDataProvider;
import com.sap.sailing.gwt.ui.datamining.presentation.dataproviders.DurationDataProvider;
import com.sap.sailing.gwt.ui.datamining.presentation.dataproviders.NumberDataProvider;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public abstract class AbstractNumericResultsPresenter<SettingsType extends Settings> extends AbstractResultsPresenter<Settings> {
    
    private final NumberDataProvider numberDataProvider;
    private final Map<String, AbstractNumericDataProvider<? extends Object>> dataProviders;
    private final ValueListBox<String> dataSelectionListBox;
    private AbstractNumericDataProvider<? extends Object> currentDataProvider;

    public AbstractNumericResultsPresenter(Component<?> parent, ComponentContext<?> context,
            StringMessages stringMessages) {
        super(parent, context, stringMessages);
        numberDataProvider = new NumberDataProvider();
        dataProviders = new HashMap<>();
        AbstractNumericDataProvider<Distance> distanceDataProvider = new DistanceDataProvider();
        dataProviders.put(distanceDataProvider.getResultType().getName(), distanceDataProvider);
        AbstractNumericDataProvider<Duration> durationDataProvider = new DurationDataProvider();
        dataProviders.put(durationDataProvider.getResultType().getName(), durationDataProvider);
        AbstractNumericDataProvider<Bearing> bearingDataProvider = new BearingDataProvider();
        dataProviders.put(bearingDataProvider.getResultType().getName(), bearingDataProvider);
        dataSelectionListBox = new ValueListBox<>(new AbstractObjectRenderer<String>() {
            @Override
            protected String convertObjectToString(String dataKey) {
                return currentDataProvider.getLocalizedNameForDataKey(stringMessages, dataKey);
            }
        });
        dataSelectionListBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                Map<GroupKey, Number> resultValues = currentDataProvider.getData(getCurrentResult(), dataSelectionListBox.getValue());
                Map<GroupKey, Pair<Number, Number>> errorMargins = currentDataProvider.getErrorData(getCurrentResult(), dataSelectionListBox.getValue());
                internalShowNumericResult(resultValues, errorMargins);
            }
        });
        addControl(dataSelectionListBox);
    }
    
    /**
     * Adjusts, if necessary, the {@link #currentDataProvider} to the type of result, then requests from the
     * {@link #currentDataProvider} the mapping of the {@code result} to {@link Number}s for each group key.
     * The {@link Number}s returned by the provider will then be passed to {@link #internalShowNumericResult(Map, Map)}
     * for display.
     */
    protected void internalShowResults(QueryResultDTO<?> result) {
        currentDataProvider = selectCurrentDataProvider();
        updateDataSelectionListBox();
        if (currentDataProvider != null) {
            Map<GroupKey, Number> resultValues = currentDataProvider.getData(getCurrentResult(), dataSelectionListBox.getValue());
            Map<GroupKey, Pair<Number, Number>> errorMargins = currentDataProvider.getErrorData(getCurrentResult(), dataSelectionListBox.getValue());
            internalShowNumericResult(resultValues, errorMargins);
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
    
    private AbstractNumericDataProvider<? extends Object> selectCurrentDataProvider() {
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

    protected abstract void internalShowNumericResult(Map<GroupKey, Number> resultValues, Map<GroupKey, Pair<Number, Number>> errorMargins);

    String getSelectedDataKey() {
        return dataSelectionListBox.getValue();
    }
    
    void setSelectedDataKey(String dataKey) {
        if (!currentDataProvider.isValidDataKey(dataKey)) {
            throw new IllegalArgumentException("The given data key '" + dataKey + "' isn't valid");
        }
        dataSelectionListBox.setValue(dataKey, true);
    }

}
