package com.sap.sailing.gwt.ui.datamining.selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
import com.sap.sailing.gwt.ui.polarsheets.PolarSheetGenerationSettingsDialogComponent;
import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
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
    private final ValueListBox<DataRetrieverChainDefinitionDTO<SerializableSettings>> retrieverChainsListBox;
    private Anchor settingsAnchor;

    public SimpleDataRetrieverChainDefinitionProvider(final StringMessages stringMessages, DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter) {
        this.stringMessages = stringMessages;
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        listeners = new HashSet<>();
        isAwaitingReload = false;
        
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

    private ValueListBox<DataRetrieverChainDefinitionDTO<SerializableSettings>> createRetrieverChainsListBox() {
        ValueListBox<DataRetrieverChainDefinitionDTO<SerializableSettings>> retrieverChainsListBox = new ValueListBox<>(new AbstractRenderer<DataRetrieverChainDefinitionDTO<SerializableSettings>>() {
            @Override
            public String render(DataRetrieverChainDefinitionDTO<SerializableSettings> retrieverChain) {
                return retrieverChain == null ? "" : retrieverChain.getName();
            }
            
        });
        retrieverChainsListBox.addValueChangeHandler(new ValueChangeHandler<DataRetrieverChainDefinitionDTO<SerializableSettings>>() {
            @Override
            public void onValueChange(ValueChangeEvent<DataRetrieverChainDefinitionDTO<SerializableSettings>> event) {
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
                new AsyncCallback<ArrayList<DataRetrieverChainDefinitionDTO<SerializableSettings>>>() {
                    @Override
                    public void onSuccess(ArrayList<DataRetrieverChainDefinitionDTO<SerializableSettings>> dataRetrieverChainDefinitions) {
                        if (dataRetrieverChainDefinitions.iterator().hasNext()) {
                            List<DataRetrieverChainDefinitionDTO<SerializableSettings>> sortedRetrieverChains = new ArrayList<>();
                            for (DataRetrieverChainDefinitionDTO<SerializableSettings> dataRetrieverChainDefinition : dataRetrieverChainDefinitions) {
                                sortedRetrieverChains.add(dataRetrieverChainDefinition);
                            }
                            Collections.sort(sortedRetrieverChains);
                            
                            DataRetrieverChainDefinitionDTO<SerializableSettings> currentRetrieverChain = getDataRetrieverChainDefinition();
                            DataRetrieverChainDefinitionDTO<SerializableSettings> retrieverChainToBeSelected = sortedRetrieverChains.contains(currentRetrieverChain) ?
                                                                                            currentRetrieverChain :
                                                                                            sortedRetrieverChains.get(0);
                            
                            retrieverChainsListBox.setValue(retrieverChainToBeSelected);
                            retrieverChainsListBox.setAcceptableValues(sortedRetrieverChains);
                            
                            if (isAwaitingReload || !retrieverChainToBeSelected.equals(currentRetrieverChain)) {
                                isAwaitingReload = false;
                                notifyListeners();
                            }
                        } else {
                            retrieverChainsListBox.setValue(null);
                            retrieverChainsListBox.setAcceptableValues(new ArrayList<DataRetrieverChainDefinitionDTO<SerializableSettings>>());
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
    public DataRetrieverChainDefinitionDTO<SerializableSettings> getDataRetrieverChainDefinition() {
        return retrieverChainsListBox.getValue();
    }

    @Override
    public void addDataRetrieverChainDefinitionChangedListener(DataRetrieverChainDefinitionChangedListener listener) {
        listeners.add(listener);
    }
    
    private void notifyListeners() {
        DataRetrieverChainDefinitionDTO<SerializableSettings> dataRetrieverChainDefinition = getDataRetrieverChainDefinition();
        for (DataRetrieverChainDefinitionChangedListener listener : listeners) {
            listener.dataRetrieverChainDefinitionChanged(dataRetrieverChainDefinition);
        }
        if (dataRetrieverChainDefinition != null) {
            settingsAnchor.setVisible(dataRetrieverChainDefinition.hasSettings());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void applyQueryDefinition(StatisticQueryDefinitionDTO queryDefinition) {
        //FIXME since the sse.datamining.shared bundle cannot have a dependency on sse.common, we can't use extends-wildcards in the classes. They
        // should be limited to Settings objects. This is enforced everywhere but in the shared classes.
        retrieverChainsListBox.setValue((DataRetrieverChainDefinitionDTO<SerializableSettings>) queryDefinition.getDataRetrieverChainDefinition(), false);
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
        return getDataRetrieverChainDefinition().hasSettings();
    }

    @SuppressWarnings("unchecked")
    @Override
    public SettingsDialogComponent<SerializableSettings> getSettingsDialogComponent() {
        SettingsDialogComponent<? extends Settings> result = null;
        if (hasSettings()) {
            Settings settings = getDataRetrieverChainDefinition().getSettings();
            if (settings instanceof PolarSheetGenerationSettings) {
                PolarSheetGenerationSettings polarSettings = (PolarSheetGenerationSettings) settings;
                result = new PolarSheetGenerationSettingsDialogComponent(polarSettings);
            }
        }
        return (SettingsDialogComponent<SerializableSettings>) result;
    }

    @Override
    public void updateSettings(SerializableSettings newSettings) { 
        getDataRetrieverChainDefinition().setSettings(newSettings);
    }

}
