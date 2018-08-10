package com.sap.sse.datamining.ui.client.selection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.builder.shared.TableCellBuilder;
import com.google.gwt.dom.builder.shared.TableRowBuilder;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.RowHoverEvent;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.DefaultSelectionEventManager.EventTranslator;
import com.google.gwt.view.client.DefaultSelectionEventManager.SelectAction;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.sap.sse.common.Util;
import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.ReducedDimensionsDTO;
import com.sap.sse.datamining.ui.client.AbstractDataMiningComponent;
import com.sap.sse.datamining.ui.client.DataMiningServiceAsync;
import com.sap.sse.datamining.ui.client.DataRetrieverChainDefinitionProvider;
import com.sap.sse.datamining.ui.client.FilterSelectionChangedListener;
import com.sap.sse.datamining.ui.client.FilterSelectionPresenter;
import com.sap.sse.datamining.ui.client.FilterSelectionProvider;
import com.sap.sse.datamining.ui.client.StringMessages;
import com.sap.sse.datamining.ui.client.presentation.PlainFilterSelectionPresenter;
import com.sap.sse.datamining.ui.client.resources.DataMiningDataGridResources;
import com.sap.sse.datamining.ui.client.resources.DataMiningDataGridResources.DataMiningDataGridStyle;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.BaseCellTableBuilder;
import com.sap.sse.gwt.client.panels.AbstractFilterablePanel;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class HierarchicalDimensionListFilterSelectionProvider extends AbstractDataMiningComponent<SerializableSettings> implements FilterSelectionProvider {
    
    private static final Logger logger = Logger.getLogger(HierarchicalDimensionListFilterSelectionProvider.class.getSimpleName());
    
    private static final String DimensionListSubheaderAttribute = "subheader";
    private static final Unit LayoutUnit = Unit.PX;
    private static final double SelectionPresenterHeight = 100;
    private static final double DimensionSelectionWidth = 350;
    private static final double DimensionSelectionHeaderHeight = 30;
    private static final double FilterFilterDimensionsHeight = 40;
    private static final double FilterSelectionTableWidth = 250;
    
    private static final Consumer<Iterable<String>> EmptyCallback = m -> { };

    private final DataMiningSession session;
    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    private final DataRetrieverChainDefinitionProvider retrieverChainProvider;
    private final Set<FilterSelectionChangedListener> listeners;

    private boolean isAwaitingReload;
    private boolean isUpdatingFilterDimensions;
    private boolean ignoreSelectionChangedNotifications;
    private DataRetrieverChainDefinitionDTO retrieverChain;
    private ReducedDimensionsDTO reducedDimensions;
    private final List<DimensionWithContext> availableFilterDimensions;
    private final ListDataProvider<DimensionWithContext> filteredFilterDimensions;
    
    private final DockLayoutPanel mainPanel;
    private final Button clearSelectionButton;
    
    private final AbstractFilterablePanel<DimensionWithContext> filterFilterDimensionsPanel;
    private final MultiSelectionModel<DimensionWithContext> filterDimensionSelectionModel;
    private final DataGrid<DimensionWithContext> filterDimensionsList;
    private final Column<DimensionWithContext, Boolean> checkboxColumn;
    
    private final DockLayoutPanel dimensionFilterSelectionProvidersPanel;
    private final Map<DimensionWithContext, DimensionFilterSelectionProvider> dimensionFilterSelectionProviders;
    private boolean isUpdatingAvailableFilterValues;
    
    private final FilterSelectionPresenter filterSelectionPresenter;
    private final ScrollPanel filterSelectionPresenterContainer;
    
    private HashMap<DataRetrieverLevelDTO, HashMap<FunctionDTO, HashSet<? extends Serializable>>> selectionToBeApplied;
    private Consumer<Iterable<String>> selectionCallback;
    
    public HierarchicalDimensionListFilterSelectionProvider(Component<?> parent, ComponentContext<?> context,
            DataMiningSession session, DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter,
            DataRetrieverChainDefinitionProvider retrieverChainProvider) {
        super(parent, context);
        this.session = session;
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        this.retrieverChainProvider = retrieverChainProvider;
        retrieverChainProvider.addDataRetrieverChainDefinitionChangedListener(this);
        
        listeners = new HashSet<>();
        isAwaitingReload = false;
        retrieverChain = null;
        availableFilterDimensions = new ArrayList<>();
        filteredFilterDimensions = new ListDataProvider<>();
        
        StringMessages stringMessages = getDataMiningStringMessages();

        Label filterDimensionsSelectionTitleLabel = new Label(stringMessages.selectDimensionsToFilterBy());
        filterDimensionsSelectionTitleLabel.addStyleName("emphasizedLabel");
        filterDimensionsSelectionTitleLabel.addStyleName("dataMiningMarginLeft");
        filterDimensionsSelectionTitleLabel.addStyleName("filterDimensionsTitleLabel");

        clearSelectionButton = new Button(stringMessages.clear());
        clearSelectionButton.addStyleName("floatRight");
        clearSelectionButton.addStyleName("dataMiningMarginRight");
        clearSelectionButton.addClickHandler(e -> clearSelection());

        DataMiningDataGridResources resources = GWT.create(DataMiningDataGridResources.class);
        filterDimensionsList = new DataGrid<>(Integer.MAX_VALUE, resources);
        filterDimensionsList.setAutoHeaderRefreshDisabled(true);
        filterDimensionsList.setAutoFooterRefreshDisabled(true);
        filterDimensionsList.setTableBuilder(new FilterDimensionsListBuilder(filterDimensionsList, resources.dataGridStyle()));
        filteredFilterDimensions.addDataDisplay(filterDimensionsList);
        
        filterFilterDimensionsPanel = new AbstractFilterablePanel<DimensionWithContext>(
            null, filterDimensionsList, filteredFilterDimensions)
        {
            @Override
            public Iterable<String> getSearchableStrings(DimensionWithContext dimension) {
                return dimension.getMatchingStrings();
            }
        };
        filterFilterDimensionsPanel.addStyleName("filterFilterDimensionsPanel");
        filterFilterDimensionsPanel.setSpacing(2);
        filterFilterDimensionsPanel.setWidth("100%");
        filterFilterDimensionsPanel.setHeight("100%");
        filterFilterDimensionsPanel.getTextBox().setWidth("100%");
        filterFilterDimensionsPanel.getTextBox().getElement().setPropertyString("placeholder", stringMessages.filterShownDimensions());
        
        filterDimensionSelectionModel = new MultiSelectionModel<>();
        filterDimensionSelectionModel.addSelectionChangeHandler(this::selectedFilterDimensionsChanged);
        filterDimensionsList.setSelectionModel(filterDimensionSelectionModel, DefaultSelectionEventManager.createCustomManager(new CustomCheckboxEventTranslator()));
        
        checkboxColumn = new Column<DimensionWithContext, Boolean>(new CheckboxCell(true, false)) {
            @Override
            public Boolean getValue(DimensionWithContext object) {
                return filterDimensionSelectionModel.isSelected(object);
            }
        };
        filterDimensionsList.addColumn(checkboxColumn);
        TextColumn<DimensionWithContext> dimensionColumn = new TextColumn<DimensionWithContext>() {
            @Override
            public String getValue(DimensionWithContext object) {
                return object.getDimension().getDisplayName();
            }
        };
        filterDimensionsList.addColumn(dimensionColumn);
        
        FlowPanel headerPanel = new FlowPanel();
        headerPanel.addStyleName("dataMiningMarginTop");
        headerPanel.add(filterDimensionsSelectionTitleLabel);
        headerPanel.add(clearSelectionButton);
        
        DockLayoutPanel filterDimensionsSelectionPanel = new DockLayoutPanel(LayoutUnit);
        filterDimensionsSelectionPanel.addNorth(headerPanel, DimensionSelectionHeaderHeight);
        filterDimensionsSelectionPanel.addNorth(filterFilterDimensionsPanel, FilterFilterDimensionsHeight);
        filterDimensionsSelectionPanel.add(filterDimensionsList);
        
        dimensionFilterSelectionProviders = new HashMap<>();
        dimensionFilterSelectionProvidersPanel = new DockLayoutPanel(LayoutUnit);
        dimensionFilterSelectionProvidersPanel.addStyleName("dimensionFilterSelectionTablesContainer");
        dimensionFilterSelectionProvidersPanel.addStyleName("dataMiningBorderLeft");
        
        filterSelectionPresenter = new PlainFilterSelectionPresenter(this, context, stringMessages, this);
        filterSelectionPresenter.getEntryWidget().addStyleName("dataMiningMarginLeft");
        filterSelectionPresenterContainer = new ScrollPanel(filterSelectionPresenter.getEntryWidget());
        filterSelectionPresenterContainer.addStyleName("dataMiningBorderTop");
        
        mainPanel = new DockLayoutPanel(LayoutUnit);
        mainPanel.addSouth(filterSelectionPresenterContainer, SelectionPresenterHeight);
        mainPanel.setWidgetHidden(filterSelectionPresenterContainer, true);
        mainPanel.addWest(filterDimensionsSelectionPanel, DimensionSelectionWidth);
        mainPanel.add(dimensionFilterSelectionProvidersPanel);
        updateControls();
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
        updateFilterDimensions();
        isAwaitingReload = false;
        notifyListeners();
    }
    
    @Override
    public void dataRetrieverChainDefinitionChanged(DataRetrieverChainDefinitionDTO newDataRetrieverChainDefinition) {
        if (!Objects.equals(retrieverChain, newDataRetrieverChainDefinition)) {
            retrieverChain = newDataRetrieverChainDefinition;
            if (!isAwaitingReload && retrieverChain != null) {
                HashMap<DataRetrieverLevelDTO, HashMap<FunctionDTO, HashSet<? extends Serializable>>> currentSelection = getSelection();
                if (!currentSelection.isEmpty()) {
                    selectionToBeApplied = currentSelection;
                    selectionCallback = EmptyCallback;
                }
                updateFilterDimensions();
            } else if (!isAwaitingReload) {
                clearContent();
                updateControls();
                selectionToBeApplied = null;
                selectionCallback = null;
            }
        }
    }
    
    private void updateFilterDimensions() {
        isUpdatingFilterDimensions = true;
        clearContent();
        dataMiningService.getReducedDimensionsMappedByLevelFor(retrieverChain, LocaleInfo.getCurrentLocale().getLocaleName(), new AsyncCallback<ReducedDimensionsDTO>() {
            @Override
            public void onSuccess(ReducedDimensionsDTO result) {
                reducedDimensions = result;
                for (Entry<DataRetrieverLevelDTO, HashSet<FunctionDTO>> entry : reducedDimensions.getReducedDimensions().entrySet()) {
                    DataRetrieverLevelDTO retrieverlevel = entry.getKey();
                    for (FunctionDTO dimension : entry.getValue()) {
                        availableFilterDimensions.add(new DimensionWithContext(dimension, retrieverlevel));
                    }
                }
                availableFilterDimensions.sort(null);
                filterFilterDimensionsPanel.updateAll(availableFilterDimensions);

                updateControls();
                if (selectionToBeApplied != null) {
                    ignoreSelectionChangedNotifications = true;
                    setSelection(selectionToBeApplied, selectionCallback);
                    selectionToBeApplied = null;
                    selectionCallback = null;
                }
                isUpdatingFilterDimensions = false;
            }
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error fetching the dimensions of the retriever chain from the server: " + caught.getMessage());
                updateControls();
                selectionToBeApplied = null;
                selectionCallback = null;
                isUpdatingFilterDimensions = false;
            }
        });
    }

    private void clearContent() {
        reducedDimensions = null;
        availableFilterDimensions.clear();
        filterDimensionSelectionModel.clear();
        filterFilterDimensionsPanel.removeAll();
    }

    private void updateControls() {
        boolean controlsEnabled = !availableFilterDimensions.isEmpty();
        clearSelectionButton.setEnabled(controlsEnabled);
        filterFilterDimensionsPanel.getTextBox().setEnabled(controlsEnabled);
    }
    
    @Override
    public void applyQueryDefinition(StatisticQueryDefinitionDTO queryDefinition, Consumer<Iterable<String>> callback) {
        DataRetrieverChainDefinitionDTO retrieverChain = queryDefinition.getDataRetrieverChainDefinition();
        selectionToBeApplied = queryDefinition.getFilterSelection();
        selectionCallback = callback;
        if (!isUpdatingFilterDimensions && !isAwaitingReload && retrieverChain.equals(this.retrieverChain)) {
            ignoreSelectionChangedNotifications = true;
            filterDimensionSelectionModel.clear();
            setSelection(selectionToBeApplied, selectionCallback);
            selectionToBeApplied = null;
            selectionCallback = null;
        }
    }

    private void setSelection(HashMap<DataRetrieverLevelDTO, HashMap<FunctionDTO, HashSet<? extends Serializable>>> filterSelection, Consumer<Iterable<String>> callback) {
        Set<InnerSelectionCallback> innerCallbacks = new HashSet<>();
        Collection<String> callbackMessages = new ArrayList<>();
        Collection<DimensionWithContext> missingDimensions = new ArrayList<>();
        for (DataRetrieverLevelDTO retrieverLevel : filterSelection.keySet()) {
            HashMap<FunctionDTO, HashSet<? extends Serializable>> levelSelection = filterSelection.get(retrieverLevel);
            for (FunctionDTO dimension : levelSelection.keySet()) {
                DimensionWithContext dimensionWithContext = new DimensionWithContext(dimension, retrieverLevel);
                int index = availableFilterDimensions.indexOf(dimensionWithContext);
                if (index != -1) {
                    InnerSelectionCallback innerCallback = new InnerSelectionCallback(callbackMessages, innerCallbacks, callback);
                    innerCallbacks.add(innerCallback);
                    setDimensionSelection(availableFilterDimensions.get(index), levelSelection.get(dimension), innerCallback);
                } else {
                    missingDimensions.add(dimensionWithContext);
                }
            }
        }
        
        if (!missingDimensions.isEmpty()) {
            String listedDimensions = missingDimensions.stream().map(d -> d.getDimension().getDisplayName())
                                                                .collect(Collectors.joining(", "));
            callbackMessages.add(getDataMiningStringMessages().filterDimensionsAreNotAvailable(listedDimensions));
        }

        if (!innerCallbacks.isEmpty()) {
            for (InnerSelectionCallback innerCallback : innerCallbacks) {
                innerCallback.canPublishMessages = true;
            }
        } else {
            setSelectionCompleted(callback, callbackMessages);
        }
    }
    
    private void setSelectionCompleted(Consumer<Iterable<String>> callback, Iterable<String> messages) {
        HashMap<DataRetrieverLevelDTO, HashMap<FunctionDTO, HashSet<? extends Serializable>>> selection = getSelection();
        boolean updateAvailableFilterValues = selection.size() >= 2;
        if (!updateAvailableFilterValues) {
            for (HashMap<FunctionDTO, HashSet<? extends Serializable>> levelSelection : selection.values()) {
                if (levelSelection.size() >= 2) {
                    updateAvailableFilterValues = true;
                    break;
                }
            }
        }

        ignoreSelectionChangedNotifications = false;
        Runnable finalizeSelection = () -> {
            mainPanel.setWidgetHidden(filterSelectionPresenterContainer, selection.isEmpty());
            filterSelectionPresenter.selectionChanged();
            callback.accept(messages);
        };
        if (updateAvailableFilterValues) {
            isUpdatingAvailableFilterValues = false;
            updateAvailableFilterValues(retrieverChain.getRetrieverLevel(0), null, finalizeSelection);
        } else {
            finalizeSelection.run();
        }
    }
    
    private class InnerSelectionCallback implements Consumer<Iterable<String>> {
        
        private final Collection<String> allMessages;
        private final Set<InnerSelectionCallback> innerCallbacks;
        private final Consumer<Iterable<String>> outerCallback;
        private boolean canPublishMessages;

        public InnerSelectionCallback(Collection<String> allMessages, Set<InnerSelectionCallback> innerCallbacks,
                Consumer<Iterable<String>> outerCallback) {
            this.allMessages = allMessages;
            this.innerCallbacks = innerCallbacks;
            this.outerCallback = outerCallback;
        }

        @Override
        public void accept(Iterable<String> messages) {
            Util.addAll(messages, allMessages);
            innerCallbacks.remove(this);
            if (canPublishMessages && innerCallbacks.isEmpty()) {
                setSelectionCompleted(outerCallback, allMessages);
            }
        }
        
    }

    @Override
    public void setFilterSelectionForUnreducedDimension(FunctionDTO unreducedDimension, Serializable filterValue, Runnable onCompletion) {
        FunctionDTO reducedDimension = reducedDimensions == null ? unreducedDimension : reducedDimensions.getReducedDimension(unreducedDimension);
        DimensionWithContext dimensionToChange = null;
        for (DimensionWithContext dimensionWithContext : availableFilterDimensions) {
            if (dimensionWithContext.getDimension().equals(reducedDimension)) {
                dimensionToChange = dimensionWithContext;
                break;
            }
        }

        if (dimensionToChange != null) {
            ignoreSelectionChangedNotifications = true;
            setDimensionSelection(dimensionToChange, Collections.singleton(filterValue), EmptyCallback);
            ignoreSelectionChangedNotifications = false;
            updateAvailableFilterValues(dimensionToChange.getRetrieverLevel(), dimensionToChange, onCompletion);
        }
    }

    private void setDimensionSelection(DimensionWithContext dimension, Collection<? extends Serializable> items, Consumer<Iterable<String>> callback) {
        DimensionFilterSelectionProvider selectionProvider;
        if (filterDimensionSelectionModel.isSelected(dimension)) {
            selectionProvider = dimensionFilterSelectionProviders.get(dimension);
        } else {
            selectionProvider = addDimensionFilterSelectionProvider(dimension);
            filterDimensionSelectionModel.setSelected(dimension, true);
        }
        selectionProvider.setSelection(items, callback);
    }
    
    private void selectedFilterDimensionsChanged(SelectionChangeEvent event) {
        Iterable<DimensionWithContext> displayedDimensions = new HashSet<>(dimensionFilterSelectionProviders.keySet());
        for (DimensionWithContext displayedDimension : displayedDimensions) {
            if (!filterDimensionSelectionModel.isSelected(displayedDimension)) {
                removeDimensionFilterSelectionProvider(displayedDimension);
            }
        }
        
        for (DimensionWithContext selectedDimension : filterDimensionSelectionModel.getSelectedSet()) {
            if (!dimensionFilterSelectionProviders.containsKey(selectedDimension)) {
                addDimensionFilterSelectionProvider(selectedDimension);
            }
        }
    }
    
    private DimensionFilterSelectionProvider addDimensionFilterSelectionProvider(DimensionWithContext dimension) {
        DimensionFilterSelectionProvider selectionProvider = new DimensionFilterSelectionProvider(this, getComponentContext(),
                dataMiningService, errorReporter, session, retrieverChainProvider, this, dimension.getRetrieverLevel(), dimension.getDimension());
        selectionProvider.addListener(() -> filterSelectionChanged(dimension));
        dimensionFilterSelectionProviders.put(dimension, selectionProvider);

        DimensionWithContext nextDimension = null;
        for (DimensionWithContext displayedDimension : dimensionFilterSelectionProviders.keySet()) {
            if (displayedDimension.compareTo(dimension) > 0 &&
                (nextDimension == null || displayedDimension.compareTo(nextDimension) < 0)) {
                nextDimension = displayedDimension;
            }
        }
        Widget beforeWidget = nextDimension != null ? dimensionFilterSelectionProviders.get(nextDimension).getEntryWidget() : null;
        dimensionFilterSelectionProvidersPanel.insertWest(selectionProvider.getEntryWidget(), FilterSelectionTableWidth, beforeWidget);
        
        return selectionProvider;
    }

    private void removeDimensionFilterSelectionProvider(DimensionWithContext dimension) {
        DimensionFilterSelectionProvider selectionProvider = dimensionFilterSelectionProviders.get(dimension);
        if (selectionProvider != null) {
            selectionProvider.clearSelection();
            dimensionFilterSelectionProviders.remove(dimension);
            dimensionFilterSelectionProvidersPanel.remove(selectionProvider.getEntryWidget());
            dimensionFilterSelectionProvidersPanel.animate(0); // Schedule Layout
        }
    }
    
    private void filterSelectionChanged(DimensionWithContext changedDimension) {
        if (!isUpdatingAvailableFilterValues && !ignoreSelectionChangedNotifications) {
            updateAvailableFilterValues(changedDimension.getRetrieverLevel(), changedDimension, this::notifyListeners);
        }
    }
    
    private void updateAvailableFilterValues(DataRetrieverLevelDTO fromRetrieverLevel, DimensionWithContext exceptDimension, Runnable onCompletion) {
        if (isUpdatingAvailableFilterValues) {
            throw new IllegalStateException("Update of availabe filter values already in progress");
        }

        List<DimensionWithContext> dimensionsToUpdate = new ArrayList<>();
        for (DimensionWithContext dimension : dimensionFilterSelectionProviders.keySet()) {
            if (dimension.getRetrieverLevel().getLevel() >= fromRetrieverLevel.getLevel()) {
                dimensionsToUpdate.add(dimension);
            }
        }
        dimensionsToUpdate.sort(null);

        isUpdatingAvailableFilterValues = true;
        updateDimensionFilterSelectionProviders(dimensionsToUpdate.iterator(), exceptDimension, onCompletion);
    }

    private void updateDimensionFilterSelectionProviders(Iterator<DimensionWithContext> dimensionIterator, DimensionWithContext exceptDimension, Runnable onCompletion) {
        if (dimensionIterator.hasNext()) {
            DimensionWithContext dimension = dimensionIterator.next();
            if (dimension.equals(exceptDimension)) {
                updateDimensionFilterSelectionProviders(dimensionIterator, exceptDimension, onCompletion);
            } else {
                DimensionFilterSelectionProvider selectionProvider = dimensionFilterSelectionProviders.get(dimension);
                HashSet<? extends Serializable> selectionBefore = selectionProvider.getSelection();
                selectionProvider.updateContent(() -> {
                    boolean selectionChanged = !selectionBefore.equals(selectionProvider.getSelection());
                    if (selectionChanged) {
                        isUpdatingAvailableFilterValues = false;
                        updateAvailableFilterValues(dimension.getRetrieverLevel(), dimension, onCompletion);
                    } else {
                        updateDimensionFilterSelectionProviders(dimensionIterator, exceptDimension, onCompletion);
                    }
                });
            }
        } else {
            isUpdatingAvailableFilterValues = false;
            onCompletion.run();
        }
    }

    @Override
    public HashMap<DataRetrieverLevelDTO, HashMap<FunctionDTO, HashSet<? extends Serializable>>> getSelection() {
        HashMap<DataRetrieverLevelDTO, HashMap<FunctionDTO, HashSet<? extends Serializable>>> filterSelection = new HashMap<>();
        for (DimensionWithContext dimensionWithContext : availableFilterDimensions) {
            DataRetrieverLevelDTO retrieverLevel = dimensionWithContext.getRetrieverLevel();
            FunctionDTO dimension = dimensionWithContext.getDimension();
            DimensionFilterSelectionProvider selectionProvider = dimensionFilterSelectionProviders.get(dimensionWithContext);
            if (selectionProvider != null) {
                HashSet<? extends Serializable> dimensionFilterSelection = selectionProvider.getSelection();
                if (!dimensionFilterSelection.isEmpty()) {
                    HashMap<FunctionDTO, HashSet<? extends Serializable>> retrieverFilterSelection = filterSelection.get(retrieverLevel);
                    if (retrieverFilterSelection == null) {
                        retrieverFilterSelection = new HashMap<>();
                        filterSelection.put(retrieverLevel, retrieverFilterSelection);
                    }
                    retrieverFilterSelection.put(dimension, dimensionFilterSelection);
                }
            }
        }
        return filterSelection;
    }
    
    @Override
    public void clearSelection() {
        for (DimensionFilterSelectionProvider selectionProvider : dimensionFilterSelectionProviders.values()) {
            selectionProvider.clearSelection();
        }
    }

    @Override
    public void addSelectionChangedListener(FilterSelectionChangedListener listener) {
        listeners.add(listener);
    }
    
    private void notifyListeners() {
        mainPanel.setWidgetHidden(filterSelectionPresenterContainer, getSelection().isEmpty());
        for (FilterSelectionChangedListener listener : listeners) {
            listener.selectionChanged();
        }
    }

    @Override
    public String getLocalizedShortName() {
        return getClass().getSimpleName();
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
        return "hierarchicalDimensionListFilterSelectionProvider";
    }

    @Override
    public boolean hasSettings() {
        return false;
    }

    @Override
    public SettingsDialogComponent<SerializableSettings> getSettingsDialogComponent(SerializableSettings settings) {
        return null;
    }

    @Override
    public void updateSettings(SerializableSettings newSettings) {
        // no-op
    }

    @Override
    public SerializableSettings getSettings() {
        return null;
    }

    @Override
    public String getId() {
        return "HierarchicalDimensionListFilterSelectionProvider";
    }
    
    private static class DimensionWithContext implements Comparable<DimensionWithContext> {

        private final FunctionDTO dimension;
        private final DataRetrieverLevelDTO retrieverLevel;
        private Collection<String> matchingStrings;
        
        public DimensionWithContext(FunctionDTO dimension, DataRetrieverLevelDTO retrieverLevel) {
            this.dimension = dimension;
            this.retrieverLevel = retrieverLevel;
        }

        public FunctionDTO getDimension() {
            return dimension;
        }

        public DataRetrieverLevelDTO getRetrieverLevel() {
            return retrieverLevel;
        }

        public Collection<String> getMatchingStrings() {
            if (matchingStrings == null) {
                matchingStrings = new ArrayList<>(2);
                matchingStrings.add(dimension.getDisplayName());
                matchingStrings.add(retrieverLevel.getRetrievedDataType().getDisplayName());
            }
            return matchingStrings;
        }
        
        @Override
        public int compareTo(DimensionWithContext o) {
            int retrieverLevelComparison = retrieverLevel.compareTo(o.retrieverLevel);
            if (retrieverLevelComparison != 0) return retrieverLevelComparison;
            
            return dimension.compareTo(o.dimension);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((dimension == null) ? 0 : dimension.hashCode());
            result = prime * result + ((retrieverLevel == null) ? 0 : retrieverLevel.hashCode());
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
            DimensionWithContext other = (DimensionWithContext) obj;
            if (dimension == null) {
                if (other.dimension != null)
                    return false;
            } else if (!dimension.equals(other.dimension))
                return false;
            if (retrieverLevel == null) {
                if (other.retrieverLevel != null)
                    return false;
            } else if (!retrieverLevel.equals(other.retrieverLevel))
                return false;
            return true;
        }
        
    }
    
    private class FilterDimensionsListBuilder extends BaseCellTableBuilder<DimensionWithContext> {
        
        private final String headerStyle;
        private final String subHeaderStyle;
        private final String spacedSubHeaderStyle;
        private final String subHeaderLabelStyle;
        private final String firstColumnStyle;
        private final String hoveredRowStyle;
        private final String hoveredRowCellStyle;
        private final String clearFocus;

        public FilterDimensionsListBuilder(AbstractCellTable<DimensionWithContext> cellTable, DataMiningDataGridStyle style) {
            super(cellTable);
            headerStyle = style.dataGridHeader();
            subHeaderStyle = style.dataGridSubHeader();
            firstColumnStyle = style.dataGridFirstColumn();
            spacedSubHeaderStyle = style.dataGridSpacedSubHeader();
            hoveredRowStyle = style.dataGridHoveredRow();
            hoveredRowCellStyle = style.dataGridHoveredRowCell();
            subHeaderLabelStyle = style.dataGridSubHeaderLabel();
            clearFocus = style.dataGridClearFocus();
            
            cellTable.addRowHoverHandler(new RowHoverEvent.Handler() {
                @Override
                public void onRowHover(RowHoverEvent event) {
                    TableRowElement tr = event.getHoveringRow();
                    if (tr.hasAttribute(DimensionListSubheaderAttribute)) {
                        tr.removeClassName(hoveredRowStyle);
                        NodeList<TableCellElement> cells = tr.getCells();
                        for (int i = 0; i < cells.getLength(); i++) {
                            cells.getItem(i).removeClassName(hoveredRowCellStyle);
                        }
                    }
                }
            });
        }
        
        @Override
        public void buildRowImpl(DimensionWithContext rowValue, int absRowIndex) {
            DataRetrieverLevelDTO valueLevel = rowValue.getRetrieverLevel();
            DataRetrieverLevelDTO previousLevel = null;
            if (absRowIndex > 0) {
                previousLevel = filteredFilterDimensions.getList().get(absRowIndex - 1).getRetrieverLevel();
            }
            
            if (!Objects.equals(previousLevel, valueLevel)) {
                StringBuilder styleBuilder = new StringBuilder();
                styleBuilder.append(subHeaderStyle).append(" ").append(headerStyle)
                            .append(" ").append(clearFocus);
                if (absRowIndex != 0) {
                    styleBuilder.append(" ").append(spacedSubHeaderStyle);
                }
                String style = styleBuilder.toString();

                String subHeaderText = valueLevel.getRetrievedDataType().getDisplayName();
                TableRowBuilder subHeaderBuilder = startRow().className(clearFocus).attribute(DimensionListSubheaderAttribute, subHeaderText);
                // Additional cell to fix the checkbox column size
                subHeaderBuilder.startTD().className(style + " " + firstColumnStyle).endTD();
                // Actual header cell
                TableCellBuilder headerCellBuilder = subHeaderBuilder.startTD();
                headerCellBuilder.colSpan(this.cellTable.getColumnCount() - 1).className(style);
                headerCellBuilder.startDiv().className(subHeaderLabelStyle).text(subHeaderText).endDiv();
                headerCellBuilder.endTD();
                subHeaderBuilder.endTR();
            }
            
            super.buildRowImpl(rowValue, absRowIndex);
        }
    }
    
    private class CustomCheckboxEventTranslator implements EventTranslator<DimensionWithContext> {
        
        @Override
        public boolean clearCurrentSelection(CellPreviewEvent<DimensionWithContext> event) {
            return false;
        }

        @Override
        public SelectAction translateSelectionEvent(CellPreviewEvent<DimensionWithContext> event) {
            SelectAction action = SelectAction.TOGGLE;
            
            NativeEvent nativeEvent = event.getNativeEvent();
            Element targetRow = Element.as(nativeEvent.getEventTarget());
            while (!TableRowElement.is(targetRow) && targetRow != null) {
                targetRow = targetRow.getParentElement();
            }
            if (targetRow != null && targetRow.hasAttribute(DimensionListSubheaderAttribute)) {
                action = SelectAction.IGNORE;
            }
            return action;
        }
        
    }

}
