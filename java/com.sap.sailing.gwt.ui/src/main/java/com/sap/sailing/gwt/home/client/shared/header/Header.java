package com.sap.sailing.gwt.home.client.shared.header;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.impl.HyperlinkImpl;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.app.PlaceNavigation;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
import com.sap.sailing.gwt.home.client.place.searchresult.SearchResultPlace;
import com.sap.sailing.gwt.home.client.place.solutions.SolutionsPlace;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;

public class Header extends Composite {
    @UiField Anchor startPageLink;
    @UiField Anchor eventsPageLink;
    @UiField Anchor solutionsPageLink;
    @UiField AnchorElement homeLink;
//    @UiField Anchor sponsoringPageLink;
    
    @UiField TextBox searchText;
    @UiField Button searchButton;

    private static final HyperlinkImpl IMPL = GWT.create(HyperlinkImpl.class);
    
    private final List<Anchor> links;
    private final HomePlacesNavigator navigator;

    private final PlaceNavigation<StartPlace> homeNavigation;
    private final PlaceNavigation<EventsPlace> eventsNavigation;
    private final PlaceNavigation<SolutionsPlace> solutionsNavigation;
    
    interface HeaderUiBinder extends UiBinder<Widget, Header> {
    }
    
    private static HeaderUiBinder uiBinder = GWT.create(HeaderUiBinder.class);

    public Header(final HomePlacesNavigator navigator) {
        this.navigator = navigator;

        HeaderResources.INSTANCE.css().ensureInjected();
        StyleInjector.injectAtEnd("@media (min-width: 50em) { "+HeaderResources.INSTANCE.largeCss().getText()+"}");

        initWidget(uiBinder.createAndBindUi(this));
        links = Arrays.asList(new Anchor[] { startPageLink, eventsPageLink, solutionsPageLink });

        homeNavigation = navigator.getHomeNavigation();
        eventsNavigation = navigator.getEventsNavigation();
        solutionsNavigation = navigator.getSolutionsNavigation();

        startPageLink.setHref(homeNavigation.getTargetUrl());
        eventsPageLink.setHref(eventsNavigation.getTargetUrl());
        solutionsPageLink.setHref(solutionsNavigation.getTargetUrl());
        
        searchText.getElement().setAttribute("placeholder", TextMessages.INSTANCE.headerSearchPlaceholder());
        searchText.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                    searchButton.click();
                }
            }
        });
    }

    @UiHandler("startPageLink")
    public void goToHome(ClickEvent e) {
        navigator.goToPlace(homeNavigation);
        e.preventDefault();
        setActiveLink(startPageLink);
    }

    @UiHandler("eventsPageLink")
    public void goToEvents(ClickEvent e) {
        if (IMPL.handleAsClick((Event) e.getNativeEvent())) {
            navigator.goToPlace(eventsNavigation);
            e.preventDefault();
            setActiveLink(eventsPageLink);
         }
        
    }

    @UiHandler("solutionsPageLink")
    public void goToSolutions(ClickEvent e) {
        navigator.goToPlace(solutionsNavigation);
        e.preventDefault();
        setActiveLink(solutionsPageLink);
    }

//    @UiHandler("sponsoringPageLink")
//    public void goToSponsoring(ClickEvent e) {
//    navigator.goToPlace(sponsoringNavigation);
//    e.preventDefault();
//    setActiveLink(sponsoringPageLink);
//    }

    @UiHandler("searchButton")
    void searchButtonClick(ClickEvent event) {
        if(searchText.getText().isEmpty()) {
            Window.alert(TextMessages.INSTANCE.pleaseEnterASearchTerm());
        } else {
            PlaceNavigation<SearchResultPlace> searchResultNavigation = navigator.getSearchResultNavigation(searchText.getText());
            navigator.goToPlace(searchResultNavigation);
        }
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

}
