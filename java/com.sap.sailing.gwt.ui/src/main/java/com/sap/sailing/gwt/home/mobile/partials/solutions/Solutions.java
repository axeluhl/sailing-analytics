package com.sap.sailing.gwt.home.mobile.partials.solutions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.desktop.places.whatsnew.WhatsNewPlace.WhatsNewNavigationTabs;
import com.sap.sailing.gwt.home.mobile.app.MobilePlacesNavigator;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class Solutions extends Composite {

    interface MyUiBinder extends UiBinder<Widget, Solutions> {
    }
    
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiField StringMessages i18n;

    @UiField AnchorElement sailingAnalyticsDetailsAnchor;
    @UiField AnchorElement raceManagerAppDetailsAnchor;
    @UiField AnchorElement sailInSightAppDetailsAnchor;
    @UiField AnchorElement buoyPingerAppDetailsAnchor;
    @UiField AnchorElement simulatorAppDetailsAnchor;
    @UiField AnchorElement raceManagerPlayStoreLinkUi;
    @UiField AnchorElement inSightAppStoreLinkUi;
    @UiField AnchorElement buoyPingerPlayStoreLinkUi;
    @UiField AnchorElement inSightPlayStoreLinkUi;
    @UiField ImageElement raceManagerPlayStoreImgUi;
    @UiField ImageElement inSightPlayStoreImgUi;
    @UiField ImageElement buoyPingerPlayStoreImgUi;
    @UiField ImageElement inSightAppStoreImgUi;
    
    public Solutions(MobilePlacesNavigator placesNavigator) {
        initWidget(uiBinder.createAndBindUi(this));
        String playstorebadgeSrc = UriUtils.fromString(i18n.playstoreBadge()).asString();
        String insightAppstoreSrc = UriUtils.fromString("images/home/appstore" + i18n.appstoreBadgeSuffix() + ".svg")
                .asString();
        raceManagerPlayStoreLinkUi.setHref(UriUtils.fromString(i18n.playstoreRacecommitteeApp()));
        inSightAppStoreLinkUi.setHref(UriUtils.fromString(i18n.appstoreSapSailInsight()));
        buoyPingerPlayStoreLinkUi.setHref(UriUtils.fromString(i18n.playStoreBuoyPingerApp()));
        inSightPlayStoreLinkUi.setHref(UriUtils.fromString(i18n.playstoreInsightApp()));
        raceManagerPlayStoreImgUi.setSrc(playstorebadgeSrc);
        inSightPlayStoreImgUi.setSrc(playstorebadgeSrc);
        buoyPingerPlayStoreImgUi.setSrc(playstorebadgeSrc);
        inSightAppStoreImgUi.setSrc(insightAppstoreSrc);

        initWhatsNewLink(placesNavigator, WhatsNewNavigationTabs.SailingAnalytics, sailingAnalyticsDetailsAnchor);
        initWhatsNewLink(placesNavigator, WhatsNewNavigationTabs.RaceManagerApp, raceManagerAppDetailsAnchor);
        initWhatsNewLink(placesNavigator, WhatsNewNavigationTabs.InSightApp, sailInSightAppDetailsAnchor);
        initWhatsNewLink(placesNavigator, WhatsNewNavigationTabs.BuoyPingerApp, buoyPingerAppDetailsAnchor);
        initWhatsNewLink(placesNavigator, WhatsNewNavigationTabs.SailingSimulator, simulatorAppDetailsAnchor);
    }
    
    private void initWhatsNewLink(MobilePlacesNavigator placesNavigator, WhatsNewNavigationTabs tab, AnchorElement anchor) {
        placesNavigator.getWhatsNewNavigation(tab).configureAnchorElement(anchor);
    }

}
