package com.sap.sailing.gwt.home.desktop.places.whatsnew;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.impl.HyperlinkImpl;
import com.sap.sailing.gwt.home.desktop.app.DesktopPlacesNavigator;
import com.sap.sailing.gwt.home.desktop.places.whatsnew.WhatsNewPlace.WhatsNewNavigationTabs;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;

public class TabletAndDesktopWhatsNewView extends Composite implements WhatsNewView {
    private static SailingAnalyticsPageViewUiBinder uiBinder = GWT.create(SailingAnalyticsPageViewUiBinder.class);

    interface SailingAnalyticsPageViewUiBinder extends UiBinder<Widget, TabletAndDesktopWhatsNewView> {
    }

    private static final HyperlinkImpl HYPERLINK_IMPL = GWT.create(HyperlinkImpl.class);

    @UiField HTML sailingAnalyticsNotes;
    @UiField HTML sailingSimulatorNotes;
    @UiField HTML raceCommitteeAppNotes;
    @UiField HTML inSightAppNotes;
    @UiField HTML buoyPingerAppNotes;
    
    @UiField Anchor sailingAnalyticsNotesAnchor;
    @UiField Anchor sailingSimulatorNotesAnchor;
    @UiField Anchor raceCommitteeAppNotesAnchor;
    @UiField Anchor inSightAppNotesAnchor;
    @UiField Anchor buoyPingerAppNotesAnchor;

    private final PlaceNavigation<WhatsNewPlace> sailingAnalyticNotesNavigation; 
    private final PlaceNavigation<WhatsNewPlace> sailingSimulatorNoteNavigation; 
    private final PlaceNavigation<WhatsNewPlace> raceCommitteeAppNotesNavigation; 
    private final PlaceNavigation<WhatsNewPlace> inSightAppNotesNavigation;
    private final PlaceNavigation<WhatsNewPlace> buoyPingerAppNotesNavigation;

    private final List<Anchor> links;
    private final List<HTML> contentWidgets;
    
    public TabletAndDesktopWhatsNewView(WhatsNewNavigationTabs navigationTab, DesktopPlacesNavigator placesNavigator) {
        super();
    
        WhatsNewResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    
        sailingAnalyticsNotes.setHTML(WhatsNewResources.INSTANCE.getSailingAnalyticsNotesHtml().getText());
        sailingSimulatorNotes.setHTML(WhatsNewResources.INSTANCE.getSailingSimulatorNotesHtml().getText());
        raceCommitteeAppNotes.setHTML(WhatsNewResources.INSTANCE.getRaceCommitteeAppNotesHtml().getText());
        inSightAppNotes.setHTML(WhatsNewResources.INSTANCE.getInSightAppNotesHtml().getText());
        buoyPingerAppNotes.setHTML(WhatsNewResources.INSTANCE.getBuoyPingerAppNotesHtml().getText());

        sailingAnalyticNotesNavigation = placesNavigator.getWhatsNewNavigation(WhatsNewNavigationTabs.SailingAnalytics); 
        sailingSimulatorNoteNavigation = placesNavigator.getWhatsNewNavigation(WhatsNewNavigationTabs.SailingSimulator); 
        raceCommitteeAppNotesNavigation = placesNavigator.getWhatsNewNavigation(WhatsNewNavigationTabs.RaceManagerApp);
        inSightAppNotesNavigation = placesNavigator.getWhatsNewNavigation(WhatsNewNavigationTabs.InSightApp);
        buoyPingerAppNotesNavigation = placesNavigator.getWhatsNewNavigation(WhatsNewNavigationTabs.BuoyPingerApp);

        sailingAnalyticsNotesAnchor.setHref(sailingAnalyticNotesNavigation.getTargetUrl());
        sailingSimulatorNotesAnchor.setHref(sailingSimulatorNoteNavigation.getTargetUrl());
        raceCommitteeAppNotesAnchor.setHref(raceCommitteeAppNotesNavigation.getTargetUrl());
        inSightAppNotesAnchor.setHref(inSightAppNotesNavigation.getTargetUrl());
        buoyPingerAppNotesAnchor.setHref(buoyPingerAppNotesNavigation.getTargetUrl());

        links = Arrays.asList(new Anchor[] { sailingAnalyticsNotesAnchor, sailingSimulatorNotesAnchor, raceCommitteeAppNotesAnchor, inSightAppNotesAnchor, buoyPingerAppNotesAnchor });
        contentWidgets = Arrays.asList(new HTML[] { sailingAnalyticsNotes, sailingSimulatorNotes, raceCommitteeAppNotes, inSightAppNotes, buoyPingerAppNotes });

        switch(navigationTab) {
            case BuoyPingerApp:
                setActiveContent(buoyPingerAppNotes, buoyPingerAppNotesAnchor);
                break;
            case InSightApp:
                setActiveContent(inSightAppNotes, inSightAppNotesAnchor);
                break;
            case PostRaceAnalytics:
                break;
            case RaceManagerApp:
                setActiveContent(raceCommitteeAppNotes, raceCommitteeAppNotesAnchor);
                break;
            case SailingAnalytics:
                setActiveContent(sailingAnalyticsNotes, sailingAnalyticsNotesAnchor);
                break;
            case SailingSimulator:
                setActiveContent(sailingSimulatorNotes, sailingSimulatorNotesAnchor);
                break;
            case TrainingDiary:
                break;
        }
    }

    @UiHandler("sailingAnalyticsNotesAnchor")
    void overviewClicked(ClickEvent event) {
        setActiveContent(sailingAnalyticsNotes, sailingAnalyticsNotesAnchor);
        handleClickEventWithLocalNavigation(event, sailingAnalyticNotesNavigation);
    }

    @UiHandler("sailingSimulatorNotesAnchor")
    void featuresClicked(ClickEvent event) {
        setActiveContent(sailingSimulatorNotes, sailingSimulatorNotesAnchor);
        handleClickEventWithLocalNavigation(event, sailingSimulatorNoteNavigation);
    }

    @UiHandler("raceCommitteeAppNotesAnchor")
    void releaseNotesClicked(ClickEvent event) {
        setActiveContent(raceCommitteeAppNotes, raceCommitteeAppNotesAnchor);
        handleClickEventWithLocalNavigation(event, raceCommitteeAppNotesNavigation);
    }
    
    private void setActiveContent(HTML activeHTML, Anchor activeLink) {
        for (HTML html: contentWidgets) {
            html.setVisible(html == activeHTML);
        }
        for (Anchor link : links) {
            if (link == activeLink) {
                link.addStyleName(WhatsNewResources.INSTANCE.css().whatsnew_nav_linkactive());
            } else {
                link.removeStyleName(WhatsNewResources.INSTANCE.css().whatsnew_nav_linkactive());
            }
        }
    }
    
    private void handleClickEventWithLocalNavigation(ClickEvent e, PlaceNavigation<?> placeNavigation) {
        if (HYPERLINK_IMPL.handleAsClick((Event) e.getNativeEvent())) {
            // don't use the placecontroller for navigation here as we want to avoid a page reload
            History.newItem(placeNavigation.getHistoryUrl(), false);
            e.preventDefault();
         }
    }

}
