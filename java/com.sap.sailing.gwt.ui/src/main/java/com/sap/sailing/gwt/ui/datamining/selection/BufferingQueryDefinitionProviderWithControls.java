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
import com.sap.sailing.gwt.ui.datamining.DataMiningSettingsControl;
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
import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.AggregationProcessorDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
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
    private final FlowPanel controlsPanel;
    private final DataMiningSettingsControl settingsControl;
    
    private final ProviderListener providerListener;
    private final Collection<DataMiningComponentProvider> providers;
    private final DataRetrieverChainDefinitionProvider retrieverChainProvider;
    private final StatisticProvider statisticProvider;
    private final GroupingProvider groupingProvider;
    private final FilterSelectionProvider filterSelectionProvider;

    public BufferingQueryDefinitionProviderWithControls(DataMiningSession session, StringMessages stringMessages,
            DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter, DataMiningSettingsControl settingsControl) {
        super(stringMessages, dataMiningService, errorReporter);
        providerListener = new ProviderListener();
        queryDefinitionReleaseTimer = new Timer() {
            @Override
            public void run() {
                notifyQueryDefinitionChanged();
            }
        };
        
        // Creating the header panel, that contains the retriever chain provider and the controls
        controlsPanel = new FlowPanel();
        controlsPanel.addStyleName("definitionProviderControls");
        
        this.settingsControl = settingsControl;
        addControl(this.settingsControl.getEntryWidget());

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

        retrieverChainProvider = new SimpleDataRetrieverChainDefinitionProvider(getStringMessages(), getDataMiningService(), getErrorReporter());
        retrieverChainProvider.addDataRetrieverChainDefinitionChangedListener(providerListener);
        
        SplitLayoutPanel headerPanel = new SplitLayoutPanel(15);
        headerPanel.addWest(retrieverChainProvider.getEntryWidget(), 600);
        headerPanel.add(controlsPanel);
        
        // Creating the footer panel, that contains the statistic provider and the grouping provider
        statisticProvider = new SimpleStatisticProvider(getStringMessages(), getDataMiningService(), getErrorReporter(), retrieverChainProvider);
        statisticProvider.addStatisticChangedListener(providerListener);

        groupingProvider = new MultiDimensionalGroupingProvider(getStringMessages(), getDataMiningService(), getErrorReporter(), statisticProvider);
        groupingProvider.addGroupingChangedListener(providerListener);
        
        SplitLayoutPanel footerPanel = new SplitLayoutPanel(15);
        footerPanel.addEast(new ScrollPanel(statisticProvider.getEntryWidget()), 400);
        footerPanel.add(new ScrollPanel(groupingProvider.getEntryWidget()));
        
        // Composing the different components
        SplitLayoutPanel filterSplitPanel = new SplitLayoutPanel(15);
        filterSplitPanel.addSouth(footerPanel, footerPanelHeight);
        filterSelectionProvider = new ListRetrieverChainFilterSelectionProvider(session, stringMessages, dataMiningService, errorReporter, retrieverChainProvider);
        filterSelectionProvider.addSelectionChangedListener(providerListener);
        filterSplitPanel.add(filterSelectionProvider.getEntryWidget());
        
        mainPanel = new DockLayoutPanel(Unit.PX);
        mainPanel.addNorth(headerPanel, headerPanelHeight);
        mainPanel.add(filterSplitPanel);
        
        // Storing the different component providers in a list
        providers = new ArrayList<>();
        providers.add(retrieverChainProvider);
        providers.add(statisticProvider);
        providers.add(groupingProvider);
        providers.add(filterSelectionProvider);
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
        
        for (Entry<DataRetrieverLevelDTO, SerializableSettings> retrieverSettingsEntry : filterSelectionProvider.getRetrieverSettings().entrySet()) {
            queryDTO.setRetrieverSettings(retrieverSettingsEntry.getKey(), retrieverSettingsEntry.getValue());
        }
        
        for (Entry<DataRetrieverLevelDTO, HashMap<FunctionDTO, HashSet<? extends Serializable>>> filterSelectionEntry : filterSelectionProvider.getSelection().entrySet()) {
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
    public SettingsDialogComponent<SerializableSettings> getSettingsDialogComponent() {
        return null;
    }

    @Override
    public void updateSettings(SerializableSettings newSettings) { }

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
