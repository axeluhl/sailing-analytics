package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public interface CompetitorFilterResources extends ClientBundle {
    public static final CompetitorFilterResources INSTANCE = GWT.create(CompetitorFilterResources.class);
    
    @Source("com/sap/sailing/gwt/ui/client/images/SAP_RV_Clear.png")
    ImageResource clearButton();

    @Source("com/sap/sailing/gwt/ui/client/images/SAP_RV_CompetitorsFilter_INACTIVE.png")
    ImageResource filterInactiveButton();

    @Source("com/sap/sailing/gwt/ui/client/images/SAP_RV_CompetitorsFilter_ACTIVE.png")
    ImageResource filterActiveButton();

    @Source("com/sap/sailing/gwt/ui/client/images/SAP_RV_Search.png")
    ImageResource searchButton();

    @Source("com/sap/sailing/gwt/ui/client/images/SAP_RV_Settings.png")
    ImageResource settingsButton();

    @Source("com/sap/sailing/gwt/ui/leaderboard/CompetitorFilter.css")
    CompetitorFilterCss css();

    public interface CompetitorFilterCss extends CssResource {
        String button();
        String hiddenButton();
        String clearButton();
        String searchButton();
        String settingsButton();
        String filterButton();
        String competitorFilterContainer();
        String searchBox();
        String searchInput();
        String filterInactiveButtonBackgroundImage();
        String filterActiveButtonBackgroundImage();
        String clearButtonBackgroundImage();
        String searchButtonBackgroundImage();
        String settingsButtonBackgroundImage();
        String headerPanel();
    }

}
