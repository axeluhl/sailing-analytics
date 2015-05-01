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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.datamining.DataMiningControls;
import com.sap.sailing.gwt.ui.datamining.DataMiningServiceAsync;
import com.sap.sailing.gwt.ui.datamining.DataRetrieverChainDefinitionChangedListener;
import com.sap.sailing.gwt.ui.datamining.DataRetrieverChainDefinitionProvider;
import com.sap.sailing.gwt.ui.datamining.FilterSelectionChangedListener;
import com.sap.sailing.gwt.ui.datamining.FilterSelectionProvider;
import com.sap.sailing.gwt.ui.datamining.GroupingChangedListener;
import com.sap.sailing.gwt.ui.datamining.GroupingProvider;
import com.sap.sailing.gwt.ui.datamining.StatisticChangedListener;
import com.sap.sailing.gwt.ui.datamining.StatisticProvider;
import com.sap.sailing.gwt.ui.datamining.selection.filter.ListRetrieverChainFilterSelectionProvider;
import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.components.AggregatorType;
import com.sap.sse.datamining.shared.dto.QueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.QueryDefinitionDTOImpl;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class BufferingQueryDefinitionProviderWithControls extends AbstractQueryDefinitionProvider implements DataMiningControls {

    /**
     * The delay before a changed query definition is submitted to the listeners.
     * This prevents unnecessary queries caused by a change of the used data type, that then causes
     * a change of the dimension to group by and the data retriever chain.
     * Or caused by quick changes of the filter selection.
     */
    private static final int queryBufferTimeInMillis = 200;
    
    private final ProviderListener providerListener;
    /**
     * This effects the incoming change notifications.<br>
     * If <code>true</code>, the outgoing change notification is blocked and the next provider (in
     * the hierarchy) is told to update its components. This will automatically be set to <code>false</code>
     * when the last provider notifies a change and then the listeners will be notified.<br>
     * See {@link BufferingQueryDefinitionProviderWithControls#ProviderListener} for the concrete implemenation.
     */
    private boolean isReloading;
    private final Timer queryDefinitionReleaseTimer;
    
    private final DockLayoutPanel mainPanel;
    private FlowPanel controlsPanel;
    
    private StatisticProvider statisticProvider;
    private DataRetrieverChainDefinitionProvider retrieverChainProvider;
    private GroupingProvider groupingProvider;
    private FilterSelectionProvider filterSelectionProvider;

    public BufferingQueryDefinitionProviderWithControls(DataMiningSession session, StringMessages stringMessages, DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter) {
        super(stringMessages, dataMiningService, errorReporter);
        providerListener = new ProviderListener();
        isReloading = false;
        queryDefinitionReleaseTimer = new Timer() {
            @Override
            public void run() {
                notifyQueryDefinitionChanged();
            }
        };
        
        mainPanel = new DockLayoutPanel(Unit.PX);
        mainPanel.addNorth(createFunctionsPanel(), 92);

        filterSelectionProvider = new ListRetrieverChainFilterSelectionProvider(session, stringMessages, dataMiningService, errorReporter, retrieverChainProvider);
        filterSelectionProvider.addSelectionChangedListener(providerListener);
        mainPanel.add(filterSelectionProvider.getEntryWidget());
    }

    private Widget createFunctionsPanel() {
        FlowPanel statisticAndRetrieverChainPanel = new FlowPanel();
        statisticProvider = new SimpleStatisticProvider(getStringMessages(), getDataMiningService(), getErrorReporter());
        statisticProvider.addStatisticChangedListener(providerListener);
        statisticAndRetrieverChainPanel.add(statisticProvider.getEntryWidget());
        
        retrieverChainProvider = new SimpleDataRetrieverChainDefinitionProvider(getStringMessages(), getDataMiningService(), getErrorReporter(), statisticProvider);
        retrieverChainProvider.addDataRetrieverChainDefinitionChangedListener(providerListener);
        statisticAndRetrieverChainPanel.add(retrieverChainProvider.getEntryWidget());

        groupingProvider = new MultiDimensionalGroupingProvider(getStringMessages(), getDataMiningService(), getErrorReporter(), statisticProvider);
        groupingProvider.addGroupingChangedListener(providerListener);
        ScrollPanel groupBySelectionScrollPanel = new ScrollPanel(groupingProvider.getEntryWidget());
        
        controlsPanel = new FlowPanel();
        controlsPanel.addStyleName("definitionProviderControls");

        Button clearSelectionButton = new Button(this.getStringMessages().clearSelection());
        clearSelectionButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                filterSelectionProvider.clearSelection();
            }
        });
        addControl(clearSelectionButton);
        
        Button reloadButton = new Button(getStringMessages().reload());
        reloadButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                reloadComponents();
            }
        });
        addControl(reloadButton);
        
        SplitLayoutPanel controlsSplitPanel = new SplitLayoutPanel(15);
        controlsSplitPanel.addWest(new ScrollPanel(statisticAndRetrieverChainPanel), 800);
        controlsSplitPanel.addEast(new ScrollPanel(controlsPanel), 150);
        controlsSplitPanel.add(groupBySelectionScrollPanel);
        return controlsSplitPanel;
    }
    
    private void scheduleQueryDefinitionChanged() {
        queryDefinitionReleaseTimer.schedule(queryBufferTimeInMillis);
    }
    
    @Override
    public void awaitReloadComponents() {
        // Nothing to do here
    }
    
    @Override
    public void reloadComponents() {
        isReloading = true;
        statisticProvider.awaitReloadComponents();
        groupingProvider.awaitReloadComponents();
        retrieverChainProvider.awaitReloadComponents();
        filterSelectionProvider.awaitReloadComponents();
        statisticProvider.reloadComponents();
    }
    
    @Override
    public QueryDefinitionDTO getQueryDefinition() {
        QueryDefinitionDTOImpl queryDTO = new QueryDefinitionDTOImpl(LocaleInfo.getCurrentLocale().getLocaleName(), statisticProvider.getStatisticToCalculate(),
                                                               statisticProvider.getAggregatorType(), retrieverChainProvider.getDataRetrieverChainDefinition());
        
        for (FunctionDTO dimension : groupingProvider.getDimensionsToGroupBy()) {
            queryDTO.appendDimensionToGroupBy(dimension);
        }
        
        for (Entry<Integer, Map<FunctionDTO, Collection<? extends Serializable>>> filterSelectionEntry : filterSelectionProvider.getSelection().entrySet()) {
            queryDTO.setFilterSelectionFor(filterSelectionEntry.getKey(), filterSelectionEntry.getValue());
        }
        
        return queryDTO;
    }

    @Override
    public void applyQueryDefinition(QueryDefinitionDTO queryDefinition) {
        setBlockChangeNotification(true);
        statisticProvider.applyQueryDefinition(queryDefinition);
        retrieverChainProvider.applyQueryDefinition(queryDefinition);
        groupingProvider.applyQueryDefinition(queryDefinition);
        filterSelectionProvider.applySelection(queryDefinition);
        setBlockChangeNotification(false);
        
        scheduleQueryDefinitionChanged();
    }

    @Override
    public void addControl(Widget controlWidget) {
        controlWidget.addStyleName("definitionProviderControlsElements");
        controlsPanel.add(controlWidget);
    }

    @Override
    public Widget getEntryWidget() {
        return mainPanel;
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
    public SettingsDialogComponent<AbstractSettings> getSettingsDialogComponent() {
        return null;
    }

    @Override
    public void updateSettings(AbstractSettings newSettings) { }

    @Override
    public String getDependentCssClassName() {
        return "queryDefinitionProviderWithControls";
    }
    
    private class ProviderListener implements StatisticChangedListener, DataRetrieverChainDefinitionChangedListener,
                                              GroupingChangedListener, FilterSelectionChangedListener {

        @Override
        public void statisticChanged(FunctionDTO newStatisticToCalculate, AggregatorType newAggregatorType) {
            if (isReloading) {
                groupingProvider.statisticChanged(statisticProvider.getStatisticToCalculate(), statisticProvider.getAggregatorType());
                groupingProvider.reloadComponents();
                
                retrieverChainProvider.statisticChanged(statisticProvider.getStatisticToCalculate(), statisticProvider.getAggregatorType());
                retrieverChainProvider.reloadComponents();
            } else {
                scheduleQueryDefinitionChanged();
            }
        }

        @Override
        public void groupingChanged() {
            if (!isReloading) {
                scheduleQueryDefinitionChanged();
            }
        }

        @Override
        public void dataRetrieverChainDefinitionChanged(DataRetrieverChainDefinitionDTO newDataRetrieverChainDefinition) {
            if (isReloading) {
                filterSelectionProvider.dataRetrieverChainDefinitionChanged(retrieverChainProvider.getDataRetrieverChainDefinition());
                filterSelectionProvider.reloadComponents();
            } else {
                scheduleQueryDefinitionChanged();
            }
        }

        @Override
        public void selectionChanged() {
            if (isReloading) {
                isReloading = false;
            }
            
            scheduleQueryDefinitionChanged();
        }
        
    }

}
