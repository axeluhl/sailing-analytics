package com.sap.sailing.gwt.home.client.place.solutions.analytics;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;

public class TabletAndDesktopSailingAnalyticsView extends Composite implements SailingAnalyticsView {
    private static SailingAnalyticsPageViewUiBinder uiBinder = GWT.create(SailingAnalyticsPageViewUiBinder.class);

    interface SailingAnalyticsPageViewUiBinder extends UiBinder<Widget, TabletAndDesktopSailingAnalyticsView> {
    }

    @UiField HTML contentOverview;
    @UiField HTML contentFeatures;
    @UiField HTML contentReleaseNotes;
    
    @UiField Anchor overviewAnchor;
    @UiField Anchor featuresAnchor;
    @UiField Anchor releaseNotesAnchor;
    
    @UiField DivElement subTitle;
    
    public TabletAndDesktopSailingAnalyticsView() {
        super();
        
        initWidget(uiBinder.createAndBindUi(this));
        
        contentReleaseNotes.setHTML(SailingAnalyticsResources.INSTANCE.getReleaseNotesHtml().getText());
        contentFeatures.setHTML(SailingAnalyticsResources.INSTANCE.getFeaturesHtml().getText());
        contentOverview.setHTML(SailingAnalyticsResources.INSTANCE.getOverviewHtml().getText());
        
        contentReleaseNotes.setVisible(false);
        contentFeatures.setVisible(false);
        subTitle.setInnerText(TextMessages.INSTANCE.overview());
    }

    @UiHandler("overviewAnchor")
    void overviewClicked(ClickEvent event) {
        contentOverview.setVisible(true);
        contentFeatures.setVisible(false);
        contentReleaseNotes.setVisible(false);
        subTitle.setInnerText(TextMessages.INSTANCE.overview());
    }

    @UiHandler("featuresAnchor")
    void featuresClicked(ClickEvent event) {
        contentOverview.setVisible(false);
        contentFeatures.setVisible(true);
        contentReleaseNotes.setVisible(false);
        subTitle.setInnerText(TextMessages.INSTANCE.features());
    }

    @UiHandler("releaseNotesAnchor")
    void releaseNotesClicked(ClickEvent event) {
        contentOverview.setVisible(false);
        contentFeatures.setVisible(false);
        contentReleaseNotes.setVisible(true);
        subTitle.setInnerText(TextMessages.INSTANCE.releaseNotes());
    }
}
