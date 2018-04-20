package com.sap.sse.datamining.ui.client.selection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.Util;
import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;
import com.sap.sse.datamining.shared.impl.dto.AggregationProcessorDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.ModifiableStatisticQueryDefinitionDTO;
import com.sap.sse.datamining.ui.client.AggregatorDefinitionChangedListener;
import com.sap.sse.datamining.ui.client.AggregatorDefinitionProvider;
import com.sap.sse.datamining.ui.client.DataMiningComponentProvider;
import com.sap.sse.datamining.ui.client.DataMiningServiceAsync;
import com.sap.sse.datamining.ui.client.DataMiningSettingsControl;
import com.sap.sse.datamining.ui.client.DataMiningSettingsInfoManager;
import com.sap.sse.datamining.ui.client.DataRetrieverChainDefinitionChangedListener;
import com.sap.sse.datamining.ui.client.DataRetrieverChainDefinitionProvider;
import com.sap.sse.datamining.ui.client.ExtractionFunctionChangedListener;
import com.sap.sse.datamining.ui.client.ExtractionFunctionProvider;
import com.sap.sse.datamining.ui.client.FilterSelectionChangedListener;
import com.sap.sse.datamining.ui.client.FilterSelectionProvider;
import com.sap.sse.datamining.ui.client.GroupingChangedListener;
import com.sap.sse.datamining.ui.client.GroupingProvider;
import com.sap.sse.datamining.ui.client.ResultsPresenter;
import com.sap.sse.datamining.ui.client.WithControls;
import com.sap.sse.datamining.ui.client.developer.PredefinedQueryRunner;
import com.sap.sse.datamining.ui.client.developer.QueryDefinitionViewer;
import com.sap.sse.datamining.ui.client.selection.filter.ListRetrieverChainFilterSelectionProvider;
import com.sap.sse.datamining.ui.client.settings.AdvancedDataMiningSettings;
import com.sap.sse.datamining.ui.client.settings.AdvancedDataMiningSettingsDialogComponent;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class QueryDefinitionProviderWithControls extends AbstractQueryDefinitionProvider<AdvancedDataMiningSettings>
        implements WithControls {

    private static final double headerPanelHeight = 45;
    private static final double footerPanelHeight = 50;

    private final DockLayoutPanel mainPanel;
    private final FlowPanel controlsPanel;
    private final ToggleButton queryDefinitionViewerToggleButton;
    private final QueryDefinitionViewer queryDefinitionViewer;
    private final PredefinedQueryRunner predefinedQueryRunner;
    private final DataMiningSettingsControl settingsControl;
    private final AdvancedDataMiningSettings settings;

    private final ProviderListener providerListener;
    private final Collection<DataMiningComponentProvider<?>> providers;

    private final DataRetrieverChainDefinitionProvider retrieverChainProvider;
    private final ExtractionFunctionProvider<?> extractionFunctionProvider;
    private final AggregatorDefinitionProvider<?> aggregationDefinitionProvider;
    private final GroupingProvider groupingProvider;
    private final SplitLayoutPanel filterSplitPanel;
    private final FilterSelectionProvider filterSelectionProvider;

    public QueryDefinitionProviderWithControls(Component<?> parent, ComponentContext<?> context,
            DataMiningSession session, DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter,
            DataMiningSettingsControl settingsControl, DataMiningSettingsInfoManager settingsManager,
            ResultsPresenter<?> resultsPresenter) {
        super(parent, context, dataMiningService, errorReporter);
        providerListener = new ProviderListener();
        // Creating the header panel, that contains the retriever chain provider and the controls
        controlsPanel = new FlowPanel();
        controlsPanel.addStyleName("definitionProviderControls");

        this.settingsControl = settingsControl;
        addControl(this.settingsControl.getEntryWidget());
        settings = new AdvancedDataMiningSettings();
        this.settingsControl.addSettingsComponent(this);

        queryDefinitionViewerToggleButton = new ToggleButton(getDataMiningStringMessages().viewQueryDefinition(),
                new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        filterSplitPanel.setWidgetHidden(queryDefinitionViewer.getEntryWidget(),
                                !queryDefinitionViewerToggleButton.getValue());
                    }
                });
        queryDefinitionViewer = new QueryDefinitionViewer(parent, context, getDataMiningStringMessages());
        addQueryDefinitionChangedListener(queryDefinitionViewer);
        predefinedQueryRunner = new PredefinedQueryRunner(parent, context, session, getDataMiningStringMessages(),
                dataMiningService, errorReporter, resultsPresenter);

        Button clearSelectionButton = new Button(getDataMiningStringMessages().clearSelection());
        clearSelectionButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                filterSelectionProvider.clearSelection();
            }
        });
        addControl(clearSelectionButton);

        Button reloadButton = new Button(getDataMiningStringMessages().reload());
        reloadButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                reloadComponents();
            }
        });
        addControl(reloadButton);

        if (settings.isDeveloperOptions()) {
            addControl(queryDefinitionViewerToggleButton);
            addControl(predefinedQueryRunner.getEntryWidget());
        }

        SuggestBoxStatisticProvider statisticProvider = new SuggestBoxStatisticProvider(parent, context,
                getDataMiningService(), getErrorReporter(), settingsControl, settingsManager);

        retrieverChainProvider = statisticProvider;
        retrieverChainProvider.addDataRetrieverChainDefinitionChangedListener(providerListener);

        SplitLayoutPanel headerPanel = new SplitLayoutPanel(15);
        headerPanel.addWest(statisticProvider.getEntryWidget(), 800);
        headerPanel.add(controlsPanel);

        extractionFunctionProvider = statisticProvider;
        extractionFunctionProvider.addExtractionFunctionChangedListener(providerListener);
        aggregationDefinitionProvider = statisticProvider;
        aggregationDefinitionProvider.addAggregatorDefinitionChangedListener(providerListener);

        groupingProvider = new MultiDimensionalGroupingProvider(parent, context, getDataMiningService(),
                getErrorReporter(), retrieverChainProvider);
        groupingProvider.addGroupingChangedListener(providerListener);

        filterSplitPanel = new SplitLayoutPanel(15);
        filterSplitPanel.addSouth(groupingProvider.getEntryWidget(), footerPanelHeight);
        filterSplitPanel.addEast(queryDefinitionViewer.getEntryWidget(), 600);
        filterSplitPanel.setWidgetHidden(queryDefinitionViewer.getEntryWidget(), true);
        filterSelectionProvider = new ListRetrieverChainFilterSelectionProvider(parent, context, session,
                dataMiningService, errorReporter, retrieverChainProvider);
        filterSelectionProvider.addSelectionChangedListener(providerListener);
        filterSplitPanel.add(filterSelectionProvider.getEntryWidget());

        mainPanel = new DockLayoutPanel(Unit.PX);
        mainPanel.addNorth(headerPanel, headerPanelHeight);
        mainPanel.add(filterSplitPanel);

        // Storing the different component providers in a list
        providers = new ArrayList<>();
        providers.add(retrieverChainProvider);
        providers.add(extractionFunctionProvider);
        providers.add(aggregationDefinitionProvider);
        providers.add(groupingProvider);
        providers.add(filterSelectionProvider);

        // Set await reload flag to initialize the components, when the retriever chains have been loaded
        providers.forEach(provider -> provider.awaitReloadComponents());
    }

    /**
     * The first {@link FunctionDTO dimension} of which the {@code groupKey} is assumed to be an instance will be
     * removed from the {@link #groupingProvider}. A filter criterion for that dimension will be added to the
     * {@link #filterSelectionProvider}, filtering such that only {@code groupKey} will be accepted as value. For this
     * purpose, the retriever levels are scanned from top to bottom to find the topmost occurrence of that dimension.
     * <p>
     * 
     * If the {@link #groupingProvider} had only one grouping level, a popup menu is displayed where the user must
     * select another dimension to group by, before the query can be run again. Otherwise, the query is executed again
     * after the first grouping level has been removed and the filter has been set.
     * 
     * @param onSuccessCallback
     *            called when the drill-down process was successful, including setting the filter and removing /
     *            replacing a grouping dimension
     */
    public void drillDown(GroupKey groupKey, Runnable onSuccessCallback) {
        assert groupKey instanceof GenericGroupKey<?>;
        final GenericGroupKey<?> groupKeyForSingleDimension = (GenericGroupKey<?>) groupKey;
        final Collection<FunctionDTO> dimensionsToGroupBy = groupingProvider.getDimensionsToGroupBy();
        if (!dimensionsToGroupBy.isEmpty()) {
            final FunctionDTO firstDimension = dimensionsToGroupBy.iterator().next();
            filterSelectionProvider.setHighestRetrieverLevelWithFilterDimension(firstDimension,
                    (Serializable) groupKeyForSingleDimension.getValue());
            if (dimensionsToGroupBy.size() == 1) {
                letUserSelectADifferentFirstDimension(onSuccessCallback);
            } else {
                groupingProvider.removeDimensionToGroupBy(firstDimension);
                onSuccessCallback.run();
            }
        }
    }

    private class FirstDimensionSelectionDialog extends DataEntryDialog<FunctionDTO> {
        private ValueListBox<FunctionDTO> dimensionChooser;

        public FirstDimensionSelectionDialog(DialogCallback<FunctionDTO> callback) {
            super(getDataMiningStringMessages().chooseDifferentDimensionTitle(),
                    getDataMiningStringMessages().chooseDifferentDimensionMessage(), getDataMiningStringMessages().ok(),
                    getDataMiningStringMessages().cancel(), new DataEntryDialog.Validator<FunctionDTO>() {
                        @Override
                        public String getErrorMessage(FunctionDTO valueToValidate) {
                            if (valueToValidate == null) {
                                return getDataMiningStringMessages().pleaseSelectADimension();
                            } else {
                                return null;
                            }
                        }
                    }, callback);
            dimensionChooser = groupingProvider.createDimensionToGroupByBoxWithoutEventHandler();
            Collection<FunctionDTO> acceptableValues = new ArrayList<>();
            Util.addAll(groupingProvider.getAvailableDimensions(), acceptableValues);
            acceptableValues.add(null);
            dimensionChooser.setAcceptableValues(acceptableValues);
            dimensionChooser.addValueChangeHandler(e -> validateAndUpdate());
        }

        @Override
        protected Widget getAdditionalWidget() {
            return dimensionChooser;
        }

        @Override
        protected FunctionDTO getResult() {
            return dimensionChooser.getValue();
        }
    }

    private void letUserSelectADifferentFirstDimension(final Runnable onSuccessCallback) {
        final FirstDimensionSelectionDialog dialog = new FirstDimensionSelectionDialog(
                new DialogCallback<FunctionDTO>() {
                    @Override
                    public void ok(FunctionDTO dimensionToGroupBy) {
                        groupingProvider.setDimensionToGroupBy(0, dimensionToGroupBy);
                        onSuccessCallback.run();
                    }

                    @Override
                    public void cancel() {
                    }
                });
        dialog.show();
    }

    @Override
    public void awaitReloadComponents() {
        // Nothing to do here
    }

    @Override
    public boolean isAwatingReload() {
        for (DataMiningComponentProvider<?> provider : providers) {
            if (provider.isAwatingReload()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void reloadComponents() {
        retrieverChainProvider.awaitReloadComponents();
        extractionFunctionProvider.awaitReloadComponents();
        aggregationDefinitionProvider.awaitReloadComponents();
        groupingProvider.awaitReloadComponents();
        filterSelectionProvider.awaitReloadComponents();
        retrieverChainProvider.reloadComponents();
    }

    @Override
    public StatisticQueryDefinitionDTO getQueryDefinition() {
        ModifiableStatisticQueryDefinitionDTO queryDTO = new ModifiableStatisticQueryDefinitionDTO(
                LocaleInfo.getCurrentLocale().getLocaleName(), extractionFunctionProvider.getExtractionFunction(),
                aggregationDefinitionProvider.getAggregatorDefinition(),
                retrieverChainProvider.getDataRetrieverChainDefinition());

        for (FunctionDTO dimension : groupingProvider.getDimensionsToGroupBy()) {
            queryDTO.appendDimensionToGroupBy(dimension);
        }

        for (Entry<DataRetrieverLevelDTO, SerializableSettings> retrieverSettingsEntry : retrieverChainProvider
                .getRetrieverSettings().entrySet()) {
            queryDTO.setRetrieverSettings(retrieverSettingsEntry.getKey(), retrieverSettingsEntry.getValue());
        }

        for (Entry<DataRetrieverLevelDTO, HashMap<FunctionDTO, HashSet<? extends Serializable>>> filterSelectionEntry : filterSelectionProvider
                .getSelection().entrySet()) {
            queryDTO.setFilterSelectionFor(filterSelectionEntry.getKey(), filterSelectionEntry.getValue());
        }

        return queryDTO;
    }

    @Override
    public void applyQueryDefinition(StatisticQueryDefinitionDTO queryDefinition) {
        setBlockChangeNotification(true);
        retrieverChainProvider.applyQueryDefinition(queryDefinition);
        extractionFunctionProvider.applyQueryDefinition(queryDefinition);
        aggregationDefinitionProvider.applyQueryDefinition(queryDefinition);
        groupingProvider.applyQueryDefinition(queryDefinition);
        filterSelectionProvider.applyQueryDefinition(queryDefinition);
        setBlockChangeNotification(false);
        notifyQueryDefinitionChanged();
    }

    @Override
    public void addControl(Widget controlWidget) {
        controlWidget.addStyleName("definitionProviderControlsElements");
        controlsPanel.add(controlWidget);
    }

    @Override
    public void removeControl(Widget controlWidget) {
        controlsPanel.remove(controlWidget);
    }

    @Override
    public Widget getEntryWidget() {
        return mainPanel;
    }

    @Override
    public String getLocalizedShortName() {
        return getDataMiningStringMessages().queryDefinitionProvider();
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
        return true;
    }

    @Override
    public AdvancedDataMiningSettings getSettings() {
        return settings;
    }

    @Override
    public SettingsDialogComponent<AdvancedDataMiningSettings> getSettingsDialogComponent(
            AdvancedDataMiningSettings settings) {
        return new AdvancedDataMiningSettingsDialogComponent(settings, getDataMiningStringMessages());
    }

    @Override
    public void updateSettings(AdvancedDataMiningSettings newSettings) {
        if (settings.isDeveloperOptions() != newSettings.isDeveloperOptions()) {
            settings.setDeveloperOptions(newSettings.isDeveloperOptions());
            if (settings.isDeveloperOptions()) {
                addControl(queryDefinitionViewerToggleButton);
                filterSplitPanel.setWidgetHidden(queryDefinitionViewer.getEntryWidget(),
                        !queryDefinitionViewerToggleButton.getValue());
                addControl(predefinedQueryRunner.getEntryWidget());
            } else {
                removeControl(queryDefinitionViewerToggleButton);
                filterSplitPanel.setWidgetHidden(queryDefinitionViewer.getEntryWidget(), true);
                removeControl(predefinedQueryRunner.getEntryWidget());
            }
        }
    }

    @Override
    public String getDependentCssClassName() {
        return "queryDefinitionProviderWithControls";
    }

    @Override
    public String getId() {
        return "QueryDefinitionProviderWithControls";
    }

    private class ProviderListener
            implements DataRetrieverChainDefinitionChangedListener, ExtractionFunctionChangedListener,
            AggregatorDefinitionChangedListener, GroupingChangedListener, FilterSelectionChangedListener {

        @Override
        public void dataRetrieverChainDefinitionChanged(
                DataRetrieverChainDefinitionDTO newDataRetrieverChainDefinition) {
            if (isAwatingReload()) {
                groupingProvider.dataRetrieverChainDefinitionChanged(newDataRetrieverChainDefinition);
                groupingProvider.reloadComponents();

                filterSelectionProvider.dataRetrieverChainDefinitionChanged(newDataRetrieverChainDefinition);
                filterSelectionProvider.reloadComponents();
            } else {
                notifyQueryDefinitionChanged();
            }
        }

        @Override
        public void aggregatorDefinitionChanged(AggregationProcessorDefinitionDTO newAggregatorDefinition) {
            if (!isAwatingReload()) {
                notifyQueryDefinitionChanged();
            }
        }

        @Override
        public void extractionFunctionChanged(FunctionDTO extractionFunction) {
            if (!isAwatingReload()) {
                notifyQueryDefinitionChanged();
            }
        }

        @Override
        public void groupingChanged() {
            if (!isAwatingReload()) {
                notifyQueryDefinitionChanged();
            }
        }

        @Override
        public void selectionChanged() {
            if (!isAwatingReload()) {
                notifyQueryDefinitionChanged();
            }
        }

    }

}
