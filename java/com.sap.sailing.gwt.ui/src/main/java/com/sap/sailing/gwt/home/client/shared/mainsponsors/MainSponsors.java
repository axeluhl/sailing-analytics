package com.sap.sailing.gwt.home.client.shared.mainsponsors;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;

public class MainSponsors extends Composite {

    interface MainSponsorsUiBinder extends UiBinder<Widget, MainSponsors> {
    }
    
    private static MainSponsorsUiBinder uiBinder = GWT.create(MainSponsorsUiBinder.class);

    @UiField Anchor solutionsPageLink;
    @UiField Anchor sponsoringPageLink;
    
    private final PlaceNavigator navigator;
    
    public MainSponsors(PlaceNavigator navigator) {
        this.navigator = navigator;
        
        MainSponsorsResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

    @UiHandler("solutionsPageLink")
    public void goToSolutions(ClickEvent e) {
        navigator.goToSolutions();
    }

    @UiHandler("sponsoringPageLink")
    public void goToSponsoring(ClickEvent e) {
        navigator.goToSponsoring();
    }

}
