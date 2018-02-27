package com.sap.sailing.gwt.ui.datamining.selection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.suggestion.AbstractListSuggestOracle;
import com.sap.sailing.gwt.home.shared.partials.filter.AbstractListSuggestBoxFilter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.AbstractObjectRenderer;
import com.sap.sailing.gwt.ui.datamining.AggregatorDefinitionChangedListener;
import com.sap.sailing.gwt.ui.datamining.DataMiningServiceAsync;
import com.sap.sailing.gwt.ui.datamining.DataMiningSettingsControl;
import com.sap.sailing.gwt.ui.datamining.DataMiningSettingsInfo;
import com.sap.sailing.gwt.ui.datamining.DataMiningSettingsInfoManager;
import com.sap.sailing.gwt.ui.datamining.DataRetrieverChainDefinitionChangedListener;
import com.sap.sailing.gwt.ui.datamining.ExtractionFunctionChangedListener;
import com.sap.sailing.gwt.ui.datamining.StatisticChangedListener;
import com.sap.sailing.gwt.ui.datamining.StatisticProvider;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.filter.AbstractKeywordFilter;
import com.sap.sse.common.filter.Filter;
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
public class ListBoxStatisticProvider extends AbstractComponent<CompositeSettings> implements StatisticProvider {
    
    private static final String STATISTIC_PROVIDER_ELEMENT_STYLE = "statisticProviderElement";

    private final StringMessages stringMessages;
    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    private final Set<DataRetrieverChainDefinitionChangedListener> retrieverChainListeners;
    private final Set<ExtractionFunctionChangedListener> extractionFunctionListeners;
    private final Set<AggregatorDefinitionChangedListener> aggregatorDefinitionListeners;
    private boolean isAwaitingReload;
    private int awaitingRetrieverChainStatistics;
    
    private final DataMiningSettingsInfoManager settingsManager;
    private final DataMiningSettingsControl settingsControl;
    private final Map<DataRetrieverChainDefinitionDTO, HashMap<DataRetrieverLevelDTO, SerializableSettings>> settingsMap;
    private final List<Component<?>> retrieverLevelSettingsComponents;
    
    private final FlowPanel mainPanel;
    private final List<Pair<DataRetrieverChainDefinitionDTO, FunctionDTO>> availableExtractionFunctions;
    private final ExtractionFunctionTextBoxFilter extractionFunctionTextBox;
    private final ValueListBox<AggregationProcessorDefinitionDTO> aggregatorListBox;
    
    private Pair<DataRetrieverChainDefinitionDTO, FunctionDTO> selectedExtractionFunction;

    public ListBoxStatisticProvider(Component<?> parent, ComponentContext<?> componentContext,
            StringMessages stringMessages, DataMiningServiceAsync dataMiningService,
            ErrorReporter errorReporter, DataMiningSettingsControl settingsControl) {
        super(parent, componentContext);
        this.stringMessages = stringMessages;
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        retrieverChainListeners = new HashSet<>();
        extractionFunctionListeners = new HashSet<>();
        aggregatorDefinitionListeners = new HashSet<>();
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
        
        availableExtractionFunctions = new ArrayList<>();
        extractionFunctionTextBox = new ExtractionFunctionTextBoxFilter() {
            @Override
            protected void onSuggestionSelected(Pair<DataRetrieverChainDefinitionDTO, FunctionDTO> selectedItem) {
                selectedExtractionFunction = selectedItem;
                notifyRetrieverChainListeners();
                notifyExtractionFunctionListeners();
                updateAggregators();
            }
        };
        extractionFunctionTextBox.addStyleName(STATISTIC_PROVIDER_ELEMENT_STYLE);
        // TODO SuggestBox styling: Width, request length, enable scrolling
        extractionFunctionTextBox.setWidth("60%");
        mainPanel.add(extractionFunctionTextBox);
        
        aggregatorListBox = createAggregatorListBox();
        aggregatorListBox.addStyleName(STATISTIC_PROVIDER_ELEMENT_STYLE);
        mainPanel.add(aggregatorListBox);
        
        updateContent();
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
                notifyAggregatorDefinitionListeners();
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
        updateContent();
    }
    
