package com.sap.sailing.gwt.ui.datamining.selection;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;
import com.sap.sailing.gwt.ui.datamining.DataMiningServiceAsync;
import com.sap.sailing.gwt.ui.datamining.DataRetrieverChainDefinitionChangedListener;
import com.sap.sailing.gwt.ui.datamining.SelectionChangedListener;
import com.sap.sailing.gwt.ui.datamining.SelectionProvider;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.QueryDefinitionDTO;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.LocalizedTypeDTO;
import com.sap.sse.gwt.client.ErrorReporter;

public class ListRetrieverChainSelectionProvider implements SelectionProvider<Object>, DataRetrieverChainDefinitionChangedListener {

    private final DataMiningSession session;
    private final StringMessages stringMessages;
    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    private final Set<SelectionChangedListener> listeners;
    
    private final DockLayoutPanel mainPanel;
    private final CellList<LocalizedTypeDTO> retrieverLevelList;
    private final SingleSelectionModel<LocalizedTypeDTO> retrieverLevelSelectionModel;
    private final ListDataProvider<LocalizedTypeDTO> retrieverLevelDataProvider;
    private final SimpleLayoutPanel selectionPanel;

    private DataRetrieverChainDefinitionDTO retrieverChain;
    private final Map<LocalizedTypeDTO, SingleRetrieverLevelSelectionProvider> selectionProvidersMappedByRetrieverLevel;
    private final SelectionChangedListener selectionChangedListener;

    public ListRetrieverChainSelectionProvider(DataMiningSession session, StringMessages stringMessages,
            DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter,
            SimpleDataRetrieverChainDefinitionProvider retrieverChainProvider) {
        this.session = session;
        this.stringMessages = stringMessages;
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        listeners = new HashSet<>();
        
        mainPanel = new DockLayoutPanel(Unit.PX);
        
        retrieverLevelList = new CellList<LocalizedTypeDTO>(new RetrieverLevelCell());
        retrieverLevelSelectionModel = new RetrieverLevelSelectionModel();
        retrieverLevelList.setSelectionModel(retrieverLevelSelectionModel);
        retrieverLevelDataProvider = new ListDataProvider<>();
        retrieverLevelDataProvider.addDataDisplay(retrieverLevelList);
        ScrollPanel retrieverLevelListScrollPanel = new ScrollPanel(retrieverLevelList);
        mainPanel.addWest(retrieverLevelListScrollPanel, 300);
        
        DockLayoutPanel innerDockLayoutPanel = new DockLayoutPanel(Unit.PX);
        innerDockLayoutPanel.addNorth(new Label(this.stringMessages.filterBy()), 18);
        selectionPanel = new SimpleLayoutPanel();
        innerDockLayoutPanel.add(selectionPanel);
        mainPanel.add(innerDockLayoutPanel);
        
        retrieverChain = null;
        selectionProvidersMappedByRetrieverLevel = new HashMap<>();
        selectionChangedListener = new SelectionChangedListener() {
            @Override
            public void selectionChanged() {
                notifyListeners();
            }
        };
        
        retrieverChainProvider.addDataRetrieverChainDefinitionChangedListener(this);
    }
    
    @Override
    public void dataRetrieverChainDefinitionChanged(DataRetrieverChainDefinitionDTO newDataRetrieverChainDefinition) {
        if (!Objects.equals(retrieverChain, newDataRetrieverChainDefinition)) {
            retrieverChain = newDataRetrieverChainDefinition;
            if (retrieverChain != null) {
                updateRetrievalLevels();
            } else {
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
                int retrieverLevelIndex = 0;
                LocalizedTypeDTO firstFilterableRetrieverLevel = null;
                for (LocalizedTypeDTO retrieverLevel : retrieverChain.getRetrievedDataTypesChain()) {
                    String sourceTypeName = retrieverLevel.getTypeName();
                    if (dimensionsMappedBySourceType.containsKey(sourceTypeName) &&
                        !dimensionsMappedBySourceType.get(sourceTypeName).isEmpty()) {
                        firstFilterableRetrieverLevel = firstFilterableRetrieverLevel == null ? retrieverLevel : firstFilterableRetrieverLevel;
                        SingleRetrieverLevelSelectionProvider selectionProvider =
                                new SingleRetrieverLevelSelectionProvider(session, dataMiningService, errorReporter, retrieverChain,
                                                                          retrieverLevel, retrieverLevelIndex, selectionPanel);
                        selectionProvider.setAvailableDimensions(dimensionsMappedBySourceType.get(sourceTypeName));
                        selectionProvider.addSelectionChangedListener(selectionChangedListener);
                        selectionProvidersMappedByRetrieverLevel.put(retrieverLevel, selectionProvider);
                    }
                    retrieverLevelIndex++;
                }
                
                retrieverLevelSelectionModel.setSelected(firstFilterableRetrieverLevel, true);
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

    private void clearContent() {
        retrieverLevelDataProvider.getList().clear();
        retrieverLevelSelectionModel.clear();
        selectionProvidersMappedByRetrieverLevel.clear();
        selectionPanel.clear();
    }

    @Override
    public Map<Integer, Map<FunctionDTO, Collection<? extends Serializable>>> getFilterSelection() {
        Map<Integer, Map<FunctionDTO, Collection<? extends Serializable>>> filterSelection = new HashMap<>();
        for (SingleRetrieverLevelSelectionProvider selectionProvider : selectionProvidersMappedByRetrieverLevel.values()) {
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
        for (SingleRetrieverLevelSelectionProvider selectionProvider : selectionProvidersMappedByRetrieverLevel.values()) {
            Map<Integer, Map<FunctionDTO, Collection<? extends Serializable>>> filterSelection = queryDefinition.getFilterSelection();
            int retrieverLevel = selectionProvider.getRetrieverLevel();
            if (filterSelection.containsKey(retrieverLevel)) {
                selectionProvider.applySelection(filterSelection.get(retrieverLevel));
            }
        }
    }

    @Override
    public void clearSelection() {
        for (SingleRetrieverLevelSelectionProvider selectionProvider : selectionProvidersMappedByRetrieverLevel.values()) {
            selectionProvider.clearSelection();
        }
    }

    @Override
    public void addSelectionChangedListener(SelectionChangedListener listener) {
        listeners.add(listener);
    }
    
    private void notifyListeners() {
        for (SelectionChangedListener listener : listeners) {
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
                    selectionPanel.setWidget(selectionProvidersMappedByRetrieverLevel.get(retrieverLevelSelectionModel.getSelectedObject()).getEntryWidget());
                }
            });
        }
        
        @Override
        public void setSelected(LocalizedTypeDTO item, boolean selected) {
            if (!selected || !selectionProvidersMappedByRetrieverLevel.containsKey(item)) {
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
    public SettingsDialogComponent<Object> getSettingsDialogComponent() {
        return null;
    }

    @Override
    public void updateSettings(Object newSettings) {
    }

}
