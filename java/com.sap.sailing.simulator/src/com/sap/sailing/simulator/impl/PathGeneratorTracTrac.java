package com.sap.sailing.simulator.impl;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RacesHandle;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.TimedPositionWithSpeed;

public class PathGeneratorTracTrac extends PathGeneratorBase {

    private static final Logger LOGGER = Logger.getLogger("com.sap.sailing.simulator");
    private static final long DEFAULT_TIMEOUT_MILLISECONDS = 60000;
    private static final WindStore DEFAULT_WINDSTORE = EmptyWindStore.INSTANCE;

    private RacingEventServiceImpl service = null;
    private RacesHandle raceHandle = null;
    private URL raceURL = null;
    private URI liveURI = null;
    private URI storedURI = null;
    private double windScale = 0.0;
    private LinkedList<TimedPositionWithSpeed> raceCourse = null;

    public PathGeneratorTracTrac(SimulationParameters parameters) {

        this.parameters = parameters;
        this.service = new RacingEventServiceImpl();
    }

    private void intializeRaceHandle() {

        if (this.raceHandle != null) {
            return;
        }

        LOGGER.info("Calling service.addTracTracRace");

        try {
            this.raceHandle = this.service.addTracTracRace(this.raceURL, this.liveURI, this.storedURI, DEFAULT_WINDSTORE, DEFAULT_TIMEOUT_MILLISECONDS,
                    this);
            synchronized (this) {
                this.wait();
            }
        } catch (Exception error) {
            LOGGER.severe(error.getMessage());
        }
    }

    public void setEvaluationParameters(String raceURLString, String liveURIString, String storedURIString, double windScale) {

        try {
            this.raceURL = new URL(raceURLString);
        } catch (MalformedURLException error) {
            LOGGER.severe("MalformedURLException when constructing the raceURL " + error.getMessage());
        }

        try {
            this.liveURI = (liveURIString == null) ? null : new URI(liveURIString);
        } catch (URISyntaxException error) {
            LOGGER.severe("URISyntaxException when constructing the liveURI " + error.getMessage());
        }

        try {
            this.storedURI = (storedURIString == null) ? null : new URI(storedURIString);
        } catch (URISyntaxException error) {
            LOGGER.severe("URISyntaxException when constructing the storedURI " + error.getMessage());
        }

        this.windScale = windScale;
    }

    @Override
    public Path getPath() {

        LinkedList<TimedPositionWithSpeed> path = new LinkedList<TimedPositionWithSpeed>();

        this.intializeRaceHandle();

        LOGGER.info("Calling raceHandle.getRaces(): " + this.raceHandle);

        // wait for RaceDefinition to be completely wired in Regatta
        Set<RaceDefinition> races = this.raceHandle.getRaces();

        String regattaName = this.raceHandle.getRegatta().getName();

        Iterator<RaceDefinition> racIter = races.iterator();

        if (racIter.hasNext()) {
            RaceDefinition race = racIter.next();

            String raceName = race.getName();
            LOGGER.info("Race: \"" + raceName + "\", \"" + regattaName + "\"");

            RaceIdentifier raceIdentifier = new RegattaNameAndRaceName(regattaName, raceName);
            TrackedRace tr = this.service.getExistingTrackedRace(raceIdentifier);

            Iterable<Competitor> competitors = tr.getRace().getCompetitors();
            Iterator<Competitor> comIter = competitors.iterator();

            if (comIter.hasNext()) {

                Competitor competitor = comIter.next();
                GPSFixTrack<Competitor, GPSFixMoving> track = tr.getTrack(competitor);

                track.lockForRead();
                Iterable<GPSFixMoving> gpsFixes = track.getFixes();
                Iterator<GPSFixMoving> gpsIter = gpsFixes.iterator();

                int idx = 0;
                while ((gpsIter.hasNext()) && (idx < 1000)) {

                    GPSFixMoving gpsFix = gpsIter.next();
                    Position position = gpsFix.getPosition();
                    TimePoint timePoint = gpsFix.getTimePoint();

                    Wind gpsWind = tr.getWind(position, timePoint);

                    if (gpsWind.getKnots() == 1.0) {
                        gpsWind.scale(this.windScale);
                    }

                    path.addLast(new TimedPositionWithSpeedImpl(timePoint, position, gpsWind));

                    idx++;
                }
                track.unlockAfterRead();
            }
        }

        return new PathImpl(path, null);
    }