    private void updateContent() {
        final String localeName = LocaleInfo.getCurrentLocale().getLocaleName();
        dataMiningService.getDataRetrieverChainDefinitions(localeName,
            new AsyncCallback<ArrayList<DataRetrieverChainDefinitionDTO>>() {
                @Override
                public void onSuccess(ArrayList<DataRetrieverChainDefinitionDTO> dataRetrieverChainDefinitions) {
                    settingsMap.clear();
                    awaitingRetrieverChainStatistics = dataRetrieverChainDefinitions.size();
                    availableExtractionFunctions.clear();
                    if (awaitingRetrieverChainStatistics == 0) {
                        extractionFunctionTextBox.setSelectableValues(Collections.emptyList());
                    } else {
                        for (DataRetrieverChainDefinitionDTO retrieverChain : dataRetrieverChainDefinitions) {
                            if (retrieverChain.hasSettings()) {
                                settingsMap.put(retrieverChain, retrieverChain.getDefaultSettings());
                            }
                            dataMiningService.getStatisticsFor(retrieverChain, localeName, new AsyncCallback<HashSet<FunctionDTO>>() {
                                @Override
                                public void onSuccess(HashSet<FunctionDTO> statistics) {
                                    collectStatistics(retrieverChain, statistics);
                                }
                                @Override
                                public void onFailure(Throwable caught) {
                                    errorReporter.reportError("Error fetching the statistics for the retriever chain '" +
                                                              retrieverChain + "': " + caught.getMessage());
                                    collectStatistics(retrieverChain, Collections.emptySet());
                                }
                            });
                        }
                    }
                }
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Error fetching the retriever chain definitions: " + caught.getMessage());
                }
            }
        );
    }
    
    private void collectStatistics(DataRetrieverChainDefinitionDTO retrieverChain, Iterable<FunctionDTO> statistics) {
        for (FunctionDTO statistic : statistics) {
            availableExtractionFunctions.add(new Pair<>(retrieverChain, statistic));
        }
        
        awaitingRetrieverChainStatistics--;
        if (awaitingRetrieverChainStatistics == 0) {
            Collections.sort(availableExtractionFunctions, (p1, p2) -> {
                int retrieverChainComparison = p1.getA().compareTo(p2.getA());
                return retrieverChainComparison != 0 ? retrieverChainComparison : p1.getB().compareTo(p2.getB());
            });
            extractionFunctionTextBox.setSelectableValues(availableExtractionFunctions);
        }
    }
    
    private void updateAggregators() {
        FunctionDTO extractionFunction = getExtractionFunction();
        if (extractionFunction == null) {
            updateListBox(aggregatorListBox, Collections.emptyList());
        } else {
            dataMiningService.getAggregatorDefinitionsFor(extractionFunction, LocaleInfo.getCurrentLocale().getLocaleName(),
                new AsyncCallback<HashSet<AggregationProcessorDefinitionDTO>>() {
                    @Override
                    public void onSuccess(HashSet<AggregationProcessorDefinitionDTO> aggregators) {
                        List<AggregationProcessorDefinitionDTO> aggregatorsList = new ArrayList<>(aggregators);
                        Collections.sort(aggregatorsList);
                        updateListBox(aggregatorListBox, aggregatorsList);
                    }
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error fetching the aggregators for the extraction function'" +
                                                  extractionFunction + "': " + caught.getMessage());
                    }
                }
            );
        }
    }
    
    private <T> void updateListBox(ValueListBox<T> listBox, Collection<T> acceptableValues) {
        T currentValue = listBox.getValue();
        T valueToBeSelected = acceptableValues.contains(currentValue) ? currentValue : Util.first(acceptableValues);
        
        listBox.setValue(valueToBeSelected, true);
        listBox.setAcceptableValues(acceptableValues);
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
    public void applyQueryDefinition(StatisticQueryDefinitionDTO queryDefinition) {
        // TODO How to set the value/text of an AbstractListSuggestBoxFilter
//        DataRetrieverChainDefinitionDTO retrieverChain = queryDefinition.getDataRetrieverChainDefinition();
//        FunctionDTO extractionFunction = queryDefinition.getStatisticToCalculate();
//        extractionFunctionListBox.setValue(new Pair<>(retrieverChain, extractionFunction));
//        aggregatorListBox.setValue(queryDefinition.getAggregatorDefinition());
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
        return selectedExtractionFunction == null ? null : selectedExtractionFunction.getA();
    }
    
    @Override
    public FunctionDTO getExtractionFunction() {
        return selectedExtractionFunction == null ? null : selectedExtractionFunction.getB();
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
    
    // TODO AbstractListSuggestBoxFilter is located in home.shared.* It's a bit hacky to get that into the Data Mining module
    private static abstract class ExtractionFunctionTextBoxFilter extends
            AbstractListSuggestBoxFilter<Pair<DataRetrieverChainDefinitionDTO, FunctionDTO>, Pair<DataRetrieverChainDefinitionDTO, FunctionDTO>> {
        
        private final ExtractionFunctionFilter filter = new ExtractionFunctionFilter();
        
        public ExtractionFunctionTextBoxFilter() {
            super(new AbstractListSuggestOracle<Pair<DataRetrieverChainDefinitionDTO, FunctionDTO>>() {

                @Override
                protected Iterable<String> getMatchingStrings(
                        Pair<DataRetrieverChainDefinitionDTO, FunctionDTO> value) {
                    return Collections.singleton(value.getB().getDisplayName());
                }

                @Override
                protected String createSuggestionKeyString(Pair<DataRetrieverChainDefinitionDTO, FunctionDTO> value) {
                    return value.getB().getDisplayName();
                }

                @Override
                protected String createSuggestionAdditionalDisplayString(
                        Pair<DataRetrieverChainDefinitionDTO, FunctionDTO> value) {
                    return null;
                }
            }, "Placeholder"); // TODO Use real placeholder text
        }

        @Override
        protected Filter<Pair<DataRetrieverChainDefinitionDTO, FunctionDTO>> getFilter(String searchValue) {
            filter.setKeywords(Util.splitAlongWhitespaceRespectingDoubleQuotedPhrases(searchValue));
            return filter;
        }
        
        private class ExtractionFunctionFilter extends AbstractKeywordFilter<Pair<DataRetrieverChainDefinitionDTO, FunctionDTO>> {

            @Override
            public Iterable<String> getStrings(Pair<DataRetrieverChainDefinitionDTO, FunctionDTO> extractionFunction) {
                return Arrays.asList(extractionFunction.getB().getDisplayName().split("\\s"));
            }
            
        }
        
    }

}
