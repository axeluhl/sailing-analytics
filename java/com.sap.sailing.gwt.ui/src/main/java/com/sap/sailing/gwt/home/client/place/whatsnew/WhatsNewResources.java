package com.sap.sailing.gwt.home.client.place.whatsnew;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.TextResource;

public interface WhatsNewResources extends ClientBundle {
    public static final WhatsNewResources INSTANCE = GWT.create(WhatsNewResources.class);

    @Source("com/sap/sailing/gwt/home/solutions/SailingAnalyticsNotes.html")
    public TextResource getSailingAnalyticsNotesHtml();

    @Source("com/sap/sailing/gwt/home/solutions/SailingSimulatorNotes.html")
    public TextResource getSailingSimulatorNotesHtml();
    
    @Source("com/sap/sailing/gwt/home/solutions/RaceCommitteeAppNotes.html")
    public TextResource getRaceCommitteeAppNotesHtml();
    
    @Source("com/sap/sailing/gwt/home/client/place/whatsnew/WhatsNew.css")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String whatsnew_header();
        String whatsnew_nav();
        String whatsnew_nav_link();
        String whatsnew_nav_linkactive();
        String whatsnew_subtitle();
    }
}
