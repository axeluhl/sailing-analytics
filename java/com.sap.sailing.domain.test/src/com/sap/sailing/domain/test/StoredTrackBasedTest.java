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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Person;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.impl.DynamicGPSFixMovingTrackImpl;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;

public abstract class StoredTrackBasedTest extends TrackBasedTest {
    private static final String RESOURCES = "resources/";

    protected DynamicGPSFixTrack<Competitor, GPSFixMoving> readTrack(Competitor competitor, String regattaName) throws FileNotFoundException, IOException {
        DynamicGPSFixTrack<Competitor, GPSFixMoving> track = null;
        if (getFile(competitor, regattaName).exists()) {
            ObjectInput oi = getInputStream(competitor, regattaName);
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
    
    ObjectInput getInputStream(Competitor competitor, String regattaName) throws FileNotFoundException, IOException {
        return new ObjectInputStream(new FileInputStream(getFile(competitor, regattaName)));
    }

    ObjectOutput getOutputStream(Competitor competitor, String regattaName) throws FileNotFoundException, IOException {
        return new ObjectOutputStream(new FileOutputStream(getFile(competitor, regattaName)));
    }

    private File getFile(Competitor competitor, String regattaName) {
        return new File(RESOURCES + regattaName + "-" + competitor.getName()
                + (competitor.getBoat().getSailID() == null ? "" : "-" + competitor.getBoat().getSailID()));
    }
    
    private Set<String> getCompetitorNamesOfStoredTracks(String regattaName) {
        Set<String> result = new HashSet<String>();
        File d = new File(RESOURCES);
        final String separator = "-";
        for (String s : d.list()) {
            if (s.startsWith(regattaName+separator)) {
                result.add(s.substring(regattaName.length()+separator.length()));
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

    protected void storeTrack(Competitor competitor, DynamicGPSFixTrack<Competitor, GPSFixMoving> track, String regattaName)
            throws FileNotFoundException, IOException {
        ObjectOutput oo = getOutputStream(competitor, regattaName);
        for (GPSFixMoving fix : track.getRawFixes()) {
            writeGPSFixMoving(fix, oo);
        }
        oo.close();
    }

    protected Map<Competitor, DynamicGPSFixTrack<Competitor, GPSFixMoving>> loadTracks() throws FileNotFoundException, IOException {
        Map<Competitor, DynamicGPSFixTrack<Competitor, GPSFixMoving>> tracks = new HashMap<Competitor, DynamicGPSFixTrack<Competitor,GPSFixMoving>>();
        final String KIELER_WOCHE = "Kieler Woche";
        for (String competitorName : getCompetitorNamesOfStoredTracks(KIELER_WOCHE)) {
            Person p = new PersonImpl(competitorName, /* nationality */ null, /* dateOfBirth */ null, /* description */ null);
            Team t = new TeamImpl(competitorName, Collections.singleton(p), /* coach */ null);
            Competitor c = new CompetitorImpl(competitorName, competitorName, t, new BoatImpl(competitorName,
                    new BoatClassImpl("505", /* typicallyStartsUpwind */ true), null));
            DynamicGPSFixTrack<Competitor, GPSFixMoving> track = readTrack(c, KIELER_WOCHE);
            if (track != null) {
                tracks.put(c, track);
            }
        }
        return tracks;
    }

}
