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
import com.sap.sailing.gwt.ui.datamining.DataRetrieverChainDefinitionChangedListener;
import com.sap.sailing.gwt.ui.datamining.DataRetrieverChainDefinitionProvider;
import com.sap.sailing.gwt.ui.datamining.FilterSelectionPresenter;
import com.sap.sailing.gwt.ui.datamining.FilterSelectionProvider;
import com.sap.sailing.gwt.ui.datamining.SelectionChangedListener;
import com.sap.sailing.gwt.ui.datamining.presentation.PlainFilterSelectionPresenter;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.dto.QueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.LocalizedTypeDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class ListRetrieverChainFilterSelectionProvider implements FilterSelectionProvider, DataRetrieverChainDefinitionChangedListener {

    private final DataMiningSession session;
    private final StringMessages stringMessages;
    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    private final Set<SelectionChangedListener> listeners;

    private DataRetrieverChainDefinitionDTO retrieverChain;
    private final Map<LocalizedTypeDTO, RetrieverLevelFilterSelectionProvider> selectionProvidersMappedByRetrieverLevel;
    private final SelectionChangedListener retrieverLevelSelectionChangedListener;
    
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
        
        retrieverChain = null;
        selectionProvidersMappedByRetrieverLevel = new HashMap<>();
        retrieverLevelSelectionChangedListener = new SelectionChangedListener() {
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
                        RetrieverLevelFilterSelectionProvider selectionProvider =
                                new RetrieverLevelFilterSelectionProvider(session, dataMiningService, errorReporter, ListRetrieverChainFilterSelectionProvider.this, retrieverChain,
                                                                          retrieverLevel, retrieverLevelIndex, selectionPanel);
                        selectionProvider.setAvailableDimensions(dimensionsMappedBySourceType.get(sourceTypeName));
                        selectionProvider.addSelectionChangedListener(retrieverLevelSelectionChangedListener);
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
        selectionPanel.clear();
        clearSelection();
        selectionProvidersMappedByRetrieverLevel.clear();
    }

    @Override
    public Map<Integer, Map<FunctionDTO, Collection<? extends Serializable>>> getSelection() {
        Map<Integer, Map<FunctionDTO, Collection<? extends Serializable>>> filterSelection = new HashMap<>();
        for (RetrieverLevelFilterSelectionProvider selectionProvider : selectionProvidersMappedByRetrieverLevel.values()) {
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
        for (RetrieverLevelFilterSelectionProvider selectionProvider : selectionProvidersMappedByRetrieverLevel.values()) {
            Map<Integer, Map<FunctionDTO, Collection<? extends Serializable>>> filterSelection = queryDefinition.getFilterSelection();
            int retrieverLevel = selectionProvider.getRetrieverLevel();
            if (filterSelection.containsKey(retrieverLevel)) {
                selectionProvider.applySelection(filterSelection.get(retrieverLevel));
            }
        }
    }

    @Override
    public void clearSelection() {
        for (RetrieverLevelFilterSelectionProvider selectionProvider : selectionProvidersMappedByRetrieverLevel.values()) {
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
    public SettingsDialogComponent<Settings> getSettingsDialogComponent() {
        return null;
    }

    @Override
    public void updateSettings(Settings newSettings) {
    }

}
