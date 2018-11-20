package com.sap.sailing.gwt.home.mobile.places.user.profile.sailorprofiles;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface SailorProfileMobileResources extends ClientBundle {

    public static final SailorProfileMobileResources INSTANCE = GWT.create(SailorProfileMobileResources.class);

    @Source("SailorProfileMobile.gss")
    SailorProfileMobileCss css();

    public interface SailorProfileMobileCss extends CssResource {

        String eventEntryPanel();

        String regattaEntryPanel();

        String sumPoints();

        String regattaName();

        String regattaRank();

        String competitor();

        String regattaInfoLeft();

        String regattaInfoRight();

        String showLeaderboardButton();

        String regattaSplitter1px();

        String detailsSectionPanel();

        String competitorWithClubnameItemDescriptionId();

        String competitorWithClubnameItemDescriptionClubname();

        String eventTableHeader();

        String overviewPanel();

        String gotoEventButton();

        String eventTableHeaderText();

        String detailsEventsContainer();

        String overviewTableFooterMobile();

        String detailsCompetitorsContainer();

        String inlineTitleUi();

        String editButton();

        String footerAddButton();

        String statisticsHeaderLeft();

        String statisticsHeaderRight();

        String boatclassWithNameEntry();

        String detailsBoatclassesSelectionPanel();

        String detailsBoatclassesEmptyLabel();

        String statisticTime();

        String statisticValue();

        String statisticCompetitor();

        String statisticLabel();
    }
}
