package com.sap.sse.datamining.ui.selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.Util;
import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.AggregationProcessorDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.ui.AggregatorDefinitionChangedListener;
import com.sap.sse.datamining.ui.AggregatorDefinitionProvider;
import com.sap.sse.datamining.ui.DataMiningServiceAsync;
import com.sap.sse.datamining.ui.DataRetrieverChainDefinitionChangedListener;
import com.sap.sse.datamining.ui.DataRetrieverChainDefinitionProvider;
import com.sap.sse.datamining.ui.ExtractionFunctionChangedListener;
import com.sap.sse.datamining.ui.ExtractionFunctionProvider;
import com.sap.sse.datamining.ui.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.shared.components.AbstractComponent;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.controls.AbstractObjectRenderer;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class ExtractionFunctionAndAggregatorDefinitionProvider extends AbstractComponent<SerializableSettings>
        implements ExtractionFunctionProvider<SerializableSettings>, AggregatorDefinitionProvider<SerializableSettings>,
        DataRetrieverChainDefinitionChangedListener {

    private static final String STATISTIC_PROVIDER_ELEMENT_STYLE = "statisticProviderElement";

    private final StringMessages stringMessages;
    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    private final Collection<ExtractionFunctionChangedListener> extractionFunctionListeners;
    private final Collection<AggregatorDefinitionChangedListener> aggregatorDefinitionListeners;

    private boolean isAwaitingReload;
    private DataRetrieverChainDefinitionDTO currentRetrieverChainDefinition;

    private final FlowPanel mainPanel;
    private final ValueListBox<FunctionDTO> extractionFunctionListBox;
    private final ValueListBox<AggregationProcessorDefinitionDTO> aggregatorDefinitionListBox;

    public ExtractionFunctionAndAggregatorDefinitionProvider(Component<?> parent, ComponentContext<?> context,
            StringMessages stringMessages, DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter,
            DataRetrieverChainDefinitionProvider retrieverChainProvider) {
        super(parent, context);
        this.stringMessages = stringMessages;
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        extractionFunctionListeners = new HashSet<>();
        aggregatorDefinitionListeners = new HashSet<>();

        isAwaitingReload = false;
        currentRetrieverChainDefinition = null;

        HorizontalPanel selectionPanel = new HorizontalPanel();
        extractionFunctionListBox = createExtractionFunctionListBox();
        extractionFunctionListBox.addStyleName(STATISTIC_PROVIDER_ELEMENT_STYLE);
        selectionPanel.add(extractionFunctionListBox);
        aggregatorDefinitionListBox = createAggregatorListBox();
        aggregatorDefinitionListBox.addStyleName(STATISTIC_PROVIDER_ELEMENT_STYLE);
        selectionPanel.add(aggregatorDefinitionListBox);

        mainPanel = new FlowPanel();
        Label label = new Label(this.stringMessages.calculateThe());
        label.addStyleName(STATISTIC_PROVIDER_ELEMENT_STYLE);
        mainPanel.add(label);
        mainPanel.add(selectionPanel);

        retrieverChainProvider.addDataRetrieverChainDefinitionChangedListener(this);
    }

    private ValueListBox<FunctionDTO> createExtractionFunctionListBox() {
        ValueListBox<FunctionDTO> extractionFunctionListBox = new ValueListBox<>(
                new AbstractObjectRenderer<FunctionDTO>() {
                    @Override
                    protected String convertObjectToString(FunctionDTO function) {
                        return function.getDisplayName();
                    }
                });
        extractionFunctionListBox.addValueChangeHandler(new ValueChangeHandler<FunctionDTO>() {
            @Override
            public void onValueChange(ValueChangeEvent<FunctionDTO> event) {
                notifyExtractionFunctionListeners();
                updateAggregatorDefinitions();
            }
        });
        return extractionFunctionListBox;
    }

    private ValueListBox<AggregationProcessorDefinitionDTO> createAggregatorListBox() {
        ValueListBox<AggregationProcessorDefinitionDTO> aggregatorListBox = new ValueListBox<AggregationProcessorDefinitionDTO>(
                new AbstractObjectRenderer<AggregationProcessorDefinitionDTO>() {
                    @Override
                    protected String convertObjectToString(AggregationProcessorDefinitionDTO nonNullObject) {
                        return nonNullObject.getDisplayName();
                    }
                });
        aggregatorListBox.addValueChangeHandler(new ValueChangeHandler<AggregationProcessorDefinitionDTO>() {
            @Override
            public void onValueChange(ValueChangeEvent<AggregationProcessorDefinitionDTO> event) {
                notifyAggregatorDefinitionListeners();
            }
        });
        return aggregatorListBox;
    }

    @Override
    public void awaitReloadComponents() {
        isAwaitingReload = true;
    }

    @Override
    public boolean isAwatingReload() {
        return isAwaitingReload;
    }

    @Override
    public void reloadComponents() {
        isAwaitingReload = false;
        updateExtractionFunctions();
    }

    @Override
    public void dataRetrieverChainDefinitionChanged(DataRetrieverChainDefinitionDTO newRetrieverChainDefinition) {
        if (!Objects.equals(currentRetrieverChainDefinition, newRetrieverChainDefinition)) {
            currentRetrieverChainDefinition = newRetrieverChainDefinition;
            if (!isAwaitingReload) {
                updateExtractionFunctions();
            }
        }
    }

    private void updateExtractionFunctions() {
        if (currentRetrieverChainDefinition == null) {
            updateListBox(extractionFunctionListBox, Collections.emptyList());
        } else {
            dataMiningService.getStatisticsFor(currentRetrieverChainDefinition,
                    LocaleInfo.getCurrentLocale().getLocaleName(), new AsyncCallback<HashSet<FunctionDTO>>() {
                        @Override
                        public void onSuccess(HashSet<FunctionDTO> functions) {
                            List<FunctionDTO> sortedFunctions = new ArrayList<>(functions);
                            Collections.sort(sortedFunctions);
                            updateListBox(extractionFunctionListBox, sortedFunctions);
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError(
                                    "Error fetching the available statistics from the server: " + caught.getMessage());
                        }
                    });
        }
    }

    private void updateAggregatorDefinitions() {
        if (getExtractionFunction() == null) {
            updateListBox(aggregatorDefinitionListBox, Collections.emptyList());
        } else {
            dataMiningService.getAggregatorDefinitionsFor(getExtractionFunction(),
                    LocaleInfo.getCurrentLocale().getLocaleName(),
                    new AsyncCallback<HashSet<AggregationProcessorDefinitionDTO>>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError(
                                    "Error fetching the available aggregators from the server: " + caught.getMessage());
                        }

                        @Override
                        public void onSuccess(HashSet<AggregationProcessorDefinitionDTO> definitions) {
                            List<AggregationProcessorDefinitionDTO> sortedDefinitions = new ArrayList<>(definitions);
                            Collections.sort(sortedDefinitions);
                            updateListBox(aggregatorDefinitionListBox, sortedDefinitions);
                        }
                    });
        }
    }

    private <T> void updateListBox(ValueListBox<T> listBox, Collection<T> acceptableValues) {
        T currentValue = listBox.getValue();
        T valueToBeSelected = acceptableValues.contains(currentValue) ? currentValue : Util.first(acceptableValues);

        listBox.setValue(valueToBeSelected, true);
        listBox.setAcceptableValues(acceptableValues);
    }

    @Override
    public void applyQueryDefinition(StatisticQueryDefinitionDTO queryDefinition) {
        extractionFunctionListBox.setValue(queryDefinition.getStatisticToCalculate());
        aggregatorDefinitionListBox.setValue(queryDefinition.getAggregatorDefinition(), false);
    }

    private void notifyExtractionFunctionListeners() {
        FunctionDTO extractionFunction = getExtractionFunction();
        for (ExtractionFunctionChangedListener listener : extractionFunctionListeners) {
            listener.extractionFunctionChanged(extractionFunction);
        }
    }

    private void notifyAggregatorDefinitionListeners() {
        AggregationProcessorDefinitionDTO aggregatorDefinition = getAggregatorDefinition();
        for (AggregatorDefinitionChangedListener listener : aggregatorDefinitionListeners) {
            listener.aggregatorDefinitionChanged(aggregatorDefinition);
        }
    }

    @Override
    public void addExtractionFunctionChangedListener(ExtractionFunctionChangedListener listener) {
        extractionFunctionListeners.add(listener);
    }

    @Override
    public void addAggregatorDefinitionChangedListener(AggregatorDefinitionChangedListener listener) {
        aggregatorDefinitionListeners.add(listener);
    }

    @Override
    public FunctionDTO getExtractionFunction() {
        return extractionFunctionListBox.getValue();
    }

    @Override
    public AggregationProcessorDefinitionDTO getAggregatorDefinition() {
        return aggregatorDefinitionListBox.getValue();
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.statisticProvider();
    }

    @Override
    public Widget getEntryWidget() {
        return mainPanel;
    }

    @Override
    public boolean isVisible() {
        return mainPanel.isVisible();
    }

    @Override
    public void setVisible(boolean visibility) {
        mainPanel.setVisible(visibility);
    }

    @Override
    public boolean hasSettings() {
        return false;
    }

    @Override
    public SettingsDialogComponent<SerializableSettings> getSettingsDialogComponent(SerializableSettings settings) {
        return null;
    }

    @Override
    public void updateSettings(SerializableSettings newSettings) {
        // no-op
    }

    @Override
    public String getDependentCssClassName() {
        return "simpleStatisticsProvider";
    }

    @Override
    public SerializableSettings getSettings() {
        return null;
    }

    @Override
    public String getId() {
        return "SimpleStatisticProvider";
    }
}