    public Path getPathPolyline(Distance maxDistance) {

        LinkedList<TimedPositionWithSpeed> path = new LinkedList<TimedPositionWithSpeed>();

        this.intializeRaceHandle();

        LOGGER.info("Calling raceHandle.getRaces(): " + this.raceHandle);

        // wait for RaceDefinition to be completely wired in Regatta
        Set<RaceDefinition> races = this.raceHandle.getRaces();

        String regattaName = this.raceHandle.getRegatta().getName();

        Iterator<RaceDefinition> racIter = races.iterator();

        if (racIter.hasNext()) {
            RaceDefinition race = racIter.next();

            String raceName = race.getName();
            LOGGER.info("Race: \"" + raceName + "\", \"" + regattaName + "\"");

            RaceIdentifier raceIdentifier = new RegattaNameAndRaceName(regattaName, raceName);
            TrackedRace tr = this.service.getExistingTrackedRace(raceIdentifier);

            Iterable<Competitor> competitors = tr.getRace().getCompetitors();
            Iterator<Competitor> comIter = competitors.iterator();

            if (comIter.hasNext()) {

                Competitor competitor = comIter.next();

                // TODO: following selection of marks is just for testing
                // final implementation should do selection of marks based on user input
                NavigableSet<MarkPassing> mps = tr.getMarkPassings(competitor);
                MarkPassing mp = mps.first(); // upwind 1
                mp = mps.higher(mp); // downwind 1
                mp = mps.higher(mp); // upwind 2
                TimePoint mpStart = mp.getTimePoint();
                Position wpStart = tr.getApproximatePosition(mp.getWaypoint(), mpStart);
                mp = mps.higher(mp);
                TimePoint mpEnd = mp.getTimePoint();
                Position wpEnd = tr.getApproximatePosition(mp.getWaypoint(), mpEnd);

                this.raceCourse = new LinkedList<TimedPositionWithSpeed>();
                Wind gpsWind = tr.getWind(wpStart, mpStart);
                this.raceCourse.addLast(new TimedPositionWithSpeedImpl(mpStart, wpStart, gpsWind));
                gpsWind = tr.getWind(wpEnd, mpEnd);
                this.raceCourse.addLast(new TimedPositionWithSpeedImpl(mpEnd, wpEnd, gpsWind));

                Iterable<GPSFixMoving> gpsFixes = tr.approximate(competitor, maxDistance, mpStart, mpEnd);
                Iterator<GPSFixMoving> gpsIter = gpsFixes.iterator();

                int idx = 0;
                while ((gpsIter.hasNext()) && (idx < 1000)) {

                    GPSFixMoving gpsFix = gpsIter.next();
                    Position position = gpsFix.getPosition();
                    TimePoint timePoint = gpsFix.getTimePoint();

                    gpsWind = tr.getWind(position, timePoint);

                    if (gpsWind.getKnots() == 1.0) {
                        gpsWind.scale(this.windScale);
                    }

                    path.addLast(new TimedPositionWithSpeedImpl(gpsFix.getTimePoint(), gpsFix.getPosition(), gpsWind));

                    idx++;
                }
            }
        }

        return new PathImpl(path, null);
    }

    public Path getRaceCourse() {
        return this.raceCourse == null ? null : new PathImpl(this.raceCourse, null);
    }

    public List<String> getLegsNames() {

        this.intializeRaceHandle();

        List<String> result = new ArrayList<String>();

        for (RaceDefinition race : this.raceHandle.getRaces()) {
            for (Leg leg : race.getCourse().getLegs()) {
                result.add(leg.toString());
            }
            break;
        }

        return result;
    }

