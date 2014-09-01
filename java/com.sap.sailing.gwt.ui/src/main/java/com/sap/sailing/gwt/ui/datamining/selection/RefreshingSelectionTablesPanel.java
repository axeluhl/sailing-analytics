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
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;
import com.sap.sailing.gwt.ui.datamining.DataMiningServiceAsync;
import com.sap.sailing.gwt.ui.datamining.SelectionChangedListener;
import com.sap.sailing.gwt.ui.datamining.SelectionProvider;
import com.sap.sailing.gwt.ui.datamining.StatisticChangedListener;
import com.sap.sailing.gwt.ui.datamining.StatisticProvider;
import com.sap.sailing.gwt.ui.datamining.settings.RefreshingSelectionTablesSettings;
import com.sap.sailing.gwt.ui.datamining.settings.RefreshingSelectionTablesSettingsDialogComponent;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.QueryDefinition;
import com.sap.sse.datamining.shared.QueryResult;
import com.sap.sse.datamining.shared.components.AggregatorType;
import com.sap.sse.datamining.shared.dto.FunctionDTO;

public class RefreshingSelectionTablesPanel implements SelectionProvider<RefreshingSelectionTablesSettings>,
                                                       StatisticChangedListener {
    
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
    private Map<FunctionDTO, SelectionTable<?>> tablesMappedByDimension;
    private Set<SelectionChangedListener> listeners;
    
    private FunctionDTO currentStatisticToCalculate;
    
    public RefreshingSelectionTablesPanel(StringMessages stringMessages, DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter,
                                          StatisticProvider statisticProvider) {
        this.stringMessages = stringMessages;
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        settings = new RefreshingSelectionTablesSettings();
        listeners = new HashSet<SelectionChangedListener>();
        currentStatisticToCalculate = null;

        tablesMappedByDimension = new HashMap<FunctionDTO, SelectionTable<?>>();
        
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

        updateTables();
        doLayout(Window.getClientWidth(), Window.getClientHeight());
        statisticProvider.addStatisticChangedListener(this);
    }
    
    @Override
    public void statisticChanged(FunctionDTO newStatisticToCalculate, AggregatorType newAggregatorType) {
        if (!Objects.equals(currentStatisticToCalculate, newStatisticToCalculate)) {
            currentStatisticToCalculate = newStatisticToCalculate;
            updateAvailableTables();
        }
    }
    
    private void updateAvailableTables() {
        dataMiningService.getDimensionsFor(currentStatisticToCalculate, LocaleInfo.getCurrentLocale().getLocaleName(), new AsyncCallback<Collection<FunctionDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error fetching the dimensions from the server: " + caught.getMessage());
            }
            @Override
            public void onSuccess(Collection<FunctionDTO> dimensions) {
                Collection<FunctionDTO> dimensionsToBeRemoved = new HashSet<>(tablesMappedByDimension.keySet());
                dimensionsToBeRemoved.removeAll(dimensions);
                for (FunctionDTO dimensionToBeRemoved : dimensionsToBeRemoved) {
                    tablesMappedByDimension.remove(dimensionToBeRemoved);
                }
                
                for (FunctionDTO dimension : dimensions) {
                    if (!tablesMappedByDimension.containsKey(dimension)) {
                        SelectionTable<?> table = new SelectionTable<>(dimension);
                        tablesMappedByDimension.put(table.getDimension(), table);
                    }
                }
                
                List<FunctionDTO> sortedDimensions = new ArrayList<>(tablesMappedByDimension.keySet());
                Collections.sort(sortedDimensions);
                tablesPanel.clear();
                for (FunctionDTO dimension : sortedDimensions) {
                    tablesPanel.add(tablesMappedByDimension.get(dimension));
                }

                updateTables();
                doLayout(Window.getClientWidth(), Window.getClientHeight());
            }
        });
    }

    private void doLayout(int newWindowWidth, int newWindowHeight) {
        int absoluteWidth = (int) ((newWindowWidth - widthMargin) * relativeWidthInPercent);
        int absoluteHeight = (int) (newWindowHeight * relativeHeightInPercent);
        
        tablesPanel.setWidth(absoluteWidth + "px");
        tablesPanel.setHeight(absoluteHeight + "px");
        
        Collection<SelectionTable<?>> tables = tablesMappedByDimension.values();
        for (SelectionTable<?> table : tables) {
            table.setWidth((absoluteWidth / tables.size()) + "px");
            table.setHeight(absoluteHeight + "px");
        }
    }

    private void updateTables() {
        //It's necessary to create a new Collection for the dimensions, since using the key set directly results in a serialization error
        Collection<FunctionDTO> dimensions = new HashSet<>(tablesMappedByDimension.keySet());
        dataMiningService.getDimensionValuesFor(dimensions, new AsyncCallback<QueryResult<Set<Object>>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error fetching the dimension values from the server: " + caught.getMessage());
            }
            @Override
            public void onSuccess(QueryResult<Set<Object>> result) {
                boolean tableContentChanged = false;
                for (Entry<GroupKey, Set<Object>> resultEntry : result.getResults().entrySet()) {
                    List<?> sortedDimensionValues = new ArrayList<>(resultEntry.getValue());
                    Collections.sort(sortedDimensionValues, new Comparator<Object>() {
                        @Override
                        public int compare(Object o1, Object o2) {
                            return o1.toString().compareTo(o2.toString());
                        }
                    });
                    boolean currentTableContentChanged = tablesMappedByDimension.get(resultEntry.getKey()).updateContent(sortedDimensionValues);
                    if (!tableContentChanged) {
                        tableContentChanged = currentTableContentChanged;
                    }
                }
                
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

    @Override
    public void addSelectionChangedListener(SelectionChangedListener listener) {
        listeners.add(listener);
        for (SelectionTable<?> table : tablesMappedByDimension.values()) {
            table.addSelectionChangeHandler(new Handler() {
                @Override
                public void onSelectionChange(SelectionChangeEvent event) {
                    notifySelectionChanged();
                }
            });
        }
    }

    private void notifySelectionChanged() {
        for (SelectionChangedListener listener : listeners) {
            listener.selectionChanged();
        }
    }

    @Override
    public Map<FunctionDTO, Collection<? extends Serializable>> getFilterSelection() {
        Map<FunctionDTO, Collection<? extends Serializable>> selection = new HashMap<>();
        for (SelectionTable<?> table : tablesMappedByDimension.values()) {
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
            SelectionTable<?> selectionTable = tablesMappedByDimension.get(selectionEntry.getKey());
            selectionTable.setSelection((Iterable<?>) selectionEntry.getValue());
        }
    }

    @Override
    public void clearSelection() {
        for (SelectionTable<?> table : tablesMappedByDimension.values()) {
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
