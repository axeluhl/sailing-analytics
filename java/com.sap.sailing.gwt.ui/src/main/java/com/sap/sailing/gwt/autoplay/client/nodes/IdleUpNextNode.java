package com.sap.sailing.gwt.autoplay.client.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.events.AutoPlayHeaderEvent;
import com.sap.sailing.gwt.autoplay.client.nodes.base.FiresPlaceNode;
import com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.idleupnext.IdleUpNextPlace;
import com.sap.sailing.gwt.autoplay.client.utils.AutoplayHelper;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProviderListener;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.common.Util.Pair;

public class IdleUpNextNode extends FiresPlaceNode {
    private final AutoPlayClientFactory cf;
    private IdleUpNextPlace place;
    private RaceTimesInfoProvider raceTimesInfoProvider;

    public IdleUpNextNode(AutoPlayClientFactory cf) {
        super(IdleUpNextNode.class.getName());
        this.cf = cf;
        place = new IdleUpNextPlace();
    }

    @Override
    public void onStart() {
        String eventName = cf.getAutoPlayCtxSignalError().getEvent().getName();
        getBus().fireEvent(new AutoPlayHeaderEvent(eventName, ""));
        setPlaceToGo(place);
        firePlaceChangeAndStartTimer();
        createRaceTimeInfoProvider();
    }

    @Override
    public void onStop() {
        raceTimesInfoProvider.terminate();
        super.onStop();
    }

    private void createRaceTimeInfoProvider() {
        raceTimesInfoProvider = new RaceTimesInfoProvider(cf.getSailingService(), AutoplayHelper.asyncActionsExecutor,
                cf.getErrorReporter(), new ArrayList<RegattaAndRaceIdentifier>(), 10000l);

        StrippedLeaderboardDTO selectedLeaderboard = AutoplayHelper.getSelectedLeaderboard(
                cf.getAutoPlayCtxSignalError().getEvent(), cf.getAutoPlayCtxSignalError().getContextDefinition().getLeaderboardName());
        if (selectedLeaderboard != null) {
            for (RaceColumnDTO race : selectedLeaderboard.getRaceList()) {
                for (FleetDTO fleet : race.getFleets()) {
                    RegattaAndRaceIdentifier raceIdentifier = race.getRaceIdentifier(fleet);
                    if (raceIdentifier != null && !raceTimesInfoProvider.containsRaceIdentifier(raceIdentifier)) {
                        raceTimesInfoProvider.addRaceIdentifier(raceIdentifier, false);
                    }
                }
            }
        }
        raceTimesInfoProvider.forceTimesInfosUpdate();
        raceTimesInfoProvider.addRaceTimesInfoProviderListener(new RaceTimesInfoProviderListener() {

            @Override
            public void raceTimesInfosReceived(Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo,
                    long clientTimeWhenRequestWasSent, Date serverTimeDuringRequest,
                    long clientTimeWhenResponseWasReceived) {
                ArrayList<Pair<RegattaAndRaceIdentifier, Date>> raceToStartOfRace = new ArrayList<Pair<RegattaAndRaceIdentifier, Date>>();
                checkForRaceStarts(selectedLeaderboard, serverTimeDuringRequest, raceTimesInfoProvider,
                        raceToStartOfRace);

                Collections.sort(raceToStartOfRace, new Comparator<Pair<RegattaAndRaceIdentifier, Date>>() {

                    @Override
                    public int compare(Pair<RegattaAndRaceIdentifier, Date> o1,
                            Pair<RegattaAndRaceIdentifier, Date> o2) {
                        return Long.compare(o1.getB().getTime(), o2.getB().getTime());
                    }
                });
                place.setUpData(raceToStartOfRace);

            }
        });
    };

    public static void checkForRaceStarts(AbstractLeaderboardDTO currentLeaderboard, Date serverTimeDuringRequest,
            RaceTimesInfoProvider raceTimesInfoProvider,
            ArrayList<Pair<RegattaAndRaceIdentifier, Date>> raceToStartOfRace) {
        Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfos = raceTimesInfoProvider.getRaceTimesInfos();
        for (RaceColumnDTO race : currentLeaderboard.getRaceList()) {
            for (FleetDTO fleet : race.getFleets()) {
                RegattaAndRaceIdentifier raceIdentifier = race.getRaceIdentifier(fleet);
                if (raceIdentifier != null) {
                    RaceTimesInfoDTO raceTimes = raceTimesInfos.get(raceIdentifier);
                    if (raceTimes != null && raceTimes.startOfTracking != null && raceTimes.getStartOfRace() != null
                            && raceTimes.endOfRace == null) {
                        raceToStartOfRace.add(
                                new Pair<RegattaAndRaceIdentifier, Date>(raceIdentifier, raceTimes.getStartOfRace()));
                    }
                }
            }
        }
    }
}
