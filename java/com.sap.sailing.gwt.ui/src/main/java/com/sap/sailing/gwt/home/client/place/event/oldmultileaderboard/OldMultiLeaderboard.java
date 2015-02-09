package com.sap.sailing.gwt.home.client.place.event.oldmultileaderboard;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;
import com.sap.sailing.gwt.home.client.place.event.regattaleaderboard.EventRegattaLeaderboardResources;
import com.sap.sailing.gwt.ui.client.DebugIdHelper;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialog;
import com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.leaderboard.MultiLeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.ScoringSchemeTypeFormatter;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.player.Timer.PlayStates;

public class OldMultiLeaderboard extends Composite {
    private static OldMultiLeaderboardUiBinder uiBinder = GWT.create(OldMultiLeaderboardUiBinder.class);

    interface OldMultiLeaderboardUiBinder extends UiBinder<Widget, OldMultiLeaderboard> {
    }

    @UiField HTMLPanel oldMultiLeaderboardPanel;
    
    @UiField Anchor settingsAnchor;
    @UiField Anchor autoRefreshAnchor;
    @UiField ParagraphElement lastScoringUpdateTimeDiv;
    @UiField ParagraphElement lastScoringUpdateTextDiv;
    @UiField ParagraphElement lastScoringCommentDiv;
    @UiField ParagraphElement scoringSchemeDiv;

    private MultiLeaderboardPanel multiLeaderboardPanel;
    private Timer autoRefreshTimer;
    
    public OldMultiLeaderboard() {
        this.multiLeaderboardPanel = null;
        
        EventRegattaLeaderboardResources.INSTANCE.css().ensureInjected();
        OldMultiLeaderboardResources.INSTANCE.css().ensureInjected();
        
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    @UiHandler("autoRefreshAnchor")
    void toogleAutoRefreshClicked(ClickEvent event) {
        if(autoRefreshTimer != null) {
            if (autoRefreshTimer.getPlayState() == PlayStates.Playing) {
                autoRefreshTimer.pause();
                autoRefreshAnchor.getElement().getStyle().setBackgroundColor("#f0ab00");
            } else {
                // playing the standalone leaderboard means putting it into live mode
                autoRefreshTimer.setPlayMode(PlayModes.Live);
                autoRefreshAnchor.getElement().getStyle().setBackgroundColor("red");
            }
        }
    }
    
    @UiHandler("settingsAnchor")
    void settingsClicked(ClickEvent event) {
        if(multiLeaderboardPanel != null) {
            final String componentName = multiLeaderboardPanel.getLocalizedShortName();
            final String debugIdPrefix = DebugIdHelper.createDebugId(componentName);

            SettingsDialog<LeaderboardSettings> dialog = new SettingsDialog<LeaderboardSettings>(multiLeaderboardPanel,
                    StringMessages.INSTANCE);
            dialog.ensureDebugId(debugIdPrefix + "SettingsDialog");
            dialog.show();
        }
    }

    public void setMultiLeaderboard(MultiLeaderboardPanel multiLeaderboardPanel, final Timer timer) {
        this.autoRefreshTimer = timer;
        this.multiLeaderboardPanel = multiLeaderboardPanel;

        oldMultiLeaderboardPanel.add(multiLeaderboardPanel);
    }

    public void updatedMultiLeaderboard(LeaderboardDTO leaderboard, boolean hasLiveRace) {
        if(leaderboard != null) {
            lastScoringCommentDiv.setInnerText(leaderboard.getComment() != null ? leaderboard.getComment() : "");
            scoringSchemeDiv.setInnerText(leaderboard.scoringScheme != null ? ScoringSchemeTypeFormatter.getDescription(leaderboard.scoringScheme, StringMessages.INSTANCE) : "");
            if (leaderboard.getTimePointOfLastCorrectionsValidity() != null) {
                Date lastCorrectionDate = leaderboard.getTimePointOfLastCorrectionsValidity();
                String lastUpdate = DateAndTimeFormatterUtil.defaultDateFormatter.render(lastCorrectionDate) + ", "
                        + DateAndTimeFormatterUtil.longTimeFormatter.render(lastCorrectionDate);
                lastScoringUpdateTimeDiv.setInnerText(lastUpdate);
                lastScoringUpdateTextDiv.setInnerText(TextMessages.INSTANCE.eventRegattaLeaderboardLastScoreUpdate());
            } else {
                lastScoringUpdateTimeDiv.setInnerText("");
                lastScoringUpdateTextDiv.setInnerText("");
            }
            lastScoringUpdateTimeDiv.getStyle().setVisibility(!hasLiveRace ? Visibility.VISIBLE : Visibility.HIDDEN);
        }
    }
}