    public Path getLeg(int selectedRaceIndex, int selectedCompetitorIndex, int selectedLegIndex) {

        this.intializeRaceHandle();

        // getting the first race
        RaceDefinition raceDef = this.raceHandle.getRaces().iterator().next();
        Regatta regatta = this.raceHandle.getRegatta();

        TrackedRace trackedRace = this.service.getTrackedRace(regatta, raceDef);

        Iterator<Competitor> competitors = raceDef.getCompetitors().iterator();
        Competitor competitor = null;

        for (int index = 0; index <= selectedCompetitorIndex; index++) {
            competitor = competitors.next();
        }

        Leg leg = raceDef.getCourse().getLegs().get(selectedLegIndex);

        TimePoint startTime = trackedRace.getMarkPassing(competitor, leg.getFrom()).getTimePoint();
        TimePoint endTime = trackedRace.getMarkPassing(competitor, leg.getTo()).getTimePoint();

        GPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(competitor);
        track.lockForRead();
        Iterator<GPSFixMoving> it = track.getFixesIterator(startTime, true);

        LinkedList<TimedPositionWithSpeed> path = new LinkedList<TimedPositionWithSpeed>();

        while (it.hasNext()) {
            GPSFixMoving gpsFix = it.next();
            if (gpsFix.getTimePoint().after(endTime)) {
                break;
            }

            Position position = gpsFix.getPosition();
            TimePoint timePoint = gpsFix.getTimePoint();

            Wind gpsWind = trackedRace.getWind(position, timePoint);

            if (gpsWind.getKnots() == 1.0) {
                gpsWind.scale(this.windScale);
            }

            path.addLast(new TimedPositionWithSpeedImpl(timePoint, position, gpsWind));
        }

        track.unlockAfterRead();

        return new PathImpl(path, null);
    }

    public Path getLegPolyline(int selectedRaceIndex, int selectedCompetitorIndex, int selectedLegIndex, Distance maxDistance) {

        this.intializeRaceHandle();

        // getting the first race
        RaceDefinition raceDef = this.raceHandle.getRaces().iterator().next();
        Regatta regatta = this.raceHandle.getRegatta();

        TrackedRace trackedRace = this.service.getTrackedRace(regatta, raceDef);

        Iterator<Competitor> competitors = raceDef.getCompetitors().iterator();
        Competitor competitor = null;

        for (int index = 0; index <= selectedCompetitorIndex; index++) {
            competitor = competitors.next();
        }

        LinkedList<TimedPositionWithSpeed> path = new LinkedList<TimedPositionWithSpeed>();
        this.raceCourse = new LinkedList<TimedPositionWithSpeed>();

        Leg leg = raceDef.getCourse().getLegs().get(selectedLegIndex);

        TimePoint startTime = trackedRace.getMarkPassing(competitor, leg.getFrom()).getTimePoint();
        Position startPosition = trackedRace.getApproximatePosition(leg.getFrom(), startTime);
        Wind startWind = trackedRace.getWind(startPosition, startTime);
        this.raceCourse.addLast(new TimedPositionWithSpeedImpl(startTime, startPosition, startWind));

        TimePoint endTime = trackedRace.getMarkPassing(competitor, leg.getTo()).getTimePoint();
        Position endPosition = trackedRace.getApproximatePosition(leg.getTo(), endTime);
        Wind endWind = trackedRace.getWind(endPosition, endTime);
        this.raceCourse.addLast(new TimedPositionWithSpeedImpl(endTime, endPosition, endWind));

        Iterable<GPSFixMoving> gpsFixes = trackedRace.approximate(competitor, maxDistance, startTime, endTime);
        Iterator<GPSFixMoving> gpsIter = gpsFixes.iterator();

        while (gpsIter.hasNext()) {
            GPSFixMoving gpsFix = gpsIter.next();
            if (gpsFix.getTimePoint().after(endTime)) {
                break;
            }

            Position position = gpsFix.getPosition();
            TimePoint timePoint = gpsFix.getTimePoint();

            Wind gpsWind = trackedRace.getWind(position, timePoint);

            if (gpsWind.getKnots() == 1.0) {
                gpsWind.scale(this.windScale);
            }

            path.addLast(new TimedPositionWithSpeedImpl(timePoint, position, gpsWind));
        }

        return new PathImpl(path, null);
    }

    public List<String> getComeptitorsNames() {

        this.intializeRaceHandle();

        List<String> result = new ArrayList<String>();

        for (RaceDefinition race : this.raceHandle.getRaces()) {
            for (Competitor competitor : race.getCompetitors()) {
                result.add(competitor.getName() + ", " + competitor.getBoat().getName());
            }

            break;
        }

        return result;

    }
}
