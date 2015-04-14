package com.sap.sailing.gwt.ui.datamining.selection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.AbstractObjectRenderer;
import com.sap.sailing.gwt.ui.client.shared.controls.SimpleObjectRenderer;
import com.sap.sailing.gwt.ui.datamining.DataMiningServiceAsync;
import com.sap.sailing.gwt.ui.datamining.StatisticChangedListener;
import com.sap.sailing.gwt.ui.datamining.StatisticProvider;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.QueryDefinitionDTO;
import com.sap.sse.datamining.shared.components.AggregatorType;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class SimpleStatisticProvider implements StatisticProvider {
    
    private final StringMessages stringMessages;
    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    private final Set<StatisticChangedListener> listeners;
    
    private final ExtractionFunctionSet extractionFunctionSet;
    
    private final HorizontalPanel mainPanel;
    private final ValueListBox<StrippedFunctionDTO> extractionFunctionListBox;
    private final ValueListBox<AggregatorType> aggregatorListBox;
    private final ValueListBox<String> baseDataTypeListBox;

    public SimpleStatisticProvider(StringMessages stringMessages, DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter) {
        this.stringMessages = stringMessages;
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        listeners = new HashSet<StatisticChangedListener>();
        
        extractionFunctionSet = new ExtractionFunctionSet();
        
        mainPanel = new HorizontalPanel();
        mainPanel.setSpacing(5);

        mainPanel.add(new Label(this.stringMessages.calculateThe()));
        extractionFunctionListBox = createExtractionFunctionListBox();
        mainPanel.add(extractionFunctionListBox);
        
        aggregatorListBox = createAggregatorListBox();
        HorizontalPanel aggregatorPanel = surroundAggregatorListBoxWithBraces(aggregatorListBox);
        mainPanel.add(aggregatorPanel);
        
        mainPanel.add(new Label(this.stringMessages.basedOn()));
        baseDataTypeListBox = createBaseDataTypeListBox();
        mainPanel.add(baseDataTypeListBox);

        updateExtractionFunctions();
    }

    private void updateExtractionFunctions() {
        dataMiningService.getAllStatistics(LocaleInfo.getCurrentLocale().getLocaleName(), new AsyncCallback<Iterable<FunctionDTO>>() {
            
            @Override
            public void onSuccess(Iterable<FunctionDTO> extractionFunctions) {
                extractionFunctionSet.clear();
                
                if (extractionFunctions.iterator().hasNext()) {
                    extractionFunctionSet.addAll(extractionFunctions);

                    StrippedFunctionDTO previousExtractionFunction = extractionFunctionListBox.getValue();
                    updateExtractionFunctionListBox();
                    StrippedFunctionDTO newExtractionFunction = extractionFunctionListBox.getValue();

                    String previousBaseDataType = baseDataTypeListBox.getValue();
                    updateBaseDataTypeListBox();
                    String newBaseDataType = baseDataTypeListBox.getValue();

                    if (!newExtractionFunction.equals(previousExtractionFunction) ||
                        !newBaseDataType.equals(previousBaseDataType)) {
                        notifyListeners();
                    }
                } else {
                    clearListBox(extractionFunctionListBox);
                    clearListBox(baseDataTypeListBox);
                    notifyListeners();
                }
            }
            
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error fetching the available statistics from the server: " + caught.getMessage());
            }
        });
    }

    private void updateExtractionFunctionListBox() {
        List<StrippedFunctionDTO> acceptableFunctions = new ArrayList<>(extractionFunctionSet.getStrippedFunctionDTOs());
        Collections.sort(acceptableFunctions);
        updateListBox(extractionFunctionListBox, acceptableFunctions);
    }

    private void updateBaseDataTypeListBox() {
        List<String> acceptableBaseDataTypes = new ArrayList<>(extractionFunctionSet.getSourceTypeNames(extractionFunctionListBox.getValue()));
        Collections.sort(acceptableBaseDataTypes);
        updateListBox(baseDataTypeListBox, acceptableBaseDataTypes);
    }
    
    private <T> void updateListBox(ValueListBox<T> listBox, Collection<T> acceptableValues) {
        T currentValue = listBox.getValue();
        T valueToBeSelected = acceptableValues.contains(currentValue) ? currentValue : acceptableValues.iterator().next();
        
        listBox.setValue(valueToBeSelected);
        listBox.setAcceptableValues(acceptableValues);
    }

    private <T> void clearListBox(ValueListBox<T> listBox) {
        listBox.setValue(null);
        listBox.setAcceptableValues(new ArrayList<T>());
    }

    private ValueListBox<StrippedFunctionDTO> createExtractionFunctionListBox() {
        ValueListBox<StrippedFunctionDTO> extractionFunctionListBox = new ValueListBox<>(new AbstractObjectRenderer<StrippedFunctionDTO>() {
            @Override
            protected String convertObjectToString(StrippedFunctionDTO function) {
                return function.getDisplayName();
            }
        });
        extractionFunctionListBox.addValueChangeHandler(new ValueChangeHandler<StrippedFunctionDTO>() {
            @Override
            public void onValueChange(ValueChangeEvent<StrippedFunctionDTO> event) {
                updateBaseDataTypeListBox();
                notifyListeners();
            }
        });
        return extractionFunctionListBox;
    }

    private ValueListBox<String> createBaseDataTypeListBox() {
        ValueListBox<String> baseDataTypeListBox = new ValueListBox<>(new SimpleObjectRenderer<String>());
        baseDataTypeListBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                notifyListeners();
            }
        });
        return baseDataTypeListBox;
    }
    
    private ValueListBox<AggregatorType> createAggregatorListBox() {
        ValueListBox<AggregatorType> aggregatorListBox = new ValueListBox<AggregatorType>(new SimpleObjectRenderer<AggregatorType>());
        aggregatorListBox.addValueChangeHandler(new ValueChangeHandler<AggregatorType>() {
            @Override
            public void onValueChange(ValueChangeEvent<AggregatorType> event) {
                notifyListeners();
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
    public void applyQueryDefinition(QueryDefinitionDTO queryDefinition) {
        extractionFunctionListBox.setValue(new StrippedFunctionDTO(queryDefinition.getStatisticToCalculate()), false);
        aggregatorListBox.setValue(queryDefinition.getAggregatorType(), false);
    }

    private void notifyListeners() {
        for (StatisticChangedListener listener : listeners) {
            listener.statisticChanged(getStatisticToCalculate(), getAggregatorType());
        }
    }

    @Override
    public void addStatisticChangedListener(StatisticChangedListener listener) {
        listeners.add(listener);
    }

    @Override
    public FunctionDTO getStatisticToCalculate() {
        return extractionFunctionSet.getFunctionDTO(extractionFunctionListBox.getValue(), baseDataTypeListBox.getValue());
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
    public SettingsDialogComponent<Settings> getSettingsDialogComponent() {
        return null;
    }

    @Override
    public void updateSettings(Settings newSettings) { }
    
    @Override
    public String getDependentCssClassName() {
        return "simpleStatisticsProvider";
    }

}
