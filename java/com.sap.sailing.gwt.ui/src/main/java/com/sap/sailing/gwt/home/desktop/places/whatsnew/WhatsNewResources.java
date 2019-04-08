package com.sap.sailing.gwt.home.desktop.places.whatsnew;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.TextResource;

public interface WhatsNewResources extends ClientBundle {
    public static final WhatsNewResources INSTANCE = GWT.create(WhatsNewResources.class);

    @Source("resources/SailingAnalyticsNotes.html")
    public TextResource getSailingAnalyticsNotesHtml();

    @Source("resources/SailingSimulatorNotes.html")
    public TextResource getSailingSimulatorNotesHtml();
    
    @Source("resources/RaceCommitteeAppNotes.html")
    public TextResource getRaceCommitteeAppNotesHtml();

    @Source("resources/InSightAppNotes.html")
    public TextResource getInSightAppNotesHtml();

    @Source("resources/BuoyPingerAppNotes.html")
    public TextResource getBuoyPingerAppNotesHtml();
    
    @Source("resources/WhatsNew.css")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String whatsnew_header();
        String whatsnew_nav();
        String whatsnew_nav_link();
        String whatsnew_nav_linkactive();
        String whatsnew_subtitle();
    }
}
