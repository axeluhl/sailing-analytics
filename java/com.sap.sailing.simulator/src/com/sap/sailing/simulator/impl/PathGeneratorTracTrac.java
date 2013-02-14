package com.sap.sailing.simulator.impl;

import java.net.URI;
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
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
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
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.TimedPositionWithSpeed;

public class PathGeneratorTracTrac extends PathGeneratorBase {

    private static final Logger LOGGER = Logger.getLogger("com.sap.sailing");

    @SuppressWarnings("unused")
    private SimulationParameters simulationParameters;
    private URL paramUrl;
    private URI liveUri;
    private URI storedUri;
    private RacingEventServiceImpl service;
    private RacesHandle raceHandle;
    private LinkedList<TimedPositionWithSpeed> racecourse;
    private double windScale;

    public PathGeneratorTracTrac(SimulationParameters params) {
        this.simulationParameters = params;
        this.service = new RacingEventServiceImpl();
    }

    public void setEvaluationParameters(String paramURLStr, String liveURIStr, String storedURIStr, double windScale) {
        try {
            this.paramUrl = new URL(paramURLStr);
            if (liveURIStr != null) {
                this.liveUri = new URI(liveURIStr);
            } else {
                this.liveUri = null;
            }
            if (storedURIStr != null) {
                this.storedUri = new URI(storedURIStr);
            } else {
                this.storedUri = null;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.windScale = windScale;
    }

    private void intializeRaceHandle() {

        if (this.raceHandle == null) {
            try {
                this.raceHandle = this.service.addTracTracRace(paramUrl, liveUri, storedUri, EmptyWindStore.INSTANCE, /* timeoutInMilliseconds */60000, this);
                synchronized (this) {
                    this.wait();
                }
            } catch (Exception e) {
                LOGGER.warning(e.getMessage());
            }
        }
    }

    public List<Path> getAllLegsPathsFirstCompetitor() {

        List<Path> paths = new ArrayList<Path>();

        intializeRaceHandle();

        Set<RaceDefinition> races = this.raceHandle.getRaces();
        Iterator<RaceDefinition> racesIterator = races.iterator();
        if (racesIterator.hasNext() == false) {
            return null;
        }
        RaceDefinition race = racesIterator.next();

        List<Leg> legs = race.getCourse().getLegs();
        RaceIdentifier raceIdentifier = new RegattaNameAndRaceName(this.raceHandle.getRegatta().getName(), race.getName());
        TrackedRace trackedRace = this.service.getExistingTrackedRace(raceIdentifier);

        Iterable<Competitor> competitors = trackedRace.getRace().getCompetitors();
        Iterator<Competitor> competitorsIterator = competitors.iterator();
        if (competitorsIterator.hasNext() == false) {
            return null;
        }
        Competitor firstCompetitor = competitorsIterator.next();

        List<TimedPositionWithSpeed> points = null;
        GPSFixMoving currentGPSFixMoving = null;
        MarkPassing markPassing = null;
        Position currentPosition = null;
        TimePoint currentTimePoint = null;
        SpeedWithBearing currentSpeedWithBearing = null;
        Wind wind = null;

        GPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(firstCompetitor);
        Iterator<GPSFixMoving> fixesIterator = track.getFixes().iterator();

        track.lockForRead();
        for (Leg leg : legs) {

            points = new ArrayList<TimedPositionWithSpeed>();
            markPassing = trackedRace.getMarkPassing(firstCompetitor, leg.getTo());

            while (markPassing.getTimePoint().after((currentGPSFixMoving = fixesIterator.next()).getTimePoint())) {

                currentPosition = currentGPSFixMoving.getPosition();
                currentTimePoint = currentGPSFixMoving.getTimePoint();
                currentSpeedWithBearing = currentGPSFixMoving.getSpeed();

                wind = trackedRace.getWind(currentPosition, currentTimePoint);

                if (wind.getKnots() == 1.0) {
                    currentSpeedWithBearing = new KnotSpeedWithBearingImpl(currentSpeedWithBearing.getKnots() * this.windScale,
                            currentSpeedWithBearing.getBearing());
                }

                points.add(new TimedPositionWithSpeedImpl(currentTimePoint, currentPosition, currentSpeedWithBearing));
            }

        }
        track.unlockAfterRead();

        return paths;
    }

    @Override
    public Path getPath() {

        LinkedList<TimedPositionWithSpeed> path = new LinkedList<TimedPositionWithSpeed>();

        intializeRaceHandle();

        // logger.info("Calling raceHandle.getRaces(): " + raceHandle);
        Set<RaceDefinition> races = raceHandle.getRaces(); // wait for RaceDefinition to be completely wired in Regatta

        String regatta = raceHandle.getRegatta().getName();

        Iterator<RaceDefinition> racIter = races.iterator();
        // while (racIter.hasNext()) {
        if (racIter.hasNext()) {
            RaceDefinition race = racIter.next();
            System.out.println("Race: \"" + race.getName() + "\", \"" + regatta + "\"");

            RaceIdentifier raceIdentifier = new RegattaNameAndRaceName(regatta, race.getName());
            TrackedRace tr = service.getExistingTrackedRace(raceIdentifier);

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
                    Wind gpsWind = tr.getWind(gpsFix.getPosition(), gpsFix.getTimePoint());

                    if (gpsWind.getKnots() == 1.0) {
                        Wind scaledWind = new WindImpl(gpsFix.getPosition(), gpsFix.getTimePoint(), new KnotSpeedWithBearingImpl(windScale*gpsWind.getKnots(), gpsWind.getBearing()));
                        //System.out.println("wind: "+scaledWind.getKnots());
                        path.addLast(new TimedPositionWithSpeedImpl(gpsFix.getTimePoint(), gpsFix.getPosition(), scaledWind));
                    } else {
                        path.addLast(new TimedPositionWithSpeedImpl(gpsFix.getTimePoint(), gpsFix.getPosition(), gpsWind));
                    }

                    idx++;
                }
                track.unlockAfterRead();
            }
        }

        // return new PathImpl(path, simulationParameters.getWindField());
        return new PathImpl(path, null);
    }

