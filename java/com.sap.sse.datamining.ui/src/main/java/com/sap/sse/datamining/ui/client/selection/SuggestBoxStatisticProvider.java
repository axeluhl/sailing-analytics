package com.sap.sse.datamining.ui.client.selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.OptionElement;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SuggestBox.DefaultSuggestionDisplay;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.Util;
import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.AggregationProcessorDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.ui.client.AbstractDataMiningComponent;
import com.sap.sse.datamining.ui.client.AggregatorDefinitionChangedListener;
import com.sap.sse.datamining.ui.client.DataMiningServiceAsync;
import com.sap.sse.datamining.ui.client.DataMiningSettingsControl;
import com.sap.sse.datamining.ui.client.DataMiningSettingsInfo;
import com.sap.sse.datamining.ui.client.DataMiningSettingsInfoManager;
import com.sap.sse.datamining.ui.client.DataRetrieverChainDefinitionChangedListener;
import com.sap.sse.datamining.ui.client.ExtractionFunctionChangedListener;
import com.sap.sse.datamining.ui.client.StatisticChangedListener;
import com.sap.sse.datamining.ui.client.StatisticProvider;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.CompositeSettings;
import com.sap.sse.gwt.client.shared.components.CompositeTabbedSettingsDialogComponent;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;
import com.sap.sse.gwt.client.suggestion.AbstractListSuggestOracle;
import com.sap.sse.gwt.client.suggestion.CustomSuggestBox;

/**
 * A {@link StatisticProvider} that contains all statistics registered in the server. Each statistic is paired with the
 * corresponding {@link DataRetrieverChainDefinitionDTO}. This provides a more comfortable access to the available
 * statistics, without the need to specify the domain to analyze.
 * 
 * @author Lennart Hensler
 */
