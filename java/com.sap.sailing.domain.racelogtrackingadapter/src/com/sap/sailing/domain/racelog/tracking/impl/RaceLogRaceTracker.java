package com.sap.sailing.domain.racelog.tracking.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.TrackedRaceStatusEnum;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sailing.domain.racelog.tracking.RaceLogTrackingState;
import com.sap.sailing.domain.racelog.tracking.RaceNotCreatedException;
import com.sap.sailing.domain.racelog.tracking.analyzing.impl.RaceLogTrackingStateAnalyzer;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.RacesHandle;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.TrackedRaceStatusImpl;
import com.sap.sailing.server.RacingEventService;

public class RaceLogRaceTracker implements RaceTracker {
    private final RaceLogConnectivityParams params;
    private final Regatta regatta;

    private DynamicTrackedRace trackedRace;

    private static final Logger logger = Logger.getLogger(RaceLogRaceTracker.class.getName());

    public RaceLogRaceTracker(RaceLogConnectivityParams params, Regatta regatta) {
        this.params = params;
        if (regatta == null) {
            regatta = params.getService().getOrCreateDefaultRegatta(params.getLeaderboard().getName(),
                    params.getBoatClass().getName(), UUID.randomUUID());
        }
        this.regatta = regatta;
        params.getRaceLog().addListener(new GenericRaceLogListener(this));
        logger.info(String.format("Created tracker for race log race %s %s", params.getLeaderboard(), params.getRaceColumn()));
        if (new RaceLogTrackingStateAnalyzer(params.getRaceLog()).analyze() == RaceLogTrackingState.TRACKING) {
            onPreRacePhaseEnded();
        }
    }

    @Override
    public void stop() throws MalformedURLException, IOException, InterruptedException {
        Map<Competitor, DeviceIdentifier> competitors = new RegisteredCompetitorFinder(params.getRaceLog()).analyze();
        for (DeviceIdentifier device : competitors.values()) {
            String type = device.getIdentifierType();
            RaceLogTrackingDeviceHandler service = params.getServiceFinder().findService(
                    RaceLogTrackingDeviceHandler.class, type);
            if (service == null) {
                logger.warning("Could not deregister stop tracking for device " + device
                        + " because no service was found for this device type");
                continue;
            }
            service.stopTrackingDevice(device);
        }

        Set<RegattaAndRaceIdentifier> races = getRaceIdentifiers();
        if (races != null && !races.isEmpty()) {
            DynamicTrackedRace trackedRace = (DynamicTrackedRace) races.iterator().next()
                    .getExistingTrackedRace(params.getService());
            trackedRace.setStatus(new TrackedRaceStatusImpl(TrackedRaceStatusEnum.FINISHED, 0));
        }

        logger.info("Stopped tracking RaceLog races");
    }

    @Override
    public Regatta getRegatta() {
        return regatta;
    }

    @Override
    public Set<RaceDefinition> getRaces() {
        return trackedRace == null ? null : Collections.singleton(trackedRace.getRace());
    }

    @Override
    public Set<RegattaAndRaceIdentifier> getRaceIdentifiers() {
        return trackedRace == null ? null : Collections.singleton(trackedRace.getRaceIdentifier());
    }

    @Override
    public RacesHandle getRacesHandle() {
        return new RaceLogRacesHandle(this);
    }

    @Override
    public DynamicTrackedRegatta getTrackedRegatta() {
        return params.getService().getOrCreateTrackedRegatta(getRegatta());
    }

    @Override
    public WindStore getWindStore() {
        return params.getWindStore();
    }

    @Override
    public GPSFixStore getGPSFixStore() {
        return params.getGPSFixStore();
    }

    @Override
    public Object getID() {
        return params.getTrackerID();
    }

    public void onRaceCreated() {
        RaceColumn raceColumn = params.getRaceColumn();
        RacingEventService service = params.getService();
        RaceLog raceLog = params.getRaceLog();
        Fleet fleet = params.getFleet();
        Leaderboard leaderboard = params.getLeaderboard();
        
        Pair<String, BoatClass> raceInfo
        BoatClass boatClass = params.getBoatClass();
        RaceDefinition raceDef = null;
        if (raceColumn.getTrackedRace(fleet) != null) {
            throw new RaceNotCreatedException(String.format(
                    "Could not create race (%s): has already been created",
                    raceLog));
        }
        Map<Competitor, DeviceIdentifier> competitors = new RegisteredCompetitorFinder(raceLog).analyze();
        for (Competitor c : competitors.keySet()) {
            if (!service.isCompetitorPersistent(c)) {
                throw new RaceDefinitionCreationException(
                        String.format(
                                "Could not create race (%s): Competitor %s is not persistent",
                                raceLog, c));
            }
        }
        try {
            raceDef = new RaceDefinitionCreator(raceLog).analyze();
        } catch (RaceDefinitionCreationException e) {
            logger.warning(e.getMessage());
            e.printStackTrace();
            return;
        }
        regatta.addRace(raceDef);
        RegattaAndRaceIdentifier raceIdentifier = new RegattaNameAndRaceName(regatta.getName(), raceColumn.getName());
        raceColumn.setRaceIdentifier(fleet, raceIdentifier);
        trackedRace = service.getOrCreateTrackedRegatta(regatta).createTrackedRace(raceDef,
                Collections.<Sideline> emptyList(), params.getWindStore(), params.getGPSFixStore(),
                TrackedRace.DEFAULT_LIVE_DELAY_IN_MILLISECONDS,
                WindTrack.DEFAULT_MILLISECONDS_OVER_WHICH_TO_AVERAGE_WIND,
                boatClass.getApproximateManeuverDurationInMilliseconds(), null);
        TimePoint endOfPreRacePhase = new PreRacePhaseEndTimePointFinder(raceLog).analyze();
        trackedRace.setStartOfTrackingReceived(endOfPreRacePhase);
        trackedRace.setStatus(new TrackedRaceStatusImpl(TrackedRaceStatusEnum.TRACKING, 0));
        service.apply(new MapDeviceIdentifiers(competitors, raceIdentifier));
        logger.info(String.format("Started tracking race log race %s %s after recieving RaceLogPreRacePhaseEndedEvent",
                leaderboard.getName(), raceColumn.getName()));
        // this wakes up all waiting race handles
        synchronized (this) {
            this.notifyAll();
        }
    }

    public RaceLogConnectivityParams getParams() {
        return params;
    }
}
