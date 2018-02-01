package com.sap.sailing.gwt.ui.datamining.selection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.AbstractObjectRenderer;
import com.sap.sailing.gwt.ui.datamining.DataMiningServiceAsync;
import com.sap.sailing.gwt.ui.datamining.DataMiningSettingsControl;
import com.sap.sailing.gwt.ui.datamining.DataMiningSettingsInfo;
import com.sap.sailing.gwt.ui.datamining.DataMiningSettingsInfoManager;
import com.sap.sailing.gwt.ui.datamining.DataRetrieverChainDefinitionChangedListener;
import com.sap.sailing.gwt.ui.datamining.DataRetrieverChainDefinitionProvider;
import com.sap.sailing.gwt.ui.datamining.StatisticChangedListener;
import com.sap.sailing.gwt.ui.datamining.StatisticProvider;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.AggregationProcessorDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.shared.components.AbstractComponent;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.CompositeSettings;
import com.sap.sse.gwt.client.shared.components.CompositeTabbedSettingsDialogComponent;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

/**
 * A {@link StatisticProvider} that contains all statistics registered in the server.
 * Each statistic is paired with the corresponding {@link DataRetrieverChainDefinitionDTO}.
 * This provides a more comfortable access to the available statistics, without the need to
 * specify the domain to analyze.
 * 
 * @author Lennart Hensler
 */
