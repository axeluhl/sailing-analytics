package com.sap.sse.datamining.ui.client.selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.ui.client.AbstractDataMiningComponent;
import com.sap.sse.datamining.ui.client.DataMiningServiceAsync;
import com.sap.sse.datamining.ui.client.DataMiningSettingsControl;
import com.sap.sse.datamining.ui.client.DataMiningSettingsInfo;
import com.sap.sse.datamining.ui.client.DataMiningSettingsInfoManager;
import com.sap.sse.datamining.ui.client.DataRetrieverChainDefinitionChangedListener;
import com.sap.sse.datamining.ui.client.DataRetrieverChainDefinitionProvider;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.CompositeSettings;
import com.sap.sse.gwt.client.shared.components.CompositeTabbedSettingsDialogComponent;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class SimpleDataRetrieverChainDefinitionProvider extends AbstractDataMiningComponent<CompositeSettings>
        implements DataRetrieverChainDefinitionProvider {

    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    private final Set<DataRetrieverChainDefinitionChangedListener> listeners;
    private boolean isAwaitingReload;

    private final HorizontalPanel mainPanel;
    private final ValueListBox<DataRetrieverChainDefinitionDTO> retrieverChainsListBox;

    private final DataMiningSettingsInfoManager settingsManager;
    private final DataMiningSettingsControl settingsControl;
    private final Map<DataRetrieverChainDefinitionDTO, HashMap<DataRetrieverLevelDTO, SerializableSettings>> settingsMap;

    private final List<Component<?>> retrieverLevelSettingsComponents;

    public SimpleDataRetrieverChainDefinitionProvider(Component<?> parent, ComponentContext<?> context,
            DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter,
            DataMiningSettingsControl settingsControl, DataMiningSettingsInfoManager settingsManager) {
        super(parent, context);
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        this.retrieverLevelSettingsComponents = new ArrayList<>();
        listeners = new HashSet<>();
        isAwaitingReload = false;
        this.settingsManager = settingsManager;
        this.settingsControl = settingsControl;
        this.settingsControl.addSettingsComponent(this);
        settingsMap = new HashMap<>();

        mainPanel = new HorizontalPanel();
        mainPanel.setSpacing(5);
        mainPanel.add(new Label(getDataMiningStringMessages().analyze()));

        retrieverChainsListBox = createRetrieverChainsListBox();
        mainPanel.add(retrieverChainsListBox);

        updateRetrieverChains();
    }

    private ValueListBox<DataRetrieverChainDefinitionDTO> createRetrieverChainsListBox() {
        ValueListBox<DataRetrieverChainDefinitionDTO> retrieverChainsListBox = new ValueListBox<>(
                new AbstractRenderer<DataRetrieverChainDefinitionDTO>() {
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
    public boolean isAwaitingReload() {
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
                            DataRetrieverChainDefinitionDTO retrieverChainToBeSelected = dataRetrieverChainDefinitions
                                    .contains(currentRetrieverChain) ? currentRetrieverChain
                                            : dataRetrieverChainDefinitions.get(0);

                            retrieverChainsListBox.setValue(retrieverChainToBeSelected);
                            retrieverChainsListBox.setAcceptableValues(dataRetrieverChainDefinitions);

                            if (isAwaitingReload || !retrieverChainToBeSelected.equals(currentRetrieverChain)) {
                                isAwaitingReload = false;
                                notifyListeners();
                            }
                        } else {
                            retrieverChainsListBox.setValue(null);
                            retrieverChainsListBox
                                    .setAcceptableValues(new ArrayList<DataRetrieverChainDefinitionDTO>());
                            notifyListeners();
                        }
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(
                                "Error retrieving the available DataRetrieverChainDefinitions: " + caught.getMessage());
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
        return getDataMiningStringMessages().dataMiningRetrieval();
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
    public SettingsDialogComponent<CompositeSettings> getSettingsDialogComponent(CompositeSettings settings) {
        retrieverLevelSettingsComponents.clear();
        retrieverLevelSettingsComponents.addAll(createSettingsComponentsFor(getDataRetrieverChainDefinition()));
        return new CompositeTabbedSettingsDialogComponent(retrieverLevelSettingsComponents);
    }

    private List<Component<?>> createSettingsComponentsFor(final DataRetrieverChainDefinitionDTO retrieverChain) {
        List<Component<?>> settingsComponents = new ArrayList<>();
        for (Entry<DataRetrieverLevelDTO, SerializableSettings> retrieverLevelSettings : settingsMap.get(retrieverChain)
                .entrySet()) {
            final DataRetrieverLevelDTO retrieverLevel = retrieverLevelSettings.getKey();
            final Class<?> settingsType = retrieverLevelSettings.getValue().getClass();
            DataMiningSettingsInfo settingsInfo = settingsManager.getSettingsInfo(settingsType);
            settingsComponents.add(new RetrieverLevelSettingsComponent(SimpleDataRetrieverChainDefinitionProvider.this,
                    getComponentContext(), retrieverLevel, settingsInfo.getId(),
                    settingsInfo.getLocalizedName()) {
                @Override
                public SettingsDialogComponent<SerializableSettings> getSettingsDialogComponent(
                        SerializableSettings settings) {
                    return settingsManager.getSettingsInfo(settingsType)
                            .createSettingsDialogComponent(settingsMap.get(retrieverChain).get(retrieverLevel));
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
        Map<DataRetrieverLevelDTO, SerializableSettings> chainSettings = settingsMap
                .get(getDataRetrieverChainDefinition());
        for (Entry<String, Settings> settingsPerComponent : newSettings.getSettingsPerComponentId().entrySet()) {
            RetrieverLevelSettingsComponent component = (RetrieverLevelSettingsComponent) findComponentById(
                    settingsPerComponent.getKey());
            SerializableSettings settings = (SerializableSettings) settingsPerComponent.getValue();
            chainSettings.put(component.getRetrieverLevel(), settings);
        }
        notifyListeners();
    }

    private Component<?> findComponentById(String componentId) {
        for (Component<?> component : retrieverLevelSettingsComponents) {
            if (component.getId().equals(componentId)) {
                return component;
            }
        }
        return null;
    }

    @Override
    public CompositeSettings getSettings() {
        Map<String, Settings> settings = new HashMap<>();
        for (Entry<DataRetrieverLevelDTO, SerializableSettings> retrieverLevelSettings : settingsMap
                .get(getDataRetrieverChainDefinition()).entrySet()) {
            final DataRetrieverLevelDTO retrieverLevel = retrieverLevelSettings.getKey();
            final Class<?> settingsType = retrieverLevelSettings.getValue().getClass();
            DataMiningSettingsInfo settingsInfo = settingsManager.getSettingsInfo(settingsType);
            RetrieverLevelSettingsComponent c = new RetrieverLevelSettingsComponent(
                    SimpleDataRetrieverChainDefinitionProvider.this, getComponentContext(), retrieverLevel,
                    settingsInfo.getId(), settingsInfo.getLocalizedName()) {
                @Override
                public SettingsDialogComponent<SerializableSettings> getSettingsDialogComponent(
                        SerializableSettings settings) {
                    return null;
                }

                @Override
                public void updateSettings(SerializableSettings newSettings) {
                }
            };
            settings.put(c.getId(), c.hasSettings() ? c.getSettings() : null);
        }

        return new CompositeSettings(settings);
    }

    @Override
    public String getId() {
        return "SimpleDataRetrieverChainDefinitionProvider";
    }
}
