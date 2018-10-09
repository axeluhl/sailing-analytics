package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;

public interface SailorProfileDesktopResources extends ClientBundle {

    public static final SailorProfileDesktopResources INSTANCE = GWT.create(SailorProfileDesktopResources.class);
    public static final SailorProfileDesktopTemplates TEMPLATE = GWT.create(SailorProfileDesktopTemplates.class);

    @Source("SailorProfiles.gss")
    SailorProfilesCss css();

    public interface SailorProfileDesktopTemplates extends SafeHtmlTemplates {
        @Template("<a href='{0}'><img src='{1}' style='-webkit-transform: rotate(270deg);-moz-transform: rotate(270deg);-ms-transform: rotate(270deg); transform: rotate(270deg);width:1.33333333333em; cursor:pointer; min-width:1.33333333em'/></a>")
        SafeHtml navigator(SafeUri url, SafeUri imageUrl);
    }

    public interface SailorProfilesCss extends CssResource {
        String showAndEditHeader();

        String eventsTable();

        String eventsTableTitle();

        String eventsTableEmpty();

        String eventsTablePanel();

        String overviewTable();

        String overviewTableFooter();

        String overviewTablePanel();

        String overviewTableEmpty();

        String clickableColumn();

        String statisticsTableHeaderIcon();

        String statisticsTableHeader();
    }
}
