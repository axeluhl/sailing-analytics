package com.sap.sailing.gwt.ui.datamining.selection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;
import com.sap.sailing.gwt.ui.datamining.DataMiningServiceAsync;
import com.sap.sailing.gwt.ui.datamining.DataRetrieverChainDefinitionChangedListener;
import com.sap.sailing.gwt.ui.datamining.DataRetrieverChainDefinitionProvider;
import com.sap.sailing.gwt.ui.datamining.SelectionChangedListener;
import com.sap.sailing.gwt.ui.datamining.SelectionProvider;
import com.sap.sailing.gwt.ui.datamining.settings.RefreshingSelectionTablesSettings;
import com.sap.sailing.gwt.ui.datamining.settings.RefreshingSelectionTablesSettingsDialogComponent;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.QueryDefinition;
import com.sap.sse.datamining.shared.QueryResult;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.gwt.client.ErrorReporter;

public class RefreshingSelectionTablesPanel implements SelectionProvider<RefreshingSelectionTablesSettings>,
                                                       DataRetrieverChainDefinitionChangedListener {
    
    private static final int resizeDelay = 100;
    private static final double relativeWidthInPercent = 1;
    private static final int widthMargin = 17;
    private static final double relativeHeightInPercent = 0.35;

    private StringMessages stringMessages;
    private DataMiningServiceAsync dataMiningService;
    private ErrorReporter errorReporter;
    
    private HorizontalPanel tablesPanel;

    private RefreshingSelectionTablesSettings settings;
    private Timer timer;
    private Map<GenericGroupKey<FunctionDTO>, SelectionTable<?>> tablesMappedByDimensionAsKeys;
    private Set<SelectionChangedListener> listeners;
    
    private DataRetrieverChainDefinitionDTO currentDataRetrieverChainDefinition;
    
    public RefreshingSelectionTablesPanel(StringMessages stringMessages, DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter,
                                          DataRetrieverChainDefinitionProvider dataRetrieverChainDefinitionProvider) {
        this.stringMessages = stringMessages;
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        settings = new RefreshingSelectionTablesSettings();
        listeners = new HashSet<SelectionChangedListener>();
        currentDataRetrieverChainDefinition = null;

        tablesMappedByDimensionAsKeys = new HashMap<>();
        
        tablesPanel = new HorizontalPanel();
        
        timer = new Timer() {
            @Override
            public void run() {
                updateTables();
            }
        };

        Window.addResizeHandler(new ResizeHandler() {
            private final Timer timer = new Timer() {
                @Override
                public void run() {
                    doLayout(Window.getClientWidth(), Window.getClientHeight());
                }
            };
            @Override
            public void onResize(ResizeEvent event) {
                timer.schedule(resizeDelay);
            }
        });

        doLayout(Window.getClientWidth(), Window.getClientHeight());
        dataRetrieverChainDefinitionProvider.addDataRetrieverChainDefinitionChangedListener(this);
    }
    
    @Override
    public void dataRetrieverChainDefinitionChanged(DataRetrieverChainDefinitionDTO newDataRetrieverChainDefinition) {
        if (!Objects.equals(currentDataRetrieverChainDefinition, newDataRetrieverChainDefinition)) {
            currentDataRetrieverChainDefinition = newDataRetrieverChainDefinition;
            timer.cancel();
            updateTables();
        }
    }
    
    private void updateTables() {
        dataMiningService.getDimensionValuesFor(currentDataRetrieverChainDefinition, LocaleInfo.getCurrentLocale().getLocaleName(), new AsyncCallback<QueryResult<Set<Object>>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error fetching the dimensions from the server: " + caught.getMessage());
            }
            @Override
            public void onSuccess(QueryResult<Set<Object>> dimensionValues) {
                @SuppressWarnings("unchecked")
                Collection<GenericGroupKey<FunctionDTO>> dimensionsAsKeys = (Collection<GenericGroupKey<FunctionDTO>>)(Collection<?>) dimensionValues.getResults().keySet();
                updateAvailableTables(dimensionsAsKeys);
                
                boolean tableContentChanged = updateTableValues(dimensionValues.getResults());
                if (settings.isRerunQueryAfterRefresh() && tableContentChanged) {
                    notifySelectionChanged();
                    // TODO query is executed, before the data is ready to be analyzed
                    // TODO does this error still appear with the new concept?
                }
                if (settings.isRefreshAutomatically()) {
                    timer.schedule(settings.getRefreshIntervalInMilliseconds());
                }
            }
        });
    }

    private void updateAvailableTables(Collection<GenericGroupKey<FunctionDTO>> dimensionsAsKeys) {
        Collection<GenericGroupKey<FunctionDTO>> dimensionTablesToBeRemoved = new HashSet<>(tablesMappedByDimensionAsKeys.keySet());
        dimensionTablesToBeRemoved.removeAll(dimensionsAsKeys);
        for (GenericGroupKey<FunctionDTO> dimensionToBeRemoved : dimensionTablesToBeRemoved) {
            tablesMappedByDimensionAsKeys.remove(dimensionToBeRemoved);
        }
        
        for (GenericGroupKey<FunctionDTO> dimensionAsKey : dimensionsAsKeys) {
            if (!tablesMappedByDimensionAsKeys.containsKey(dimensionAsKey)) {
                SelectionTable<?> table = new SelectionTable<>(dimensionAsKey.getValue());
                table.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
                    @Override
                    public void onSelectionChange(SelectionChangeEvent event) {
                        notifySelectionChanged();
                    }
                });
                
                tablesMappedByDimensionAsKeys.put(dimensionAsKey, table);
            }
        }
        
        List<GenericGroupKey<FunctionDTO>> sortedDimensions = new ArrayList<>(tablesMappedByDimensionAsKeys.keySet());
        Collections.sort(sortedDimensions, new Comparator<GenericGroupKey<FunctionDTO>>() {
            @Override
            public int compare(GenericGroupKey<FunctionDTO> k1, GenericGroupKey<FunctionDTO> k2) {
                return k1.getValue().compareTo(k2.getValue());
            }
        });
        tablesPanel.clear();
        for (GenericGroupKey<FunctionDTO> dimension : sortedDimensions) {
            tablesPanel.add(tablesMappedByDimensionAsKeys.get(dimension));
        }

        doLayout(Window.getClientWidth(), Window.getClientHeight());
    }

    private void doLayout(int newWindowWidth, int newWindowHeight) {
        int absoluteWidth = (int) ((newWindowWidth - widthMargin) * relativeWidthInPercent);
        int absoluteHeight = (int) (newWindowHeight * relativeHeightInPercent);
        
        tablesPanel.setWidth(absoluteWidth + "px");
        tablesPanel.setHeight(absoluteHeight + "px");
        
        Collection<SelectionTable<?>> tables = tablesMappedByDimensionAsKeys.values();
        for (SelectionTable<?> table : tables) {
            table.setWidth((absoluteWidth / tables.size()) + "px");
            table.setHeight(absoluteHeight + "px");
        }
    }

    private boolean updateTableValues(Map<GroupKey, Set<Object>> newValues) {
        boolean tableContentChanged = false;
        for (GenericGroupKey<FunctionDTO> dimensionAsKey : tablesMappedByDimensionAsKeys.keySet()) {
            List<?> sortedDimensionValues = new ArrayList<>();
            if (newValues.containsKey(dimensionAsKey)) {
                sortedDimensionValues = new ArrayList<>(newValues.get(dimensionAsKey));
                Collections.sort(sortedDimensionValues, new Comparator<Object>() {
                    @Override
                    public int compare(Object o1, Object o2) {
                        return o1.toString().compareTo(o2.toString());
                    }
                });
            }

            boolean currentTableContentChanged = tablesMappedByDimensionAsKeys.get(dimensionAsKey).updateContent(
                    sortedDimensionValues);
            if (!tableContentChanged) {
                tableContentChanged = currentTableContentChanged;
            }
        }

        return tableContentChanged;
    }

    @Override
    public void addSelectionChangedListener(SelectionChangedListener listener) {
        listeners.add(listener);
    }

    private void notifySelectionChanged() {
        for (SelectionChangedListener listener : listeners) {
            listener.selectionChanged();
        }
    }

    @Override
    public Map<FunctionDTO, Collection<? extends Serializable>> getFilterSelection() {
        Map<FunctionDTO, Collection<? extends Serializable>> selection = new HashMap<>();
        for (SelectionTable<?> table : tablesMappedByDimensionAsKeys.values()) {
            Collection<? extends Serializable> specificSelection = table.getSelectionAsValues();
            if (!specificSelection.isEmpty()) {
                selection.put(table.getDimension(), specificSelection);
            }
        }
        return selection;
    }

    @Override
    public void applySelection(QueryDefinition queryDefinition) {
        for (Entry<FunctionDTO, Iterable<? extends Serializable>> selectionEntry : queryDefinition.getFilterSelection().entrySet()) {
            SelectionTable<?> selectionTable = tablesMappedByDimensionAsKeys.get(selectionEntry.getKey());
            selectionTable.setSelection((Iterable<?>) selectionEntry.getValue());
        }
    }

    @Override
    public void clearSelection() {
        for (SelectionTable<?> table : tablesMappedByDimensionAsKeys.values()) {
            table.clearSelection();
        }
    }
    
    @Override
    public String getLocalizedShortName() {
        return stringMessages.selectionTables();
    }

    @Override
    public Widget getEntryWidget() {
        return tablesPanel;
    }

    @Override
    public boolean isVisible() {
        return tablesPanel.isVisible();
    }

    @Override
    public void setVisible(boolean visibility) {
        tablesPanel.setVisible(visibility);
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public SettingsDialogComponent<RefreshingSelectionTablesSettings> getSettingsDialogComponent() {
        return new RefreshingSelectionTablesSettingsDialogComponent(settings, stringMessages);
    }

    @Override
    public void updateSettings(RefreshingSelectionTablesSettings newSettings) {
        if (settings.isRefreshAutomatically() != newSettings.isRefreshAutomatically()) {
            if (newSettings.isRefreshAutomatically()) {
                updateTables();
            } else {
                timer.cancel();
            }
        }
        
        if (settings.getRefreshIntervalInMilliseconds() != newSettings.getRefreshIntervalInMilliseconds()) {
            if (newSettings.isRefreshAutomatically()) {
                timer.cancel();
                timer.schedule(newSettings.getRefreshIntervalInMilliseconds());
            }
        }
        
        settings = newSettings;
    }

    @Override
    public String getDependentCssClassName() {
        return "refreshingSelectionTables";
    }

}