public class GlobalStatisticProvider extends AbstractComponent<CompositeSettings>
        implements DataRetrieverChainDefinitionProvider, StatisticProvider<CompositeSettings> {
    
    private static final String STATISTIC_PROVIDER_ELEMENT_STYLE = "statisticProviderElement";

    private final StringMessages stringMessages;
//    private final DataMiningServiceAsync dataMiningService;
//    private final ErrorReporter errorReporter;
    private final Set<DataRetrieverChainDefinitionChangedListener> retrieverChainListeners;
    private final Set<StatisticChangedListener> statisticListeners;
    private boolean isAwaitingReload;
    
    private final DataMiningSettingsInfoManager settingsManager;
    private final DataMiningSettingsControl settingsControl;
    private final Map<DataRetrieverChainDefinitionDTO, HashMap<DataRetrieverLevelDTO, SerializableSettings>> settingsMap;
    private final List<Component<?>> retrieverLevelSettingsComponents;
    
    private final FlowPanel mainPanel;
    private final ValueListBox<Pair<DataRetrieverChainDefinitionDTO, FunctionDTO>> extractionFunctionListBox;
    private final ValueListBox<AggregationProcessorDefinitionDTO> aggregatorListBox;

    public GlobalStatisticProvider(Component<?> parent, ComponentContext<?> componentContext,
            StringMessages stringMessages, DataMiningServiceAsync dataMiningService,
            ErrorReporter errorReporter, DataMiningSettingsControl settingsControl) {
        super(parent, componentContext);
        this.stringMessages = stringMessages;
//        this.dataMiningService = dataMiningService;
//        this.errorReporter = errorReporter;
        retrieverChainListeners = new HashSet<>();
        statisticListeners = new HashSet<>();
        isAwaitingReload = false;
        
        settingsManager = new DataMiningSettingsInfoManager();
        this.settingsControl = settingsControl;
        this.settingsControl.addSettingsComponent(this);
        settingsMap = new HashMap<>();
        retrieverLevelSettingsComponents = new ArrayList<>();
        
        mainPanel = new FlowPanel();
        Label label = new Label(this.stringMessages.calculateThe());
        label.addStyleName(STATISTIC_PROVIDER_ELEMENT_STYLE);
        mainPanel.add(label);
        
        extractionFunctionListBox = createExtractionFunctionListBox();
        extractionFunctionListBox.addStyleName(STATISTIC_PROVIDER_ELEMENT_STYLE);
        mainPanel.add(extractionFunctionListBox);
        
        aggregatorListBox = createAggregatorListBox();
        aggregatorListBox.addStyleName(STATISTIC_PROVIDER_ELEMENT_STYLE);
        mainPanel.add(aggregatorListBox);
    }
    
    private ValueListBox<Pair<DataRetrieverChainDefinitionDTO, FunctionDTO>> createExtractionFunctionListBox() {
        ValueListBox<Pair<DataRetrieverChainDefinitionDTO, FunctionDTO>> extractionFunctionListBox =
            new ValueListBox<>(new AbstractObjectRenderer<Pair<DataRetrieverChainDefinitionDTO, FunctionDTO>>() {
                @Override
                protected String convertObjectToString(Pair<DataRetrieverChainDefinitionDTO, FunctionDTO> nonNullObject) {
                    return nonNullObject.getB().getDisplayName();
                }
            });
        extractionFunctionListBox.addValueChangeHandler(new ValueChangeHandler<Pair<DataRetrieverChainDefinitionDTO,FunctionDTO>>() {
            @Override
            public void onValueChange(ValueChangeEvent<Pair<DataRetrieverChainDefinitionDTO, FunctionDTO>> event) {
                notifyRetrieverChainListeners();
                notifyStatisticListeners();
            }
        });
        return null;
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
                notifyStatisticListeners();
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
        // TODO load components
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
        for (Entry<DataRetrieverLevelDTO, SerializableSettings> retrieverLevelSettings : settingsMap.get(retrieverChain).entrySet()) {
            final DataRetrieverLevelDTO retrieverLevel = retrieverLevelSettings.getKey();
            final Class<?> settingsType = retrieverLevelSettings.getValue().getClass();
            DataMiningSettingsInfo settingsInfo = settingsManager.getSettingsInfo(settingsType);
            // TODO Check if this is correct or if GlobalStatisticProvider.this is necessary
            settingsComponents.add(new RetrieverLevelSettingsComponent(this, getComponentContext(),
                    retrieverLevel, settingsInfo.getId(), settingsInfo.getLocalizedName(stringMessages)) {
                @Override
                public SettingsDialogComponent<SerializableSettings> getSettingsDialogComponent(SerializableSettings settings) {
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
        for (Entry<String, Settings> settingsPerComponent : newSettings.getSettingsPerComponentId().entrySet()) {
            RetrieverLevelSettingsComponent component = (RetrieverLevelSettingsComponent) findComponentById(settingsPerComponent.getKey());
            SerializableSettings settings = (SerializableSettings) settingsPerComponent.getValue();
            chainSettings.put(component.getRetrieverLevel(), settings);
        }
        notifyRetrieverChainListeners();
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
        for (Entry<DataRetrieverLevelDTO, SerializableSettings> retrieverLevelSettings : settingsMap.get(getDataRetrieverChainDefinition()).entrySet()) {
            final DataRetrieverLevelDTO retrieverLevel = retrieverLevelSettings.getKey();
            final Class<?> settingsType = retrieverLevelSettings.getValue().getClass();
            DataMiningSettingsInfo settingsInfo = settingsManager.getSettingsInfo(settingsType);
            // TODO Check if this is correct or if GlobalStatisticProvider.this is necessary
            RetrieverLevelSettingsComponent c = new RetrieverLevelSettingsComponent(
                    this, getComponentContext(), retrieverLevel,
                    settingsInfo.getId(), settingsInfo.getLocalizedName(stringMessages)) {
                @Override
                public SettingsDialogComponent<SerializableSettings> getSettingsDialogComponent(SerializableSettings settings) {
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
    public void dataRetrieverChainDefinitionChanged(DataRetrieverChainDefinitionDTO dataRetrieverChainDefinitionDTO) {
        // TODO Delete after this has been established as the default statistic provider
    }
    
    @Override
    public void applyQueryDefinition(StatisticQueryDefinitionDTO queryDefinition) {
        DataRetrieverChainDefinitionDTO retrieverChain = queryDefinition.getDataRetrieverChainDefinition();
        FunctionDTO extractionFunction = queryDefinition.getStatisticToCalculate();
        extractionFunctionListBox.setValue(new Pair<>(retrieverChain, extractionFunction));
        aggregatorListBox.setValue(queryDefinition.getAggregatorDefinition(), false);
    }
    
    @Override
    public void addStatisticChangedListener(StatisticChangedListener listener) {
        statisticListeners.add(listener);
    }

    private void notifyStatisticListeners() {
        FunctionDTO statistic = getStatisticToCalculate();
        AggregationProcessorDefinitionDTO aggregator = getAggregatorDefinition();
        for (StatisticChangedListener listener : statisticListeners) {
            listener.statisticChanged(statistic, aggregator);
        }
    }
    
    @Override
    public void addDataRetrieverChainDefinitionChangedListener(DataRetrieverChainDefinitionChangedListener listener) {
        retrieverChainListeners.add(listener);
    }
    
    private void notifyRetrieverChainListeners() {
        DataRetrieverChainDefinitionDTO dataRetrieverChainDefinition = getDataRetrieverChainDefinition();
        for (DataRetrieverChainDefinitionChangedListener listener : retrieverChainListeners) {
            listener.dataRetrieverChainDefinitionChanged(dataRetrieverChainDefinition);
        }
    }
    
    @Override
    public DataRetrieverChainDefinitionDTO getDataRetrieverChainDefinition() {
        Pair<DataRetrieverChainDefinitionDTO, FunctionDTO> selection = extractionFunctionListBox.getValue();
        return selection == null ? null : selection.getA();
    }
    
    @Override
    public FunctionDTO getStatisticToCalculate() {
        Pair<DataRetrieverChainDefinitionDTO, FunctionDTO> selection = extractionFunctionListBox.getValue();
        return selection == null ? null : selection.getB();
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
    public String getDependentCssClassName() {
        return "globalStatisticsProvider";
    }

    @Override
    public String getId() {
        return "GlobalStatisticProvider";
    }

}
