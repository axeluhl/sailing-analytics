package com.sap.sailing.gwt.home.mobile.partials.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.LinkUtil;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;
import com.sap.sailing.gwt.home.mobile.app.MobilePlacesNavigator;
import com.sap.sailing.gwt.home.shared.ExperimentalFeatures;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.app.ResettableNavigationPathDisplay;
import com.sap.sailing.gwt.home.shared.utils.DropdownHandler;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class Header extends Composite {

    // @UiField TextBox searchText;
    // @UiField Button searchButton;
    
    @UiField ImageElement dropdownTriggerUi;
    @UiField Element dropdownContainerUi;
    @UiField FlowPanel dropdownListUi;
    @UiField FlowPanel dropdownListExtUi;
    @UiField Element searchUi;

    @UiField
    DivElement locationTitleUi;
    
    private final ResettableNavigationPathDisplay navigationPathDisplay;

    interface HeaderUiBinder extends UiBinder<Widget, Header> {
    }
    
    private static HeaderUiBinder uiBinder = GWT.create(HeaderUiBinder.class);
    private DropdownHandler dropdownHandler;
    
    public Header(final MobilePlacesNavigator placeNavigator) {
        HeaderResources.INSTANCE.css().ensureInjected();

        initWidget(uiBinder.createAndBindUi(this));
        
        dropdownListExtUi.getElement().getStyle().setDisplay(Display.NONE);
        navigationPathDisplay = new DropdownNavigationPathDisplay();
        
        addNavigation(placeNavigator.getHomeNavigation(), StringMessages.INSTANCE.home());
        addNavigation(placeNavigator.getEventsNavigation(), StringMessages.INSTANCE.events());
        addNavigation(placeNavigator.getSolutionsNavigation(), TextMessages.INSTANCE.solutions());

        dropdownHandler = new DropdownHandler(dropdownTriggerUi, dropdownContainerUi);
        
        Event.sinkEvents(searchUi, Event.ONCLICK);
        Event.setEventListener(searchUi, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                if(LinkUtil.handleLinkClick(event)) {
                    event.preventDefault();
                    placeNavigator.getSearchResultNavigation("").goToPlace();
                }
                
            }
        });
    }
    
    public ResettableNavigationPathDisplay getNavigationPathDisplay() {
        return navigationPathDisplay;
    }
    
    private void addNavigation(final PlaceNavigation<?> placeNavigation, String name) {
        HeaderNavigationItem navigationItem = new HeaderNavigationItem(name, placeNavigation.getTargetUrl());
        navigationItem.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                if(LinkUtil.handleLinkClick(event.getNativeEvent().<Event>cast())) {
                    event.preventDefault();
                    placeNavigation.goToPlace();
                    dropdownHandler.setVisible(false);
                }
            }
        });
        dropdownListUi.add(navigationItem);
    }
    
    public void setLocationTitle(String locationTitle) {
        locationTitleUi.setInnerText(locationTitle);
    }
    
    private class DropdownNavigationPathDisplay implements ResettableNavigationPathDisplay {
        @Override
        public void showNavigationPath(NavigationItem... navigationPath) {
            dropdownListExtUi.clear();
            if(ExperimentalFeatures.USE_NAVIGATION_PATH_DISPLAY_ON_MOBILE) {
                for (final NavigationItem navigationItem : navigationPath) {
                    HeaderNavigationItem headerNavItem = new HeaderNavigationItem(navigationItem.getDisplayName(), navigationItem.getTargetUrl());
                    headerNavItem.addClickHandler(new ClickHandler() {
                        
                        @Override
                        public void onClick(ClickEvent event) {
                            if(LinkUtil.handleLinkClick(event.getNativeEvent().<Event>cast())) {
                                event.preventDefault();
                                navigationItem.run();
                                dropdownHandler.setVisible(false);
                            }
                        }
                    });
                    dropdownListExtUi.add(headerNavItem);
                }
                dropdownListExtUi.getElement().getStyle().clearDisplay();
            }
        }

        @Override
        public void reset() {
            dropdownListExtUi.clear();
            dropdownListExtUi.getElement().getStyle().setDisplay(Display.NONE);
        }
    }
}
