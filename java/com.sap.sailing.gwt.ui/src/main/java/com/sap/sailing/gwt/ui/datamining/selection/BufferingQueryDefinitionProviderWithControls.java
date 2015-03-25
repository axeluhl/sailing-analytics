package com.sap.sailing.gwt.ui.datamining.selection;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;
import com.sap.sailing.gwt.ui.datamining.DataMiningControls;
import com.sap.sailing.gwt.ui.datamining.DataMiningServiceAsync;
import com.sap.sailing.gwt.ui.datamining.DataRetrieverChainDefinitionChangedListener;
import com.sap.sailing.gwt.ui.datamining.GroupingChangedListener;
import com.sap.sailing.gwt.ui.datamining.SelectionChangedListener;
import com.sap.sailing.gwt.ui.datamining.SelectionProvider;
import com.sap.sailing.gwt.ui.datamining.StatisticChangedListener;
import com.sap.sailing.gwt.ui.datamining.StatisticProvider;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.QueryDefinitionDTO;
import com.sap.sse.datamining.shared.components.AggregatorType;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.QueryDefinitionDTOImpl;
import com.sap.sse.gwt.client.ErrorReporter;

public class BufferingQueryDefinitionProviderWithControls extends AbstractQueryDefinitionProvider implements DataMiningControls {

    /**
     * The delay before a changed query definition is submitted to the listeners.
     * This prevents unnecessary queries caused by a change of the used data type, that then causes
     * a change of the dimension to group by and the data retriever chain.
     * Or caused by quick changes of the filter selection.
     */
    private static final int queryBufferTimeInMillis = 200;
    
    private final Timer queryDefinitionReleaseTimer;
    
    private final DockLayoutPanel mainPanel;
    private HorizontalPanel controlsPanel;
    
    private StatisticProvider statisticProvider;
    private SimpleDataRetrieverChainDefinitionProvider retrieverChainProvider;

    private MultiDimensionalGroupingProvider groupBySelectionPanel;
    
    private SelectionProvider<?> selectionProvider;

    public BufferingQueryDefinitionProviderWithControls(DataMiningSession session, StringMessages stringMessages, SailingServiceAsync sailingService, DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter) {
        super(stringMessages, sailingService, dataMiningService, errorReporter);
        
        mainPanel = new DockLayoutPanel(Unit.PX);

        mainPanel.addNorth(createFunctionsPanel(), 80);

        selectionProvider = new RetrieverLevelSpecificSelectionProvider(session, stringMessages, dataMiningService, errorReporter, retrieverChainProvider);
        selectionProvider.addSelectionChangedListener(new SelectionChangedListener() {
            @Override
            public void selectionChanged() {
                scheduleQueryDefinitionChanged();
            }
        });
        mainPanel.add(selectionProvider.getEntryWidget());
        
        queryDefinitionReleaseTimer = new Timer() {
            @Override
            public void run() {
                notifyQueryDefinitionChanged();
            }
        };
    }

    private Widget createFunctionsPanel() {
        HorizontalPanel functionsPanel = new HorizontalPanel();
        functionsPanel.setSpacing(5);

        VerticalPanel statisticAndRetrieverChainPanel = new VerticalPanel();
        statisticProvider = new SimpleStatisticProvider(getStringMessages(), getDataMiningService(), getErrorReporter());
        statisticProvider.addStatisticChangedListener(new StatisticChangedListener() {
            @Override
            public void statisticChanged(FunctionDTO newStatisticToCalculate, AggregatorType newAggregatorType) {
                scheduleQueryDefinitionChanged();
            }
        });
        statisticAndRetrieverChainPanel.add(statisticProvider.getEntryWidget());
        
        retrieverChainProvider = new SimpleDataRetrieverChainDefinitionProvider(getStringMessages(), getDataMiningService(), getErrorReporter(), statisticProvider);
        retrieverChainProvider.addDataRetrieverChainDefinitionChangedListener(new DataRetrieverChainDefinitionChangedListener() {
            @Override
            public void dataRetrieverChainDefinitionChanged(DataRetrieverChainDefinitionDTO newDataRetrieverChainDefinition) {
                scheduleQueryDefinitionChanged();
            }
        });
        statisticAndRetrieverChainPanel.add(retrieverChainProvider.getEntryWidget());
        functionsPanel.add(statisticAndRetrieverChainPanel);

        groupBySelectionPanel = new MultiDimensionalGroupingProvider(getStringMessages(), getDataMiningService(), getErrorReporter(), statisticProvider);
        groupBySelectionPanel.addGroupingChangedListener(new GroupingChangedListener() {
            @Override
            public void groupingChanged() {
                scheduleQueryDefinitionChanged();
            }
        });
        functionsPanel.add(groupBySelectionPanel.getEntryWidget());

        Button clearSelectionButton = new Button(this.getStringMessages().clearSelection());
        clearSelectionButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                selectionProvider.clearSelection();
            }
        });
        functionsPanel.add(clearSelectionButton);
        
        controlsPanel = new HorizontalPanel();
        controlsPanel.setSpacing(5);
        functionsPanel.add(controlsPanel);

        return functionsPanel;
    }
    
    private void scheduleQueryDefinitionChanged() {
        queryDefinitionReleaseTimer.schedule(queryBufferTimeInMillis);
    }
    
    @Override
    public QueryDefinitionDTO getQueryDefinition() {
        QueryDefinitionDTOImpl queryDTO = new QueryDefinitionDTOImpl(LocaleInfo.getCurrentLocale().getLocaleName(), statisticProvider.getStatisticToCalculate(),
                                                               statisticProvider.getAggregatorType(), retrieverChainProvider.getDataRetrieverChainDefinition());
        
        for (FunctionDTO dimension : groupBySelectionPanel.getDimensionsToGroupBy()) {
            queryDTO.appendDimensionToGroupBy(dimension);
        }
        
        for (Entry<Integer, Map<FunctionDTO, Collection<? extends Serializable>>> filterSelectionEntry : selectionProvider.getFilterSelection().entrySet()) {
            queryDTO.setFilterSelectionFor(filterSelectionEntry.getKey(), filterSelectionEntry.getValue());
        }
        
        return queryDTO;
    }

    @Override
    public void applyQueryDefinition(QueryDefinitionDTO queryDefinition) {
        setBlockChangeNotification(true);
        statisticProvider.applyQueryDefinition(queryDefinition);
        retrieverChainProvider.applyQueryDefinition(queryDefinition);
        groupBySelectionPanel.applyQueryDefinition(queryDefinition);
        selectionProvider.applySelection(queryDefinition);
        setBlockChangeNotification(false);
        
        scheduleQueryDefinitionChanged();
    }

    @Override
    public void addControl(Widget controlWidget) {
        controlsPanel.add(controlWidget);
    }

    @Override
    public Widget getEntryWidget() {
        return mainPanel;
    }

    @Override
    public SelectionProvider<?> getSelectionProvider() {
        return selectionProvider;
    }

    @Override
    public String getLocalizedShortName() {
        return getStringMessages().queryDefinitionProvider();
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
    public boolean hasSettings() {
        return false;
    }

    @Override
    public SettingsDialogComponent<Object> getSettingsDialogComponent() {
        return null;
    }

    @Override
    public void updateSettings(Object newSettings) { }

    @Override
    public String getDependentCssClassName() {
        return "queryDefinitionProviderWithControls";
    }

}
