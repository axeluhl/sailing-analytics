package com.sap.sailing.domain.test;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Person;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.BuoyImpl;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.DegreePosition;
import com.sap.sailing.domain.base.impl.EventImpl;
import com.sap.sailing.domain.base.impl.GateImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.WindSource;
import com.sap.sailing.domain.tracking.impl.DynamicGPSFixMovingTrackImpl;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.tracking.impl.MarkPassingImpl;
import com.sap.sailing.domain.tracking.impl.TrackedEventImpl;
import com.sap.sailing.domain.tracking.impl.WindImpl;

public abstract class StoredTrackBasedTest {
    private static final String RESOURCES = "resources/";

    protected DynamicTrack<Competitor, GPSFixMoving> readTrack(Competitor competitor, String eventName) throws FileNotFoundException, IOException {
        DynamicTrack<Competitor, GPSFixMoving> track = null;
        if (getFile(competitor, eventName).exists()) {
            ObjectInput oi = getInputStream(competitor, eventName);
            track = new DynamicGPSFixMovingTrackImpl<Competitor>(competitor, /* millisecondsOverWhichToAverage */
                    40000);
            try {
                GPSFixMoving fix;
                while ((fix = readGPSFixMoving(oi)) != null) {
                    track.addGPSFix(fix);
                }
            } catch (EOFException eof) {
                oi.close();
            }
        }
        return track;
    }
    
    protected void copyTracks(Map<Competitor, DynamicTrack<Competitor, GPSFixMoving>> tracks, DynamicTrackedRace trackedRace) {
        for (Map.Entry<Competitor, DynamicTrack<Competitor, GPSFixMoving>> e : tracks.entrySet()) {
            DynamicTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(e.getKey());
            for (GPSFixMoving fix : e.getValue().getRawFixes()) {
                track.addGPSFix(fix);
            }
            List<MarkPassing> markPassings = new ArrayList<MarkPassing>();
            // add a mark passing for the start gate at the very beginning to make sure everyone is on a valid leg
            markPassings.add(new MarkPassingImpl(track.getFirstRawFix().getTimePoint(), trackedRace.getRace()
                    .getCourse().getWaypoints().iterator().next(), e.getKey()));
            trackedRace.updateMarkPassings(e.getKey(), markPassings);
        }
    }

    /**
     * Creates a simple two-lap upwind-downwind course for a race/event with given name and boat class name with the
     * competitors specified. The marks are laid out such that the upwind/downwind leg detection should be alright.
     */
    protected DynamicTrackedRace createTestTrackedRace(String eventName, String raceName, String boatClassName, Iterable<Competitor> competitors) {
        BoatClassImpl boatClass = new BoatClassImpl(boatClassName);
        Event event = new EventImpl(eventName, boatClass);
        TrackedEvent trackedEvent = new TrackedEventImpl(event);
        List<Waypoint> waypoints = new ArrayList<Waypoint>();
        // create a two-lap upwind/downwind course:
        BuoyImpl left = new BuoyImpl("Left lee gate buoy");
        BuoyImpl right = new BuoyImpl("Right lee gate buoy");
        ControlPoint leeGate = new GateImpl(left, right, "Lee Gate");
        Buoy windwardMark = new BuoyImpl("Windward mark");
        waypoints.add(new WaypointImpl(leeGate));
        waypoints.add(new WaypointImpl(windwardMark));
        waypoints.add(new WaypointImpl(leeGate));
        waypoints.add(new WaypointImpl(windwardMark));
        waypoints.add(new WaypointImpl(leeGate));
        Course course = new CourseImpl(raceName, waypoints);
        RaceDefinition race = new RaceDefinitionImpl(raceName, course, boatClass, competitors);
        DynamicTrackedRace trackedRace = new DynamicTrackedRaceImpl(trackedEvent, race, EmptyWindStore.INSTANCE,
                /* millisecondsOverWhichToAverageWind */ 30000, /* millisecondsOverWhichToAverageSpeed */ 30000);
        trackedRace.setWindSource(WindSource.WEB);
        DegreePosition topPosition = new DegreePosition(54.48, 10.24);
        trackedRace.getTrack(left).addGPSFix(new GPSFixImpl(new DegreePosition(54.4680424, 10.234451), new MillisecondsTimePoint(0)));
        trackedRace.getTrack(right).addGPSFix(new GPSFixImpl(new DegreePosition(54.4680424, 10.24), new MillisecondsTimePoint(0)));
        trackedRace.getTrack(windwardMark).addGPSFix(new GPSFixImpl(topPosition, new MillisecondsTimePoint(0)));
        trackedRace.getTrack(left).addGPSFix(new GPSFixImpl(new DegreePosition(54.4680424, 10.234451), MillisecondsTimePoint.now()));
        trackedRace.getTrack(right).addGPSFix(new GPSFixImpl(new DegreePosition(54.4680424, 10.24), MillisecondsTimePoint.now()));
        trackedRace.getTrack(windwardMark).addGPSFix(new GPSFixImpl(topPosition, MillisecondsTimePoint.now()));
        trackedRace.getWindTrack(WindSource.WEB).add(new WindImpl(topPosition, MillisecondsTimePoint.now(),
                new KnotSpeedWithBearingImpl(/* speedInKnots */ 14.7, new DegreeBearingImpl(180))));
        return trackedRace;
    }

