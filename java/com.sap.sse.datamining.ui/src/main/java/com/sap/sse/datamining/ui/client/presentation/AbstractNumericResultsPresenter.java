package com.sap.sse.datamining.ui.client.presentation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.ValueListBox;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.datamining.ui.client.presentation.dataproviders.AbstractNumericDataProvider;
import com.sap.sse.datamining.ui.client.presentation.dataproviders.AverageWithStatsDataProvider;
import com.sap.sse.datamining.ui.client.presentation.dataproviders.BearingDataProvider;
import com.sap.sse.datamining.ui.client.presentation.dataproviders.DataProvidersPrecedenceList;
import com.sap.sse.datamining.ui.client.presentation.dataproviders.DistanceDataProvider;
import com.sap.sse.datamining.ui.client.presentation.dataproviders.DurationDataProvider;
import com.sap.sse.datamining.ui.client.presentation.dataproviders.NumberDataProvider;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.controls.AbstractObjectRenderer;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public abstract class AbstractNumericResultsPresenter<SettingsType extends Settings>
        extends AbstractResultsPresenter<Settings> {

    private final DataProvidersPrecedenceList dataProviders;
    private final ValueListBox<String> dataSelectionListBox;
    private AbstractNumericDataProvider<? extends Object> currentDataProvider;

    public AbstractNumericResultsPresenter(Component<?> parent, ComponentContext<?> context) {
        super(parent, context);

        final List<AbstractNumericDataProvider<? extends Serializable>> basicProviders = Arrays.asList(
                new NumberDataProvider(), new DistanceDataProvider(), new DurationDataProvider(),
                new BearingDataProvider());

        final List<AbstractNumericDataProvider<? extends Serializable>> allProviders = new ArrayList<>(basicProviders);
        allProviders.add(new AverageWithStatsDataProvider(new DataProvidersPrecedenceList(basicProviders)));
        dataProviders = new DataProvidersPrecedenceList(allProviders);
        dataSelectionListBox = new ValueListBox<>(new AbstractObjectRenderer<String>() {
            @Override
            protected String convertObjectToString(String dataKey) {
                return currentDataProvider.getLocalizedNameForDataKey(getCurrentResult(), getDataMiningStringMessages(), dataKey);
            }
        });
        dataSelectionListBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                Map<GroupKey, Number> resultValues = currentDataProvider.getData(getCurrentResult(),
                        dataSelectionListBox.getValue());
                Map<GroupKey, Triple<Number, Number, Long>> errorMargins = currentDataProvider
                        .getErrorData(getCurrentResult(), dataSelectionListBox.getValue());
                internalShowNumericResults(resultValues, errorMargins);
            }
        });
        addControl(dataSelectionListBox);
    }

    /**
     * Adjusts, if necessary, the {@link #currentDataProvider} to the type of result, then requests from the
     * {@link #currentDataProvider} the mapping of the {@code result} to {@link Number}s for each group key. The
     * {@link Number}s returned by the provider will then be passed to {@link #internalShowNumericResults(Map, Map)} for
     * display.
     */
    protected void internalShowResults(QueryResultDTO<?> result) {
        currentDataProvider = dataProviders.selectCurrentDataProvider(result.getResultType());
        updateDataSelectionListBox();
        if (currentDataProvider != null) {
            Map<GroupKey, Number> resultValues = currentDataProvider.getData(getCurrentResult(),
                    dataSelectionListBox.getValue());
            Map<GroupKey, Triple<Number, Number, Long>> errorMargins = currentDataProvider
                    .getErrorData(getCurrentResult(), dataSelectionListBox.getValue());
            internalShowNumericResults(resultValues, errorMargins);
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override
                public void execute() {
                    presentationPanel.onResize();
                }
            });
        } else {
            showError(getDataMiningStringMessages().cantDisplayDataOfType(getCurrentResult().getResultType()));
        }
    }

    private void updateDataSelectionListBox() {
        if (currentDataProvider == null) {
            dataSelectionListBox.setAcceptableValues(Collections.<String> emptyList());
        } else {
            Collection<String> dataKeys = currentDataProvider.getDataKeys(getCurrentResult());
            String keyToSelect = currentDataProvider.getDefaultDataKeyFor(getCurrentResult());
            dataSelectionListBox.setValue(keyToSelect, false);
            dataSelectionListBox.setAcceptableValues(dataKeys);
        }
    }

    protected abstract void internalShowNumericResults(Map<GroupKey, Number> resultValues,
            Map<GroupKey, Triple<Number, Number, Long>> errorMargins);

    String getSelectedDataKey() {
        return dataSelectionListBox.getValue();
    }

    void setSelectedDataKey(String dataKey) {
        if (!currentDataProvider.isValidDataKey(getCurrentResult(), dataKey)) {
            throw new IllegalArgumentException("The given data key '" + dataKey + "' isn't valid");
        }
        dataSelectionListBox.setValue(dataKey, true);
    }

}
