package com.sap.sailing.gwt.ui.datamining.selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.datamining.DataMiningEntryPoint;
import com.sap.sailing.gwt.ui.datamining.DataMiningServiceAsync;
import com.sap.sailing.gwt.ui.datamining.DataRetrieverChainDefinitionChangedListener;
import com.sap.sailing.gwt.ui.datamining.DataRetrieverChainDefinitionProvider;
import com.sap.sailing.gwt.ui.polarmining.PolarDataMiningSettingsDialogComponent;
import com.sap.sailing.polars.datamining.shared.PolarDataMiningSettings;
import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.shared.components.SettingsDialog;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class SimpleDataRetrieverChainDefinitionProvider implements DataRetrieverChainDefinitionProvider {
    
    private final StringMessages stringMessages;
    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    private final Set<DataRetrieverChainDefinitionChangedListener> listeners;
    private boolean isAwaitingReload;
    
    private final HorizontalPanel mainPanel;
    private final ValueListBox<DataRetrieverChainDefinitionDTO> retrieverChainsListBox;
    private Anchor settingsAnchor;
    
    // TODO FIXME This is currently the only use case for processors with settings. That's why it's hard coded.
    private Map<DataRetrieverChainDefinitionDTO, PolarDataMiningSettings> settingsMappedByRetrieverChain;

    public SimpleDataRetrieverChainDefinitionProvider(final StringMessages stringMessages, DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter) {
        this.stringMessages = stringMessages;
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        listeners = new HashSet<>();
        isAwaitingReload = false;
        settingsMappedByRetrieverChain = new HashMap<>();
        
        mainPanel = new HorizontalPanel();
        mainPanel.setSpacing(5);
        mainPanel.add(new Label(this.stringMessages.analyze()));
        
        retrieverChainsListBox = createRetrieverChainsListBox();
        mainPanel.add(retrieverChainsListBox);
        
        settingsAnchor = new Anchor(AbstractImagePrototype.create(DataMiningEntryPoint.resources.darkSettingsIcon()).getSafeHtml());
        settingsAnchor.addStyleName("settingsAnchor");
        settingsAnchor.setTitle(stringMessages.settings());
        settingsAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {        
                SettingsDialog<?> settingsDialog = new SettingsDialog<SerializableSettings>(SimpleDataRetrieverChainDefinitionProvider.this, stringMessages);
                settingsDialog.show();
            }
        });
        mainPanel.add(settingsAnchor);
        
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
                        settingsMappedByRetrieverChain.clear();
                        if (!dataRetrieverChainDefinitions.isEmpty()) {
                            fillSettings(dataRetrieverChainDefinitions);
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

    private void fillSettings(ArrayList<DataRetrieverChainDefinitionDTO> dataRetrieverChainDefinitions) {
        for (DataRetrieverChainDefinitionDTO retrieverChain : dataRetrieverChainDefinitions) {
            if (retrieverChain.hasSettings()) {
                // TODO FIXME This is currently the only use case for processors with settings. That's why it's hard coded.
                for (SerializableSettings settings : retrieverChain.getDefaultSettings().values()) {
                    if (settings instanceof PolarDataMiningSettings) {
                        settingsMappedByRetrieverChain.put(retrieverChain, (PolarDataMiningSettings) settings);
                    }
                }
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
        if (dataRetrieverChainDefinition != null) {
            settingsAnchor.setVisible(dataRetrieverChainDefinition.hasSettings());
        }
    }

    @Override
    public void applyQueryDefinition(StatisticQueryDefinitionDTO queryDefinition) {
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
    
    // TODO This is just a quick fix. Delete, after the settings have been improved.
    @Override
    public HashMap<DataRetrieverLevelDTO, SerializableSettings> getRetrieverSettings() {
        HashMap<DataRetrieverLevelDTO, SerializableSettings> retrieverSettings = new HashMap<>();
        if (settingsMappedByRetrieverChain.containsKey(getDataRetrieverChainDefinition())) {
            for (DataRetrieverLevelDTO retrieverLevel : getDataRetrieverChainDefinition().getRetrieverLevels()) {
                if (retrieverLevel.getDefaultSettings() instanceof PolarSheetGenerationSettings) {
                    retrieverSettings.put(retrieverLevel, settingsMappedByRetrieverChain.get(getDataRetrieverChainDefinition()));
                }
            }
        }
        return retrieverSettings;
    }

    @Override
    public boolean hasSettings() {
        return getDataRetrieverChainDefinition().hasSettings();
    }

    @SuppressWarnings("unchecked")
    @Override
    public SettingsDialogComponent<SerializableSettings> getSettingsDialogComponent() {
        SettingsDialogComponent<? extends Settings> result = null;
        if (settingsMappedByRetrieverChain.containsKey(getDataRetrieverChainDefinition())) {
            result = new PolarDataMiningSettingsDialogComponent(settingsMappedByRetrieverChain.get(getDataRetrieverChainDefinition()));
        }
        return (SettingsDialogComponent<SerializableSettings>) result;
    }

    @Override
    public void updateSettings(SerializableSettings newSettings) {
        // TODO FIXME This is currently the only use case for processors with settings. That's why it's hard coded.
        settingsMappedByRetrieverChain.put(getDataRetrieverChainDefinition(), (PolarDataMiningSettings) newSettings);
        notifyListeners();
    }

}
