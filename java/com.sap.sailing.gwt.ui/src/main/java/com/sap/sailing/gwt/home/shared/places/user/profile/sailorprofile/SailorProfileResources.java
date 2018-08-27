package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;

public interface SailorProfileResources extends ClientBundle {

    public static final SailorProfileResources INSTANCE = GWT.create(SailorProfileResources.class);

    public static final SailorProfileTemplates TEMPLATES = GWT.create(SailorProfileTemplates.class);

    @Source("SailorProfiles.gss")
    SailorProfilesCss css();

    public interface SailorProfileTemplates extends SafeHtmlTemplates {
        @Template("<div style=\"height: 40px; width: 40px; margin-left: 5px; display: inline-block; background-size: cover; background-repeat: no-repeat; background-position: center;background-image: url('{0}');\"></div>")
        SafeHtml buildBoatclassIcon(String boatClassUrl);
    }

    public interface SailorProfilesCss extends CssResource {
        String rotateLeft();

        String eventsTable();

        String eventsTableTitle();

        String eventsTableEmpty();

        String eventsTablePanel();

        String overviewTable();

        String overviewTableFooter();

        String overviewTablePanel();

        String overviewTableEmpty();

        String overviewTableFooterMobile();

        String competitorWithoutClubnameItemDescription();

        String inverseButton();
    }
}
