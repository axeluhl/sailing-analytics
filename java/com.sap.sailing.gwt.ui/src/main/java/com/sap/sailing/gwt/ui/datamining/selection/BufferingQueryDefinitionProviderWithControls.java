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
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.datamining.DataMiningControls;
import com.sap.sailing.gwt.ui.datamining.DataMiningServiceAsync;
import com.sap.sailing.gwt.ui.datamining.DataRetrieverChainDefinitionChangedListener;
import com.sap.sailing.gwt.ui.datamining.DataRetrieverChainDefinitionProvider;
import com.sap.sailing.gwt.ui.datamining.FilterSelectionProvider;
import com.sap.sailing.gwt.ui.datamining.GroupingChangedListener;
import com.sap.sailing.gwt.ui.datamining.SelectionChangedListener;
import com.sap.sailing.gwt.ui.datamining.StatisticChangedListener;
import com.sap.sailing.gwt.ui.datamining.StatisticProvider;
import com.sap.sse.common.settings.Settings;
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
    
    private final Timer queryDefinitionReleaseTimer;
    private boolean isUpdatingComponents;
    
    private final DockLayoutPanel mainPanel;
    private FlowPanel controlsPanel;
    
    private StatisticProvider statisticProvider;
    private DataRetrieverChainDefinitionProvider retrieverChainProvider;
    private MultiDimensionalGroupingProvider groupBySelectionPanel;
    private FilterSelectionProvider selectionProvider;

    public BufferingQueryDefinitionProviderWithControls(DataMiningSession session, StringMessages stringMessages, SailingServiceAsync sailingService, DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter) {
        super(stringMessages, sailingService, dataMiningService, errorReporter);
        queryDefinitionReleaseTimer = new Timer() {
            @Override
            public void run() {
                notifyQueryDefinitionChanged();
            }
        };
        isUpdatingComponents = false;
        
        mainPanel = new DockLayoutPanel(Unit.PX);
        mainPanel.addNorth(createFunctionsPanel(), 92);

        selectionProvider = new ListRetrieverChainFilterSelectionProvider(session, stringMessages, dataMiningService, errorReporter, retrieverChainProvider);
        selectionProvider.addSelectionChangedListener(new SelectionChangedListener() {
            @Override
            public void selectionChanged() {
                scheduleQueryDefinitionChanged();
            }
        });
        mainPanel.add(selectionProvider.getEntryWidget());
    }

    private Widget createFunctionsPanel() {
        FlowPanel statisticAndRetrieverChainPanel = new FlowPanel();
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

        groupBySelectionPanel = new MultiDimensionalGroupingProvider(getStringMessages(), getDataMiningService(), getErrorReporter(), statisticProvider);
        groupBySelectionPanel.addGroupingChangedListener(new GroupingChangedListener() {
            @Override
            public void groupingChanged() {
                scheduleQueryDefinitionChanged();
            }
        });
        ScrollPanel groupBySelectionScrollPanel = new ScrollPanel(groupBySelectionPanel.getEntryWidget());
        
        controlsPanel = new FlowPanel();
        controlsPanel.addStyleName("definitionProviderControls");

        Button clearSelectionButton = new Button(this.getStringMessages().clearSelection());
        clearSelectionButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                selectionProvider.clearSelection();
            }
        });
        addControl(clearSelectionButton);
        
        Button reloadButton = new Button(getStringMessages().reload());
        reloadButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                //FIXME This is just a quick fix for bug 2176
                isUpdatingComponents = true;
                // This causes all other providers to update their components, if the selected
                // statistic changed. If not, will the other providers update their components,
                // when another statistic is selected.
                // This works because, all other providers listen to the statistic provider in
                // the current implementation.
                statisticProvider.updateExtractionFunctions();
            }
        });
        addControl(reloadButton);
        
        SplitLayoutPanel controlsSplitPanel = new SplitLayoutPanel(15);
        controlsSplitPanel.addWest(new ScrollPanel(statisticAndRetrieverChainPanel), 800);
        controlsSplitPanel.addEast(new ScrollPanel(controlsPanel), 150);
        controlsSplitPanel.add(groupBySelectionScrollPanel);
        return controlsSplitPanel;
    }
    
    //FIXME This is just a quick fix for bug 2176
    @Override
    protected void notifyQueryDefinitionChanged() {
        if (isUpdatingComponents) {
            // This ensures, that the retriever chains are reloaded. This is necessary, because
            // they have UUIDs, that change after a bundle refresh.
            retrieverChainProvider.updateRetrieverChains();
            isUpdatingComponents = false;
        } else {
            super.notifyQueryDefinitionChanged();
        }
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
        
        for (Entry<Integer, Map<FunctionDTO, Collection<? extends Serializable>>> filterSelectionEntry : selectionProvider.getSelection().entrySet()) {
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
    public SettingsDialogComponent<Settings> getSettingsDialogComponent() {
        return null;
    }

    @Override
    public void updateSettings(Settings newSettings) { }

    @Override
    public String getDependentCssClassName() {
        return "queryDefinitionProviderWithControls";
    }

}
