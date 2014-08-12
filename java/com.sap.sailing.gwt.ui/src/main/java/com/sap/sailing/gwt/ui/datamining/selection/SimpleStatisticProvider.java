package com.sap.sailing.gwt.ui.datamining.selection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.datamining.shared.QueryDefinitionDeprecated;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.AbstractObjectRenderer;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;
import com.sap.sailing.gwt.ui.client.shared.components.SimpleObjectRenderer;
import com.sap.sailing.gwt.ui.datamining.DataMiningServiceAsync;
import com.sap.sailing.gwt.ui.datamining.StatisticChangedListener;
import com.sap.sailing.gwt.ui.datamining.StatisticProvider;
import com.sap.sse.datamining.shared.components.AggregatorType;
import com.sap.sse.datamining.shared.dto.FunctionDTO;

public class SimpleStatisticProvider implements StatisticProvider {
    
    private final StringMessages stringMessages;
    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    private final Set<StatisticChangedListener> listeners;
    
    private final HorizontalPanel mainPanel;
    private final ValueListBox<FunctionDTO> extractionFunctionListBox;
    private final ValueListBox<AggregatorType> aggregatorListBox;

    public SimpleStatisticProvider(StringMessages stringMessages, DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter) {
        this.stringMessages = stringMessages;
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        listeners = new HashSet<StatisticChangedListener>();
        
        mainPanel = new HorizontalPanel();
        mainPanel.setSpacing(5);

        mainPanel.add(new Label(this.stringMessages.calculateThe()));
        extractionFunctionListBox = createExtractionFunctionListBox();
        mainPanel.add(extractionFunctionListBox);
        
        aggregatorListBox = createAggregatorListBox();
        HorizontalPanel aggregatorPanel = surroundAggregatorListBoxWithBraces(aggregatorListBox);
        mainPanel.add(aggregatorPanel);

        updateExtractionFunctions();
    }

    private void updateExtractionFunctions() {
        dataMiningService.getAllStatistics(new AsyncCallback<Collection<FunctionDTO>>() {
            
            @Override
            public void onSuccess(Collection<FunctionDTO> extractionFunctions) {
                if (!extractionFunctions.isEmpty()) {
                    FunctionDTO extractionFunction = getExtractionFunction();
                    FunctionDTO valueToBeSelected = extractionFunctions.contains(extractionFunction) ? extractionFunction
                                                                                                     : extractionFunctions.iterator().next();
                    extractionFunctionListBox.setValue(valueToBeSelected);
                    extractionFunctionListBox.setAcceptableValues(extractionFunctions);
                } else {
                    extractionFunctionListBox.setValue(null);
                    extractionFunctionListBox.setAcceptableValues(new ArrayList<FunctionDTO>());
                }
            }
            
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error fetching the available statistics from the server: " + caught.getMessage());
            }
        });
    }

    private ValueListBox<FunctionDTO> createExtractionFunctionListBox() {
        ValueListBox<FunctionDTO> extractionFunctionListBox = new ValueListBox<FunctionDTO>(new AbstractObjectRenderer<FunctionDTO>() {
            @Override
            protected String convertObjectToString(FunctionDTO function) {
                return function.getDisplayName();
            }
        });
        extractionFunctionListBox.addValueChangeHandler(new ValueChangeHandler<FunctionDTO>() {
            @Override
            public void onValueChange(ValueChangeEvent<FunctionDTO> event) {
                notifyHandlers();
            }
        });
        return extractionFunctionListBox;
    }
    
    private ValueListBox<AggregatorType> createAggregatorListBox() {
        ValueListBox<AggregatorType> aggregatorListBox = new ValueListBox<AggregatorType>(new SimpleObjectRenderer<AggregatorType>());
        aggregatorListBox.addValueChangeHandler(new ValueChangeHandler<AggregatorType>() {
            @Override
            public void onValueChange(ValueChangeEvent<AggregatorType> event) {
                notifyHandlers();
            }
        });
        
        List<AggregatorType> acceptableValues = Arrays.asList(AggregatorType.values());
        aggregatorListBox.setAcceptableValues(acceptableValues);
        aggregatorListBox.setValue(acceptableValues.get(0), false);
        aggregatorListBox.setAcceptableValues(acceptableValues);
        
        return aggregatorListBox;
    }

    private HorizontalPanel surroundAggregatorListBoxWithBraces(ValueListBox<AggregatorType> aggregatorListBox) {
        HorizontalPanel aggregatorPanel = new HorizontalPanel();
        aggregatorPanel.setSpacing(1);
        aggregatorPanel.add(new Label("("));
        
        aggregatorPanel.add(aggregatorListBox);
        aggregatorPanel.add(new Label(")"));
        return aggregatorPanel;
    }

    @Override
    public void applyQueryDefinition(QueryDefinitionDeprecated queryDefinition) {
        extractionFunctionListBox.setValue(queryDefinition.getExtractionFunction(), false);
        aggregatorListBox.setValue(queryDefinition.getAggregatorType(), false);
    }

    private void notifyHandlers() {
        for (StatisticChangedListener listener : listeners) {
            listener.statisticChanged();
        }
    }

    @Override
    public void addStatisticChangedListener(StatisticChangedListener listener) {
        listeners.add(listener);
    }

    @Override
    public FunctionDTO getExtractionFunction() {
        return extractionFunctionListBox.getValue();
    }

    @Override
    public AggregatorType getAggregatorType() {
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
    public SettingsDialogComponent<Object> getSettingsDialogComponent() {
        return null;
    }

    @Override
    public void updateSettings(Object newSettings) { }

}
