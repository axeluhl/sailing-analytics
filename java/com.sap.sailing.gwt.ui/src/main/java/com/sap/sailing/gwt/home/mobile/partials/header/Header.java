package com.sap.sailing.gwt.home.mobile.partials.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
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
import com.sap.sailing.gwt.home.client.shared.DropdownHandler;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class Header extends Composite {

    // @UiField TextBox searchText;
    // @UiField Button searchButton;
    
    @UiField ImageElement dropdownTriggerUi;
    @UiField Element dropdownContainerUi;
    @UiField FlowPanel dropdownListUi;
    @UiField Element searchUi;

    @UiField
    DivElement locationTitleUi;

    interface HeaderUiBinder extends UiBinder<Widget, Header> {
    }
    
    private static HeaderUiBinder uiBinder = GWT.create(HeaderUiBinder.class);
    private DropdownHandler dropdownHandler;
//    private MobileApplicationClientFactory appContext;
    
    public Header(final MobileApplicationClientFactory appContext) {
//        this.appContext = appContext;
        HeaderResources.INSTANCE.css().ensureInjected();

        initWidget(uiBinder.createAndBindUi(this));
        
        addNavigation(appContext.getNavigator().getHomeNavigation(), StringMessages.INSTANCE.home());
        addNavigation(appContext.getNavigator().getEventsNavigation(), StringMessages.INSTANCE.events());
        addNavigation(appContext.getNavigator().getSolutionsNavigation(), TextMessages.INSTANCE.solutions());

        dropdownHandler = new DropdownHandler(dropdownTriggerUi, dropdownContainerUi);
        
        Event.sinkEvents(searchUi, Event.ONCLICK);
        Event.setEventListener(searchUi, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                if(LinkUtil.handleLinkClick(event)) {
                    event.preventDefault();
                    appContext.getNavigator().getSearchResultNavigation("").goToPlace();
                }
                
            }
        });
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
}
