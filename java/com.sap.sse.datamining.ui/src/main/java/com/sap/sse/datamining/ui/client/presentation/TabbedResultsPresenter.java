package com.sap.sse.datamining.ui.client.presentation;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.datamining.ui.client.AbstractDataMiningComponent;
import com.sap.sse.datamining.ui.client.CompositeResultsPresenter;
import com.sap.sse.datamining.ui.client.ResultsPresenter;
import com.sap.sse.datamining.ui.client.presentation.ResultsChart.DrillDownCallback;
import com.sap.sse.datamining.ui.client.resources.DataMiningResources;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.controls.ScrolledTabLayoutPanel;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class TabbedResultsPresenter extends AbstractDataMiningComponent<Settings>
        implements CompositeResultsPresenter<Settings> {

    protected static final DataMiningResources resources = GWT.create(DataMiningResources.class);
    private static final String IdPrefix = "Tab";
    
    private final AtomicInteger idCounter;
    protected final ScrolledTabLayoutPanel tabPanel;
    protected final Map<String, CloseablePresenterTab> tabsMappedById;
    protected final DrillDownCallback drillDownCallback;
    protected final Map<String, ResultsPresenter<Settings>> registeredResultPresenterMap;
    private final Set<CurrentPresenterChangedListener> listeners;

    public TabbedResultsPresenter(Component<?> parent, ComponentContext<?> context, DrillDownCallback drillDownCallback) {
        super(parent, context);
        idCounter = new AtomicInteger();
        tabPanel = new ScrolledTabLayoutPanel(30, Unit.PX, resources.arrowLeftIcon(), resources.arrowRightIcon());
        tabPanel.setAnimationDuration(0);
        tabsMappedById = new HashMap<>();
        this.drillDownCallback = drillDownCallback;
        registeredResultPresenterMap = new HashMap<>();
        listeners = new HashSet<>();

        addNewTabTab();
        addTabAndFocus(new MultiResultsPresenter(this, context, drillDownCallback));
        
        tabPanel.addSelectionHandler(event -> {
            String presenterId = ((CloseablePresenterTab) tabPanel.getTabWidget(event.getSelectedItem())).getId();
            for (CurrentPresenterChangedListener listener : listeners) {
                listener.currentPresenterChanged(presenterId);
            }
        });
    }

    private void addNewTabTab() {
        Label widget = new Label("This should never be shown");
        FlowPanel header = new FlowPanel();
        header.addStyleName("resultsPresenterTabHeader");
        header.add(new Image(resources.plusIcon()));
        tabPanel.add(widget, header);
        // This is necessary to stop the selection of this pseudo tab
        tabPanel.addBeforeSelectionHandler(event -> {
            if (event.getItem() == tabPanel.getWidgetCount() - 1) {
                event.cancel();
                addTabAndFocus(new MultiResultsPresenter(TabbedResultsPresenter.this, getComponentContext(),
                        drillDownCallback));
            }
        });
    }

    @Override
    public String getCurrentPresenterId() {
        return getSelectedTab().getId();
    }

    @Override
    public Iterable<String> getPresenterIds() {
        Set<String> idSet = new HashSet<>(tabsMappedById.keySet());
        return Collections.unmodifiableSet(idSet);
    }
    
    @Override
    public boolean containsPresenter(String presenterId) {
        return tabsMappedById.containsKey(presenterId);
    }
    
    @Override
    public QueryResultDTO<?> getResult(String presenterId) {
        CloseablePresenterTab tab = getTab(presenterId);
        return tab != null ?  tab.getPresenter().getCurrentResult() : null;
    }
    
    @Override
    public StatisticQueryDefinitionDTO getQueryDefinition(String presenterId) {
        CloseablePresenterTab tab = getTab(presenterId);
        return tab != null ?  tab.getPresenter().getCurrentQueryDefinition() : null;
    }

    @Override
    public void showResult(String presenterId, StatisticQueryDefinitionDTO queryDefinition, QueryResultDTO<?> result) {
        CloseablePresenterTab oldTab = getTab(presenterId);
        if (oldTab == null) {
            return;
        }
        
        try {
            if (result != null) {
                if (registeredResultPresenterMap.containsKey(result.getResultType())) {
                    presenterId = addTabAndFocus(registeredResultPresenterMap.get(result.getResultType()));
                    removeTab(oldTab);
                } else {
                    if (!(oldTab.getPresenter() instanceof MultiResultsPresenter)) {
                        presenterId = addTabAndFocus(new MultiResultsPresenter(this, getComponentContext(), drillDownCallback));
                        removeTab(oldTab);
                    }
                }
                getTab(presenterId).setText(result.getResultSignifier());
            }
        } finally {
            getTab(presenterId).getPresenter().showResult(queryDefinition, result);
        }
    }

    @Override
    public void showError(String presenterId, String error) {
        CloseablePresenterTab tab = getTab(presenterId);
        if (tab == null) {
            return;
        }
        
        tab.setText(getDataMiningStringMessages().error());
        tab.getPresenter().showError(error);
    }

    @Override
    public void showError(String presenterId, String mainError, Iterable<String> detailedErrors) {
        CloseablePresenterTab tab = getTab(presenterId);
        if (tab == null) {
            return;
        }
        
        tab.setText(getDataMiningStringMessages().error());
        tab.getPresenter().showError(mainError, detailedErrors);
    }

    @Override
    public void showBusyIndicator(String presenterId) {
        CloseablePresenterTab tab = getTab(presenterId);
        if (tab == null) {
            return;
        }
        
        tab.setText(getDataMiningStringMessages().runningQuery());
        tab.getPresenter().showBusyIndicator();
    }

    @Override
    public void addCurrentPresenterChangedListener(CurrentPresenterChangedListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeCurrentPresenterChangedListener(CurrentPresenterChangedListener listener) {
        listeners.remove(listener);
    }

    /**
     * Register a {@link ResultsPresenter} to handle the retrieved {@link resultType}. Each {@link resultType} can only
     * be registered once. Multiple registrations will cause an {@link IllegalStateException}. When
     * {@link #showResult(QueryResultDTO)} executes it checks whether the {@link resultType} was registered before and
     * triggers the corresponding {@link ResultsPresenter}.
     * 
     * @param resultType
     *            of the datamining query.
     * @param resultPresenter
     *            which shall be used to handle the datamaining query result.
     * 
     * @throws IllegalStateException
     *             if the {@link resultType} is already registered.
     */
    public void registerResultsPresenter(Class<?> resultType, ResultsPresenter<Settings> resultPresenter)
            throws IllegalStateException {
        String className = resultType.getName();
        if (!registeredResultPresenterMap.containsKey(className)) {
            registeredResultPresenterMap.put(className, resultPresenter);
        } else {
            throw new IllegalStateException(
                    "Multiple registration for result type key: " + resultType.toString() + "not allowed.");
        }
    }
    
    protected CloseablePresenterTab getSelectedTab() {
        return (CloseablePresenterTab) tabPanel.getTabWidget(tabPanel.getSelectedIndex());
    }
    
    protected CloseablePresenterTab getTab(String id) {
        return tabsMappedById.get(id);
    }

    protected String addTabAndFocus(ResultsPresenter<?> tabPresenter) {
        String tabId = IdPrefix + idCounter.getAndIncrement();
        CloseablePresenterTab tabHeader = new CloseablePresenterTab(tabId, tabPresenter);
        tabsMappedById.put(tabId, tabHeader);

        tabPanel.insert(tabPresenter.getEntryWidget(), tabHeader, tabPanel.getWidgetCount() - 1);
        int presenterIndex = tabPanel.getWidgetIndex(tabPresenter.getEntryWidget());
        tabPanel.selectTab(presenterIndex);
        tabPanel.scrollToTab(presenterIndex);
        return tabId;
    }

    protected void removeTab(CloseablePresenterTab tab) {
        tab.removeFromParent();
        tabsMappedById.remove(tab.getId());
    }

    @Override
    public String getLocalizedShortName() {
        return getDataMiningStringMessages().tabbedResultsPresenter();
    }

    @Override
    public Widget getEntryWidget() {
        return tabPanel;
    }

    @Override
    public boolean isVisible() {
        return tabPanel.isVisible();
    }

    @Override
    public void setVisible(boolean visibility) {
        tabPanel.setVisible(visibility);
    }

    @Override
    public boolean hasSettings() {
        return false;
    }

    @Override
    public SettingsDialogComponent<Settings> getSettingsDialogComponent(Settings settings) {
        return null;
    }

    @Override
    public void updateSettings(Settings newSettings) {
        // no-op
    }

    @Override
    public Settings getSettings() {
        return null;
    }

    @Override
    public String getId() {
        return "TabbedResultsPresenter";
    }

    @Override
    public String getDependentCssClassName() {
        return "tabbedResultsPresenters";
    }

    private class CloseablePresenterTab extends FlowPanel {

        private final String id;
        private final Label headerLabel;
        private final ResultsPresenter<?> presenter;

        public CloseablePresenterTab(String id, ResultsPresenter<?> presenter) {
            this.id = id;
            this.presenter = presenter;
            this.addStyleName("resultsPresenterTabHeader");
            
            headerLabel = new Label(getDataMiningStringMessages().empty());
            this.add(headerLabel);
            Image closeImage = new Image(resources.closeIcon());
            closeImage.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (tabPanel.getWidgetCount() > 2) {
                        removeTab(CloseablePresenterTab.this);
                    }
                }
            });
            this.add(closeImage);
        }
        
        public String getId() {
            return id;
        }
        
        public ResultsPresenter<?> getPresenter() {
            return presenter;
        }

        public void setText(String text) {
            headerLabel.setText(text);
            tabPanel.checkIfScrollButtonsNecessary();
        }

    }
}
