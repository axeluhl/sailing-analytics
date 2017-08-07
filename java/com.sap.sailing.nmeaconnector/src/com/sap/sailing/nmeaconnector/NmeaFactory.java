package com.sap.sailing.nmeaconnector;

import java.io.InputStream;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.nmeaconnector.impl.NmeaFactoryImpl;

import net.sf.marineapi.nmea.sentence.MWVSentence;
import net.sf.marineapi.nmea.sentence.ZDASentence;

public interface NmeaFactory {
    static NmeaFactory INSTANCE = new NmeaFactoryImpl();
    
    NmeaUtil getUtil();
    
    /**
     * Reads from a series of NMEA 0183 sentences and looks for timing information in the form of {@link ZDASentence}s
     * as well as for weather records ({@link MWVSentence} sentences). If multiple {@code MWV} sentences are found between
     * two time stamps, linear interpolation is used to assign time points to each wind reading. The information is
     * furthermore combined with position and motion data. If the wind readings are apparent readings, the speed of
     * the wind sensor is taken into account to transform into a true reading. NMEA specifies that a "true" reading
     * is relative to "true north" which we can only believe; it means that the instrument needs to have an understanding
     * of the magnetic declination at the time and position of the measurement.
     * 
     * @param inputStream
     *            will be consumed up to its end but will not be closed; the caller remains responsible for closing the
     *            input stream.
     * @return a sequence of {@link Wind} objects as extracted from the NMEA 0183 sentences found in the contents of the
     *         {@code reader}; never {@code null}, but possibly empty
     */
    Iterable<Wind> readWind(InputStream inputStream) throws InterruptedException;
}
