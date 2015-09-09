package com.sap.sailing.gwt.ui.datamining.selection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
import com.sap.sailing.gwt.ui.datamining.DataMiningComponentProvider;
import com.sap.sailing.gwt.ui.datamining.DataMiningServiceAsync;
import com.sap.sailing.gwt.ui.datamining.DataRetrieverChainDefinitionChangedListener;
import com.sap.sailing.gwt.ui.datamining.DataRetrieverChainDefinitionProvider;
import com.sap.sailing.gwt.ui.datamining.FilterSelectionChangedListener;
import com.sap.sailing.gwt.ui.datamining.FilterSelectionProvider;
import com.sap.sailing.gwt.ui.datamining.GroupingChangedListener;
import com.sap.sailing.gwt.ui.datamining.GroupingProvider;
import com.sap.sailing.gwt.ui.datamining.StatisticChangedListener;
import com.sap.sailing.gwt.ui.datamining.StatisticProvider;
import com.sap.sailing.gwt.ui.datamining.WithControls;
import com.sap.sailing.gwt.ui.datamining.selection.filter.ListRetrieverChainFilterSelectionProvider;
import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.AggregationProcessorDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.ModifiableStatisticQueryDefinitionDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class BufferingQueryDefinitionProviderWithControls extends AbstractQueryDefinitionProvider implements WithControls {

    /**
     * The delay before a changed query definition is submitted to the listeners.
     * This prevents unnecessary queries caused by a change of the used data type, that then causes
     * a change of the dimension to group by and the data retriever chain.
     * Or caused by quick changes of the filter selection.
     */
    private static final int queryBufferTimeInMillis = 200;

    private static final double headerPanelHeight = 45;
    private static final double footerPanelHeight = 50;
    
    private final Timer queryDefinitionReleaseTimer;
    
    private final DockLayoutPanel mainPanel;
    private FlowPanel controlsPanel;
    
    private final ProviderListener providerListener;
    private final Collection<DataMiningComponentProvider> providers;
    private DataRetrieverChainDefinitionProvider retrieverChainProvider;
    private StatisticProvider statisticProvider;
    private GroupingProvider groupingProvider;
    private FilterSelectionProvider filterSelectionProvider;

    public BufferingQueryDefinitionProviderWithControls(DataMiningSession session, StringMessages stringMessages, DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter) {
        super(stringMessages, dataMiningService, errorReporter);
        providerListener = new ProviderListener();
        queryDefinitionReleaseTimer = new Timer() {
            @Override
            public void run() {
                notifyQueryDefinitionChanged();
            }
        };
        
        // The header panel has to be created before the footer panel, because the component provider in the footer panel depend on
        // the retriever chain provider. To be more exact: The retriever chain provider mustn't be null, when createFooterPanel() is called.
        Widget headerPanel = createHeaderPanel();
        
        SplitLayoutPanel filterSplitPanel = new SplitLayoutPanel(15);
        filterSplitPanel.addSouth(createFooterPanel(), footerPanelHeight);
        filterSelectionProvider = new ListRetrieverChainFilterSelectionProvider(session, stringMessages, dataMiningService, errorReporter, retrieverChainProvider);
        filterSelectionProvider.addSelectionChangedListener(providerListener);
        filterSplitPanel.add(filterSelectionProvider.getEntryWidget());
        
        mainPanel = new DockLayoutPanel(Unit.PX);
        mainPanel.addNorth(headerPanel, headerPanelHeight);
        mainPanel.add(filterSplitPanel);
        
        providers = new ArrayList<>();
        providers.add(retrieverChainProvider);
        providers.add(statisticProvider);
        providers.add(groupingProvider);
        providers.add(filterSelectionProvider);
    }

    private Widget createHeaderPanel() {
        retrieverChainProvider = new SimpleDataRetrieverChainDefinitionProvider(getStringMessages(), getDataMiningService(), getErrorReporter());
        retrieverChainProvider.addDataRetrieverChainDefinitionChangedListener(providerListener);
        
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
        
        SplitLayoutPanel headerSplitPanel = new SplitLayoutPanel(15);
        headerSplitPanel.addWest(retrieverChainProvider.getEntryWidget(), 600);
        headerSplitPanel.add(controlsPanel);
        return headerSplitPanel;
    }

    private Widget createFooterPanel() {
        statisticProvider = new SimpleStatisticProvider(getStringMessages(), getDataMiningService(), getErrorReporter(), retrieverChainProvider);
        statisticProvider.addStatisticChangedListener(providerListener);

        groupingProvider = new MultiDimensionalGroupingProvider(getStringMessages(), getDataMiningService(), getErrorReporter(), statisticProvider);
        groupingProvider.addGroupingChangedListener(providerListener);
        
        SplitLayoutPanel controlsSplitPanel = new SplitLayoutPanel(15);
        controlsSplitPanel.addEast(new ScrollPanel(statisticProvider.getEntryWidget()), 400);
        controlsSplitPanel.add(new ScrollPanel(groupingProvider.getEntryWidget()));
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
    public boolean isAwatingReload() {
        for (DataMiningComponentProvider provider : providers) {
            if (provider.isAwatingReload()) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void reloadComponents() {
        retrieverChainProvider.awaitReloadComponents();
        statisticProvider.awaitReloadComponents();
        groupingProvider.awaitReloadComponents();
        filterSelectionProvider.awaitReloadComponents();
        retrieverChainProvider.reloadComponents();
    }
    
    @Override
    public StatisticQueryDefinitionDTO getQueryDefinition() {
        ModifiableStatisticQueryDefinitionDTO queryDTO = new ModifiableStatisticQueryDefinitionDTO(LocaleInfo.getCurrentLocale().getLocaleName(), statisticProvider.getStatisticToCalculate(),
                                                               statisticProvider.getAggregatorDefinition(), retrieverChainProvider.getDataRetrieverChainDefinition());
        
        for (FunctionDTO dimension : groupingProvider.getDimensionsToGroupBy()) {
            queryDTO.appendDimensionToGroupBy(dimension);
        }
        
        for (Entry<Integer, HashMap<FunctionDTO, HashSet<? extends Serializable>>> filterSelectionEntry : filterSelectionProvider.getSelection().entrySet()) {
            queryDTO.setFilterSelectionFor(filterSelectionEntry.getKey(), filterSelectionEntry.getValue());
        }
        
        return queryDTO;
    }

    @Override
    public void applyQueryDefinition(StatisticQueryDefinitionDTO queryDefinition) {
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
        public void statisticChanged(FunctionDTO newStatisticToCalculate, AggregationProcessorDefinitionDTO newAggregatorDefinition) {
            if (isAwatingReload()) {
                groupingProvider.statisticChanged(statisticProvider.getStatisticToCalculate(), statisticProvider.getAggregatorDefinition());
                groupingProvider.reloadComponents();
            } else {
                scheduleQueryDefinitionChanged();
            }
        }

        @Override
        public void groupingChanged() {
            if (!isAwatingReload()) {
                scheduleQueryDefinitionChanged();
            }
        }

        @Override
        public void dataRetrieverChainDefinitionChanged(DataRetrieverChainDefinitionDTO newDataRetrieverChainDefinition) {
            if (isAwatingReload()) {
                statisticProvider.dataRetrieverChainDefinitionChanged(retrieverChainProvider.getDataRetrieverChainDefinition());
                statisticProvider.reloadComponents();
                
                filterSelectionProvider.dataRetrieverChainDefinitionChanged(retrieverChainProvider.getDataRetrieverChainDefinition());
                filterSelectionProvider.reloadComponents();
            } else {
                scheduleQueryDefinitionChanged();
            }
        }

        @Override
        public void selectionChanged() {
            if (!isAwatingReload()) {
                scheduleQueryDefinitionChanged();
            }
        }
        
    }

}
