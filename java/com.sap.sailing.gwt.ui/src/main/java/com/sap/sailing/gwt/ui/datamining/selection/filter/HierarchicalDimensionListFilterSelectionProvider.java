package com.sap.sailing.gwt.ui.datamining.selection.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.builder.shared.TableCellBuilder;
import com.google.gwt.dom.builder.shared.TableRowBuilder;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.DefaultSelectionEventManager.CheckboxEventTranslator;
import com.google.gwt.view.client.DefaultSelectionEventManager.SelectAction;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.datamining.DataMiningServiceAsync;
import com.sap.sailing.gwt.ui.datamining.DataRetrieverChainDefinitionProvider;
import com.sap.sailing.gwt.ui.datamining.FilterSelectionChangedListener;
import com.sap.sailing.gwt.ui.datamining.FilterSelectionPresenter;
import com.sap.sailing.gwt.ui.datamining.FilterSelectionProvider;
import com.sap.sailing.gwt.ui.datamining.presentation.PlainFilterSelectionPresenter;
import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.BaseCellTableBuilder;
import com.sap.sse.gwt.client.shared.components.AbstractComponent;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class HierarchicalDimensionListFilterSelectionProvider extends AbstractComponent<SerializableSettings> implements FilterSelectionProvider {

    private final DataMiningSession session;
    private final StringMessages stringMessages;
    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    private final DataRetrieverChainDefinitionProvider retrieverChainProvider;
    private final Set<FilterSelectionChangedListener> listeners;

    private boolean isAwaitingReload;
    private boolean blockDataUpdates;
    private DataRetrieverChainDefinitionDTO retrieverChain;
    private final MultiSelectionModel<DimensionWithContext> filterDimensionSelectionModel;
    private final ListDataProvider<DimensionWithContext> availableFilterDimensions;
    
    private final DockLayoutPanel mainPanel;
    private final DataGrid<DimensionWithContext> filterDimensionsList;
    private final Column<DimensionWithContext, Boolean> checkboxColumn;
    private final FilterSelectionPresenter selectionPresenter;
    private final ScrollPanel selectionPresenterScrollPanel;
    
    public HierarchicalDimensionListFilterSelectionProvider(Component<?> parent, ComponentContext<?> context,
            DataMiningSession session, StringMessages stringMessages,
            DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter,
            DataRetrieverChainDefinitionProvider retrieverChainProvider) {
        super(parent, context);
        this.session = session;
        this.stringMessages = stringMessages;
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        this.retrieverChainProvider = retrieverChainProvider;
        retrieverChainProvider.addDataRetrieverChainDefinitionChangedListener(this);
        
        listeners = new HashSet<>();
        isAwaitingReload = false;
        blockDataUpdates = false;
        retrieverChain = null;
        filterDimensionSelectionModel = new MultiSelectionModel<>();
        filterDimensionSelectionModel.addSelectionChangeHandler(e -> selectedFilterDimensionsChanged(e));
        availableFilterDimensions = new ListDataProvider<>();
        
        checkboxColumn = new Column<DimensionWithContext, Boolean>(new CheckboxCell(true, false)) {
            @Override
            public Boolean getValue(DimensionWithContext object) {
                return filterDimensionSelectionModel.isSelected(object);
            }
        };
        TextColumn<DimensionWithContext> dimensionColumn = new TextColumn<DimensionWithContext>() {
            @Override
            public String getValue(DimensionWithContext object) {
                return object.getDimension().getDisplayName();
            }
        };

        FilterDimensionsListResources filterDimensionsListResources = GWT.create(FilterDimensionsListResources.class);
        filterDimensionsList = new DataGrid<DimensionWithContext>(Integer.MAX_VALUE, filterDimensionsListResources);
        filterDimensionsList.setAutoHeaderRefreshDisabled(true);
        filterDimensionsList.setAutoFooterRefreshDisabled(true);
        filterDimensionsList.addColumn(checkboxColumn);
        filterDimensionsList.addColumn(dimensionColumn);
        filterDimensionsList.setTableBuilder(new FilterDimensionsListBuilder(filterDimensionsList));
        filterDimensionsList.setSelectionModel(filterDimensionSelectionModel, DefaultSelectionEventManager.createCustomManager(new CustomCheckboxEventTranslator()));
        availableFilterDimensions.addDataDisplay(filterDimensionsList);
        
        DockLayoutPanel filterDimensionsSelectionPanel = new DockLayoutPanel(Unit.PX);
        filterDimensionsSelectionPanel.addNorth(new Label(stringMessages.filterBy()), 45); // TODO Add dimension search box
        filterDimensionsSelectionPanel.add(filterDimensionsList);
        
        selectionPresenter = new PlainFilterSelectionPresenter(this, context, stringMessages, retrieverChainProvider, this);
        selectionPresenterScrollPanel = new ScrollPanel(selectionPresenter.getEntryWidget());
        
        mainPanel = new DockLayoutPanel(Unit.PX);
        mainPanel.addSouth(selectionPresenterScrollPanel, 100);
        mainPanel.addWest(filterDimensionsSelectionPanel, 300);
        mainPanel.add(new SimplePanel()); // TODO
        mainPanel.setWidgetHidden(selectionPresenterScrollPanel, true);
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
        updateFilterDimensions();
        isAwaitingReload = false;
        notifyListeners();
    }
    
    @Override
    public void dataRetrieverChainDefinitionChanged(DataRetrieverChainDefinitionDTO newDataRetrieverChainDefinition) {
        if (!Objects.equals(retrieverChain, newDataRetrieverChainDefinition)) {
            retrieverChain = newDataRetrieverChainDefinition;
            if (!isAwaitingReload && retrieverChain != null) {
                updateFilterDimensions();
            } else if (!isAwaitingReload) {
                availableFilterDimensions.getList().clear();
            }
        }
    }
    
    private void updateFilterDimensions() {
        // TODO Auto-generated method stub
    }
    
    private void selectedFilterDimensionsChanged(SelectionChangeEvent event) {
        // TODO Auto-generated method stub
    }
    
    @Override
    public HashMap<DataRetrieverLevelDTO, HashMap<FunctionDTO, HashSet<? extends Serializable>>> getSelection() {
        // TODO Auto-generated method stub
        return new HashMap<>();
    }
    
    @Override
    public void applyQueryDefinition(StatisticQueryDefinitionDTO queryDefinition) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void clearSelection() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setHighestRetrieverLevelWithFilterDimension(FunctionDTO dimension, Serializable groupKey) {
        // TODO Auto-generated method stub
        
    }
    
    HashMap<DataRetrieverLevelDTO, SerializableSettings> getRetrieverSettings() {
        return retrieverChainProvider.getRetrieverSettings();
    }

    @Override
    public void addSelectionChangedListener(FilterSelectionChangedListener listener) {
        listeners.add(listener);
    }
    
    private void notifyListeners() {
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
    
    private interface FilterDimensionsListResources extends DataGrid.Resources {

        @Source({ CellTable.Style.DEFAULT_CSS, "FilterTable.css" })
        FilterDimensionsListStyle cellTableStyle();

        interface FilterDimensionsListStyle extends DataGrid.Style {
        }
    }
    
    private static class DimensionWithContext implements Comparable<DimensionWithContext> {

        private final FunctionDTO dimension;
        private final DataRetrieverLevelDTO retrieverLevel;
        private final Collection<String> matchingStrings;
        
        public DimensionWithContext(FunctionDTO dimension, DataRetrieverLevelDTO retrieverLevel) {
            this.dimension = dimension;
            this.retrieverLevel = retrieverLevel;
            matchingStrings = new ArrayList<>(2);
            matchingStrings.add(dimension.getDisplayName());
            matchingStrings.add(retrieverLevel.getRetrievedDataType().getDisplayName());
        }

        public FunctionDTO getDimension() {
            return dimension;
        }

        public DataRetrieverLevelDTO getRetrieverLevel() {
            return retrieverLevel;
        }

        public Collection<String> getMatchingStrings() {
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
        
        private DataRetrieverLevelDTO currentLevel;
        private int dimensionIndex;

        public FilterDimensionsListBuilder(AbstractCellTable<DimensionWithContext> cellTable) {
            super(cellTable);
            headerStyle = cellTable.getResources().style().header();
        }
        
        @Override
        public void buildRowImpl(DimensionWithContext rowValue, int absRowIndex) {
            if (!Objects.equals(currentLevel, rowValue.getRetrieverLevel())) {
                currentLevel = rowValue.getRetrieverLevel();
                dimensionIndex = 0;
                TableRowBuilder levelRowBuilder = startRow();
                TableCellBuilder levelCellBuilder = levelRowBuilder.startTD();
                levelCellBuilder.colSpan(this.cellTable.getColumnCount()).className(headerStyle);
                levelCellBuilder.text(currentLevel.getRetrievedDataType().getDisplayName());
                levelCellBuilder.endTD();
                levelRowBuilder.endTR();
            }
            
            super.buildRowImpl(rowValue, dimensionIndex);
            dimensionIndex++;
        }
        
    }
    
    private class CustomCheckboxEventTranslator extends CheckboxEventTranslator<DimensionWithContext> {
        
        @Override
        public boolean clearCurrentSelection(CellPreviewEvent<DimensionWithContext> event) {
            return !isCheckboxColumn(event.getColumn());
        }

        @Override
        public SelectAction translateSelectionEvent(CellPreviewEvent<DimensionWithContext> event) {
            NativeEvent nativeEvent = event.getNativeEvent();
            if (BrowserEvents.CLICK.equals(nativeEvent.getType())) {
                if (nativeEvent.getCtrlKey()) {
                    DimensionWithContext value = event.getValue();
                    filterDimensionSelectionModel.setSelected(value, !filterDimensionSelectionModel.isSelected(value));
                    return SelectAction.IGNORE;
                }
                if (!filterDimensionSelectionModel.getSelectedSet().isEmpty() && !isCheckboxColumn(event.getColumn())) {
                    return SelectAction.DEFAULT;
                }
            }
            return SelectAction.TOGGLE;
        }

        private boolean isCheckboxColumn(int columnIndex) {
            return columnIndex == filterDimensionsList.getColumnIndex(checkboxColumn);
        }
        
    }

}
