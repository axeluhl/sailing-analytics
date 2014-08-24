package com.sap.sailing.gwt.home.client.place.leaderboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.home.client.i18n.TextMessages;
import com.sap.sailing.gwt.home.client.place.event.header.CompactEventHeader;
import com.sap.sailing.gwt.home.client.place.event.regattaleaderboard.EventRegattaLeaderboardResources;
import com.sap.sailing.gwt.home.client.shared.leaderboard.AbstractLeaderboardViewer;
import com.sap.sailing.gwt.home.client.shared.leaderboard.LeaderboardViewer;
import com.sap.sailing.gwt.home.client.shared.leaderboard.MetaLeaderboardViewer;
import com.sap.sailing.gwt.ui.client.DebugIdHelper;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialog;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;

public class TabletAndDesktopLeaderboardView extends Composite implements LeaderboardView {
    private static LeaderboardPageViewUiBinder uiBinder = GWT.create(LeaderboardPageViewUiBinder.class);

    interface LeaderboardPageViewUiBinder extends UiBinder<Widget, TabletAndDesktopLeaderboardView> {
    }

    @UiField HTMLPanel leaderboardPanel;
    @UiField(provided=true) CompactEventHeader eventHeader;
    
    // temp fields --> will be moved to a leaderboard partial later on
    @UiField HeadingElement title;
    @UiField Anchor settingsAnchor;
    @UiField Anchor reloadAnchor;
    
    private AbstractLeaderboardViewer leaderboardViewer;
    
    public TabletAndDesktopLeaderboardView(EventDTO event, String leaderboardName, Timer timerForClientServerOffset, PlaceNavigator placeNavigator) {
        eventHeader = new CompactEventHeader(event, leaderboardName, placeNavigator);
    
        EventRegattaLeaderboardResources.INSTANCE.css().ensureInjected();
        
        initWidget(uiBinder.createAndBindUi(this));
        
        reloadAnchor.setVisible(false);
        title.setInnerText(TextMessages.INSTANCE.leaderboard() + ": " + leaderboardName);
    }
    
    public void createLeaderboardViewer(final SailingServiceAsync sailingService, final AsyncActionsExecutor asyncActionsExecutor,
            final Timer timer, final LeaderboardSettings leaderboardSettings, final RegattaAndRaceIdentifier preselectedRace,
            final String leaderboardGroupName, String leaderboardName, final ErrorReporter errorReporter,
            final StringMessages stringMessages, final UserAgentDetails userAgent, boolean showRaceDetails,  
            boolean autoExpandLastRaceColumn, boolean showOverallLeaderboard) {
        leaderboardViewer = new LeaderboardViewer(sailingService, new AsyncActionsExecutor(),
                timer, leaderboardSettings, preselectedRace, leaderboardGroupName, leaderboardName, errorReporter, stringMessages,
                userAgent, showRaceDetails, autoExpandLastRaceColumn, showOverallLeaderboard);

        ScrollPanel contentScrollPanel = new ScrollPanel();
        contentScrollPanel.setWidget(leaderboardViewer);
        leaderboardPanel.add(contentScrollPanel);
    }

    public void createMetaLeaderboardViewer(SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor, 
            Timer timer, LeaderboardSettings leaderboardSettings, String preselectedLeaderboardName, RegattaAndRaceIdentifier preselectedRace,
            String leaderboardGroupName, String metaLeaderboardName, ErrorReporter errorReporter,
            StringMessages stringMessages, UserAgentDetails userAgent, boolean showRaceDetails,  
            boolean autoExpandLastRaceColumn, boolean showSeriesLeaderboards) {
        leaderboardViewer = new MetaLeaderboardViewer(sailingService, new AsyncActionsExecutor(),
                timer, leaderboardSettings, null, preselectedRace, leaderboardGroupName, metaLeaderboardName, errorReporter, stringMessages,
                userAgent, showRaceDetails, autoExpandLastRaceColumn, showSeriesLeaderboards);
       
        ScrollPanel contentScrollPanel = new ScrollPanel();
        contentScrollPanel.setWidget(leaderboardViewer);
        leaderboardPanel.add(contentScrollPanel);
    }

    @UiHandler("reloadAnchor")
    void reloadClicked(ClickEvent event) {
    }
    
    @UiHandler("settingsAnchor")
    void settingsClicked(ClickEvent event) {
        LeaderboardPanel leaderboardComponent = leaderboardViewer.getLeaderboardPanel();
        
        final String componentName = leaderboardComponent.getLocalizedShortName();
        final String debugIdPrefix = DebugIdHelper.createDebugId(componentName);

        SettingsDialog<LeaderboardSettings> dialog = new SettingsDialog<LeaderboardSettings>(leaderboardComponent,
                StringMessages.INSTANCE);
        dialog.ensureDebugId(debugIdPrefix + "SettingsDialog");
        dialog.show();
    }
}
