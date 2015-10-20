package com.sap.sailing.gwt.ui.datamining.selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
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
import com.sap.sailing.gwt.ui.datamining.DataMiningSettingsControl;
import com.sap.sailing.gwt.ui.datamining.DataMiningSettingsInfoManager;
import com.sap.sailing.gwt.ui.datamining.DataRetrieverChainDefinitionChangedListener;
import com.sap.sailing.gwt.ui.datamining.DataRetrieverChainDefinitionProvider;
import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.CompositeSettings;
import com.sap.sse.gwt.client.shared.components.CompositeSettings.ComponentAndSettingsPair;
import com.sap.sse.gwt.client.shared.components.CompositeTabbedSettingsDialogComponent;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class SimpleDataRetrieverChainDefinitionProvider implements DataRetrieverChainDefinitionProvider {
    
    private final StringMessages stringMessages;
    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    private final Set<DataRetrieverChainDefinitionChangedListener> listeners;
    private boolean isAwaitingReload;
    
    private final HorizontalPanel mainPanel;
    private final ValueListBox<DataRetrieverChainDefinitionDTO> retrieverChainsListBox;
    
    private final DataMiningSettingsInfoManager settingsManager;
    private final DataMiningSettingsControl settingsControl;
    private final Map<DataRetrieverChainDefinitionDTO, HashMap<DataRetrieverLevelDTO, SerializableSettings>> settingsMap;

    public SimpleDataRetrieverChainDefinitionProvider(final StringMessages stringMessages, DataMiningServiceAsync dataMiningService,
            ErrorReporter errorReporter, DataMiningSettingsControl settingsControl) {
        this.stringMessages = stringMessages;
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        listeners = new HashSet<>();
        isAwaitingReload = false;
        settingsManager = new DataMiningSettingsInfoManager();
        this.settingsControl = settingsControl;
        this.settingsControl.addSettingsComponent(this);
        settingsMap = new HashMap<>();
        
        mainPanel = new HorizontalPanel();
        mainPanel.setSpacing(5);
        mainPanel.add(new Label(this.stringMessages.analyze()));
        
        retrieverChainsListBox = createRetrieverChainsListBox();
        mainPanel.add(retrieverChainsListBox);
        
        updateRetrieverChains();
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
    public void awaitReloadComponents() {
        isAwaitingReload = true;
    }
    
    @Override
    public boolean isAwatingReload() {
        return isAwaitingReload;
    }
    
    @Override
    public void reloadComponents() {
        updateRetrieverChains();
    }
    
    private void updateRetrieverChains() {
        dataMiningService.getDataRetrieverChainDefinitions(LocaleInfo.getCurrentLocale().getLocaleName(),
                new AsyncCallback<ArrayList<DataRetrieverChainDefinitionDTO>>() {
                    @Override
                    public void onSuccess(ArrayList<DataRetrieverChainDefinitionDTO> dataRetrieverChainDefinitions) {
                        settingsMap.clear();
                        if (!dataRetrieverChainDefinitions.isEmpty()) {
                            fillSettingsMap(dataRetrieverChainDefinitions);
                            Collections.sort(dataRetrieverChainDefinitions);
                            
                            DataRetrieverChainDefinitionDTO currentRetrieverChain = getDataRetrieverChainDefinition();
                            DataRetrieverChainDefinitionDTO retrieverChainToBeSelected = dataRetrieverChainDefinitions.contains(currentRetrieverChain) ?
                                                                                            currentRetrieverChain :
                                                                                            dataRetrieverChainDefinitions.get(0);
                            
                            retrieverChainsListBox.setValue(retrieverChainToBeSelected);
                            retrieverChainsListBox.setAcceptableValues(dataRetrieverChainDefinitions);
                            
                            if (isAwaitingReload || !retrieverChainToBeSelected.equals(currentRetrieverChain)) {
                                isAwaitingReload = false;
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

    private void fillSettingsMap(ArrayList<DataRetrieverChainDefinitionDTO> retrieverChains) {
        for (DataRetrieverChainDefinitionDTO retrieverChain : retrieverChains) {
            if (retrieverChain.hasSettings()) {
                settingsMap.put(retrieverChain, retrieverChain.getDefaultSettings());
            }
        }
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
        DataRetrieverChainDefinitionDTO dataRetrieverChainDefinition = getDataRetrieverChainDefinition();
        for (DataRetrieverChainDefinitionChangedListener listener : listeners) {
            listener.dataRetrieverChainDefinitionChanged(dataRetrieverChainDefinition);
        }
    }

    @Override
    public void applyQueryDefinition(StatisticQueryDefinitionDTO queryDefinition) {
        retrieverChainsListBox.setValue(queryDefinition.getDataRetrieverChainDefinition(), false);
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.dataMiningRetrieval();
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
    public HashMap<DataRetrieverLevelDTO, SerializableSettings> getRetrieverSettings() {
        if (settingsMap.containsKey(getDataRetrieverChainDefinition())) {
            return settingsMap.get(getDataRetrieverChainDefinition());
        } else {
            return new HashMap<>();
        }
    }

    @Override
    public boolean hasSettings() {
        return getDataRetrieverChainDefinition().hasSettings();
    }

    @Override
    public SettingsDialogComponent<CompositeSettings> getSettingsDialogComponent() {
        return new CompositeTabbedSettingsDialogComponent(createSettingsComponentsFor(getDataRetrieverChainDefinition()));
    }

    private Iterable<Component<?>> createSettingsComponentsFor(final DataRetrieverChainDefinitionDTO retrieverChain) {
        Collection<Component<?>> settingsComponents = new ArrayList<>();
        for (Entry<DataRetrieverLevelDTO, SerializableSettings> retrieverLevelSettings : settingsMap.get(retrieverChain).entrySet()) {
            final DataRetrieverLevelDTO retrieverLevel = retrieverLevelSettings.getKey();
            final Class<?> settingsType = retrieverLevelSettings.getValue().getClass();
            settingsComponents.add(new RetrieverLevelSettingsComponent(retrieverLevel, settingsManager.getSettingsInfo(settingsType).getLocalizedName(stringMessages)) {
                @Override
                public SettingsDialogComponent<SerializableSettings> getSettingsDialogComponent() {
                    return settingsManager.getSettingsInfo(settingsType).createSettingsDialogComponent(settingsMap.get(retrieverChain).get(retrieverLevel));
                }
                @Override
                public void updateSettings(SerializableSettings newSettings) {
                    settingsMap.get(retrieverChain).put(retrieverLevel, newSettings);
                }
                
            });
        }
        return settingsComponents;
    }

    @Override
    public void updateSettings(CompositeSettings newSettings) {
        Map<DataRetrieverLevelDTO, SerializableSettings> chainSettings = settingsMap.get(getDataRetrieverChainDefinition());
        for (ComponentAndSettingsPair<?> settingsPerComponent : newSettings.getSettingsPerComponent()) {
            RetrieverLevelSettingsComponent component = (RetrieverLevelSettingsComponent) settingsPerComponent.getA();
            SerializableSettings settings = (SerializableSettings) settingsPerComponent.getB();
            chainSettings.put(component.getRetrieverLevel(), settings);
        }
        notifyListeners();
    }

}
