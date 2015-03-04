package com.sap.sailing.gwt.common.client.controls.tabbar;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Defines whole layout for site, including the header with the breadcrumbs and tab bar, and the content.
 * <p/>
 * Created by pgtaboada on 25.11.14.
 */

public class TabPanel<PRESENTER> extends Composite {
    private static TabPanelUiBinder ourUiBinder = GWT.create(TabPanelUiBinder.class);
    private final Map<Class<Place>, TabView<Place, PRESENTER>> knownTabs = new HashMap<>();
    @UiField
    SimplePanel additionalHeader;
    @UiField
    SimplePanel tabContentPanelUi;
    @UiField
    TabBar tabBar;
    @UiField BreadcrumbPane breadcrumbs;
    private TabView<Place, PRESENTER> currentTab;
    
    private final PlaceHistoryMapper historyMapper;
    private final PRESENTER presenter;

    public TabPanel(PRESENTER presenter, PlaceHistoryMapper historyMapper) {
        this.presenter = presenter;
        this.historyMapper = historyMapper;
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public TabView<?, PRESENTER> getCurrentTab() {
        return currentTab;
    }
    
    @UiChild
    public void addHeader(Widget widget) {
        additionalHeader.setWidget(widget);
    }

    /**
     * Adds all tabs to the tab bar.
     *
     * @param tab
     *            The tab to add
     * @param title
     *            The label for the tab.
     */
    @UiChild
    public void addTabContent(final TabView<Place, PRESENTER> tab, String title) {

        GWT.log("Adding TAB: " + title);
        
        // TODO: check if place class already known, reject...
        tab.setPresenter(presenter);
        knownTabs.put(tab.getPlaceClassForActivation(), tab);

        String link = "#" + historyMapper.getToken(tab.placeToFire());
        tabBar.addTab(title, tab.getPlaceClassForActivation(), link);
    }

    /**
     * Handles given SelectionEvent, when tab is clicked. Fires a TabPanelPlaceSelectionEvent. The listener to this
     * event should properly fire place change events.
     * 
     * @param event
     */
    @UiHandler("tabBar")
    void onTabSelection(SelectionEvent<Class<Place>> event) {

        TabView<?, PRESENTER> selectedTabActivity = knownTabs.get(event.getSelectedItem());
        if (selectedTabActivity != null) {
            GWT.log("Tab selection: " + selectedTabActivity.getPlaceClassForActivation().getName());
            if (currentTab != null) {
                currentTab.stop();
            }
            fireEvent(new TabPanelPlaceSelectionEvent(selectedTabActivity));
        }

    }

    /**
     * Starts the tab Activity and selects the tab based on the given Place.
     *
     * @param placeToGo
     *            the given place.
     */
    public void activatePlace(Place placeToGo) {
        if (knownTabs.containsKey(placeToGo.getClass())) {

            final TabView<Place, PRESENTER> newTabActivity = knownTabs.get(placeToGo.getClass());

            newTabActivity.start(placeToGo, tabContentPanelUi);
            tabBar.select(placeToGo);

            currentTab = newTabActivity;

        }

    }
    
    public void addBreadcrumbItem(String title, SafeUri link, final Runnable runnable) {
        breadcrumbs.addBreadcrumbItem(title, link, runnable);
    }

    public HandlerRegistration addTabPanelPlaceSelectionEventHandler(TabPanelPlaceSelectionEvent.Handler handler) {
        return addHandler(handler, TabPanelPlaceSelectionEvent.TYPE);
    }

    interface TabPanelUiBinder extends UiBinder<FlowPanel, TabPanel<?>> {
    }

}