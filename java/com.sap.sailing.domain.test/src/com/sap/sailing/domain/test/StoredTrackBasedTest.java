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

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.DegreePosition;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.impl.DynamicGPSFixMovingTrackImpl;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;

public abstract class StoredTrackBasedTest {
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

    ObjectInput getInputStream(Competitor competitor, String eventName) throws FileNotFoundException, IOException {
        return new ObjectInputStream(new FileInputStream(getFile(competitor, eventName)));
    }

    ObjectOutput getOutputStream(Competitor competitor, String eventName) throws FileNotFoundException, IOException {
        return new ObjectOutputStream(new FileOutputStream(getFile(competitor, eventName)));
    }

    private File getFile(Competitor competitor, String eventName) {
        return new File("resources/"+eventName+"-"+competitor.getName());
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

}
