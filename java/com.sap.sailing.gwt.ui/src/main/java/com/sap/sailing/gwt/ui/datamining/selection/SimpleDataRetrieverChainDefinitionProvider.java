package com.sap.sailing.gwt.ui.datamining.selection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import com.sap.sailing.gwt.ui.polarsheets.PolarSheetGenerationSettingsDialogComponent;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialog;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class SimpleDataRetrieverChainDefinitionProvider implements DataRetrieverChainDefinitionProvider {
    
    private final StringMessages stringMessages;
    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    private final Set<DataRetrieverChainDefinitionChangedListener> listeners;
    private final Map<Class<? extends Settings>, Class<? extends SettingsDialogComponent<?>>> settingsDialogRegistration;
    private boolean isAwaitingReload;
    
    private final HorizontalPanel mainPanel;
    private final ValueListBox<DataRetrieverChainDefinitionDTO<Settings>> retrieverChainsListBox;

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
        
        Anchor settingsAnchor = new Anchor(AbstractImagePrototype.create(DataMiningEntryPoint.resources.darkSettingsIcon()).getSafeHtml());
        settingsAnchor.addStyleName("settingsAnchor");
        settingsAnchor.setTitle(stringMessages.settings());
        settingsAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {        
                // Hacky because we don't know the type of settings used here.
                SettingsDialog<?> settingsDialog = null;
                try {
                    Constructor<?> constructor = SettingsDialog.class.getConstructor(Component.class, StringMessages.class);
                    settingsDialog = (SettingsDialog<?>) constructor.newInstance(this, stringMessages);
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                    e.printStackTrace();
                }
                if (settingsDialog != null) {
                    settingsDialog.show();
                }
            }
        });
        mainPanel.add(settingsAnchor);
        
        updateRetrieverChains();
        
        settingsDialogRegistration = initializeSettingsDialogRegistration();
    }

    /**
     * Use this method to create a mapping between setting classes and its UI dialogs.
     * @return
     */
    private Map<Class<? extends Settings>, Class<? extends SettingsDialogComponent<?>>> initializeSettingsDialogRegistration() {
        Map<Class<? extends Settings>, Class<? extends SettingsDialogComponent<?>>> registration = new HashMap<>();
        registration.put(PolarSheetGenerationSettings.class, PolarSheetGenerationSettingsDialogComponent.class);
        return registration;
    }

    private ValueListBox<DataRetrieverChainDefinitionDTO<Settings>> createRetrieverChainsListBox() {
        ValueListBox<DataRetrieverChainDefinitionDTO<Settings>> retrieverChainsListBox = new ValueListBox<>(new AbstractRenderer<DataRetrieverChainDefinitionDTO<Settings>>() {
            @Override
            public String render(DataRetrieverChainDefinitionDTO<Settings> retrieverChain) {
                return retrieverChain == null ? "" : retrieverChain.getName();
            }
            
        });
        retrieverChainsListBox.addValueChangeHandler(new ValueChangeHandler<DataRetrieverChainDefinitionDTO<Settings>>() {
            @Override
            public void onValueChange(ValueChangeEvent<DataRetrieverChainDefinitionDTO<Settings>> event) {
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
                new AsyncCallback<ArrayList<DataRetrieverChainDefinitionDTO<Settings>>>() {
                    @Override
                    public void onSuccess(ArrayList<DataRetrieverChainDefinitionDTO<Settings>> dataRetrieverChainDefinitions) {
                        if (dataRetrieverChainDefinitions.iterator().hasNext()) {
                            List<DataRetrieverChainDefinitionDTO<Settings>> sortedRetrieverChains = new ArrayList<>();
                            for (DataRetrieverChainDefinitionDTO<Settings> dataRetrieverChainDefinition : dataRetrieverChainDefinitions) {
                                sortedRetrieverChains.add(dataRetrieverChainDefinition);
                            }
                            Collections.sort(sortedRetrieverChains);
                            
                            DataRetrieverChainDefinitionDTO<Settings> currentRetrieverChain = getDataRetrieverChainDefinition();
                            DataRetrieverChainDefinitionDTO<Settings> retrieverChainToBeSelected = sortedRetrieverChains.contains(currentRetrieverChain) ?
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
                            retrieverChainsListBox.setAcceptableValues(new ArrayList<DataRetrieverChainDefinitionDTO<Settings>>());
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
    public DataRetrieverChainDefinitionDTO<Settings> getDataRetrieverChainDefinition() {
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

    @SuppressWarnings("unchecked")
    @Override
    public void applyQueryDefinition(StatisticQueryDefinitionDTO queryDefinition) {
        //FIXME since the sse.datamining.shared bundle cannot have a dependency on sse.common, we can't use extends-wildcards in the classes. They
        // should be limited to Settings objects. This is enforced everywhere but in the shared classes.
        retrieverChainsListBox.setValue((DataRetrieverChainDefinitionDTO<Settings>) queryDefinition.getDataRetrieverChainDefinition(), false);
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
    public SettingsDialogComponent<Settings> getSettingsDialogComponent() {
        SettingsDialogComponent<Settings> result;
        if (hasSettings()) {
            Settings settings = getDataRetrieverChainDefinition().getSettings();
            if (settingsDialogRegistration.containsKey(settings.getClass())) {
                Class<? extends SettingsDialogComponent<?>> dialogClass = settingsDialogRegistration.get(settings.getClass());
                try {
                    Constructor<?> constructor = dialogClass.getConstructor(settings.getClass());
                    result = (SettingsDialogComponent<Settings>) constructor.newInstance(settings);
                } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                        | IllegalArgumentException | InvocationTargetException e) {
                    e.printStackTrace();
                    result = null;
                }
            } else {
                result = null;
            }
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public void updateSettings(Settings newSettings) { 
        getDataRetrieverChainDefinition().setSettings(newSettings);
    }

}
