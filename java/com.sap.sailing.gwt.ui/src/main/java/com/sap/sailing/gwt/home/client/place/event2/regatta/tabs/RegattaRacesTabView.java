package com.sap.sailing.gwt.home.client.place.event2.regatta.tabs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.sap.sailing.gwt.home.client.place.event2.partials.regattaraces.EventRegattaRaces;
import com.sap.sailing.gwt.home.client.place.event2.regatta.EventRegattaView.Presenter;
import com.sap.sailing.gwt.home.client.place.event2.regatta.RegattaTabView;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;
import com.sap.sse.common.Util.Triple;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class RegattaRacesTabView extends Composite implements RegattaTabView<RegattaRacesPlace> {

    interface MyBinder extends UiBinder<HTMLPanel, RegattaRacesTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);

    private Presenter currentPresenter;

    @UiField(provided = true)
    protected EventRegattaRaces regattaRaces;

    public RegattaRacesTabView() {

    }

    @Override
    public void setPresenter(Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;

    }
    @Override
    public void start(RegattaRacesPlace myPlace, final AcceptsOneWidget contentArea) {

        // TODO: understand, and than move this into appropiate place (probably context)
        final long clientTimeWhenRequestWasSent = System.currentTimeMillis();


        final EventViewDTO eventDTO = myPlace.getCtx().getEventDTO();
        final String selectedRegattaId = myPlace.getCtx().getRegattaId();

        currentPresenter.getSailingService().getRegattaStructureOfEvent(eventDTO.id,
                new AsyncCallback<List<RaceGroupDTO>>() {
                    @Override
                    public void onSuccess(List<RaceGroupDTO> raceGroups) {
                        if (raceGroups.size() > 0) {
                            for (LeaderboardGroupDTO leaderboardGroupDTO : eventDTO.getLeaderboardGroups()) {
                                final long clientTimeWhenResponseWasReceived = System.currentTimeMillis();
                                if (leaderboardGroupDTO.getAverageDelayToLiveInMillis() != null) {
                                    currentPresenter.getTimerForClientServerOffset().setLivePlayDelayInMillis(
                                            leaderboardGroupDTO
                                            .getAverageDelayToLiveInMillis());
                                }
                                currentPresenter.getTimerForClientServerOffset().adjustClientServerOffset(
                                        clientTimeWhenRequestWasSent,
                                        leaderboardGroupDTO.getCurrentServerTime(), clientTimeWhenResponseWasReceived);
                            }
                            // createEventView(eventDTO, raceGroups, panel);

                            Map<String, Triple<RaceGroupDTO, StrippedLeaderboardDTO, LeaderboardGroupDTO>> regattaStructure = getRegattaStructure(
                                    eventDTO, raceGroups);


                            Triple<RaceGroupDTO, StrippedLeaderboardDTO, LeaderboardGroupDTO> selectedRegatta = regattaStructure
                                    .get(selectedRegattaId);

                            regattaRaces = new EventRegattaRaces(currentPresenter);

                            regattaRaces.setRaces(selectedRegatta.getC(), false, selectedRegatta.getB(),
                                    selectedRegatta.getA());

                            // if (eventDTO.isRunning()) {
                            // // create update time for race states only for running events
                            // serverUpdateTimer.addTimeListener(new TimeListener() {
                            // @Override
                            // public void timeChanged(Date newTime, Date oldTime) {
                            // loadAndUpdateEventRaceStatesLog();
                            // }
                            // });
                            // }
                            initWidget(ourUiBinder.createAndBindUi(RegattaRacesTabView.this));
                            contentArea.setWidget(RegattaRacesTabView.this);
                        } else {
                            // createEventWithoutRegattasView(event, panel);
                        }
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        // createErrorView(
                        // "Error while loading the regatta structure with service getRegattaStructureOfEvent()",
                        // caught, panel);
                    }
                });




       

    }

    @Override
    public void stop() {

    }

    @Override
    public RegattaRacesPlace placeToFire() {
        return new RegattaRacesPlace(currentPresenter.getCtx());
    }

    @Override
    public Class<RegattaRacesPlace> getPlaceClassForActivation() {
        return RegattaRacesPlace.class;
    }

    private Map<String, Triple<RaceGroupDTO, StrippedLeaderboardDTO, LeaderboardGroupDTO>> getRegattaStructure(
            EventDTO event, List<RaceGroupDTO> raceGroups) {
        Map<String, Triple<RaceGroupDTO, StrippedLeaderboardDTO, LeaderboardGroupDTO>> result = new HashMap<>();
        Map<String, RaceGroupDTO> raceGroupsMap = new HashMap<>();
        for (RaceGroupDTO raceGroup : raceGroups) {
            raceGroupsMap.put(raceGroup.getName(), raceGroup);
        }

        for (LeaderboardGroupDTO leaderboardGroup : event.getLeaderboardGroups()) {
            for (StrippedLeaderboardDTO leaderboard : leaderboardGroup.getLeaderboards()) {
                String leaderboardName = leaderboard.name;
                result.put(leaderboardName, new Triple<RaceGroupDTO, StrippedLeaderboardDTO, LeaderboardGroupDTO>(
                        raceGroupsMap.get(leaderboardName), leaderboard, leaderboardGroup));
            }
        }
        return result;
    }

}