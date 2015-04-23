package com.sap.sailing.gwt.ui.datamining.selection;

import java.io.Serializable;
import java.util.Collection;
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
import com.google.gwt.user.client.ui.CustomScrollPanel;
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
import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.dto.QueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.LocalizedTypeDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class ListRetrieverChainFilterSelectionProvider implements FilterSelectionProvider {

    private final DataMiningSession session;
    private final StringMessages stringMessages;
    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    private final Set<FilterSelectionChangedListener> listeners;

    private boolean isAwaitingReload;
    private DataRetrieverChainDefinitionDTO retrieverChain;
    private final Map<LocalizedTypeDTO, RetrieverLevelFilterSelectionProvider> selectionProvidersMappedByRetrievedDataType;
    private final FilterSelectionChangedListener retrieverLevelSelectionChangedListener;
    
    private final DockLayoutPanel mainPanel;
    private final CellList<LocalizedTypeDTO> retrieverLevelList;
    private final SingleSelectionModel<LocalizedTypeDTO> retrieverLevelSelectionModel;
    private final ListDataProvider<LocalizedTypeDTO> retrieverLevelDataProvider;
    private final SizeProvidingScrollPanel selectionPanel;
    private final FilterSelectionPresenter selectionPresenter;
    private final ScrollPanel selectionPresenterScrollPanel;

    public ListRetrieverChainFilterSelectionProvider(DataMiningSession session, StringMessages stringMessages,
            DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter,
            DataRetrieverChainDefinitionProvider retrieverChainProvider) {
        this.session = session;
        this.stringMessages = stringMessages;
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        listeners = new HashSet<>();
        retrieverChainProvider.addDataRetrieverChainDefinitionChangedListener(this);
        
        isAwaitingReload = false;
        retrieverChain = null;
        selectionProvidersMappedByRetrievedDataType = new HashMap<>();
        retrieverLevelSelectionChangedListener = new FilterSelectionChangedListener() {
            @Override
            public void selectionChanged() {
                mainPanel.setWidgetHidden(selectionPresenterScrollPanel, getSelection().isEmpty());
                notifyListeners();
            }
        };
        
        retrieverLevelList = new CellList<LocalizedTypeDTO>(new RetrieverLevelCell());
        retrieverLevelSelectionModel = new RetrieverLevelSelectionModel();
        retrieverLevelList.setSelectionModel(retrieverLevelSelectionModel);
        retrieverLevelDataProvider = new ListDataProvider<>();
        retrieverLevelDataProvider.addDataDisplay(retrieverLevelList);
        ScrollPanel retrieverLevelListScrollPanel = new ScrollPanel(retrieverLevelList);
        
        DockLayoutPanel selectionDockLayoutPanel = new DockLayoutPanel(Unit.PX);
        selectionDockLayoutPanel.addNorth(new Label(this.stringMessages.filterBy()), 18);
        selectionPanel = new SizeProvidingScrollPanel();
        selectionDockLayoutPanel.add(selectionPanel);
        
        selectionPresenter = new PlainFilterSelectionPresenter(stringMessages, retrieverChainProvider, this);
        selectionPresenterScrollPanel = new ScrollPanel(selectionPresenter.getEntryWidget());
        
        mainPanel = new DockLayoutPanel(Unit.PX);
        mainPanel.addSouth(selectionPresenterScrollPanel, 100);
        mainPanel.addWest(retrieverLevelListScrollPanel, 300);
        mainPanel.add(selectionDockLayoutPanel);
        mainPanel.setWidgetHidden(selectionPresenterScrollPanel, true);
    }
    
    @Override
    public void awaitReloadComponents() {
        isAwaitingReload = true;
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
        retrieverLevelDataProvider.getList().addAll(retrieverChain.getRetrievedDataTypesChain());
        retrieverLevelList.setPageSize(retrieverLevelDataProvider.getList().size());
        
        dataMiningService.getDimensionsFor(retrieverChain, LocaleInfo.getCurrentLocale().getLocaleName(), new AsyncCallback<Iterable<FunctionDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error fetching the dimensions of the retrieval chain from the server: " + caught.getMessage());
            }
            @Override
            public void onSuccess(Iterable<FunctionDTO> dimensions) {
                Map<String, Collection<FunctionDTO>> dimensionsMappedBySourceType = mapBySourceType(dimensions);
                Map<Integer, Collection<FunctionDTO>> reducedDimensionsMappedByRetrieverLevel = reduceAndMapByRetrieverLevel(dimensionsMappedBySourceType);
                int firstFilterableRetrieverLevel = Integer.MAX_VALUE;
                for (Entry<Integer, Collection<FunctionDTO>> dimensionsEntry : reducedDimensionsMappedByRetrieverLevel.entrySet()) {
                    if (!dimensionsEntry.getValue().isEmpty()) {
                        int retrieverLevel = dimensionsEntry.getKey();
                        LocalizedTypeDTO retrievedDataType = retrieverChain.getRetrievedDataType(retrieverLevel);
                        RetrieverLevelFilterSelectionProvider selectionProvider =
                                new RetrieverLevelFilterSelectionProvider(session, dataMiningService, errorReporter, ListRetrieverChainFilterSelectionProvider.this, retrieverChain,
                                                                          retrievedDataType, retrieverLevel, selectionPanel);
                        selectionProvider.setAvailableDimensions(dimensionsEntry.getValue());
                        selectionProvider.addSelectionChangedListener(retrieverLevelSelectionChangedListener);
                        selectionProvidersMappedByRetrievedDataType.put(retrievedDataType, selectionProvider);
                        
                        firstFilterableRetrieverLevel = retrieverLevel < firstFilterableRetrieverLevel ? retrieverLevel : firstFilterableRetrieverLevel;
                    }
                }
                
                retrieverLevelSelectionModel.setSelected(retrieverChain.getRetrievedDataType(firstFilterableRetrieverLevel), true);
            }
        });
    }

    private Map<String, Collection<FunctionDTO>> mapBySourceType(Iterable<FunctionDTO> dimensions) {
        Map<String, Collection<FunctionDTO>> dimensionsMappedBySourceType = new HashMap<>();
        for (FunctionDTO dimension : dimensions) {
            if (!dimensionsMappedBySourceType.containsKey(dimension.getSourceTypeName())) {
                dimensionsMappedBySourceType.put(dimension.getSourceTypeName(), new HashSet<FunctionDTO>());
            }
            dimensionsMappedBySourceType.get(dimension.getSourceTypeName()).add(dimension);
        }
        return dimensionsMappedBySourceType;
    }
    
    private Map<Integer, Collection<FunctionDTO>> reduceAndMapByRetrieverLevel(Map<String, Collection<FunctionDTO>> dimensionsMappedBySourceType) {
        Map<Integer, Collection<FunctionDTO>> reducedDimensionsMappedBySourceType = new HashMap<>();
        for (int retrieverLevel = 0; retrieverLevel < retrieverChain.size(); retrieverLevel++) {
            LocalizedTypeDTO retrievedDataType = retrieverChain.getRetrievedDataType(retrieverLevel);
            Collection<FunctionDTO> dimensions = dimensionsMappedBySourceType.get(retrievedDataType.getTypeName());
            Collection<FunctionDTO> previousDimensions =
                    retrieverLevel > 0 ? dimensionsMappedBySourceType.get(retrieverChain.getRetrievedDataType(retrieverLevel - 1).getTypeName()) : null;
            if (dimensions != null && !dimensions.isEmpty()) {
                if (reducedDimensionsMappedBySourceType.isEmpty() || previousDimensions == null || previousDimensions.isEmpty()) {
                    reducedDimensionsMappedBySourceType.put(retrieverLevel, dimensions);
                } else {
                    reducedDimensionsMappedBySourceType.put(retrieverLevel, reduce(dimensions, previousDimensions));
                }
            }
        }
        return reducedDimensionsMappedBySourceType;
    }

    private Collection<FunctionDTO> reduce(Collection<FunctionDTO> dimensionsToReduce, Collection<FunctionDTO> byDimensions) {
        Collection<FunctionDTO> reducedDimensions = new HashSet<>();
        for (FunctionDTO dimension : dimensionsToReduce) {
            boolean isDimensionAllowed = true;
            for (FunctionDTO forbiddenDimension : byDimensions) {
                if (areDimensionsLogicalEqual(dimension, forbiddenDimension)) {
                    isDimensionAllowed = false;
                    break;
                }
            }
            if (isDimensionAllowed) {
                reducedDimensions.add(dimension);
            }
        }
        return reducedDimensions;
    }

    private boolean areDimensionsLogicalEqual(FunctionDTO dimension1, FunctionDTO dimension2) {
        return dimension1.getDisplayName().equals(dimension2.getDisplayName()) &&
               dimension1.getParameterTypeNames().equals(dimension2.getParameterTypeNames()) &&
               dimension1.getReturnTypeName().equals(dimension2.getReturnTypeName());
    }

    private void clearContent() {
        retrieverLevelDataProvider.getList().clear();
        retrieverLevelSelectionModel.clear();
        selectionPanel.clear();
        clearSelection();
        selectionProvidersMappedByRetrievedDataType.clear();
    }

    @Override
    public Map<Integer, Map<FunctionDTO, Collection<? extends Serializable>>> getSelection() {
        Map<Integer, Map<FunctionDTO, Collection<? extends Serializable>>> filterSelection = new HashMap<>();
        for (RetrieverLevelFilterSelectionProvider selectionProvider : selectionProvidersMappedByRetrievedDataType.values()) {
            Map<FunctionDTO, Collection<? extends Serializable>> levelFilterSelection = selectionProvider.getFilterSelection();
            if (!levelFilterSelection.isEmpty()) {
                filterSelection.put(selectionProvider.getRetrieverLevel(),
                        new HashMap<FunctionDTO, Collection<? extends Serializable>>(levelFilterSelection));
            }
        }
        return filterSelection;
    }

    @Override
    public void applySelection(QueryDefinitionDTO queryDefinition) {
        for (RetrieverLevelFilterSelectionProvider selectionProvider : selectionProvidersMappedByRetrievedDataType.values()) {
            Map<Integer, Map<FunctionDTO, Collection<? extends Serializable>>> filterSelection = queryDefinition.getFilterSelection();
            int retrieverLevel = selectionProvider.getRetrieverLevel();
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
    
    private class RetrieverLevelCell extends AbstractCell<LocalizedTypeDTO> {
        @Override
        public void render(Cell.Context context, LocalizedTypeDTO value, SafeHtmlBuilder sb) {
            if (value == null) {
                return;
            }

            sb.appendHtmlConstant("<table>");
            sb.appendHtmlConstant("<tr><td>" + value.getDisplayName() + "</td></tr>");
            
            List<LocalizedTypeDTO> dataList = retrieverLevelDataProvider.getList();
            if (!dataList.get(dataList.size() - 1).equals(value)) {
                sb.appendHtmlConstant("<tr><td>|</td></tr>");
            }
            
            sb.appendHtmlConstant("</table>");
        }
    }
    
    private class RetrieverLevelSelectionModel extends SingleSelectionModel<LocalizedTypeDTO> {
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
        public void setSelected(LocalizedTypeDTO item, boolean selected) {
            if (!selected || !selectionProvidersMappedByRetrievedDataType.containsKey(item)) {
                //Prevents the selection of retriever levels, that don't have any dimensions and can't filter
                return;
            }
            
            super.setSelected(item, selected);
        }
    }
    
    private class SizeProvidingScrollPanel extends CustomScrollPanel implements SizeProvider {
        @Override
        public int getFreeWidthInPX() {
            return getOffsetWidth() - getVerticalScrollbar().asWidget().getOffsetWidth();
        }
        @Override
        public int getFreeHeightInPX() {
            //Subtracting a value prevents weird scroll behavior
            return getOffsetHeight() - getHorizontalScrollbar().asWidget().getOffsetHeight() - 3;
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
    public SettingsDialogComponent<AbstractSettings> getSettingsDialogComponent() {
        return null;
    }

    @Override
    public void updateSettings(AbstractSettings newSettings) {
    }

}
