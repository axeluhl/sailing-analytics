package com.sap.sse.datamining.ui.client.presentation;

import java.util.ArrayList;
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
import com.sap.sse.common.Util.Pair;
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
    
    /**
     * Used as a prefix for the ID strings of the presenter tabs which are then used as keys in the
     * {@link #tabsMappedById} map
     */
    private static final String IdPrefix = "Tab";
    
    /**
     * Counts the presenter tabs and is used in the construction of presenter tab IDs which then become keys in
     * {@link #tabsMappedById}.
     */
    private final AtomicInteger idCounter;
    private final ScrolledTabLayoutPanel tabPanel;
    
    /**
     * Presenter tabs, keyed by strings that are constructed from the {@link #IdPrefix} and the {@link #idCounter}
     */
    private final Map<String, CloseablePresenterTab> tabsMappedById;
    private final DrillDownCallback drillDownCallback;
    private final Map<String, ResultsPresenterFactory<?>> registeredPresenterFactories;
    private final ResultsPresenterFactory<MultiResultsPresenter> defaultFactory;
    private final Set<CurrentPresenterChangedListener> currentPresenterChangedListeners;
    private final Set<PresenterRemovedListener> presenterRemovedListeners;
    
    public TabbedResultsPresenter(Component<?> parent, ComponentContext<?> context, DrillDownCallback drillDownCallback) {
        super(parent, context);
        idCounter = new AtomicInteger();
        tabPanel = new ScrolledTabLayoutPanel(30, Unit.PX, resources.arrowLeftIcon(), resources.arrowRightIcon());
        tabPanel.setAnimationDuration(0);
        tabsMappedById = new HashMap<>();
        this.drillDownCallback = drillDownCallback;
        registeredPresenterFactories = new HashMap<>();
        defaultFactory = new ResultsPresenterFactory<>(MultiResultsPresenter.class,
                () -> new MultiResultsPresenter(this, getComponentContext(), drillDownCallback));
        currentPresenterChangedListeners = new HashSet<>();
        presenterRemovedListeners = new HashSet<>();
        addNewTab();
        addTabAndFocus(new MultiResultsPresenter(this, context, drillDownCallback));
        tabPanel.addSelectionHandler(event -> {
            final String presenterId = ((CloseablePresenterTab) tabPanel.getTabWidget(event.getSelectedItem())).getId();
            for (final CurrentPresenterChangedListener listener : currentPresenterChangedListeners) {
                listener.currentPresenterChanged(presenterId);
            }
        });
    }

    /**
     * Adds the virtual tab with the "+" symbol that allows a user to add a new panel to this tabbed results presenter.
     * When "selected," the selecting event is cancelled and instead the {@link #addTabAndFocus(ResultsPresenter)}
     * method is invoked, adding a "real" panel just before the virtual "+" panel.
     */
    private void addNewTab() {
        final Label widget = new Label("This should never be shown");
        final FlowPanel header = new FlowPanel();
        header.addStyleName("resultsPresenterTabHeader");
        header.add(new Image(resources.plusIcon()));
        tabPanel.add(widget, header);
        // This is necessary to stop the selection of this pseudo tabe 
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
    public int getCurrentPresenterIndex() {
        return tabPanel.getSelectedIndex();
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
    public int getPresenterIndex(String presenterId) {
        CloseablePresenterTab tab = getTab(presenterId);
        return tab != null ? tabPanel.getWidgetIndex(tab) : -1;
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
        CloseablePresenterTab presenterTab = getTab(presenterId);
        if (presenterTab != null) {
            if (result != null) {
                ResultsPresenter<?> presenter = presenterTab.getPresenter();
                ResultsPresenterFactory<?> factory = registeredPresenterFactories.getOrDefault(result.getResultType(), defaultFactory);
                if (presenter.getClass() != factory.getProducedType()) {
                    CloseablePresenterTab newPresenterTab = addTabAndFocus(factory.createPresenter());
                    removeTab(presenterTab);
                    presenterTab = newPresenterTab;
                }
                presenterTab.setText(result.getResultSignifier());
            } else {
                presenterTab.setText(getDataMiningStringMessages().empty());
            }
            presenterTab.getPresenter().showResult(queryDefinition, result);
        }
    }
    
    @Override
    public void showResults(Iterable<Pair<StatisticQueryDefinitionDTO, QueryResultDTO<?>>> results) {
        // TODO bug4789: should we check for changes in the tabs before removing them?
        new ArrayList<>(tabsMappedById.keySet()).stream().map(tabsMappedById::get).forEach(this::removeTab);
        for (final Pair<StatisticQueryDefinitionDTO, QueryResultDTO<?>> entry : results) {
            final StatisticQueryDefinitionDTO queryDefinition = entry.getA();
            final QueryResultDTO<?> result = entry.getB();
            final ResultsPresenterFactory<?> factory = registeredPresenterFactories.getOrDefault(result.getResultType(), defaultFactory);
            final CloseablePresenterTab presenterTab = addTabAndFocus(factory.createPresenter());
            presenterTab.setText(result.getResultSignifier());
            presenterTab.getPresenter().showResult(queryDefinition, result);
        }
        // Needed to remove the remaining empty tab
        for (final CloseablePresenterTab tab : tabsMappedById.values()) {
            if (tabPanel.getWidgetIndex(tab.getPresenter().getEntryWidget()) == 0) {
                removeTab(tab);
                break;
            }
        }
        tabPanel.selectTab(0); // this will also fire the selection event, so the query provider will update according to the query
    }
    
    @Override
    public void showError(String presenterId, String error) {
        CloseablePresenterTab tab = getTab(presenterId);
        if (tab != null) {
            tab.setText(getDataMiningStringMessages().error());
            tab.getPresenter().showError(error);
        }
    }

    @Override
    public void showError(String presenterId, String mainError, Iterable<String> detailedErrors) {
        CloseablePresenterTab tab = getTab(presenterId);
        if (tab != null) {
            tab.setText(getDataMiningStringMessages().error());
            tab.getPresenter().showError(mainError, detailedErrors);
        }
    }

    @Override
    public void showBusyIndicator(String presenterId) {
        CloseablePresenterTab tab = getTab(presenterId);
        if (tab != null) {
            tab.setText(getDataMiningStringMessages().runningQuery());
            tab.getPresenter().showBusyIndicator();
        }
    }

    @Override
    public void addCurrentPresenterChangedListener(CurrentPresenterChangedListener listener) {
        currentPresenterChangedListeners.add(listener);
    }

    @Override
    public void removeCurrentPresenterChangedListener(CurrentPresenterChangedListener listener) {
        currentPresenterChangedListeners.remove(listener);
    }

    @Override
    public void addPresenterRemovedListener(PresenterRemovedListener listener) {
        presenterRemovedListeners.add(listener);
    }
    
    @Override
    public void removePresenterRemovedListener(PresenterRemovedListener listener) {
        presenterRemovedListeners.remove(listener);
    }

    /**
     * Register a {@link ResultsPresenterFactory} that creates {@link ResultsPresenter} to handle the retrieved
     * {@link resultType}. Each {@link resultType} can only be registered once. Multiple registrations will cause an
     * {@link IllegalStateException}. When {@link #showResult(QueryResultDTO)} executes it checks whether the
     * {@link resultType} was registered before and triggers the corresponding {@link ResultsPresenterFactory}.
     * 
     * @param resultType
     *            of the data mining query.
     * @param presenterFactory
     *            creates {@link ResultsPresenter} which shall be used to handle the data mining query result.
     * 
     * @throws IllegalStateException
     *             if the {@link resultType} is already registered.
     */
    public void registerResultsPresenter(Class<?> resultType, ResultsPresenterFactory<?> presenterFactory)
            throws IllegalStateException {
        String className = resultType.getName();
        if (!registeredPresenterFactories.containsKey(className)) {
            registeredPresenterFactories.put(className, presenterFactory);
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

    protected CloseablePresenterTab addTabAndFocus(ResultsPresenter<?> presenter) {
        String tabId = IdPrefix + idCounter.getAndIncrement();
        CloseablePresenterTab presenterTab = new CloseablePresenterTab(tabId, presenter);
        tabsMappedById.put(tabId, presenterTab);
        tabPanel.insert(presenter.getEntryWidget(), presenterTab, tabPanel.getWidgetCount() - 1);
        int presenterIndex = tabPanel.getWidgetIndex(presenter.getEntryWidget());
        tabPanel.selectTab(presenterIndex);
        tabPanel.scrollToTab(presenterIndex);
        return presenterTab;
    }

    protected void removeTab(CloseablePresenterTab tab) {
        String presenterId = tab.getId();
        int index = getPresenterIndex(presenterId);
        tab.removeFromParent();
        tabsMappedById.remove(presenterId);
        this.presenterRemovedListeners.forEach(l -> l.onPresenterRemoved(presenterId, index, tab.getPresenter().getCurrentQueryDefinition()));
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
    
    @FunctionalInterface
    public static interface ResultsPresenterSupplier<T extends ResultsPresenter<?>> {
        T create();
    }
    
    public static class ResultsPresenterFactory<T extends ResultsPresenter<?>> {
        private final Class<T> presenterType;
        private final ResultsPresenterSupplier<T> presenterSupplier;
        
        public ResultsPresenterFactory(Class<T> presenterType, ResultsPresenterSupplier<T> presenterSupplier) {
            this.presenterType = presenterType;
            this.presenterSupplier = presenterSupplier;
        }

        public Class<T> getProducedType() {
            return presenterType;
        }
        
        public T createPresenter() {
            return presenterSupplier.create();
        }
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
