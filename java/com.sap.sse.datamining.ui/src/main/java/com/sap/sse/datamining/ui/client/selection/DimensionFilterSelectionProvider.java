package com.sap.sse.datamining.ui.client.selection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.DefaultSelectionEventManager.EventTranslator;
import com.google.gwt.view.client.DefaultSelectionEventManager.SelectAction;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.datamining.ui.client.AbstractDataMiningComponent;
import com.sap.sse.datamining.ui.client.DataMiningServiceAsync;
import com.sap.sse.datamining.ui.client.DataRetrieverChainDefinitionProvider;
import com.sap.sse.datamining.ui.client.FilterSelectionChangedListener;
import com.sap.sse.datamining.ui.client.FilterSelectionProvider;
import com.sap.sse.datamining.ui.client.ManagedDataMiningQueriesCounter;
import com.sap.sse.datamining.ui.client.execution.ManagedDataMiningQueryCallback;
import com.sap.sse.datamining.ui.client.execution.SimpleManagedDataMiningQueriesCounter;
import com.sap.sse.datamining.ui.client.resources.DataMiningDataGridResources;
import com.sap.sse.datamining.ui.client.resources.DataMiningResources;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.controls.busyindicator.SimpleBusyIndicator;
import com.sap.sse.gwt.client.panels.AbstractFilterablePanel;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class DimensionFilterSelectionProvider extends AbstractDataMiningComponent<SerializableSettings> {

    private static final DataMiningResources resources = GWT.create(DataMiningResources.class);
    private static final NaturalComparator NaturalComparator = new NaturalComparator();
    
    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    private final DataMiningSession session;
    private final ManagedDataMiningQueriesCounter counter;
    private final Set<FilterSelectionChangedListener> listeners;

    private final DataRetrieverChainDefinitionProvider retrieverChainProvider;
    private final FilterSelectionProvider filterSelectionProvider;
    
    private final DataRetrieverLevelDTO retrieverLevel;
    private final FunctionDTO dimension;
    
    private final DockLayoutPanel mainPanel;
    private final AbstractFilterablePanel<Serializable> filterPanel;
    private final LayoutPanel contentContainer;
    private final SimpleBusyIndicator busyIndicator;

    private final Set<Serializable> availableData;
    private final ListDataProvider<Serializable> filteredData;
    private final MultiSelectionModel<Serializable> selectionModel;
    private final DataGrid<Serializable> dataGrid;
    private final Column<Serializable, Boolean> checkboxColumn;
    
    private Iterable<? extends Serializable> selectionToBeApplied;
    private Consumer<Iterable<String>> selectionCallback;

    public DimensionFilterSelectionProvider(Component<?> parent, ComponentContext<?> componentContext, DataMiningServiceAsync dataMiningService,
            ErrorReporter errorReporter, DataMiningSession session, DataRetrieverChainDefinitionProvider retrieverChainProvider,
            FilterSelectionProvider filterSelectionProvider, DataRetrieverLevelDTO retrieverLevel, FunctionDTO dimension) {
        super(parent, componentContext);
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        this.session = session;
        this.retrieverChainProvider = retrieverChainProvider;
        this.filterSelectionProvider = filterSelectionProvider;
        this.retrieverLevel = retrieverLevel;
        this.dimension = dimension;

        counter = new SimpleManagedDataMiningQueriesCounter();
        listeners = new HashSet<>();

        DataMiningDataGridResources dataGridResources = GWT.create(DataMiningDataGridResources.class);
        dataGrid = new DataGrid<>(Integer.MAX_VALUE, dataGridResources);
        dataGrid.setAutoHeaderRefreshDisabled(true);
        dataGrid.setAutoFooterRefreshDisabled(true);
        dataGrid.addStyleName("dataMiningBorderTop");
        
        availableData = new HashSet<>();
        filteredData = new ListDataProvider<Serializable>(this::elementAsString);
        filterPanel = new AbstractFilterablePanel<Serializable>(null, filteredData) {
            @Override
            public Iterable<String> getSearchableStrings(Serializable element) {
                return Collections.singleton(elementAsString(element));
            }

            @Override
            public AbstractCellTable<Serializable> getCellTable() {
                return dataGrid;
            }
        };
        filterPanel.setWidth("100%");
        filterPanel.setSpacing(1);
        filterPanel.getTextBox().setWidth("100%");
        filterPanel.getAllListDataProvider().addDataDisplay(dataGrid);
        
        selectionModel = new MultiSelectionModel<>(this::elementAsString);
        selectionModel.addSelectionChangeHandler(this::selectionChanged);
        dataGrid.setSelectionModel(selectionModel, DefaultSelectionEventManager.createCustomManager(new CustomCheckboxEventTranslator()));
        
        checkboxColumn = new Column<Serializable, Boolean>(new CheckboxCell(true, false)) {
            @Override
            public Boolean getValue(Serializable object) {
                return selectionModel.isSelected(object);
            }
        };
        dataGrid.addColumn(checkboxColumn);
        TextColumn<Serializable> contentColumn = new TextColumn<Serializable>() {
            @Override
            public String getValue(Serializable element) {
                return elementAsString(element);
            }
        };
        contentColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        dataGrid.addColumn(contentColumn);
        
        busyIndicator = new SimpleBusyIndicator(false, 0.85f);
        busyIndicator.getElement().getStyle().setTextAlign(TextAlign.CENTER);
        
        contentContainer = new LayoutPanel();
        contentContainer.add(busyIndicator);
        contentContainer.setWidgetTopBottom(busyIndicator, 10, Unit.PX, 10, Unit.PX);
        contentContainer.setWidgetLeftRight(busyIndicator, 10, Unit.PX, 10, Unit.PX);
        
        mainPanel = new DockLayoutPanel(Unit.PX);
        mainPanel.addNorth(createHeaderPanel(), 40);
        mainPanel.addNorth(filterPanel, 35);
        mainPanel.setWidgetHidden(filterPanel, true);
        mainPanel.add(contentContainer);
        
        updateContent(null);
    }

    private Widget createHeaderPanel() {
        HorizontalPanel headerPanel = new HorizontalPanel();
        headerPanel.setSpacing(2);
        headerPanel.setWidth("100%");
        headerPanel.setHeight("100%");
        headerPanel.addStyleName("dimensionFilterSelectionHeader");
        headerPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        
        Label headerLabel = new Label(dimension.getDisplayName());
        headerLabel.addStyleName("emphasizedLabel");
        headerPanel.add(headerLabel);
        headerPanel.setCellWidth(headerLabel, "100%");
        headerPanel.setCellHorizontalAlignment(headerLabel, HasHorizontalAlignment.ALIGN_CENTER);

        ToggleButton toggleFilterButton = new ToggleButton(new Image(resources.searchIcon()));
        toggleFilterButton.addClickHandler(e -> {
            boolean enabled = toggleFilterButton.isDown();
            mainPanel.setWidgetHidden(filterPanel, !enabled);
            if (enabled) {
                Scheduler.get().scheduleDeferred(() -> {
                    filterPanel.getTextBox().setFocus(true);
                    filterPanel.getTextBox().selectAll();
                });
            }

            ListDataProvider<Serializable> oldProvider = enabled ? filterPanel.getAllListDataProvider(): filteredData;
            ListDataProvider<Serializable> newProvider = enabled ? filteredData : filterPanel.getAllListDataProvider();
            oldProvider.removeDataDisplay(dataGrid);
            newProvider.addDataDisplay(dataGrid);
        });
        headerPanel.add(toggleFilterButton);
        headerPanel.setCellHorizontalAlignment(toggleFilterButton, HasHorizontalAlignment.ALIGN_RIGHT);
        
        return headerPanel;
    }

    public void updateContent(Runnable callback) {
        HashMap<DataRetrieverLevelDTO,SerializableSettings> retrieverSettings = retrieverChainProvider.getRetrieverSettings();
        HashMap<DataRetrieverLevelDTO, HashMap<FunctionDTO, HashSet<? extends Serializable>>> filterSelection = filterSelectionProvider.getSelection();
        if (filterSelection.containsKey(retrieverLevel)) {
            filterSelection.get(retrieverLevel).remove(dimension);
        }
        HashSet<FunctionDTO> dimensions = new HashSet<>();
        dimensions.add(dimension);
        
        availableData.clear();
        counter.increase();
        contentContainer.remove(dataGrid);
        busyIndicator.setBusy(true);
        dataMiningService.getDimensionValuesFor(session, retrieverChainProvider.getDataRetrieverChainDefinition(), retrieverLevel, dimensions,
                retrieverSettings, filterSelection, LocaleInfo.getCurrentLocale().getLocaleName(), new ManagedDataMiningQueryCallback<HashSet<Object>>(counter) {
                    @SuppressWarnings("unchecked")
                    @Override
                    protected void handleSuccess(QueryResultDTO<HashSet<Object>> result) {
                        Map<GroupKey, HashSet<Object>> results = result.getResults();
                        List<Serializable> sortedData = new ArrayList<>();
                        
                        if (!results.isEmpty()) {
                            GroupKey contentKey = new GenericGroupKey<FunctionDTO>(dimension);
                            availableData.addAll((Collection<? extends Serializable>) results.get(contentKey));
                            sortedData.addAll(availableData);
                            sortedData.sort((o1, o2) -> NaturalComparator.compare(o1.toString(), o2.toString()));
                        }
                        
                        busyIndicator.setBusy(false);
                        filterPanel.updateAll(sortedData);
                        contentContainer.add(dataGrid);
                        
                        internalSetSelection(selectionToBeApplied != null ? selectionToBeApplied : selectionModel.getSelectedSet(),
                                             selectionCallback != null ? selectionCallback : m -> { });
                        selectionToBeApplied = null;
                        selectionCallback = null;
                        
                        if (callback != null) {
                            callback.run();
                        }
                    }
                    @Override
                    protected void handleFailure(Throwable caught) {
                        errorReporter.reportError("Error fetching the dimension values of " + dimension + ": " + caught.getMessage());
                        selectionToBeApplied = null;
                        selectionCallback = null;
                    }
                });
    }
    
    private String elementAsString(Object element) {
        return element.toString();
    }
    
    private void selectionChanged(SelectionChangeEvent event) {
        notifyListeners();
    }

    public HashSet<? extends Serializable> getSelection() {
        return new HashSet<>(selectionModel.getSelectedSet());
    }
    
    public void setSelection(Iterable<? extends Serializable> selection, Consumer<Iterable<String>> callback) {
        selectionToBeApplied = selection;
        selectionCallback = callback;
        
        if (!busyIndicator.isBusy()) {
            internalSetSelection(selectionToBeApplied, selectionCallback);
            selectionToBeApplied = null;;
            selectionCallback = null;
        }
    }

    private void internalSetSelection(Iterable<? extends Serializable> selection, Consumer<Iterable<String>> callback) {
        clearSelection();
        Collection<Serializable> missingValues = new ArrayList<>();
        for (Serializable value : selection) {
            if (availableData.contains(value)) {
                selectionModel.setSelected(value, true);
            } else {
                missingValues.add(value);
            }
        }
        
        if (!missingValues.isEmpty()) {
            String listedValues = missingValues.stream().map(this::elementAsString).collect(Collectors.joining(", "));
            callback.accept(Collections.singleton(getDataMiningStringMessages()
                    .filterValuesOfDimensionAreNotAvailable(dimension.getDisplayName(), listedValues)));
        } else {
            callback.accept(Collections.emptySet());
        }
    }

    public void clearSelection() {
        selectionModel.clear();
    }

    public void addListener(FilterSelectionChangedListener listener) {
        listeners.add(listener);
    }
    
    private void notifyListeners() {
        for (FilterSelectionChangedListener listener : listeners) {
            listener.selectionChanged();
        }
    }

    @Override
    public Widget getEntryWidget() {
        return mainPanel;
    }

    @Override
    public String getId() {
        return "DimensionFilterSelectionProvider";
    }

    @Override
    public String getLocalizedShortName() {
        return getClass().getSimpleName();
    }

    @Override
    public boolean isVisible() {
        return getEntryWidget().isVisible();
    }

    @Override
    public void setVisible(boolean visibility) {
        getEntryWidget().setVisible(visibility);
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
    public SerializableSettings getSettings() {
        return null;
    }

    @Override
    public void updateSettings(SerializableSettings newSettings) {
        // no-op
    }

    @Override
    public String getDependentCssClassName() {
        return "dimensionFilterSelectionProvider";
    }
    
    private class CustomCheckboxEventTranslator implements EventTranslator<Serializable> {
        
        @Override
        public boolean clearCurrentSelection(CellPreviewEvent<Serializable> event) {
            return !isCheckboxColumn(event.getColumn());
        }

        @Override
        public SelectAction translateSelectionEvent(CellPreviewEvent<Serializable> event) {
            NativeEvent nativeEvent = event.getNativeEvent();
            if (BrowserEvents.CLICK.equals(nativeEvent.getType())) {
                if (nativeEvent.getCtrlKey()) {
                    Serializable value = event.getValue();
                    selectionModel.setSelected(value, !selectionModel.isSelected(value));
                    return SelectAction.IGNORE;
                }
                if (!selectionModel.getSelectedSet().isEmpty() && !isCheckboxColumn(event.getColumn())) {
                    return SelectAction.DEFAULT;
                }
            }
            return SelectAction.TOGGLE;
        }

        private boolean isCheckboxColumn(int columnIndex) {
            return columnIndex == dataGrid.getColumnIndex(checkboxColumn);
        }
        
    }

}
