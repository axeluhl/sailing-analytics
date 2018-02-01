package com.sap.sailing.gwt.ui.datamining.selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.AbstractObjectRenderer;
import com.sap.sailing.gwt.ui.datamining.DataMiningServiceAsync;
import com.sap.sailing.gwt.ui.datamining.DataRetrieverChainDefinitionProvider;
import com.sap.sailing.gwt.ui.datamining.StatisticChangedListener;
import com.sap.sailing.gwt.ui.datamining.StatisticProvider;
import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.AggregationProcessorDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.shared.components.AbstractComponent;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class SimpleStatisticProvider extends AbstractComponent<SerializableSettings> implements StatisticProvider<SerializableSettings> {
    
    private static final String STATISTIC_PROVIDER_ELEMENT_STYLE = "statisticProviderElement";
    
    private final StringMessages stringMessages;
    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    private final Set<StatisticChangedListener> listeners;
    
    private boolean isAwaitingReload;
    private DataRetrieverChainDefinitionDTO currentRetrieverChainDefinition;
    private final Collection<FunctionDTO> extractionFunctions;
    private final Collection<AggregationProcessorDefinitionDTO> aggregatorDefinitions;
    
    private final FlowPanel mainPanel;
    private final ValueListBox<FunctionDTO> extractionFunctionListBox;
    private final ValueListBox<AggregationProcessorDefinitionDTO> aggregatorListBox;

    public SimpleStatisticProvider(Component<?> parent, ComponentContext<?> context, StringMessages stringMessages,
            DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter,
                                   DataRetrieverChainDefinitionProvider retrieverChainProvider) {
        super(parent, context);
        this.stringMessages = stringMessages;
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        listeners = new HashSet<StatisticChangedListener>();
        
        isAwaitingReload = false;
        currentRetrieverChainDefinition = null;
        extractionFunctions = new HashSet<>();
        aggregatorDefinitions = new HashSet<>();
        
        HorizontalPanel selectionPanel = new HorizontalPanel();
        extractionFunctionListBox = createExtractionFunctionListBox();
        extractionFunctionListBox.addStyleName(STATISTIC_PROVIDER_ELEMENT_STYLE);
        selectionPanel.add(extractionFunctionListBox);
        aggregatorListBox = createAggregatorListBox();
        aggregatorListBox.addStyleName(STATISTIC_PROVIDER_ELEMENT_STYLE);
        selectionPanel.add(aggregatorListBox);
        
        mainPanel = new FlowPanel();
        Label label = new Label(this.stringMessages.calculateThe());
        label.addStyleName(STATISTIC_PROVIDER_ELEMENT_STYLE);
        mainPanel.add(label);
        mainPanel.add(selectionPanel);

        retrieverChainProvider.addDataRetrieverChainDefinitionChangedListener(this);
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
        updateContent();
    }
    
    @Override
    public void dataRetrieverChainDefinitionChanged(DataRetrieverChainDefinitionDTO newRetrieverChainDefinition) {
        if (!Objects.equals(currentRetrieverChainDefinition, newRetrieverChainDefinition)) {
            currentRetrieverChainDefinition = newRetrieverChainDefinition;
            if (!isAwaitingReload && currentRetrieverChainDefinition != null) {
                updateContent();
            } else if (!isAwaitingReload) {
                clearContent();
            }
        }
    }

    private void updateContent() {
        dataMiningService.getStatisticsFor(currentRetrieverChainDefinition, LocaleInfo.getCurrentLocale().getLocaleName(), new AsyncCallback<HashSet<FunctionDTO>>() {
            @Override
            public void onSuccess(HashSet<FunctionDTO> functions) {
                extractionFunctions.clear();
                
                if (functions.iterator().hasNext()) {
                    for (FunctionDTO extractionFunction : functions) {
                        extractionFunctions.add(extractionFunction);
                    }
                    updateExtractionFunctionListBox();
                } else {
                    clearContent();
                }

                notifyListeners();
            }
            
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error fetching the available statistics from the server: " + caught.getMessage());
            }
        });
    }
    
    private void updateAggregators() {
        dataMiningService.getAggregatorDefinitionsFor(getStatisticToCalculate(), LocaleInfo.getCurrentLocale().getLocaleName(), new AsyncCallback<HashSet<AggregationProcessorDefinitionDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error fetching the available aggregators from the server: " + caught.getMessage());
            }
            @Override
            public void onSuccess(HashSet<AggregationProcessorDefinitionDTO> definitions) {
                aggregatorDefinitions.clear();
                
                if (definitions.iterator().hasNext()) {
                    for (AggregationProcessorDefinitionDTO definitionDTO : definitions) {
                        aggregatorDefinitions.add(definitionDTO);
                    }
                    updateAggregatorListBox();
                } else {
                    clearListBox(aggregatorListBox);
                }
            }
        });
    }

    private void updateExtractionFunctionListBox() {
        List<FunctionDTO> acceptableFunctions = new ArrayList<>(extractionFunctions);
        Collections.sort(acceptableFunctions);
        updateListBox(extractionFunctionListBox, acceptableFunctions);
    }
    
    private void updateAggregatorListBox() {
        List<AggregationProcessorDefinitionDTO> acceptableDefinitions = new ArrayList<>(aggregatorDefinitions);
        Collections.sort(acceptableDefinitions);
        updateListBox(aggregatorListBox, acceptableDefinitions);
    }
    
    private <T> void updateListBox(ValueListBox<T> listBox, Collection<T> acceptableValues) {
        T currentValue = listBox.getValue();
        T valueToBeSelected = acceptableValues.contains(currentValue) ? currentValue : acceptableValues.iterator().next();
        
        listBox.setValue(valueToBeSelected, true);
        listBox.setAcceptableValues(acceptableValues);
    }
    
    private void clearContent() {
        clearListBox(extractionFunctionListBox);
        clearListBox(aggregatorListBox);
    }

    private <T> void clearListBox(ValueListBox<T> listBox) {
        listBox.setValue(null);
        listBox.setAcceptableValues(new ArrayList<T>());
    }

    private ValueListBox<FunctionDTO> createExtractionFunctionListBox() {
        ValueListBox<FunctionDTO> extractionFunctionListBox = new ValueListBox<>(new AbstractObjectRenderer<FunctionDTO>() {
            @Override
            protected String convertObjectToString(FunctionDTO function) {
                return function.getDisplayName();
            }
        });
        extractionFunctionListBox.addValueChangeHandler(new ValueChangeHandler<FunctionDTO>() {
            @Override
            public void onValueChange(ValueChangeEvent<FunctionDTO> event) {
                updateAggregators();
                notifyListeners();
            }
        });
        return extractionFunctionListBox;
    }
    
    private ValueListBox<AggregationProcessorDefinitionDTO> createAggregatorListBox() {
        ValueListBox<AggregationProcessorDefinitionDTO> aggregatorListBox = new ValueListBox<AggregationProcessorDefinitionDTO>(new AbstractObjectRenderer<AggregationProcessorDefinitionDTO>() {
            @Override
            protected String convertObjectToString(AggregationProcessorDefinitionDTO nonNullObject) {
                return nonNullObject.getDisplayName();
            }
        });
        aggregatorListBox.addValueChangeHandler(new ValueChangeHandler<AggregationProcessorDefinitionDTO>() {
            @Override
            public void onValueChange(ValueChangeEvent<AggregationProcessorDefinitionDTO> event) {
                notifyListeners();
            }
        });
        return aggregatorListBox;
    }

    @Override
    public void applyQueryDefinition(StatisticQueryDefinitionDTO queryDefinition) {
        extractionFunctionListBox.setValue(queryDefinition.getStatisticToCalculate());
        aggregatorListBox.setValue(queryDefinition.getAggregatorDefinition(), false);
    }

    private void notifyListeners() {
        for (StatisticChangedListener listener : listeners) {
            listener.statisticChanged(getStatisticToCalculate(), getAggregatorDefinition());
        }
    }

    @Override
    public void addStatisticChangedListener(StatisticChangedListener listener) {
        listeners.add(listener);
    }

    @Override
    public FunctionDTO getStatisticToCalculate() {
        return extractionFunctionListBox.getValue();
    }

    @Override
    public AggregationProcessorDefinitionDTO getAggregatorDefinition() {
        return aggregatorListBox.getValue();
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
