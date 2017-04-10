package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.events.AutoplayFailureEvent;
import com.sap.sailing.gwt.autoplay.client.events.LiferaceDetectedEvent;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.HelperSixty;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderboardDTO;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderbordAction;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.gwt.dispatch.shared.commands.ResultWithTTL;

public class UpcomingRacesMonitor {
    private final AutoPlayClientFactorySixtyInch cf;
    private EventDTO eventToMonitor;
    private String leaderBoardName;
    private int errorCount = 0;;

    private Timer checkTimer = new Timer() {
        @Override
        public void run() {
            doCheck();
        }
    };

    public UpcomingRacesMonitor( AutoPlayClientFactorySixtyInch cf) {
        this.cf = cf;
    }

    public void startMonitoring() {
        this.eventToMonitor = cf.getSlideCtx().getEvent();
        this.leaderBoardName = cf.getSlideCtx().getSettings().getLeaderBoardName();
        doCheck();
    }

    private void doCheck() {
        cf.getDispatch().execute(new GetMiniLeaderbordAction(eventToMonitor.id, leaderBoardName),
                new AsyncCallback<ResultWithTTL<GetMiniLeaderboardDTO>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorCount++;
                        if (errorCount > 5) {
                            cf.getEventBus().fireEvent(new AutoplayFailureEvent(caught));
                        }
                    }

                    @Override
                    public void onSuccess(ResultWithTTL<GetMiniLeaderboardDTO> resultTTL) {
                        final GetMiniLeaderboardDTO dto = resultTTL.getDto();
                        HelperSixty.getLifeRace(cf.getSailingService(), cf.getErrorReporter(),
                                cf.getSlideCtx().getEvent(), leaderBoardName, cf.getDispatch(),
                                new AsyncCallback<RegattaAndRaceIdentifier>() {
                                    @Override
                                    public void onSuccess(RegattaAndRaceIdentifier lifeRace) {
                                        errorCount = 0;
                                        if (lifeRace == null) {
                                            checkTimer.schedule(5000);
                                        } else {
                                            GetMiniLeaderboardDTO dto = resultTTL.getDto();
                                            cf.getEventBus().fireEvent(new LiferaceDetectedEvent(lifeRace, dto));
                                        }
                                    }

                                    @Override
                                    public void onFailure(Throwable caught) {
                                        errorCount++;
                                        if (errorCount > 5) {
                                            cf.getEventBus().fireEvent(new AutoplayFailureEvent(caught));
                                        }
                                    }
                                });
                    }
                });
    }
}
