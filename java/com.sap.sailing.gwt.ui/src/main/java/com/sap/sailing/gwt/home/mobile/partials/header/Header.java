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
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.common.client.LinkUtil;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;
import com.sap.sailing.gwt.home.mobile.app.MobilePlacesNavigator;
import com.sap.sailing.gwt.home.shared.ExperimentalFeatures;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.app.ResettableNavigationPathDisplay;
import com.sap.sailing.gwt.home.shared.usermanagement.UserManagementContextEvent;
import com.sap.sailing.gwt.home.shared.usermanagement.UserManagementRequestEvent;
import com.sap.sailing.gwt.home.shared.utils.DropdownHandler;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class Header extends Composite {

    // @UiField TextBox searchText;
    // @UiField Button searchButton;
    
    @UiField HeaderResources local_res;
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
    private final DropdownHandler dropdownHandler;
    private final HeaderNavigationItem signInNavigationItem;
    private final HeaderNavigationItem userDetailsNavigationItem;
    private final HeaderNavigationItem signOutNavigationItem;
    
    public Header(final MobilePlacesNavigator placeNavigator, final EventBus eventBus) {
        initWidget(uiBinder.createAndBindUi(this));
        local_res.css().ensureInjected();
        
        dropdownListExtUi.getElement().getStyle().setDisplay(Display.NONE);
        navigationPathDisplay = new DropdownNavigationPathDisplay();
        
        addNavigation(placeNavigator.getHomeNavigation(), StringMessages.INSTANCE.home());
        addNavigation(placeNavigator.getEventsNavigation(), StringMessages.INSTANCE.events());
        addNavigation(placeNavigator.getSolutionsNavigation(), TextMessages.INSTANCE.solutions());
        addUrl("http://blog.sapsailing.com", TextMessages.INSTANCE.blog());
        signInNavigationItem = addNavigation(placeNavigator.getSignInNavigation(), com.sap.sse.security.ui.client.i18n.StringMessages.INSTANCE.signIn());
        userDetailsNavigationItem = addNavigation(placeNavigator.getUserProfileNavigation(), com.sap.sse.security.ui.client.i18n.StringMessages.INSTANCE.userDetails());
        signOutNavigationItem = addNavigation(com.sap.sse.security.ui.client.i18n.StringMessages.INSTANCE.signOut(), new Runnable() {
            @Override
            public void run() {
                eventBus.fireEvent(new UserManagementRequestEvent(false));
            }
        });
        
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
        
        eventBus.addHandler(UserManagementContextEvent.TYPE, new UserManagementContextEvent.Handler() {
            @Override
            public void onUserChangeEvent(UserManagementContextEvent event) {
                if(event.getCtx().isLoggedIn()) {
                    signInNavigationItem.getElement().getStyle().setDisplay(Display.NONE);
                    userDetailsNavigationItem.getElement().getStyle().clearDisplay();
                    signOutNavigationItem.getElement().getStyle().clearDisplay();
                } else {
                    signInNavigationItem.getElement().getStyle().clearDisplay();
                    userDetailsNavigationItem.getElement().getStyle().setDisplay(Display.NONE);
                    signOutNavigationItem.getElement().getStyle().setDisplay(Display.NONE);
                    
                }
            }
        });
    }
    
    public ResettableNavigationPathDisplay getNavigationPathDisplay() {
        return navigationPathDisplay;
    }
    
    private HeaderNavigationItem addNavigation(final PlaceNavigation<?> placeNavigation, String name) {
        return addNavigation(placeNavigation.getTargetUrl(), name, new Runnable() {
            @Override
            public void run() {
                placeNavigation.goToPlace();
                dropdownHandler.setVisible(false);
            }
            
        });
    }
    
    private HeaderNavigationItem addNavigation(String name, final Runnable action) {
        return addNavigation(null, name, action);
    }
    
    private HeaderNavigationItem addNavigation(String url, String name, final Runnable action) {
        HeaderNavigationItem navigationItem = new HeaderNavigationItem(name, url);
        navigationItem.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if(LinkUtil.handleLinkClick(event.getNativeEvent().<Event>cast())) {
                    event.preventDefault();
                    action.run();
                }
            }
        });
        dropdownListUi.add(navigationItem);
        return navigationItem;
    }
    
    private void addUrl(String url, String name) {
        HeaderNavigationItem navigationItem = new HeaderNavigationItem(name, url);
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
                    headerNavItem.addStyleName(local_res.css().header_navigation_nav_sublist_item());
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
