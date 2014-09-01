package com.sap.sailing.gwt.home.client.shared.leaderboard;

import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.home.client.i18n.TextMessages;
import com.sap.sailing.gwt.home.client.place.event.header.CompactEventHeader;
import com.sap.sailing.gwt.home.client.place.event.regattaleaderboard.EventRegattaLeaderboardResources;
import com.sap.sailing.gwt.ui.client.DebugIdHelper;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.LeaderboardUpdateListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialog;
import com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.player.Timer.PlayStates;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;

public class OldLeaderboardPanel extends Composite implements LeaderboardUpdateListener {
    private static OldLeaderboardPanelUiBinder uiBinder = GWT.create(OldLeaderboardPanelUiBinder.class);

    interface OldLeaderboardPanelUiBinder extends UiBinder<Widget, OldLeaderboardPanel> {
    }

    @UiField HTMLPanel leaderboardPanel;
    @UiField(provided=true) CompactEventHeader eventHeader;
    
    // temp fields --> will be moved to a leaderboard partial later on
    @UiField SpanElement title;
    @UiField Anchor settingsAnchor;
    @UiField Anchor autoRefreshAnchor;
    @UiField DivElement lastScoringUpdateTimeDiv;
    @UiField DivElement lastScoringCommentDiv;
    @UiField DivElement liveRaceDiv;
    
    private AbstractLeaderboardViewer leaderboardViewer;
    private Timer autoRefreshTimer;
    
    public OldLeaderboardPanel(EventDTO event, String leaderboardName, Timer timerForClientServerOffset, PlaceNavigator placeNavigator) {
        eventHeader = new CompactEventHeader(event, leaderboardName, placeNavigator);
    
        EventRegattaLeaderboardResources.INSTANCE.css().ensureInjected();
        
        initWidget(uiBinder.createAndBindUi(this));
        
        title.setInnerText(leaderboardName);
    }
    
    public void createLeaderboardViewer(final SailingServiceAsync sailingService, final AsyncActionsExecutor asyncActionsExecutor,
            final Timer timer, final LeaderboardSettings leaderboardSettings, final RaceIdentifier preselectedRace,
            final String leaderboardGroupName, String leaderboardName, final ErrorReporter errorReporter,
            final UserAgentDetails userAgent, boolean showRaceDetails,  
            boolean autoExpandLastRaceColumn, boolean showOverallLeaderboard) {
        this.autoRefreshTimer = timer;
        leaderboardViewer = new LeaderboardViewer(sailingService, new AsyncActionsExecutor(),
                timer, leaderboardSettings, preselectedRace, leaderboardGroupName, leaderboardName, errorReporter, 
                userAgent, showRaceDetails, autoExpandLastRaceColumn, showOverallLeaderboard);

        ScrollPanel contentScrollPanel = new ScrollPanel();
        contentScrollPanel.setWidget(leaderboardViewer);
        leaderboardPanel.add(contentScrollPanel);
        leaderboardViewer.getLeaderboardPanel().addLeaderboardUpdateListener(this);
    }

    public void createMetaLeaderboardViewer(SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor, 
            Timer timer, LeaderboardSettings leaderboardSettings, String preselectedLeaderboardName, RaceIdentifier preselectedRace,
            String leaderboardGroupName, String metaLeaderboardName, ErrorReporter errorReporter,
            UserAgentDetails userAgent, boolean showRaceDetails,  
            boolean autoExpandLastRaceColumn, boolean showSeriesLeaderboards) {
        this.autoRefreshTimer = timer;
        leaderboardViewer = new MetaLeaderboardViewer(sailingService, new AsyncActionsExecutor(),
                timer, leaderboardSettings, null, preselectedRace, leaderboardGroupName, metaLeaderboardName, errorReporter,
                userAgent, showRaceDetails, autoExpandLastRaceColumn, showSeriesLeaderboards);
       
        ScrollPanel contentScrollPanel = new ScrollPanel();
        contentScrollPanel.setWidget(leaderboardViewer);
        leaderboardPanel.add(contentScrollPanel);
    }
    
    @UiHandler("autoRefreshAnchor")
    void toogleAutoRefreshClicked(ClickEvent event) {
        if (autoRefreshTimer.getPlayState() == PlayStates.Playing) {
            autoRefreshTimer.pause();
            autoRefreshAnchor.getElement().getStyle().setBackgroundColor("#f0ab00");
        } else {
            // playing the standalone leaderboard means putting it into live mode
            autoRefreshTimer.setPlayMode(PlayModes.Live);
            autoRefreshAnchor.getElement().getStyle().setBackgroundColor("red");
        }
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

    @Override
    public void updatedLeaderboard(LeaderboardDTO leaderboard) {
        StringMessages stringMessages = StringMessages.INSTANCE;
        
        if(leaderboard != null) {
            lastScoringCommentDiv.setInnerText(leaderboard.getComment() != null ? leaderboard.getComment() : "");
            if (leaderboard.getTimePointOfLastCorrectionsValidity() != null) {
                Date lastCorrectionDate = leaderboard.getTimePointOfLastCorrectionsValidity();
                String lastUpdate = DateAndTimeFormatterUtil.longDateFormatter.render(lastCorrectionDate) + ", "
                        + DateAndTimeFormatterUtil.longTimeFormatter.render(lastCorrectionDate);
                lastScoringUpdateTimeDiv.setInnerText(TextMessages.INSTANCE.eventRegattaLeaderboardLastScoreUpdate() + ": " + lastUpdate);
            } else {
                lastScoringUpdateTimeDiv.setInnerText("");
            }
            
            List<Pair<RaceColumnDTO, FleetDTO>> liveRaces = leaderboard.getLiveRaces(autoRefreshTimer.getLiveTimePointInMillis());
            boolean hasLiveRace = !liveRaces.isEmpty();
            if (hasLiveRace) {
                String liveRaceText = "";
                if(liveRaces.size() == 1) {
                    Pair<RaceColumnDTO, FleetDTO> liveRace = liveRaces.get(0);
                    liveRaceText = stringMessages.raceIsLive("'" + liveRace.getA().getRaceColumnName() + "'");
                } else {
                    String raceNames = "";
                    for (Pair<RaceColumnDTO, FleetDTO> liveRace : liveRaces) {
                        raceNames += "'" + liveRace.getA().getRaceColumnName() + "', ";
                    }
                    // remove last ", "
                    raceNames = raceNames.substring(0, raceNames.length() - 2);
                    liveRaceText = stringMessages.racesAreLive(raceNames);
                }
                liveRaceDiv.setInnerText(liveRaceText);
            } else {
                liveRaceDiv.setInnerText("");
            }
            HTML.wrap(lastScoringUpdateTimeDiv).setVisible(!hasLiveRace);
            HTML.wrap(liveRaceDiv).setVisible(hasLiveRace);
        }
    }

    @Override
    public void currentRaceSelected(RaceIdentifier raceIdentifier, RaceColumnDTO raceColumn) {
    }
}