public class SuggestBoxStatisticProvider extends AbstractDataMiningComponent<CompositeSettings>
        implements StatisticProvider {

    private static final String StatisticProviderStyle = "statisticProvider";
    private static final String StatisticProviderLabelStyle = "statisticProviderLabel";
    private static final String StatisticProviderElementStyle = "statisticProviderElement";
    private static final String SuggestBoxContainerStyle = "statisticSuggestBoxContainer";

    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    private final Set<DataRetrieverChainDefinitionChangedListener> retrieverChainListeners;
    private final Set<ExtractionFunctionChangedListener> extractionFunctionListeners;
    private final Set<AggregatorDefinitionChangedListener> aggregatorDefinitionListeners;
    private boolean isAwaitingReload;
    private int awaitingRetrieverChainStatistics;
    private int awaitingAggregators;

    private final DataMiningSettingsInfoManager settingsManager;
    private final DataMiningSettingsControl settingsControl;
    private final Map<DataRetrieverChainDefinitionDTO, HashMap<DataRetrieverLevelDTO, SerializableSettings>> settingsMap;
    private final List<Component<?>> retrieverLevelSettingsComponents;

    private final FlowPanel mainPanel;
    private final Label labelBetweenAggregatorAndStatistic;
    
    private final Map<String, Set<AggregationProcessorDefinitionDTO>> collectedAggregators;
    private AggregatorGroup currentAggregator;
    private final List<AggregatorGroup> availableAggregators;
    private final AggregatorListBox aggregatorListBox;

    private FunctionDTO identityFunction;
    private final List<IdentityFunctionWithContext> availableIdentityFunctions;
    private final List<StatisticWithContext> availableStatistics;
    private final ExtractionFunctionSuggestBox extractionFunctionSuggestBox;

    public SuggestBoxStatisticProvider(Component<?> parent, ComponentContext<?> componentContext,
            DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter,
            DataMiningSettingsControl settingsControl, DataMiningSettingsInfoManager settingsManager) {
        super(parent, componentContext);
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        retrieverChainListeners = new HashSet<>();
        extractionFunctionListeners = new HashSet<>();
        aggregatorDefinitionListeners = new HashSet<>();
        isAwaitingReload = false;
        this.settingsManager = settingsManager;
        this.settingsControl = settingsControl;
        this.settingsControl.addSettingsComponent(this);
        settingsMap = new HashMap<>();
        retrieverLevelSettingsComponents = new ArrayList<>();

        availableIdentityFunctions = new ArrayList<>();
        availableStatistics = new ArrayList<>();
        extractionFunctionSuggestBox = new ExtractionFunctionSuggestBox();
        extractionFunctionSuggestBox.setValueChangeHandler(this::extractionFunctionSelectionChanged);
        extractionFunctionSuggestBox.getValueBox().addFocusHandler(e -> {
            extractionFunctionSuggestBox.getValueBox().selectAll();
            extractionFunctionSuggestBox.showSuggestionList();
        });
        extractionFunctionSuggestBox.getValueBox().addKeyUpHandler(e -> {
            int keyCode = e.getNativeEvent().getKeyCode();
            if (keyCode == KeyCodes.KEY_ESCAPE) {
                extractionFunctionSuggestBox.hideSuggestionList();
                extractionFunctionSuggestBox.setFocus(false);
            }
        });
        extractionFunctionSuggestBox.getElement().setPropertyString("placeholder", getDataMiningStringMessages().searchAvailableStatistics());
        extractionFunctionSuggestBox.setLimit(Integer.MAX_VALUE);
        extractionFunctionSuggestBox.addStyleName(StatisticProviderElementStyle);
        extractionFunctionSuggestBox.setWidth("100%");
        
        SimplePanel suggestBoxContainer = new SimplePanel(extractionFunctionSuggestBox);
        suggestBoxContainer.addStyleName(StatisticProviderElementStyle);
        suggestBoxContainer.addStyleName(SuggestBoxContainerStyle);

        collectedAggregators = new HashMap<>();
        availableAggregators = new ArrayList<>();
        aggregatorListBox = new AggregatorListBox("<" + getDataMiningStringMessages().any() + ">");
        aggregatorListBox.addValueChangeHandler(new ValueChangeHandler<AggregatorGroup>() {
            @Override
            public void onValueChange(ValueChangeEvent<AggregatorGroup> event) {
                aggregatorSelectionChanged(event.getValue());
            }
        });
        aggregatorListBox.addStyleName(StatisticProviderElementStyle);
        aggregatorListBox.addStyleName("dataMiningListBox");
        aggregatorListBox.setEnabled(false);

        mainPanel = new FlowPanel();
        mainPanel.addStyleName(StatisticProviderStyle);
        mainPanel.add(aggregatorListBox);
        labelBetweenAggregatorAndStatistic = new Label(getDataMiningStringMessages().of());
        labelBetweenAggregatorAndStatistic.addStyleName(StatisticProviderLabelStyle);
        labelBetweenAggregatorAndStatistic.addStyleName("emphasizedLabel");
        mainPanel.add(labelBetweenAggregatorAndStatistic);
        mainPanel.add(suggestBoxContainer);
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
        isAwaitingReload = false;
        updateContent();
    }

    private void updateContent() {
        final String localeName = LocaleInfo.getCurrentLocale().getLocaleName();
        dataMiningService.getDataRetrieverChainDefinitions(localeName,
            new AsyncCallback<ArrayList<DataRetrieverChainDefinitionDTO>>() {
                @Override
                public void onSuccess(ArrayList<DataRetrieverChainDefinitionDTO> dataRetrieverChainDefinitions) {
                    settingsMap.clear();
                    for (DataRetrieverChainDefinitionDTO retrieverChain : dataRetrieverChainDefinitions) {
                        if (retrieverChain.hasSettings()) {
                            settingsMap.put(retrieverChain, retrieverChain.getDefaultSettings());
                        }
                    }
                    
                    updateIdentityFunctions(localeName, dataRetrieverChainDefinitions);
                    updateStatistics(localeName, dataRetrieverChainDefinitions);
                }

                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Error fetching the retriever chain definitions: " + caught.getMessage());
                }
            });
    }

    private void updateIdentityFunctions(String localeName, Collection<DataRetrieverChainDefinitionDTO> retrieverChains) {
        availableIdentityFunctions.clear();
        if (retrieverChains.isEmpty()) {
            return;
        }
        
        dataMiningService.getIdentityFunction(localeName, new AsyncCallback<FunctionDTO>() {
            @Override
            public void onSuccess(FunctionDTO identityFunction) {
                SuggestBoxStatisticProvider.this.identityFunction = identityFunction;
                for (DataRetrieverChainDefinitionDTO retrieverChain : retrieverChains) {
                    availableIdentityFunctions.add(new IdentityFunctionWithContext(retrieverChain, identityFunction));
                }
                Collections.sort(availableIdentityFunctions);
            }
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error fetching the identity function: " + caught.getMessage());
            }
        });
    }

    private void updateStatistics(String localeName, Collection<DataRetrieverChainDefinitionDTO> retrieverChains) {
        availableStatistics.clear();
        if (retrieverChains.isEmpty()) {
            extractionFunctionSuggestBox.setValue(null);
            extractionFunctionSuggestBox.setSelectableValues(Collections.emptySet());
            return;
        }
        
        awaitingRetrieverChainStatistics = retrieverChains.size();
        awaitingAggregators = 0;
        for (DataRetrieverChainDefinitionDTO retrieverChain : retrieverChains) {
            dataMiningService.getStatisticsFor(retrieverChain, localeName,
                new AsyncCallback<HashSet<FunctionDTO>>() {
                    @Override
                    public void onSuccess(HashSet<FunctionDTO> statistics) {
                        collectStatistics(localeName, retrieverChain, statistics);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error fetching the statistics for the retriever chain '"
                                        + retrieverChain + "': " + caught.getMessage());
                        collectStatistics(localeName, retrieverChain, Collections.emptySet());
                    }
                });
        }
    }

    private void collectStatistics(String localeName, DataRetrieverChainDefinitionDTO retrieverChain,
            Iterable<FunctionDTO> extractionFunctions) {
        for (FunctionDTO extractionFunction : extractionFunctions) {
            availableStatistics.add(new StatisticWithContext(retrieverChain, extractionFunction));
            if (!collectedAggregators.containsKey(extractionFunction.getReturnTypeName())) {
                awaitingAggregators++;
                dataMiningService.getAggregatorDefinitionsFor(extractionFunction, localeName, new AsyncCallback<HashSet<AggregationProcessorDefinitionDTO>>() {
                    @Override
                    public void onSuccess(HashSet<AggregationProcessorDefinitionDTO> aggregators) {
                        collectAggregators(extractionFunction, aggregators);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error fetching the aggregators for " + extractionFunction, caught.getMessage());
                        collectAggregators(null, null);
                    }
                });
            }
        }

        awaitingRetrieverChainStatistics--;
        if (awaitingRetrieverChainStatistics <= 0) {
            extractionFunctionSuggestBox.setValue(null);
            Collections.sort(availableStatistics);
            extractionFunctionSuggestBox.setSelectableValues(availableStatistics);
            if (awaitingAggregators <= 0) {
                collectAggregators(null, null);
            }
        }
    }

    private void collectAggregators(FunctionDTO extractionFunction, Iterable<AggregationProcessorDefinitionDTO> aggregators) {
        if (extractionFunction != null) {
            String returnTypeName = extractionFunction.getReturnTypeName();
            Set<AggregationProcessorDefinitionDTO> aggregatorsForType = collectedAggregators.get(returnTypeName);
            if (aggregatorsForType == null) {
                aggregatorsForType = new HashSet<>();
                collectedAggregators.put(returnTypeName, aggregatorsForType);
            }
            Util.addAll(aggregators, aggregatorsForType);
        }
        
        awaitingAggregators--;
        if (awaitingAggregators <= 0) {
            Map<String, AggregatorGroup> aggregatorGroups = new HashMap<>();
            for (String returnTypeName : collectedAggregators.keySet()) {
                for (AggregationProcessorDefinitionDTO aggregator : collectedAggregators.get(returnTypeName)) {
                    String aggregatorKey = aggregator.getMessageKey();
                    AggregatorGroup aggregatorGroup = aggregatorGroups.get(aggregatorKey);
                    if (aggregatorGroup == null) {
                        aggregatorGroup = new AggregatorGroup(aggregatorKey);
                        aggregatorGroups.put(aggregatorKey, aggregatorGroup);
                    }
                    
                    String extractedTypeName = aggregator.getExtractedTypeName();
                    if (!aggregatorGroup.supportsType(extractedTypeName)) {
                        aggregatorGroup.setForType(extractedTypeName, aggregator);
                    }
                    if (!aggregatorGroup.supportsType(returnTypeName)) {
                        aggregatorGroup.setForType(returnTypeName, aggregator);
                    }
                }
            }
            
            availableAggregators.clear();
            availableAggregators.addAll(aggregatorGroups.values());
            Collections.sort(availableAggregators);
            aggregatorListBox.setValue(null);
            aggregatorListBox.setAcceptableValues(availableAggregators);
            aggregatorListBox.setEnabled(!availableAggregators.isEmpty());
        }
    }

    private void aggregatorSelectionChanged(AggregatorGroup newAggregator) {
        if (!Objects.equals(currentAggregator, newAggregator)) {
            boolean currentAggregatorSupportsIdentityFunction = currentAggregator != null
                    && currentAggregator.supportsType(identityFunction.getReturnTypeName());
            boolean newAggregatorSupportsIdentityFunction = newAggregator != null
                    && newAggregator.supportsType(identityFunction.getReturnTypeName());
            
            List<ExtractionFunctionWithContext> selectableExtractionFunctions = new ArrayList<>();
            String labelBetweenAggregatorAndStatisticText = null;
            if (newAggregator == null) {
                selectableExtractionFunctions.addAll(availableStatistics);
                labelBetweenAggregatorAndStatisticText = getDataMiningStringMessages().of();
            } else {
                if (newAggregatorSupportsIdentityFunction) {
                    selectableExtractionFunctions.addAll(availableIdentityFunctions);
                    labelBetweenAggregatorAndStatisticText = getDataMiningStringMessages().the();
                } else {
                    for (StatisticWithContext statistic : availableStatistics) {
                        if (newAggregator.supportsType(statistic.getExtractionFunction().getReturnTypeName())) {
                            selectableExtractionFunctions.add(statistic);
                        }
                    }
                    labelBetweenAggregatorAndStatisticText = getDataMiningStringMessages().of();
                }
            }
            
            ExtractionFunctionWithContext currentExtractionFunction = extractionFunctionSuggestBox.getExtractionFunction();
            if (currentExtractionFunction != null && !selectableExtractionFunctions.contains(currentExtractionFunction)) {
                ExtractionFunctionWithContext extractionFunctionToSelect = null;
                if (newAggregatorSupportsIdentityFunction && !currentAggregatorSupportsIdentityFunction) {
                    for (IdentityFunctionWithContext identityFunction : availableIdentityFunctions) {
                        if (identityFunction.getRetrieverChain().equals(currentExtractionFunction.getRetrieverChain())) {
                            extractionFunctionToSelect = identityFunction;
                            break;
                        }
                    }
                }
                extractionFunctionSuggestBox.setExtractionFunction(extractionFunctionToSelect);
            }
            Collections.sort(selectableExtractionFunctions);
            extractionFunctionSuggestBox.setSelectableValues(selectableExtractionFunctions);
            labelBetweenAggregatorAndStatistic.setText(labelBetweenAggregatorAndStatisticText);
            currentAggregator = newAggregator;
        }
        notifyAggregatorDefinitionListeners();
    }
    
    private void extractionFunctionSelectionChanged(ExtractionFunctionWithContext oldExtractionFunction,
            ExtractionFunctionWithContext newExtractionFunction) {
        String returnTypeName = newExtractionFunction == null ? null
                : newExtractionFunction.getExtractionFunction().getReturnTypeName();
        aggregatorListBox.updateItemStyles(returnTypeName);
        
        DataRetrieverChainDefinitionDTO oldRetrieverChain = oldExtractionFunction == null ? null
                : oldExtractionFunction.getRetrieverChain();
        DataRetrieverChainDefinitionDTO newRetrieverChain = newExtractionFunction == null ? null
                : newExtractionFunction.getRetrieverChain();
        if (!Objects.equals(oldRetrieverChain, newRetrieverChain)) {
            notifyRetrieverChainListeners();
        }
        notifyExtractionFunctionListeners();
    }

    @Override
    public HashMap<DataRetrieverLevelDTO, SerializableSettings> getRetrieverSettings() {
        DataRetrieverChainDefinitionDTO retrieverChain = getDataRetrieverChainDefinition();
        if (retrieverChain != null && settingsMap.containsKey(retrieverChain)) {
            return settingsMap.get(retrieverChain);
        } else {
            return new HashMap<>();
        }
    }

    @Override
    public boolean hasSettings() {
        DataRetrieverChainDefinitionDTO retrieverChain = getDataRetrieverChainDefinition();
        return retrieverChain != null && retrieverChain.hasSettings();
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
            settingsComponents.add(new RetrieverLevelSettingsComponent(this, getComponentContext(), retrieverLevel,
                    settingsInfo.getId(), settingsInfo.getLocalizedName()) {
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
        DataRetrieverChainDefinitionDTO retrieverChain = getDataRetrieverChainDefinition();
        if (retrieverChain != null) {
            for (Entry<DataRetrieverLevelDTO, SerializableSettings> retrieverLevelSettings : settingsMap
                    .get(retrieverChain).entrySet()) {
                final DataRetrieverLevelDTO retrieverLevel = retrieverLevelSettings.getKey();
                final Class<?> settingsType = retrieverLevelSettings.getValue().getClass();
                DataMiningSettingsInfo settingsInfo = settingsManager.getSettingsInfo(settingsType);
                RetrieverLevelSettingsComponent c = new RetrieverLevelSettingsComponent(this, getComponentContext(),
                        retrieverLevel, settingsInfo.getId(), settingsInfo.getLocalizedName()) {
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
        }
        return new CompositeSettings(settings);
    }

    @Override
    public void applyQueryDefinition(StatisticQueryDefinitionDTO queryDefinition, Consumer<Iterable<String>> callback) {
        Collection<String> errorMessages = new ArrayList<>();

        AggregationProcessorDefinitionDTO aggregator = queryDefinition.getAggregatorDefinition();
        AggregatorGroup aggregatorToSelect = null;
        for (AggregatorGroup availableAggregator : availableAggregators) {
            if (availableAggregator.getKey().equals(aggregator.getMessageKey()) &&
                    availableAggregator.supportsType(aggregator.getExtractedTypeName())) {
                aggregatorToSelect = availableAggregator;
                break;
            }
        }
        aggregatorListBox.setValue(aggregatorToSelect, true);
        if (aggregatorToSelect == null) {
            errorMessages.add(getDataMiningStringMessages().aggregatorNotAvailable(aggregator.getDisplayName()));
        }

        DataRetrieverChainDefinitionDTO retrieverChain = queryDefinition.getDataRetrieverChainDefinition();
        FunctionDTO extractionFunction = queryDefinition.getStatisticToCalculate();
        boolean isIdentityFunction = identityFunction != null && identityFunction.equals(extractionFunction);
        ExtractionFunctionWithContext extractionFunctionToSelect = null;
        if (isIdentityFunction) {
            IdentityFunctionWithContext identityFunctionWrapper = new IdentityFunctionWithContext(retrieverChain, extractionFunction);
            int index = availableIdentityFunctions.indexOf(identityFunctionWrapper);
            if (index != -1) {
                extractionFunctionToSelect = availableIdentityFunctions.get(index);
            }
        } else {
            StatisticWithContext statisticWrapper = new StatisticWithContext(retrieverChain, extractionFunction);
            int index = availableStatistics.indexOf(statisticWrapper);
            if (index != -1) {
                extractionFunctionToSelect = availableStatistics.get(index);
            }
        }
        extractionFunctionSuggestBox.setExtractionFunction(extractionFunctionToSelect);
        if (extractionFunctionToSelect != null) {
            setSettings(retrieverChain, queryDefinition.getRetrieverSettings());
        } else {
            errorMessages.add(getDataMiningStringMessages().statisticNotAvailable(
                    extractionFunction.getDisplayName()));
        }

        callback.accept(errorMessages);
    }

    private void setSettings(DataRetrieverChainDefinitionDTO retrieverChain, HashMap<DataRetrieverLevelDTO, SerializableSettings> settings) {
        HashMap<DataRetrieverLevelDTO, SerializableSettings> newSettings = new HashMap<>();
        for (int level = 0; level < retrieverChain.getLevelAmount(); level++) {
            DataRetrieverLevelDTO retrieverLevel = retrieverChain.getRetrieverLevel(level);
            if (retrieverLevel.hasSettings()) {
                SerializableSettings levelSettings = settings.get(retrieverLevel);
                if (levelSettings == null) {
                    levelSettings = retrieverLevel.getDefaultSettings();
                }
                newSettings.put(retrieverLevel, levelSettings);
            }
        }
        settingsMap.put(retrieverChain, newSettings);
    }

    @Override
    public void addStatisticChangedListener(StatisticChangedListener listener) {
        retrieverChainListeners.add(listener);
        extractionFunctionListeners.add(listener);
        aggregatorDefinitionListeners.add(listener);
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
    public void addExtractionFunctionChangedListener(ExtractionFunctionChangedListener listener) {
        extractionFunctionListeners.add(listener);
    }

    private void notifyExtractionFunctionListeners() {
        FunctionDTO extractionFunction = getExtractionFunction();
        for (ExtractionFunctionChangedListener listener : extractionFunctionListeners) {
            listener.extractionFunctionChanged(extractionFunction);
        }
    }

    @Override
    public void addAggregatorDefinitionChangedListener(AggregatorDefinitionChangedListener listener) {
        aggregatorDefinitionListeners.add(listener);
    }

    private void notifyAggregatorDefinitionListeners() {
        AggregationProcessorDefinitionDTO aggregatorDefinition = getAggregatorDefinition();
        for (AggregatorDefinitionChangedListener listener : aggregatorDefinitionListeners) {
            listener.aggregatorDefinitionChanged(aggregatorDefinition);
        }
    }

    @Override
    public DataRetrieverChainDefinitionDTO getDataRetrieverChainDefinition() {
        ExtractionFunctionWithContext extractionFunction = extractionFunctionSuggestBox.getExtractionFunction();
        return extractionFunction == null ? null : extractionFunction.getRetrieverChain();
    }

    @Override
    public FunctionDTO getExtractionFunction() {
        ExtractionFunctionWithContext extractionFunction = extractionFunctionSuggestBox.getExtractionFunction();
        return extractionFunction == null ? null : extractionFunction.getExtractionFunction();
    }

    @Override
    public AggregationProcessorDefinitionDTO getAggregatorDefinition() {
        AggregatorGroup aggregator = aggregatorListBox.getValue();
        FunctionDTO extractionFunction = getExtractionFunction();
        if (aggregator == null || extractionFunction == null) {
            return null;
        }
        return aggregator.getForType(extractionFunction.getReturnTypeName());
    }

    @Override
    public String getLocalizedShortName() {
        return getDataMiningStringMessages().statisticProvider();
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

    private static abstract class ExtractionFunctionWithContext implements Comparable<ExtractionFunctionWithContext> {

        private final DataRetrieverChainDefinitionDTO retrieverChain;
        private final FunctionDTO extractionFunction;
        private final Collection<String> matchingStrings;

        protected ExtractionFunctionWithContext(DataRetrieverChainDefinitionDTO retrieverChain,
                FunctionDTO extractionFunction) {
            this.retrieverChain = retrieverChain;
            this.extractionFunction = extractionFunction;
            matchingStrings = new ArrayList<>(4);
        }

        public DataRetrieverChainDefinitionDTO getRetrieverChain() {
            return retrieverChain;
        }

        public FunctionDTO getExtractionFunction() {
            return extractionFunction;
        }
        
        public abstract String getDisplayString();
        
        public abstract String getAdditionalDisplayString();

        public Iterable<String> getMatchingStrings() {
            return matchingStrings;
        }
        
        protected void addMatchingString(String matchingString) {
            matchingStrings.add(matchingString);
        }

        @Override
        public int compareTo(ExtractionFunctionWithContext o) {
            String otherDisplayName = o.getExtractionFunction().getDisplayName();
            int comparedDisplayName = extractionFunction.getDisplayName().compareToIgnoreCase(otherDisplayName);
            if (comparedDisplayName != 0) {
                return comparedDisplayName;
            }
            return retrieverChain.compareTo(o.getRetrieverChain());
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((extractionFunction == null) ? 0 : extractionFunction.hashCode());
            result = prime * result + ((retrieverChain == null) ? 0 : retrieverChain.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ExtractionFunctionWithContext other = (ExtractionFunctionWithContext) obj;
            if (extractionFunction == null) {
                if (other.extractionFunction != null)
                    return false;
            } else if (!extractionFunction.equals(other.extractionFunction))
                return false;
            if (retrieverChain == null) {
                if (other.retrieverChain != null)
                    return false;
            } else if (!retrieverChain.equals(other.retrieverChain))
                return false;
            return true;
        }

    }
    
    private static class StatisticWithContext extends ExtractionFunctionWithContext {

        protected StatisticWithContext(DataRetrieverChainDefinitionDTO retrieverChain, FunctionDTO extractionFunction) {
            super(retrieverChain, extractionFunction);
            addMatchingString(retrieverChain.getName());
            addMatchingString(extractionFunction.getDisplayName());
        }

        @Override
        public String getDisplayString() {
            return getExtractionFunction().getDisplayName();
        }

        @Override
        public String getAdditionalDisplayString() {
            return getRetrieverChain().getName();
        }
        
    }
    
    private static class IdentityFunctionWithContext extends ExtractionFunctionWithContext {

        protected IdentityFunctionWithContext(DataRetrieverChainDefinitionDTO retrieverChain,
                FunctionDTO identityFunction) {
            super(retrieverChain, identityFunction);
            addMatchingString(retrieverChain.getName());
        }

        @Override
        public String getDisplayString() {
            return getRetrieverChain().getName();
        }

        @Override
        public String getAdditionalDisplayString() {
            return null;
        }
        
    }
    
    private static class AggregatorGroup implements Comparable<AggregatorGroup> {
        
        private final String key;
        private final Map<String, AggregationProcessorDefinitionDTO> aggregatorsBySupportedTypeName;
        
        private String displayName;
        
        public AggregatorGroup(String key) {
            this.key = key;
            aggregatorsBySupportedTypeName = new HashMap<>();
        }
        
        public String getKey() {
            return key;
        }
        
        public boolean supportsType(String typeName) {
            return aggregatorsBySupportedTypeName.containsKey(typeName);
        }
        
        public AggregationProcessorDefinitionDTO getForType(String typeName) {
            return aggregatorsBySupportedTypeName.get(typeName);
        }
        
        public void setForType(String typeName, AggregationProcessorDefinitionDTO aggregator) {
            if (displayName == null) {
                displayName = aggregator.getDisplayName();
            }
            aggregatorsBySupportedTypeName.put(typeName, aggregator);
        }
        
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public int compareTo(AggregatorGroup other) {
            if (other == null) {
                return 1;
            }
            return displayName.compareToIgnoreCase(other.displayName);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((aggregatorsBySupportedTypeName == null) ? 0 : aggregatorsBySupportedTypeName.hashCode());
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            AggregatorGroup other = (AggregatorGroup) obj;
            if (aggregatorsBySupportedTypeName == null) {
                if (other.aggregatorsBySupportedTypeName != null)
                    return false;
            } else if (!aggregatorsBySupportedTypeName.equals(other.aggregatorsBySupportedTypeName))
                return false;
            if (key == null) {
                if (other.key != null)
                    return false;
            } else if (!key.equals(other.key))
                return false;
            return true;
        }
        
    }
    
    private static class AggregatorListBox extends ValueListBox<AggregatorGroup> {
        
        private static final String UnsupportedItemStyle = "unsupportedAggregatorListItem";
        
        private final Map<String, AggregatorGroup> aggregatorsByDisplayName;
        
        public AggregatorListBox(String nullDisplayString) {
            super(new AbstractRenderer<AggregatorGroup>() {
                @Override
                public String render(AggregatorGroup aggregator) {
                    return aggregator == null ? nullDisplayString : aggregator.getDisplayName();
                }
            });
            aggregatorsByDisplayName = new HashMap<>();
        }
        
        public void updateItemStyles(String extractionFunctionReturnTypeName) {
            ListBox listBox = (ListBox) getWidget();
            SelectElement selectElement = SelectElement.as(listBox.getElement());
            NodeList<OptionElement> options = selectElement.getOptions();
            for (int i = 0; i < options.getLength(); i++) {
                OptionElement option = options.getItem(i);
                AggregatorGroup aggregator = aggregatorsByDisplayName.get(option.getText());
                String className = aggregator == null || extractionFunctionReturnTypeName == null
                        || aggregator.supportsType(extractionFunctionReturnTypeName) ? "" : UnsupportedItemStyle;
                option.setClassName(className);
            }
        }
        
        @Override
        public void setAcceptableValues(Collection<AggregatorGroup> newValues) {
            aggregatorsByDisplayName.clear();
            for (AggregatorGroup aggregator : newValues) {
                aggregatorsByDisplayName.put(aggregator.displayName, aggregator);
            }
            super.setAcceptableValues(newValues);
        }
        
    }

    private static class ExtractionFunctionSuggestBox extends CustomSuggestBox<ExtractionFunctionWithContext> {
        
        @FunctionalInterface
        public interface ValueChangeHandler {
            void valueChanged(ExtractionFunctionWithContext oldValue, ExtractionFunctionWithContext newValue);
        }

        private final AbstractListSuggestOracle<ExtractionFunctionWithContext> suggestOracle;
        private final ScrollableSuggestionDisplay display;
        private ValueChangeHandler valueChangeHandler;
        
        private ExtractionFunctionWithContext extractionFunction;

        @SuppressWarnings("unchecked")
        public ExtractionFunctionSuggestBox() {
            super(new AbstractListSuggestOracle<ExtractionFunctionWithContext>() {
                @Override
                protected Iterable<String> getKeywordStrings(Iterable<String> queryTokens) {
                    String filterText = Util.first(queryTokens);
                    if (filterText == null) {
                        return queryTokens;
                    }
                    return Util.splitAlongWhitespaceRespectingDoubleQuotedPhrases(filterText);
                }

                @Override
                protected Iterable<String> getMatchingStrings(ExtractionFunctionWithContext value) {
                    return value.getMatchingStrings();
                }

                @Override
                protected String createSuggestionKeyString(ExtractionFunctionWithContext value) {
                    return value.getDisplayString();
                }

                @Override
                protected String createSuggestionAdditionalDisplayString(ExtractionFunctionWithContext value) {
                    return value.getAdditionalDisplayString();
                }
            }, new ScrollableSuggestionDisplay());
            suggestOracle = (AbstractListSuggestOracle<ExtractionFunctionWithContext>) getSuggestOracle();
            display = (ScrollableSuggestionDisplay) getSuggestionDisplay();
            addSuggestionSelectionHandler(this::setExtractionFunction);
        }
        
        public void setValueChangeHandler(ValueChangeHandler valueChangeHandler) {
            this.valueChangeHandler = valueChangeHandler;
        }

        public void setSelectableValues(Collection<? extends ExtractionFunctionWithContext> selectableValues) {
            suggestOracle.setSelectableValues(selectableValues);
        }

        public void setExtractionFunction(ExtractionFunctionWithContext extractionFunction) {
            if (!Objects.equals(this.extractionFunction, extractionFunction)) {
                ExtractionFunctionWithContext oldValue = this.extractionFunction;
                this.extractionFunction = extractionFunction;
                setValue(extractionFunction == null ? null : extractionFunction.getDisplayString(), false);
                if (valueChangeHandler != null) {
                    valueChangeHandler.valueChanged(oldValue, extractionFunction);
                }
            }
            setFocus(false);
        }

        public ExtractionFunctionWithContext getExtractionFunction() {
            return extractionFunction;
        }
        
        @Override
        public void hideSuggestionList() {
            display.hideSuggestions();
        }

    }

    private static class ScrollableSuggestionDisplay extends DefaultSuggestionDisplay {

        public ScrollableSuggestionDisplay() {
            getPopupPanel().addStyleName("statisticSuggestBoxPopup");
        }
        
        @Override
        protected void moveSelectionUp() {
            super.moveSelectionUp();
            scrollSelectedItemIntoView();
        }

        @Override
        protected void moveSelectionDown() {
            super.moveSelectionDown();
            scrollSelectedItemIntoView();
        }

        private void scrollSelectedItemIntoView() {
            getSelectedMenuItem().getElement().scrollIntoView();
        }

        private native MenuItem getSelectedMenuItem() /*-{
                        var menu = this.@com.google.gwt.user.client.ui.SuggestBox.DefaultSuggestionDisplay::suggestionMenu;
                        return menu.@com.google.gwt.user.client.ui.MenuBar::selectedItem;
        }-*/;

    }

}
