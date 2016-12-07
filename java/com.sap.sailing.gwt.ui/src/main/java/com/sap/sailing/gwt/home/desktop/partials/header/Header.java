package com.sap.sailing.gwt.home.desktop.partials.header;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.place.shared.Place;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.impl.HyperlinkImpl;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.home.client.place.event.legacy.EventPlace;
import com.sap.sailing.gwt.home.client.place.event.legacy.RegattaPlace;
import com.sap.sailing.gwt.home.desktop.app.DesktopPlacesNavigator;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.places.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.shared.places.events.EventsPlace;
import com.sap.sailing.gwt.home.shared.places.searchresult.SearchResultPlace;
import com.sap.sailing.gwt.home.shared.places.solutions.SolutionsPlace;
import com.sap.sailing.gwt.home.shared.places.solutions.SolutionsPlace.SolutionsNavigationTabs;
import com.sap.sailing.gwt.home.shared.places.start.StartPlace;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.mvp.PlaceChangedEvent;
import com.sap.sse.security.ui.authentication.view.AuthenticationMenuView;
import com.sap.sse.security.ui.authentication.view.AuthenticationMenuViewImpl;

public class Header extends Composite {
    @UiField Anchor startPageLink;
    @UiField Anchor eventsPageLink;
    @UiField Anchor solutionsPageLink;
//    @UiField Anchor sponsoringPageLink;
    
    @UiField TextBox searchText;
    @UiField Button searchButton;
    
    @UiField Anchor usermenu;

    private static final HyperlinkImpl HYPERLINK_IMPL = GWT.create(HyperlinkImpl.class);
    
    private final List<Anchor> links;
    private final DesktopPlacesNavigator navigator;

    private final PlaceNavigation<StartPlace> homeNavigation;
    private final PlaceNavigation<EventsPlace> eventsNavigation;
    private final PlaceNavigation<SolutionsPlace> solutionsNavigation;
    
    private final AuthenticationMenuView authenticationMenuView;
    
    interface HeaderUiBinder extends UiBinder<Widget, Header> {
    }
    
    private static HeaderUiBinder uiBinder = GWT.create(HeaderUiBinder.class);

    public Header(final DesktopPlacesNavigator navigator, EventBus eventBus) {
        this.navigator = navigator;

        HeaderResources.INSTANCE.css().ensureInjected();
        
        initWidget(uiBinder.createAndBindUi(this));
        links = Arrays.asList(new Anchor[] { startPageLink, eventsPageLink, solutionsPageLink });
        
        homeNavigation = navigator.getHomeNavigation();
        eventsNavigation = navigator.getEventsNavigation();
        solutionsNavigation = navigator.getSolutionsNavigation(SolutionsNavigationTabs.SapInSailing);
        
        startPageLink.setHref(homeNavigation.getTargetUrl());
        eventsPageLink.setHref(eventsNavigation.getTargetUrl());
        solutionsPageLink.setHref(solutionsNavigation.getTargetUrl());
        
        searchText.getElement().setAttribute("placeholder", StringMessages.INSTANCE.headerSearchPlaceholder());
        searchText.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                    searchButton.click();
                }
            }
        });
        
        eventBus.addHandler(PlaceChangedEvent.TYPE, new PlaceChangedEvent.Handler() {
            @Override
            public void onPlaceChanged(PlaceChangedEvent event) {
                updateActiveLink(event.getNewPlace());
            }
        });
        
        authenticationMenuView = new AuthenticationMenuViewImpl(usermenu, HeaderResources.INSTANCE.css().loggedin(), HeaderResources.INSTANCE.css().open());
    }

    @UiHandler("startPageLink")
    public void goToHome(ClickEvent e) {
        handleClickEvent(e, homeNavigation, startPageLink);
    }

    @UiHandler("eventsPageLink")
    public void goToEvents(ClickEvent e) {
        handleClickEvent(e, eventsNavigation, eventsPageLink);
    }

    @UiHandler("solutionsPageLink")
    public void goToSolutions(ClickEvent e) {
        handleClickEvent(e, solutionsNavigation, solutionsPageLink);
    }

//    @UiHandler("sponsoringPageLink")
//    public void goToSponsoring(ClickEvent e) {
//        handleClickEvent(e, sponsoringNavigation, sponsoringPageLink);
//    }

    @UiHandler("searchButton")
    void searchButtonClick(ClickEvent event) {
        PlaceNavigation<SearchResultPlace> searchResultNavigation = navigator.getSearchResultNavigation(searchText
                .getText());
        navigator.goToPlace(searchResultNavigation);
    }
    
    private void updateActiveLink(Place place) {
        if(place instanceof EventsPlace
                || place instanceof AbstractEventPlace
                || place instanceof EventPlace
                || place instanceof RegattaPlace) {
            setActiveLink(eventsPageLink);
        } else if(place instanceof StartPlace) {
            setActiveLink(startPageLink);
        } else if(place instanceof SolutionsPlace) {
            setActiveLink(solutionsPageLink);
        } else {
            setActiveLink(null);
        }
        // TODO add more rules
    }
    
    private void setActiveLink(Anchor link) {
        final String activeStyle = HeaderResources.INSTANCE.css().sitenavigation_linkactive();
        for (Anchor l : links) {
            if (l == link) {
                l.addStyleName(activeStyle);
            } else {
                l.removeStyleName(activeStyle);
            }
        }
    }

    private void handleClickEvent(ClickEvent e, PlaceNavigation<?> placeNavigation, Anchor activeLink) {
        if (HYPERLINK_IMPL.handleAsClick((Event) e.getNativeEvent())) {
            navigator.goToPlace(placeNavigation);
            e.preventDefault();
            setActiveLink(activeLink);
         }
    }

    public AuthenticationMenuView getAuthenticationMenuView() {
        return authenticationMenuView;
    }
}