    public Path getPathPolyline(Distance maxDistance) {

        LinkedList<TimedPositionWithSpeed> path = new LinkedList<TimedPositionWithSpeed>();

        if (service == null) {
            service = new RacingEventServiceImpl();
        }
        // logger.info("Calling service.addTracTracRace");
        if (raceHandle == null) {
            try {
                raceHandle = service.addTracTracRace(paramUrl, liveUri, storedUri, EmptyWindStore.INSTANCE, /* timeoutInMilliseconds */60000, this);
                synchronized (this) {
                    this.wait();
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        // logger.info("Calling raceHandle.getRaces(): " + raceHandle);
        Set<RaceDefinition> races = raceHandle.getRaces(); // wait for RaceDefinition to be completely wired in Regatta
        // logger.info("Obtained races: " + races.size());

        String regatta = raceHandle.getRegatta().getName();

        Iterator<RaceDefinition> racIter = races.iterator();
        // while (racIter.hasNext()) {
        if (racIter.hasNext()) {
            RaceDefinition race = racIter.next();
            System.out.println("Race: \"" + race.getName() + "\", \"" + regatta + "\"");

            RaceIdentifier raceIdentifier = new RegattaNameAndRaceName(regatta, race.getName());
            TrackedRace tr = service.getExistingTrackedRace(raceIdentifier);

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

                racecourse = new LinkedList<TimedPositionWithSpeed>();
                Wind gpsWind = tr.getWind(wpStart, mpStart);
                racecourse.addLast(new TimedPositionWithSpeedImpl(mpStart, wpStart, gpsWind));
                gpsWind = tr.getWind(wpEnd, mpEnd);
                racecourse.addLast(new TimedPositionWithSpeedImpl(mpEnd, wpEnd, gpsWind));

                Iterable<GPSFixMoving> gpsFixes = tr.approximate(competitor, maxDistance, mpStart, mpEnd);
                Iterator<GPSFixMoving> gpsIter = gpsFixes.iterator();

                int idx = 0;
                while ((gpsIter.hasNext()) && (idx < 1000)) {

                    GPSFixMoving gpsFix = gpsIter.next();
                    gpsWind = tr.getWind(gpsFix.getPosition(), gpsFix.getTimePoint());

                    if (gpsWind.getKnots() == 1.0) {
                        Wind scaledWind = new WindImpl(gpsFix.getPosition(), gpsFix.getTimePoint(), new KnotSpeedWithBearingImpl(windScale*gpsWind.getKnots(), gpsWind.getBearing()));
                        //System.out.println("wind: "+scaledWind.getKnots());
                        path.addLast(new TimedPositionWithSpeedImpl(gpsFix.getTimePoint(), gpsFix.getPosition(), scaledWind));
                    } else {
                        path.addLast(new TimedPositionWithSpeedImpl(gpsFix.getTimePoint(), gpsFix.getPosition(), gpsWind));
                    }

                    idx++;
                }
                //track.unlockAfterRead();
            }
        }

        // return new PathImpl(path, simulationParameters.getWindField());
        return new PathImpl(path, null);
    }

    public Path getRaceCourse() {
        return new PathImpl(racecourse, null);
    }

}
