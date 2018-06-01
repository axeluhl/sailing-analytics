package com.sap.sailing.gwt.ui.datamining.selection.filter;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
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
import com.sap.sse.datamining.shared.impl.dto.ReducedDimensionsDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.shared.components.AbstractComponent;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class ListRetrieverChainFilterSelectionProvider extends AbstractComponent<SerializableSettings> implements FilterSelectionProvider {

    private final DataMiningSession session;
    private final StringMessages stringMessages;
    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    private final DataRetrieverChainDefinitionProvider retrieverChainProvider;
    private final Set<FilterSelectionChangedListener> listeners;

    private boolean isAwaitingReload;
    private boolean blockDataUpdates;
    private DataRetrieverChainDefinitionDTO retrieverChain;
    private final Map<DataRetrieverLevelDTO, RetrieverLevelFilterSelectionProvider> selectionProvidersMappedByRetrievedDataType;
    
    private final DockLayoutPanel mainPanel;
    private final CellList<DataRetrieverLevelDTO> retrieverLevelList;
    private final SingleSelectionModel<DataRetrieverLevelDTO> retrieverLevelSelectionModel;
    private final ListDataProvider<DataRetrieverLevelDTO> retrieverLevelDataProvider;
    private final ScrollPanel selectionPanel;
    private final FilterSelectionPresenter selectionPresenter;
    private final ScrollPanel selectionPresenterScrollPanel;
    
    /**
     * When the {@link #retrieverLevelList retriever levels} have been received, this field is initialized
     * with the mapping of dimension functions to the reduced set shown in the per-level filters. This way,
     * an original dimension can be {@link ReducedDimensionsDTO#getReducedDimension(FunctionDTO) mapped} to
     * its corresponding dimension from the reduced set of dimensions that are shown in the per-level filters.
     */
    private ReducedDimensionsDTO reducedDimensions;

    public ListRetrieverChainFilterSelectionProvider(Component<?> parent, ComponentContext<?> context,
            DataMiningSession session, StringMessages stringMessages,
            DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter,
            DataRetrieverChainDefinitionProvider retrieverChainProvider) {
        super(parent, context);
        this.session = session;
        this.stringMessages = stringMessages;
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        this.retrieverChainProvider = retrieverChainProvider;
        listeners = new HashSet<>();
        retrieverChainProvider.addDataRetrieverChainDefinitionChangedListener(this);
        
        isAwaitingReload = false;
        blockDataUpdates = false;
        retrieverChain = null;
        selectionProvidersMappedByRetrievedDataType = new HashMap<>();
        
        retrieverLevelList = new CellList<>(new RetrieverLevelCell());
        retrieverLevelSelectionModel = new RetrieverLevelSelectionModel();
        retrieverLevelList.setSelectionModel(retrieverLevelSelectionModel);
        retrieverLevelDataProvider = new ListDataProvider<>();
        retrieverLevelDataProvider.addDataDisplay(retrieverLevelList);
        ScrollPanel retrieverLevelListScrollPanel = new ScrollPanel(retrieverLevelList);
        
        DockLayoutPanel selectionDockLayoutPanel = new DockLayoutPanel(Unit.PX);
        selectionDockLayoutPanel.addNorth(new Label(this.stringMessages.filterBy()), 18);
        selectionPanel = new ScrollPanel();
        selectionDockLayoutPanel.add(selectionPanel);
        
        selectionPresenter = new PlainFilterSelectionPresenter(this, context, stringMessages, retrieverChainProvider,
                this);
        selectionPresenterScrollPanel = new ScrollPanel(selectionPresenter.getEntryWidget());
        
        mainPanel = new DockLayoutPanel(Unit.PX);
        mainPanel.addSouth(selectionPresenterScrollPanel, 100);
        mainPanel.addWest(retrieverLevelListScrollPanel, 300);
        mainPanel.add(selectionDockLayoutPanel);
        mainPanel.setWidgetHidden(selectionPresenterScrollPanel, true);
    }
    
    @Override
    public void setHighestRetrieverLevelWithFilterDimension(FunctionDTO dimension, Serializable groupKey) {
        FunctionDTO dimensionMappedToReducedDimensions = reducedDimensions == null ? dimension : reducedDimensions.getReducedDimension(dimension);
        for (final DataRetrieverLevelDTO retrieverLevel : retrieverChain.getRetrieverLevels()) {
            final RetrieverLevelFilterSelectionProvider selectionProvider = selectionProvidersMappedByRetrievedDataType.get(retrieverLevel);
            if (selectionProvider.hasDimension(dimensionMappedToReducedDimensions)) {
                selectionProvider.addFilter(dimensionMappedToReducedDimensions, Collections.singleton(groupKey));
                break;
            }
        }
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
        updateRetrievalLevels();
        isAwaitingReload = false;
        notifyListeners();
    }
    
    @Override
    public void dataRetrieverChainDefinitionChanged(DataRetrieverChainDefinitionDTO newDataRetrieverChainDefinition) {
        if (!Objects.equals(retrieverChain, newDataRetrieverChainDefinition)) {
            retrieverChain = newDataRetrieverChainDefinition;
            if (!isAwaitingReload && retrieverChain != null) {
                updateRetrievalLevels();
            } else if (!isAwaitingReload) {
                clearContent();
            }
        }
    }

    private void updateRetrievalLevels() {
        clearContent();
        retrieverLevelDataProvider.getList().addAll(retrieverChain.getRetrieverLevels());
        retrieverLevelList.setPageSize(retrieverLevelDataProvider.getList().size());
        dataMiningService.getReducedDimensionsMappedByLevelFor(retrieverChain, LocaleInfo.getCurrentLocale().getLocaleName(),
                new AsyncCallback<ReducedDimensionsDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error fetching the dimensions of the retrieval chain from the server: " + caught.getMessage());
            }
            @Override
            public void onSuccess(ReducedDimensionsDTO dimensionsMappedByLevel) {
                ListRetrieverChainFilterSelectionProvider.this.reducedDimensions = dimensionsMappedByLevel;
                int firstFilterableRetrieverLevel = Integer.MAX_VALUE;
                for (Entry<DataRetrieverLevelDTO, HashSet<FunctionDTO>> dimensionsEntry : dimensionsMappedByLevel.getReducedDimensions().entrySet()) {
                    if (!dimensionsEntry.getValue().isEmpty()) {
                        DataRetrieverLevelDTO retrieverLevel = dimensionsEntry.getKey();
                        RetrieverLevelFilterSelectionProvider selectionProvider =
                                        new RetrieverLevelFilterSelectionProvider(
                                                ListRetrieverChainFilterSelectionProvider.this, getComponentContext(),
                                                session, dataMiningService, errorReporter,
                                                                          ListRetrieverChainFilterSelectionProvider.this,
                                                                          retrieverChain,retrieverLevel);
                        selectionProvider.setAvailableDimensions(dimensionsEntry.getValue());
                        selectionProvidersMappedByRetrievedDataType.put(retrieverLevel, selectionProvider);
                        firstFilterableRetrieverLevel = retrieverLevel.getLevel() < firstFilterableRetrieverLevel ?
                                                            retrieverLevel.getLevel() : firstFilterableRetrieverLevel;
                    }
                }
                retrieverLevelSelectionModel.setSelected(retrieverChain.getRetrieverLevel(firstFilterableRetrieverLevel), true);
            }
        });
    }

    private void clearContent() {
        blockDataUpdates = true;
        retrieverLevelDataProvider.getList().clear();
        retrieverLevelSelectionModel.clear();
        selectionPanel.clear();
        clearSelection();
        selectionProvidersMappedByRetrievedDataType.clear();
        blockDataUpdates = false;
        forwardSelectionChanged();
    }
    
    HashMap<DataRetrieverLevelDTO, SerializableSettings> getRetrieverSettings() {
        return retrieverChainProvider.getRetrieverSettings();
    }
    
    void retrieverLevelFilterSelectionChanged(RetrieverLevelFilterSelectionProvider retrieverLevelFilterSelectionProvider, DimensionFilterSelectionProvider dimensionFilterSelectionProvider) {
        if (!blockDataUpdates) {
            updateFilterSelectionProviders(retrieverLevelFilterSelectionProvider.getRetrieverLevel(),
                                           dimensionFilterSelectionProvider.getSelectedDimension());
        }
    }
    
    void updateFilterSelectionProviders(DataRetrieverLevelDTO beginningWithLevel, final FunctionDTO exceptForDimension) {
        DataRetrieverLevelDTO currentLevel = beginningWithLevel;
        DataRetrieverLevelDTO retrieverLevelToUpdate = null;
        while (currentLevel != null) {
            if (selectionProvidersMappedByRetrievedDataType.containsKey(currentLevel)) {
                retrieverLevelToUpdate = currentLevel;
                break;
            }
            currentLevel = retrieverChain.getNextRetrieverLevel(currentLevel);
        }
        
        if (retrieverLevelToUpdate != null) {
            selectionProvidersMappedByRetrievedDataType.get(retrieverLevelToUpdate).updateAvailableData(exceptForDimension);
        } else {
            //The update of the whole retriever chain is completed. Notify the listeners.
            forwardSelectionChanged();
        }
    }

    private void forwardSelectionChanged() {
        mainPanel.setWidgetHidden(selectionPresenterScrollPanel, getSelection().isEmpty());
        notifyListeners();
    }

    @Override
    public HashMap<DataRetrieverLevelDTO, HashMap<FunctionDTO, HashSet<? extends Serializable>>> getSelection() {
        HashMap<DataRetrieverLevelDTO, HashMap<FunctionDTO, HashSet<? extends Serializable>>> filterSelection = new HashMap<>();
        for (RetrieverLevelFilterSelectionProvider selectionProvider : selectionProvidersMappedByRetrievedDataType.values()) {
            Map<FunctionDTO, HashSet<? extends Serializable>> levelFilterSelection = selectionProvider.getFilterSelection();
            if (!levelFilterSelection.isEmpty()) {
                filterSelection.put(selectionProvider.getRetrieverLevel(), new HashMap<>(levelFilterSelection));
            }
        }
        return filterSelection;
    }

    @Override
    public void applyQueryDefinition(StatisticQueryDefinitionDTO queryDefinition) {
        for (RetrieverLevelFilterSelectionProvider selectionProvider : selectionProvidersMappedByRetrievedDataType.values()) {
            Map<DataRetrieverLevelDTO, HashMap<FunctionDTO, HashSet<? extends Serializable>>> filterSelection = queryDefinition.getFilterSelection();
            DataRetrieverLevelDTO retrieverLevel = selectionProvider.getRetrieverLevel();
            if (filterSelection.containsKey(retrieverLevel)) {
                selectionProvider.applySelection(filterSelection.get(retrieverLevel));
            }
        }
    }

    @Override
    public void clearSelection() {
        for (RetrieverLevelFilterSelectionProvider selectionProvider : selectionProvidersMappedByRetrievedDataType.values()) {
            selectionProvider.clearSelection();
        }
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
    
    private class RetrieverLevelCell extends AbstractCell<DataRetrieverLevelDTO> {
        @Override
        public void render(Cell.Context context, DataRetrieverLevelDTO value, SafeHtmlBuilder sb) {
            if (value == null) {
                return;
            }

            sb.appendHtmlConstant("<table>");
            sb.appendHtmlConstant("<tr><td>" + value.getRetrievedDataType().getDisplayName() + "</td></tr>");
            
            List<DataRetrieverLevelDTO> dataList = retrieverLevelDataProvider.getList();
            if (!dataList.get(dataList.size() - 1).equals(value)) {
                sb.appendHtmlConstant("<tr><td>|</td></tr>");
            }
            
            sb.appendHtmlConstant("</table>");
        }
    }
    
    private class RetrieverLevelSelectionModel extends SingleSelectionModel<DataRetrieverLevelDTO> {
        public RetrieverLevelSelectionModel() {
            super();
            addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
                @Override
                public void onSelectionChange(SelectionChangeEvent event) {
                    selectionPanel.setWidget(selectionProvidersMappedByRetrievedDataType.get(retrieverLevelSelectionModel.getSelectedObject()).getEntryWidget());
                }
            });
        }
        
        @Override
        public void setSelected(DataRetrieverLevelDTO item, boolean selected) {
            if (!selected || !selectionProvidersMappedByRetrievedDataType.containsKey(item)) {
                //Prevents the selection of retriever levels, that don't have any dimensions and can't filter
                return;
            }
            
            super.setSelected(item, selected);
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
        return "listRetrieverChainSelectionProvider";
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
        return "ListRetrieverChainFilterSelectionProvider";
    }
}
