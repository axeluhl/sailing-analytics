package com.sap.sailing.gwt.ui.datamining.selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.datamining.DataMiningServiceAsync;
import com.sap.sailing.gwt.ui.datamining.DataRetrieverChainDefinitionChangedListener;
import com.sap.sailing.gwt.ui.datamining.DataRetrieverChainDefinitionProvider;
import com.sap.sailing.gwt.ui.datamining.StatisticChangedListener;
import com.sap.sailing.gwt.ui.datamining.StatisticProvider;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.QueryDefinitionDTO;
import com.sap.sse.datamining.shared.components.AggregatorType;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class SimpleDataRetrieverChainDefinitionProvider implements DataRetrieverChainDefinitionProvider,
                                                                   StatisticChangedListener {
    
    private final StringMessages stringMessages;
    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    private final Set<DataRetrieverChainDefinitionChangedListener> listeners;
    
    private FunctionDTO currentStatisticToCalculate;
    
    private final HorizontalPanel mainPanel;
    private final ValueListBox<DataRetrieverChainDefinitionDTO> retrieverChainsListBox;

    public SimpleDataRetrieverChainDefinitionProvider(StringMessages stringMessages, DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter,
                                                      StatisticProvider statisticProvider) {
        this.stringMessages = stringMessages;
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        listeners = new HashSet<>();
        currentStatisticToCalculate = null;
        
        mainPanel = new HorizontalPanel();
        mainPanel.setSpacing(5);
        mainPanel.add(new Label(this.stringMessages.retrieveWith()));
        
        retrieverChainsListBox = createRetrieverChainsListBox();
        mainPanel.add(retrieverChainsListBox);
        
        statisticProvider.addStatisticChangedListener(this);
    }

    private ValueListBox<DataRetrieverChainDefinitionDTO> createRetrieverChainsListBox() {
        ValueListBox<DataRetrieverChainDefinitionDTO> retrieverChainsListBox = new ValueListBox<>(new AbstractRenderer<DataRetrieverChainDefinitionDTO>() {
            @Override
            public String render(DataRetrieverChainDefinitionDTO retrieverChain) {
                return retrieverChain == null ? "" : retrieverChain.getName();
            }
            
        });
        retrieverChainsListBox.addValueChangeHandler(new ValueChangeHandler<DataRetrieverChainDefinitionDTO>() {
            @Override
            public void onValueChange(ValueChangeEvent<DataRetrieverChainDefinitionDTO> event) {
                notifyListeners();
            }
        });
        return retrieverChainsListBox;
    }
    
    @Override
    public void statisticChanged(FunctionDTO newStatisticToCalculate, AggregatorType newAggregatorType) {
        if (!Objects.equals(currentStatisticToCalculate, newStatisticToCalculate)) {
            String currentSourceType = currentStatisticToCalculate == null ? null : currentStatisticToCalculate.getSourceTypeName();
            currentStatisticToCalculate = newStatisticToCalculate;
            if (!Objects.equals(currentSourceType, newStatisticToCalculate.getSourceTypeName())) {
                updateAvailableRetrieverChains();
            }
        }
    }
    
    private void updateAvailableRetrieverChains() {
        dataMiningService.getDataRetrieverChainDefinitionsFor(currentStatisticToCalculate, LocaleInfo.getCurrentLocale().getLocaleName(),
                new AsyncCallback<Iterable<DataRetrieverChainDefinitionDTO>>() {
                    @Override
                    public void onSuccess(Iterable<DataRetrieverChainDefinitionDTO> dataRetrieverChainDefinitions) {
                        if (dataRetrieverChainDefinitions.iterator().hasNext()) {
                            List<DataRetrieverChainDefinitionDTO> sortedRetrieverChains = new ArrayList<>();
                            for (DataRetrieverChainDefinitionDTO dataRetrieverChainDefinition : dataRetrieverChainDefinitions) {
                                sortedRetrieverChains.add(dataRetrieverChainDefinition);
                            }
                            Collections.sort(sortedRetrieverChains);
                            
                            DataRetrieverChainDefinitionDTO currentRetrieverChain = getDataRetrieverChainDefinition();
                            DataRetrieverChainDefinitionDTO retrieverChainToBeSelected = sortedRetrieverChains.contains(currentRetrieverChain) ?
                                                                                            currentRetrieverChain :
                                                                                            sortedRetrieverChains.get(0);
                            
                            retrieverChainsListBox.setValue(retrieverChainToBeSelected);
                            retrieverChainsListBox.setAcceptableValues(sortedRetrieverChains);
                            
                            if (!retrieverChainToBeSelected.equals(currentRetrieverChain)) {
                                notifyListeners();
                            }
                        } else {
                            retrieverChainsListBox.setValue(null);
                            retrieverChainsListBox.setAcceptableValues(new ArrayList<DataRetrieverChainDefinitionDTO>());
                            notifyListeners();
                        }
                    }
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error retrieving the available DataRetrieverChainDefinitions: " + caught.getMessage());
                    }
                });
    }

    @Override
    public DataRetrieverChainDefinitionDTO getDataRetrieverChainDefinition() {
        return retrieverChainsListBox.getValue();
    }

    @Override
    public void addDataRetrieverChainDefinitionChangedListener(DataRetrieverChainDefinitionChangedListener listener) {
        listeners.add(listener);
    }
    
    private void notifyListeners() {
        for (DataRetrieverChainDefinitionChangedListener listener : listeners) {
            listener.dataRetrieverChainDefinitionChanged(getDataRetrieverChainDefinition());
        }
    }

    @Override
    public void applyQueryDefinition(QueryDefinitionDTO queryDefinition) {
        retrieverChainsListBox.setValue(queryDefinition.getDataRetrieverChainDefinition(), false);
    }

    @Override
    public String getLocalizedShortName() {
        return SimpleDataRetrieverChainDefinitionProvider.class.getSimpleName();
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
    public String getDependentCssClassName() {
        return "simpleDataRetrieverChainDefinition";
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

}