    ObjectInput getInputStream(Competitor competitor, String eventName) throws FileNotFoundException, IOException {
        return new ObjectInputStream(new FileInputStream(getFile(competitor, eventName)));
    }

    ObjectOutput getOutputStream(Competitor competitor, String eventName) throws FileNotFoundException, IOException {
        return new ObjectOutputStream(new FileOutputStream(getFile(competitor, eventName)));
    }

    private File getFile(Competitor competitor, String eventName) {
        return new File(RESOURCES+eventName+"-"+competitor.getName());
    }
    
    private Set<String> getCompetitorNamesOfStoredTracks(String eventName) {
        Set<String> result = new HashSet<String>();
        File d = new File(RESOURCES);
        final String separator = "-";
        for (String s : d.list()) {
            if (s.startsWith(eventName+separator)) {
                result.add(s.substring(eventName.length()+separator.length()));
            }
        }
        return result;
    }

    private void writeGPSFixMoving(GPSFixMoving fix, ObjectOutput oo) throws IOException {
        oo.writeLong(fix.getTimePoint().asMillis());
        oo.writeDouble(fix.getPosition().getLatDeg());
        oo.writeDouble(fix.getPosition().getLngDeg());
        oo.writeDouble(fix.getSpeed().getKnots());
        oo.writeDouble(fix.getSpeed().getBearing().getDegrees());
    }

    private GPSFixMoving readGPSFixMoving(ObjectInput oi) throws IOException {
        TimePoint timePoint = new MillisecondsTimePoint(oi.readLong());
        Position position = new DegreePosition(oi.readDouble(), oi.readDouble());
        SpeedWithBearing speedWithBearing = new KnotSpeedWithBearingImpl(oi.readDouble(), new DegreeBearingImpl(oi.readDouble()));
        return new GPSFixMovingImpl(position, timePoint, speedWithBearing);
    }

    protected void storeTrack(Competitor competitor, DynamicTrack<Competitor, GPSFixMoving> track, String eventName)
            throws FileNotFoundException, IOException {
        ObjectOutput oo = getOutputStream(competitor, eventName);
        for (GPSFixMoving fix : track.getRawFixes()) {
            writeGPSFixMoving(fix, oo);
        }
        oo.close();
    }

    protected Map<Competitor, DynamicTrack<Competitor, GPSFixMoving>> loadTracks() throws FileNotFoundException, IOException {
        Map<Competitor, DynamicTrack<Competitor, GPSFixMoving>> tracks = new HashMap<Competitor, DynamicTrack<Competitor,GPSFixMoving>>();
        final String KIELER_WOCHE = "Kieler Woche";
        for (String competitorName : getCompetitorNamesOfStoredTracks(KIELER_WOCHE)) {
            Person p = new PersonImpl(competitorName, /* nationality */ null, /* dateOfBirth */ null, /* description */ null);
            Team t = new TeamImpl(competitorName, Collections.singleton(p), /* coach */ null);
            Competitor c = new CompetitorImpl(competitorName, competitorName, t, new BoatImpl(competitorName,
                    new BoatClassImpl("505")));
            DynamicTrack<Competitor, GPSFixMoving> track = readTrack(c, KIELER_WOCHE);
            if (track != null) {
                tracks.put(c, track);
            }
        }
        return tracks;
    }

}
