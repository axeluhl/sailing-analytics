package com.sap.sailing.gwt.common.client.controls.tabbar;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView.State;

/**
 * Defines the basic Layout of tabbed pages with tabs, an optional header and a content area.
 * <p/>
 * Created by pgtaboada on 25.11.14.
 */

public class TabPanel<PLACE extends Place, PRESENTER, TABVIEW extends TabView<PLACE, PRESENTER>> extends Composite {
    private static final Logger logger = Logger.getLogger(TabPanel.class.getName());
    private static TabPanelUiBinder ourUiBinder = GWT.create(TabPanelUiBinder.class);
    private final Map<Class<PLACE>, TABVIEW> knownTabs = new LinkedHashMap<>();
    private final Map<Class<PLACE>, String> knownTabTitles = new HashMap<>();

    @UiField
    SimplePanel additionalHeader;
    @UiField
    SimplePanel tabContentPanelUi;
    
    // Should be TabBar<Class<PLACE>> but UiBinder causes weired generics errors related to the @UiHandler that would have a nested generic too. 
    @UiField
    TabBar<Class<?>> tabBar;
    @UiField FlowPanel tabExtension;
    private TABVIEW currentTab;
    

    private final PlaceHistoryMapper historyMapper;
    private final PRESENTER presenter;

    public TabPanel(PRESENTER presenter, PlaceHistoryMapper historyMapper) {
        this.presenter = presenter;
        this.historyMapper = historyMapper;
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public TABVIEW getCurrentTab() {
        return currentTab;
    }
    
    @UiChild(limit = 1)
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
    public void addTabContent(final TABVIEW tab, String title) {
        GWT.log("Adding TAB: " + title);
        
        // TODO: check if place class already known, reject...
        tab.setPresenter(presenter);
        final Class<PLACE> classForActivation = tab.getPlaceClassForActivation();
        knownTabs.put(classForActivation, tab);
        knownTabTitles.put(classForActivation, title);

        if (tab.getState() == State.VISIBLE) {
            String link = "#" + historyMapper.getToken(tab.placeToFire());
            tabBar.addTab(title, classForActivation, link);
        }
    }

    /**
     * Handles given SelectionEvent, when tab is clicked. Fires a TabPanelPlaceSelectionEvent. The listener to this
     * event should properly fire place change events.
     * 
     * @param event
     */
    @UiHandler("tabBar")
    void onTabSelection(SelectionEvent<Class<?>> event) {

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
    public void activatePlace(PLACE placeToGo) {
        if (knownTabs.containsKey(placeToGo.getClass())) {
            final TABVIEW newTab = knownTabs.get(placeToGo.getClass());
            
            if (newTab.getState() == State.NOT_AVAILABLE_REDIRECT
                    || newTab.getState() == State.NOT_AVAILABLE_SHOW_NEXT_AVAILABLE) {
                for(TabView<PLACE, PRESENTER> tab : knownTabs.values()) {
                    if(tab.getState() == State.VISIBLE) {
                        // TODO is this redirect wanted? Just silently select another tab?
                        if (currentTab != null) {
                            currentTab.stop();
                        }

                        if (newTab.getState() == State.NOT_AVAILABLE_REDIRECT) {
                            fireEvent(new TabPanelPlaceSelectionEvent(tab));
                        } else {
                            activatePlace(tab.placeToFire());
                        }
                        return;
                    }
                }
            }

            try{
                newTab.start(placeToGo, tabContentPanelUi);
            } catch(Exception e) {
                // TODO better error handling
                logger.log(Level.SEVERE, "Error while initializing Tab for place " + placeToGo.getClass().getName(), e);
            }
            @SuppressWarnings("unchecked")
            final Class<PLACE> placeClass = (Class<PLACE>) placeToGo.getClass();
            tabBar.select(placeClass);

            currentTab = newTab;

        }

    }
    
    /**
     * Directly sets the content of the current widget until it is reloaded/ reset
     * 
     * @param widget
     */
    public void overrideCurrentContentInTab(IsWidget widget) {
        tabContentPanelUi.setWidget(widget);
    }

    public HandlerRegistration addTabPanelPlaceSelectionEventHandler(TabPanelPlaceSelectionEvent.Handler handler) {
        return addHandler(handler, TabPanelPlaceSelectionEvent.TYPE);
    }

    interface TabPanelUiBinder extends UiBinder<FlowPanel, TabPanel<?, ?, ?>> {
    }

    public String getCurrentTabTitle() {
        if (currentTab == null)
            return null;
        return knownTabTitles.get(currentTab.getPlaceClassForActivation());
    }

    @UiChild
    public void addTabExtension(IsWidget widget) {
        tabExtension.add(widget);
    }
}