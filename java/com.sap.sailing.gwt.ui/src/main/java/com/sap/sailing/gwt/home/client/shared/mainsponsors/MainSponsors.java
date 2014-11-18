package com.sap.sailing.gwt.home.client.shared.mainsponsors;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.app.PlaceNavigation;
import com.sap.sailing.gwt.home.client.place.solutions.SolutionsPlace;
import com.sap.sailing.gwt.home.client.place.solutions.SolutionsPlace.SolutionsNavigationTabs;
import com.sap.sailing.gwt.home.client.place.sponsoring.SponsoringPlace;

public class MainSponsors extends Composite {

    interface MainSponsorsUiBinder extends UiBinder<Widget, MainSponsors> {
    }
    
    private static MainSponsorsUiBinder uiBinder = GWT.create(MainSponsorsUiBinder.class);

    @UiField Anchor solutionsPageLink;
    @UiField Anchor sponsoringPageLink;
    
    private final HomePlacesNavigator navigator;
    
    private final PlaceNavigation<SolutionsPlace> solutionsNavigation;
    private final PlaceNavigation<SponsoringPlace> sponsoringNavigation;

    public MainSponsors(HomePlacesNavigator navigator) {
        this.navigator = navigator;
        
        MainSponsorsResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        solutionsNavigation = navigator.getSolutionsNavigation(SolutionsNavigationTabs.SailingAnalytics);
        sponsoringNavigation = navigator.getSponsoringNavigation();
        
        solutionsPageLink.setHref(solutionsNavigation.getTargetUrl());
        sponsoringPageLink.setHref(sponsoringNavigation.getTargetUrl());
    }

    @UiHandler("solutionsPageLink")
    public void goToSolutions(ClickEvent e) {
        navigator.goToPlace(solutionsNavigation);
        e.preventDefault();
    }

    @UiHandler("sponsoringPageLink")
    public void goToSponsoring(ClickEvent e) {
        navigator.goToPlace(sponsoringNavigation);
        e.preventDefault();
    }

}
