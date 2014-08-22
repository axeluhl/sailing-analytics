package com.sap.sailing.gwt.ui.datamining.selection;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map.Entry;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.datamining.shared.DimensionIdentifier;
import com.sap.sailing.datamining.shared.QueryDefinitionDeprecated;
import com.sap.sailing.datamining.shared.impl.QueryDefinitionDeprecatedImpl;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;
import com.sap.sailing.gwt.ui.client.shared.panels.ResizingFlowPanel;
import com.sap.sailing.gwt.ui.datamining.DataMiningControls;
import com.sap.sailing.gwt.ui.datamining.GroupingChangedListener;
import com.sap.sailing.gwt.ui.datamining.GroupingProvider;
import com.sap.sailing.gwt.ui.datamining.SelectionChangedListener;
import com.sap.sailing.gwt.ui.datamining.SelectionProvider;
import com.sap.sailing.gwt.ui.datamining.StatisticChangedListener;
import com.sap.sailing.gwt.ui.datamining.StatisticProvider;

public class QueryDefinitionProviderWithControls extends AbstractQueryDefinitionProvider implements DataMiningControls {

    private FlowPanel mainPanel;
    private HorizontalPanel controlsPanel;

    private SelectionProvider<?> selectionProvider;
    
    private StatisticProvider statisticProvider;

    private GroupingProvider groupBySelectionPanel;

    public QueryDefinitionProviderWithControls(StringMessages stringMessages, SailingServiceAsync sailingService, ErrorReporter errorReporter) {
        super(stringMessages, sailingService, errorReporter);
        
        mainPanel = new ResizingFlowPanel() {
            @Override
            public void onResize() {
                Widget selectionWidget = selectionProvider.getEntryWidget();
                if (selectionWidget instanceof RequiresResize) {
                    ((RequiresResize) selectionWidget).onResize();
                }
            }
        };

        mainPanel.add(createFunctionsPanel());

        selectionProvider = new RefreshingSelectionTablesPanel(stringMessages, sailingService, errorReporter);
        selectionProvider.addSelectionChangedListener(new SelectionChangedListener() {
            @Override
            public void selectionChanged() {
                notifyQueryDefinitionChanged();
            }
        });
        mainPanel.add(selectionProvider.getEntryWidget());
        
    }
    
    @Override
    public QueryDefinitionDeprecated getQueryDefinition() {
        QueryDefinitionDeprecatedImpl queryDTO = new QueryDefinitionDeprecatedImpl(LocaleInfo.getCurrentLocale().getLocaleName(), groupBySelectionPanel.getGrouperType(),
                                                                   statisticProvider.getStatisticType(), statisticProvider.getAggregatorType(), 
                                                                   statisticProvider.getDataType());
        
        switch (queryDTO.getGrouperType()) {
        case Dimensions:
        default:
            for (DimensionIdentifier dimension : groupBySelectionPanel.getDimensionsToGroupBy()) {
                queryDTO.appendDimensionToGroupBy(dimension);
            }
            break;
        }
        
        for (Entry<DimensionIdentifier, Collection<? extends Serializable>> selectionEntry : selectionProvider.getSelection().entrySet()) {
            queryDTO.setSelectionFor(selectionEntry.getKey(), selectionEntry.getValue());
        }
        
        return queryDTO;
    }

    @Override
    public void applyQueryDefinition(QueryDefinitionDeprecated queryDefinition) {
        setBlockChangeNotification(true);
        selectionProvider.applySelection(queryDefinition);
        groupBySelectionPanel.applyQueryDefinition(queryDefinition);
        statisticProvider.applyQueryDefinition(queryDefinition);
        setBlockChangeNotification(false);
        
        notifyQueryDefinitionChanged();
    }

    private Widget createFunctionsPanel() {
        HorizontalPanel functionsPanel = new HorizontalPanel();
        functionsPanel.setSpacing(5);

        statisticProvider = new ComplexStatisticProvider(getStringMessages(), SimpleStatisticsManager.createManagerWithStandardStatistics());
        statisticProvider.addStatisticChangedListener(new StatisticChangedListener() {
            @Override
            public void statisticChanged(SimpleStatistic newStatistic) {
                notifyQueryDefinitionChanged();
            }
        });
        functionsPanel.add(statisticProvider.getEntryWidget());

        groupBySelectionPanel = new RestrictedGroupingProvider(getStringMessages());
        groupBySelectionPanel.addGroupingChangedListener(new GroupingChangedListener() {
            @Override
            public void groupingChanged() {
                notifyQueryDefinitionChanged();
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
