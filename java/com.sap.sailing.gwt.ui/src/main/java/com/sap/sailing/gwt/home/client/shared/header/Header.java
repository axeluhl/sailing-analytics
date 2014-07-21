package com.sap.sailing.gwt.home.client.shared.header;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;

public class Header extends Composite {
    @UiField Anchor startPageLink;
    @UiField Anchor eventsPageLink;
    @UiField Anchor solutionsPageLink;
//    @UiField Anchor sponsoringPageLink;
    
    @UiField TextBox searchText;
    @UiField Button searchButton;

    private final List<Anchor> links;
    private final PlaceNavigator navigator;

    interface HeaderUiBinder extends UiBinder<Widget, Header> {
    }
    
    private static HeaderUiBinder uiBinder = GWT.create(HeaderUiBinder.class);

    public Header(PlaceNavigator navigator) {
        this.navigator = navigator;

        HeaderResources.INSTANCE.css().ensureInjected();
        StyleInjector.injectAtEnd("@media (min-width: 50em) { "+HeaderResources.INSTANCE.largeCss().getText()+"}");

        initWidget(uiBinder.createAndBindUi(this));
        links = Arrays.asList(new Anchor[] { startPageLink, eventsPageLink, solutionsPageLink });
        
        searchText.getElement().setAttribute("placeholder", "Search SAPSailing.com");
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
        navigator.goToHome();
        setActiveLink(startPageLink);
    }

    @UiHandler("eventsPageLink")
    public void goToEvents(ClickEvent e) {
        navigator.goToEvents();
    }

    @UiHandler("solutionsPageLink")
    public void goToSolutions(ClickEvent e) {
        navigator.goToSolutions();
    }

//    @UiHandler("sponsoringPageLink")
//    public void goToSponsoring(ClickEvent e) {
//        navigator.goToSponsoring();
//    }

    @UiHandler("searchButton")
    void searchButtonClick(ClickEvent event) {
        if(searchText.getText().isEmpty()) {
            Window.alert("Please enter a search term.");
        } else {
            navigator.goToSearchResult(searchText.getText());
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
