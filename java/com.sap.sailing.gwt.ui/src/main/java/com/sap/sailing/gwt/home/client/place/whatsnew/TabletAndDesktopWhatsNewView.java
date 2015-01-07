package com.sap.sailing.gwt.home.client.place.whatsnew;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public class TabletAndDesktopWhatsNewView extends Composite implements WhatsNewView {
    private static SailingAnalyticsPageViewUiBinder uiBinder = GWT.create(SailingAnalyticsPageViewUiBinder.class);

    interface SailingAnalyticsPageViewUiBinder extends UiBinder<Widget, TabletAndDesktopWhatsNewView> {
    }

    @UiField HTML sailingAnalyticsNotes;
    @UiField HTML sailingSimulatorNotes;
    @UiField HTML raceCommitteeAppNotes;
    
    @UiField Anchor sailingAnalyticsNotesAnchor;
    @UiField Anchor sailingSimulatorNotesAnchor;
    @UiField Anchor raceCommitteeAppNotesAnchor;

    private final List<Anchor> links;
    private final List<HTML> contentWidgets;

    public TabletAndDesktopWhatsNewView() {
        super();

        WhatsNewResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        sailingAnalyticsNotes.setHTML(WhatsNewResources.INSTANCE.getSailingAnalyticsNotesHtml().getText());
        sailingSimulatorNotes.setHTML(WhatsNewResources.INSTANCE.getSailingSimulatorNotesHtml().getText());
        raceCommitteeAppNotes.setHTML(WhatsNewResources.INSTANCE.getRaceCommitteeAppNotesHtml().getText());
        
        sailingSimulatorNotes.setVisible(false);
        raceCommitteeAppNotes.setVisible(false);
        
        links = Arrays.asList(new Anchor[] { sailingAnalyticsNotesAnchor, sailingSimulatorNotesAnchor, raceCommitteeAppNotesAnchor });
        contentWidgets = Arrays.asList(new HTML[] { sailingAnalyticsNotes, sailingSimulatorNotes, raceCommitteeAppNotes });
        
        setActiveContent(sailingAnalyticsNotes, sailingAnalyticsNotesAnchor);
    }

    @UiHandler("sailingAnalyticsNotesAnchor")
    void overviewClicked(ClickEvent event) {
        setActiveContent(sailingAnalyticsNotes, sailingAnalyticsNotesAnchor);
    }

    @UiHandler("sailingSimulatorNotesAnchor")
    void featuresClicked(ClickEvent event) {
        setActiveContent(sailingSimulatorNotes, sailingSimulatorNotesAnchor);
    }

    @UiHandler("raceCommitteeAppNotesAnchor")
    void releaseNotesClicked(ClickEvent event) {
        setActiveContent(raceCommitteeAppNotes, raceCommitteeAppNotesAnchor);
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
}
