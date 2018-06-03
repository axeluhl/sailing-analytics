package com.sap.sailing.gwt.ui.datamining.selection.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.LocaleInfo;
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
import com.sap.sailing.gwt.ui.datamining.DataMiningServiceAsync;
import com.sap.sailing.gwt.ui.datamining.DataRetrieverChainDefinitionProvider;
import com.sap.sailing.gwt.ui.datamining.FilterSelectionChangedListener;
import com.sap.sailing.gwt.ui.datamining.FilterSelectionProvider;
import com.sap.sailing.gwt.ui.datamining.ManagedDataMiningQueriesCounter;
import com.sap.sailing.gwt.ui.datamining.execution.ManagedDataMiningQueryCallback;
import com.sap.sailing.gwt.ui.datamining.execution.SimpleManagedDataMiningQueriesCounter;
import com.sap.sailing.gwt.ui.datamining.resources.DataMiningDataGridResources;
import com.sap.sailing.gwt.ui.datamining.resources.DataMiningResources;
import com.sap.sse.common.Util;
import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
import com.sap.sse.gwt.client.controls.busyindicator.SimpleBusyIndicator;
import com.sap.sse.gwt.client.panels.AbstractFilterablePanel;
import com.sap.sse.gwt.client.shared.components.AbstractComponent;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class DimensionFilterSelectionProvider extends AbstractComponent<SerializableSettings> {

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
    private Iterable<? extends Serializable> selectionToBeApplied;
    
    private final DockLayoutPanel mainPanel;
    private final AbstractFilterablePanel<Serializable> filterPanel;
    private final LayoutPanel contentContainer;
    private final SimpleBusyIndicator busyIndicator;

    private final ListDataProvider<Serializable> filteredData;
    private final RefreshableMultiSelectionModel<Serializable> selectionModel;
    private final DataGrid<Serializable> dataGrid;
    private final Column<Serializable, Boolean> checkboxColumn;

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
        dataGrid.addStyleName("dimensionFilterSelectionTable");
        
        filteredData = new ListDataProvider<Serializable>(this::elementAsString);
        filterPanel = new AbstractFilterablePanel<Serializable>(null, dataGrid, filteredData) {
            @Override
            public Iterable<String> getSearchableStrings(Serializable element) {
                return Collections.singleton(elementAsString(element));
            }
        };
        filterPanel.setWidth("100%");
        filterPanel.setSpacing(1);
        filterPanel.getTextBox().setWidth("100%");
        filterPanel.getAllListDataProvider().addDataDisplay(dataGrid);
        
        selectionModel = new RefreshableMultiSelectionModel<>(new EntityIdentityComparator<Serializable>() {
            @Override
            public boolean representSameEntity(Serializable e1, Serializable e2) {
                String e1String = e1 == null ? null : elementAsString(e1);
                String e2String = e2 == null ? null : elementAsString(e2);
                return Util.equalsWithNull(e1String, e2String);
            }
            @Override
            public int hashCode(Serializable e) {
                return e == null ? 0 : elementAsString(e).hashCode();
            }
        }, filterPanel.getAllListDataProvider());
        selectionModel.addSelectionChangeHandler(e -> notifyListeners());
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
        
        // TODO Layout of the header and filter panel
        mainPanel = new DockLayoutPanel(Unit.PX);
        mainPanel.addNorth(createHeaderPanel(), 40);
        mainPanel.addNorth(filterPanel, 35);
        mainPanel.setWidgetHidden(filterPanel, true);
        mainPanel.add(contentContainer);
        
        updateContent();
    }

    private Widget createHeaderPanel() {
        HorizontalPanel headerPanel = new HorizontalPanel();
        headerPanel.setSpacing(2);
        headerPanel.setWidth("100%");
        headerPanel.setHeight("100%");
        headerPanel.addStyleName("dimensionFilterSelectionHeader");
        headerPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        
        Label headerLabel = new Label(dimension.getDisplayName());
        headerPanel.add(headerLabel);
        headerPanel.setCellWidth(headerLabel, "100%");
        headerPanel.setCellHorizontalAlignment(headerLabel, HasHorizontalAlignment.ALIGN_CENTER);

        ToggleButton toggleFilterButton = new ToggleButton(new Image(resources.searchIcon()));
        toggleFilterButton.addClickHandler(e -> {
            boolean enabled = toggleFilterButton.isDown();
            mainPanel.setWidgetHidden(filterPanel, !enabled);

            ListDataProvider<Serializable> oldProvider = enabled ? filterPanel.getAllListDataProvider(): filteredData;
            ListDataProvider<Serializable> newProvider = enabled ? filteredData : filterPanel.getAllListDataProvider();
            oldProvider.removeDataDisplay(dataGrid);
            newProvider.addDataDisplay(dataGrid);
        });
        headerPanel.add(toggleFilterButton);
        headerPanel.setCellHorizontalAlignment(toggleFilterButton, HasHorizontalAlignment.ALIGN_RIGHT);
        
        return headerPanel;
    }

    private void updateContent() {
        HashMap<DataRetrieverLevelDTO,SerializableSettings> retrieverSettings = retrieverChainProvider.getRetrieverSettings();
        HashMap<DataRetrieverLevelDTO, HashMap<FunctionDTO, HashSet<? extends Serializable>>> filterSelection = filterSelectionProvider.getSelection();
        if (filterSelection.containsKey(retrieverLevel)) {
            filterSelection.get(retrieverLevel).remove(dimension);
        }
        HashSet<FunctionDTO> dimensions = new HashSet<>();
        dimensions.add(dimension);
        
        counter.increase();
        contentContainer.remove(dataGrid);
        busyIndicator.setBusy(true);
        dataMiningService.getDimensionValuesFor(session, retrieverChainProvider.getDataRetrieverChainDefinition(), retrieverLevel, dimensions,
                retrieverSettings, filterSelection, LocaleInfo.getCurrentLocale().getLocaleName(), new ManagedDataMiningQueryCallback<HashSet<Object>>(counter) {
                    @SuppressWarnings("unchecked")
                    @Override
                    protected void handleSuccess(QueryResultDTO<HashSet<Object>> result) {
                        Map<GroupKey, HashSet<Object>> results = result.getResults();
                        List<Serializable> content = new ArrayList<>();
                        
                        if (!results.isEmpty()) {
                            GroupKey contentKey = new GenericGroupKey<FunctionDTO>(dimension);
                            content.addAll((Collection<? extends Serializable>) results.get(contentKey));
                            content.sort((o1, o2) -> NaturalComparator.compare(o1.toString(), o2.toString()));
                        }
                        
                        busyIndicator.setBusy(false);
                        filterPanel.updateAll(content);
                        contentContainer.add(dataGrid);
                        if (selectionToBeApplied != null) {
                            internalSetSelection(selectionToBeApplied);
                            selectionToBeApplied = null;
                        }
                    }
                    @Override
                    protected void handleFailure(Throwable caught) {
                        errorReporter.reportError("Error fetching the dimension values of " + dimension + ": " + caught.getMessage());
                    }
                });
    }
    
    private String elementAsString(Object element) {
        return element.toString();
    }

    public HashSet<? extends Serializable> getSelection() {
        return new HashSet<>(selectionModel.getSelectedSet());
    }
    
    public void setSelection(Iterable<? extends Serializable> items) {
        if (busyIndicator.isBusy()) {
            selectionToBeApplied = items;
        } else {
            internalSetSelection(items);
        }
    }

    private void internalSetSelection(Iterable<? extends Serializable> items) {
        clearSelection();
        for (Serializable item : items) {
            selectionModel.setSelected(item, true);
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
