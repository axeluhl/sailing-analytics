package com.sap.sailing.gwt.home.mobile.partials.solutions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.desktop.places.whatsnew.WhatsNewPlace.WhatsNewNavigationTabs;
import com.sap.sailing.gwt.home.mobile.app.MobilePlacesNavigator;
import com.sap.sailing.gwt.home.shared.utils.CollapseAnimation;

public class Solutions extends Composite {

    interface MyUiBinder extends UiBinder<Widget, Solutions> {
    }
    
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiField DivElement sapInSailingDiv;
    @UiField DivElement sapSailingAnalyticsUi;
    @UiField DivElement raceCommitteeAppUi;
    @UiField DivElement inSightAppUi;
    @UiField DivElement buoyPingerAppUi;
    @UiField DivElement postRaceAnalyticsUi;
    @UiField DivElement strategySimulatorUi;
    
    @UiField AnchorElement sailingAnalyticsDetailsAnchor;
    @UiField AnchorElement raceManagerAppDetailsAnchor;
    @UiField AnchorElement sailInSightAppDetailsAnchor;
    @UiField AnchorElement buoyPingerAppDetailsAnchor;
    @UiField AnchorElement simulatorAppDetailsAnchor;
    
    public Solutions(MobilePlacesNavigator placesNavigator) {
        SolutionsResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        initWhatsNewLink(placesNavigator, WhatsNewNavigationTabs.SailingAnalytics, sailingAnalyticsDetailsAnchor);
        initWhatsNewLink(placesNavigator, WhatsNewNavigationTabs.RaceManagerApp, raceManagerAppDetailsAnchor);
        initWhatsNewLink(placesNavigator, WhatsNewNavigationTabs.InSightApp, sailInSightAppDetailsAnchor);
        initWhatsNewLink(placesNavigator, WhatsNewNavigationTabs.BuoyPingerApp, buoyPingerAppDetailsAnchor);
        initWhatsNewLink(placesNavigator, WhatsNewNavigationTabs.SailingSimulator, simulatorAppDetailsAnchor);
        
        initAnimation(sapInSailingDiv, true);
        initAnimation(sapSailingAnalyticsUi, false);
        initAnimation(raceCommitteeAppUi, false);
        initAnimation(inSightAppUi, false);
        initAnimation(buoyPingerAppUi, false);
        initAnimation(postRaceAnalyticsUi, false);
        initAnimation(strategySimulatorUi, false);
    }
    
    private void initWhatsNewLink(MobilePlacesNavigator placesNavigator, WhatsNewNavigationTabs tab, AnchorElement anchor) {
        placesNavigator.getWhatsNewNavigation(tab).configureAnchorElement(anchor);
    }

    private void initAnimation(final DivElement rootElement, boolean showInitial) {
        final CollapseAnimation animation = new CollapseAnimation(getContent(rootElement), showInitial);
        setClassName(rootElement, SolutionsResources.INSTANCE.css().accordioncollapsed(), !showInitial);
        Element header = getHeader(rootElement);
        DOM.sinkEvents(header, Event.ONCLICK);
        DOM.setEventListener(header, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                boolean collapsed = isCollapsed(rootElement);
                setClassName(rootElement, SolutionsResources.INSTANCE.css().accordioncollapsed(), !collapsed);
                animation.animate(collapsed);
            }
        });
    }
    
    private void setClassName(Element element, String className, boolean add) {
        if (add) element.addClassName(className);
        else element.removeClassName(className);
    }
    
    private boolean isCollapsed(Element rootElement) {
        return rootElement.hasClassName(SolutionsResources.INSTANCE.css().accordioncollapsed());
    }
    
    private Element getHeader(Element rootElement) {
        return rootElement.getFirstChildElement();
    }
    
    private Element getContent(Element rootElement) {
        return rootElement.getFirstChildElement().getNextSiblingElement();
    }
}
