package com.sap.sse.datamining.ui.client.selection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import com.google.gwt.dom.builder.shared.HtmlBuilderFactory;
import com.google.gwt.dom.builder.shared.HtmlUListBuilder;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
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
import com.sap.sse.datamining.ui.client.DataMiningComponentProvider;
import com.sap.sse.datamining.ui.client.DataMiningServiceAsync;
import com.sap.sse.datamining.ui.client.DataMiningSettingsControl;
import com.sap.sse.datamining.ui.client.DataMiningSettingsInfoManager;
import com.sap.sse.datamining.ui.client.FilterSelectionChangedListener;
import com.sap.sse.datamining.ui.client.FilterSelectionProvider;
import com.sap.sse.datamining.ui.client.GroupingChangedListener;
import com.sap.sse.datamining.ui.client.GroupingProvider;
import com.sap.sse.datamining.ui.client.StatisticChangedListener;
import com.sap.sse.datamining.ui.client.StatisticProvider;
import com.sap.sse.datamining.ui.client.StringMessages;
import com.sap.sse.datamining.ui.client.WithControls;
import com.sap.sse.datamining.ui.client.developer.PredefinedQueryRunner;
import com.sap.sse.datamining.ui.client.developer.QueryDefinitionViewer;
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

    private static final double HeaderPanelHeight = 45;
    private static final double FooterPanelHeight = 50;
    private static final int SplitterSize = 10;

    private final DockLayoutPanel mainPanel;
    private final FlowPanel controlsPanel;
    private final ToggleButton queryDefinitionViewerToggleButton;
    private final QueryDefinitionViewer queryDefinitionViewer;
    private final PredefinedQueryRunner predefinedQueryRunner;
    private final DataMiningSettingsControl settingsControl;
    private final AdvancedDataMiningSettings settings;

    private final ProviderListener providerListener;
    private final Collection<DataMiningComponentProvider<?>> providers;

    private final StatisticProvider statisticProvider;
    private final GroupingProvider groupingProvider;
    private final SplitLayoutPanel filterSplitPanel;
    private final FilterSelectionProvider filterSelectionProvider;
    
    private final DialogBox confirmChangeLossDialog;
    private StatisticQueryDefinitionDTO queryDefinitionToBeApplied;
    private boolean queryDefinitionChanged;

    public QueryDefinitionProviderWithControls(Component<?> parent, ComponentContext<?> context,
            DataMiningSession session, DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter,
            DataMiningSettingsControl settingsControl, DataMiningSettingsInfoManager settingsManager,
            Consumer<StatisticQueryDefinitionDTO> queryRunner) {
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
                        boolean active = queryDefinitionViewerToggleButton.isDown();
                        filterSplitPanel.setWidgetHidden(queryDefinitionViewer.getEntryWidget(), !active);
                        queryDefinitionViewer.setActive(active);
                    }
                });
        queryDefinitionViewer = new QueryDefinitionViewer(parent, context, getDataMiningStringMessages());
        queryDefinitionViewer.getEntryWidget().addStyleName("dataMiningMarginRight");
        queryDefinitionViewer.setActive(false);
        addQueryDefinitionChangedListener(queryDefinitionViewer);
        predefinedQueryRunner = new PredefinedQueryRunner(parent, context, getDataMiningStringMessages(),
                                                          dataMiningService, errorReporter, this, queryRunner);

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
        
        confirmChangeLossDialog = createConfirmChangeLossDialog();

        statisticProvider = new SuggestBoxStatisticProvider(parent, context, dataMiningService,
                                                            errorReporter, settingsControl, settingsManager);
        statisticProvider.getEntryWidget().addStyleName("statisticProvider");
        statisticProvider.addStatisticChangedListener(providerListener);
        
        groupingProvider = new MultiDimensionalGroupingProvider(parent, context, dataMiningService,
                                                                errorReporter, statisticProvider);
        groupingProvider.addGroupingChangedListener(providerListener);
        groupingProvider.getEntryWidget().addStyleName("dataMiningMarginBase");

        filterSelectionProvider = new HierarchicalDimensionListFilterSelectionProvider(parent, context, session,
                dataMiningService, errorReporter, statisticProvider);
        filterSelectionProvider.addSelectionChangedListener(providerListener);
        filterSelectionProvider.getEntryWidget().addStyleName("dataMiningBorderTop");
        
        filterSplitPanel = new SplitLayoutPanel(SplitterSize);
        filterSplitPanel.addSouth(groupingProvider.getEntryWidget(), FooterPanelHeight);
        filterSplitPanel.addEast(queryDefinitionViewer.getEntryWidget(), 600);
        filterSplitPanel.setWidgetHidden(queryDefinitionViewer.getEntryWidget(), true);
        filterSplitPanel.add(filterSelectionProvider.getEntryWidget());

        SplitLayoutPanel headerPanel = new SplitLayoutPanel(SplitterSize);
        headerPanel.addStyleName("dataMiningMarginBase");
        headerPanel.addWest(statisticProvider.getEntryWidget(), 800);
        headerPanel.add(controlsPanel);
        
        mainPanel = new DockLayoutPanel(Unit.PX);
        mainPanel.addNorth(headerPanel, HeaderPanelHeight);
        mainPanel.add(filterSplitPanel);

        // Storing the different component providers in a list
        providers = new ArrayList<>();
        providers.add(statisticProvider);
        providers.add(groupingProvider);
        providers.add(filterSelectionProvider);
        reloadComponents();
    }

    private DialogBox createConfirmChangeLossDialog() {
        StringMessages stringMessages = getDataMiningStringMessages();
        
        DialogBox dialog = new DialogBox(false, true);
        dialog.setAnimationEnabled(true);
        dialog.setText(stringMessages.changesWillBeLost());
        dialog.setGlassEnabled(true);

        VerticalPanel contentPanel = new VerticalPanel();
        contentPanel.setSpacing(5);
        contentPanel.add(new HTML(new SafeHtmlBuilder()
                .appendEscapedLines(stringMessages.confirmQueryDefinitionChangeLoss()).toSafeHtml()));

        FlowPanel buttonPanel = new FlowPanel();
        buttonPanel.addStyleName("floatRight");
        contentPanel.add(buttonPanel);
        
        Button discardChanges = new Button(stringMessages.discardChanges());
        discardChanges.addClickHandler(e -> {
            dialog.hide();
            setQueryDefinition(queryDefinitionToBeApplied);
            queryDefinitionToBeApplied = null;
        });
        discardChanges.addStyleName("dataMiningMarginLeft");
        buttonPanel.add(discardChanges);
        
        Button keepChanges = new Button(stringMessages.keepChanges());
        keepChanges.addClickHandler(e -> dialog.hide());
        keepChanges.addStyleName("dataMiningMarginLeft");
        buttonPanel.add(keepChanges);

        dialog.setWidget(contentPanel);
        return dialog;
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
            if (dimensionsToGroupBy.size() == 1) {
                letUserSelectADifferentFirstDimension(() -> {
                    filterSelectionProvider.setHighestRetrieverLevelWithFilterDimension(firstDimension,
                            (Serializable) groupKeyForSingleDimension.getValue());
                    onSuccessCallback.run();
                });
            } else {
                filterSelectionProvider.setHighestRetrieverLevelWithFilterDimension(firstDimension,
                        (Serializable) groupKeyForSingleDimension.getValue());
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

    public boolean providersAwaitingReload() {
        for (DataMiningComponentProvider<?> provider : providers) {
            if (provider.isAwaitingReload()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void reloadComponents() {
        providers.forEach(provider -> provider.awaitReloadComponents());
        statisticProvider.reloadComponents();
    }

    @Override
    public StatisticQueryDefinitionDTO getQueryDefinition() {
        ModifiableStatisticQueryDefinitionDTO queryDTO = new ModifiableStatisticQueryDefinitionDTO(
                LocaleInfo.getCurrentLocale().getLocaleName(), statisticProvider.getExtractionFunction(),
                statisticProvider.getAggregatorDefinition(), statisticProvider.getDataRetrieverChainDefinition());
        for (FunctionDTO dimension : groupingProvider.getDimensionsToGroupBy()) {
            queryDTO.appendDimensionToGroupBy(dimension);
        }

        for (Entry<DataRetrieverLevelDTO, SerializableSettings> retrieverSettingsEntry : statisticProvider.getRetrieverSettings().entrySet()) {
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
        if (queryDefinitionChanged) {
            queryDefinitionToBeApplied = queryDefinition;
            confirmChangeLossDialog.center();
        } else {
            setQueryDefinition(queryDefinition);
        }
    }

    private void setQueryDefinition(StatisticQueryDefinitionDTO queryDefinition) {
        Set<ApplyCallback> callbacks = new HashSet<>();
        Collection<String> errorMessages = new ArrayList<>();
        String retrieverChainName = queryDefinition.getDataRetrieverChainDefinition().getName();
        
        setBlockChangeNotification(true);
        
        ApplyCallback statisticCallback = new ApplyCallback(errorMessages, callbacks, retrieverChainName);
        callbacks.add(statisticCallback);
        statisticProvider.applyQueryDefinition(queryDefinition, statisticCallback);
        // The statistic wasn't available, if the callback was called immediately and an error occurred
        // Applying the query to the remaining component providers can be skipped
        if (!callbacks.isEmpty() || errorMessages.isEmpty()) {
            ApplyCallback groupingCallback = new ApplyCallback(errorMessages, callbacks, retrieverChainName);
            callbacks.add(groupingCallback);
            groupingProvider.applyQueryDefinition(queryDefinition, groupingCallback);
            
            ApplyCallback filterCallback = new ApplyCallback(errorMessages, callbacks, retrieverChainName);
            callbacks.add(filterCallback);
            filterSelectionProvider.applyQueryDefinition(queryDefinition, filterCallback);
        }
        
        if (!callbacks.isEmpty()) {
            for (ApplyCallback callback : callbacks) {
                callback.isArmed = true;
            }
        } else {
            if (!errorMessages.isEmpty()) {
                showErrorWhileApplyingQueryDialog(errorMessages, retrieverChainName);
            }
            setBlockChangeNotification(false);
            queryDefinitionChanged = false;
        }
    }
    
    private void showErrorWhileApplyingQueryDialog(Iterable<String> errorMessages, String retrieverChainName) {
        StringMessages stringMessages = getDataMiningStringMessages();
        
        DialogBox dialog = new DialogBox(false, true);
        dialog.setText(stringMessages.anErrorOccurredWhileApplyingTheQuery());
        dialog.setAnimationEnabled(true);
        dialog.setGlassEnabled(true);
        
        VerticalPanel contentPanel = new VerticalPanel();
        contentPanel.add(new HTML(SafeHtmlUtils.fromString(stringMessages.queryBasedOnRetrieverChainCanNotBeApplied(retrieverChainName))));
        
        HtmlUListBuilder messagesBuilder = HtmlBuilderFactory.get().createUListBuilder();
        for (String errorMessage : errorMessages) {
            messagesBuilder.startLI().text(errorMessage).end();
        }
        contentPanel.add(new HTML(messagesBuilder.asSafeHtml()));

        FlowPanel buttonPanel = new FlowPanel();
        buttonPanel.addStyleName("floatRight");
        contentPanel.add(buttonPanel);
        
        Button okButton = new Button(stringMessages.ok());
        okButton.addClickHandler(e -> dialog.hide());
        buttonPanel.add(okButton);

        dialog.setWidget(contentPanel);
        dialog.center();
    }
    
    @Override
    public void queryDefinitionChangesHaveBeenStored() {
        queryDefinitionChanged = false;
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
                boolean queryDefinitionViewerActive = queryDefinitionViewerToggleButton.isDown();
                filterSplitPanel.setWidgetHidden(queryDefinitionViewer.getEntryWidget(), !queryDefinitionViewerActive);
                queryDefinitionViewer.setActive(queryDefinitionViewerActive);
                addControl(predefinedQueryRunner.getEntryWidget());
            } else {
                removeControl(queryDefinitionViewerToggleButton);
                filterSplitPanel.setWidgetHidden(queryDefinitionViewer.getEntryWidget(), true);
                queryDefinitionViewer.setActive(false);
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

    private class ApplyCallback implements Consumer<Iterable<String>> {
        
        private final Collection<String> allMessages;
        private final Set<ApplyCallback> callbacks;
        private final String retrieverChainName;
        private boolean isArmed;

        public ApplyCallback(Collection<String> allMessages, Set<ApplyCallback> callbacks, String retrieverChainName) {
            this.allMessages = allMessages;
            this.callbacks = callbacks;
            this.retrieverChainName = retrieverChainName;
        }

        @Override
        public void accept(Iterable<String> messages) {
            Util.addAll(messages, allMessages);
            callbacks.remove(this);
            if (isArmed && callbacks.isEmpty()) {
                if (!allMessages.isEmpty()) {
                    showErrorWhileApplyingQueryDialog(allMessages, retrieverChainName);
                }
                setBlockChangeNotification(false);
                queryDefinitionChanged = false;
            }
        }
        
    }

    private class ProviderListener implements StatisticChangedListener, FilterSelectionChangedListener, GroupingChangedListener {

        @Override
        public void dataRetrieverChainDefinitionChanged(DataRetrieverChainDefinitionDTO newDataRetrieverChainDefinition) {
            if (providersAwaitingReload()) {
                groupingProvider.dataRetrieverChainDefinitionChanged(newDataRetrieverChainDefinition);
                groupingProvider.reloadComponents();

                filterSelectionProvider.dataRetrieverChainDefinitionChanged(newDataRetrieverChainDefinition);
                filterSelectionProvider.reloadComponents();
            } else {
                queryDefinitionChanged();
            }
        }

        @Override
        public void aggregatorDefinitionChanged(AggregationProcessorDefinitionDTO newAggregatorDefinition) {
            queryDefinitionChanged();
        }

        @Override
        public void extractionFunctionChanged(FunctionDTO extractionFunction) {
            queryDefinitionChanged();
        }

        @Override
        public void groupingChanged() {
            queryDefinitionChanged();
        }

        @Override
        public void selectionChanged() {
            queryDefinitionChanged();
        }

        private void queryDefinitionChanged() {
            if (!providersAwaitingReload()) {
                queryDefinitionChanged = true;
                notifyQueryDefinitionChanged();
            }
        }

    }

}
