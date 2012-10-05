package com.sap.sailing.simulator.impl;

import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NavigableSet;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceDefinition;
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
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.TimedPositionWithSpeed;

public class PathGeneratorTracTrac extends PathGeneratorBase {

    // private static Logger logger = Logger.getLogger("com.sap.sailing");
    @SuppressWarnings("unused")
    private SimulationParameters simulationParameters;
    private URL paramUrl;
    private URI liveUri;
    private URI storedUri;
    private RacingEventServiceImpl service;
    private RacesHandle raceHandle;
    private LinkedList<TimedPositionWithSpeed> racecourse;
    
    public PathGeneratorTracTrac(SimulationParameters params) {
        simulationParameters = params;
    }

    public void setEvaluationParameters(String paramURLStr, String liveURIStr, String storedURIStr) {
        try {
            this.paramUrl = new URL(paramURLStr);
            this.liveUri = new URI(liveURIStr);
            this.storedUri = new URI(storedURIStr);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public Path getPath() {

        LinkedList<TimedPositionWithSpeed> path = new LinkedList<TimedPositionWithSpeed>();

        if (service == null) {
            service = new RacingEventServiceImpl();
        }
        // logger.info("Calling service.addTracTracRace");
        raceHandle = null;
        try {
            raceHandle = service.addTracTracRace(paramUrl, liveUri, storedUri, EmptyWindStore.INSTANCE, /* timeoutInMilliseconds */60000, this);
            synchronized (this) {
                this.wait();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
                GPSFixTrack<Competitor, GPSFixMoving> track = tr.getTrack(competitor);
                track.lockForRead();
                Iterable<GPSFixMoving> gpsFixes = track.getFixes();
                Iterator<GPSFixMoving> gpsIter = gpsFixes.iterator();

                int idx = 0;
                while ((gpsIter.hasNext()) && (idx < 1000)) {

                    GPSFixMoving gpsFix = gpsIter.next();
                    Wind gpsWind = tr.getWind(gpsFix.getPosition(), gpsFix.getTimePoint());
                    path.addLast(new TimedPositionWithSpeedImpl(gpsFix.getTimePoint(), gpsFix.getPosition(), gpsWind));

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
                    path.addLast(new TimedPositionWithSpeedImpl(gpsFix.getTimePoint(), gpsFix.getPosition(), gpsWind));

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
