package com.sap.sailing.domain.orc.impl;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.orc.ORCCertificate;
import com.sap.sailing.domain.orc.ORCPerformanceCurve;
import com.sap.sailing.domain.orc.ORCPerformanceCurveCourse;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.common.impl.DegreeBearingImpl;

/**
 * For a {@link Competitor} 
 * https://orc.org/index.asp?id=23
 * 
 * @author Daniel Lisunkin (I505543)
 *
 */

// TODO Improve public API in regards of semantic connection with real ORC Certificate (paper)
// TODO COMMENTS!

public class ORCCertificateImpl implements ORCCertificate {

    /*
     * Equals the column heading of the allowances table of an ORC certificate. The speeds are set by the offshore racing congress.
     */
    public static final Speed[] ALLOWANCES_TRUE_WIND_SPEEDS = {new KnotSpeedImpl( 6),
                                                               new KnotSpeedImpl( 8),
                                                               new KnotSpeedImpl(10),
                                                               new KnotSpeedImpl(12),
                                                               new KnotSpeedImpl(14),
                                                               new KnotSpeedImpl(16),
                                                               new KnotSpeedImpl(20) };
    
    /*
     * 
     */
    public static final Bearing[] ALLOWANCES_TRUE_WIND_ANGLES = {new DegreeBearingImpl( 52),
                                                                 new DegreeBearingImpl( 60),
                                                                 new DegreeBearingImpl( 75),
                                                                 new DegreeBearingImpl( 90),
                                                                 new DegreeBearingImpl(110),
                                                                 new DegreeBearingImpl(120),
                                                                 new DegreeBearingImpl(135),
                                                                 new DegreeBearingImpl(150) };
    
    /*
     * 
     */
    
    private final String sailnumber;
    private final String boatclass;
    private final Distance lengthOverAll;
    
    private final Map<Speed, Map<Bearing, Duration>> timeAllowancesPerTrueWindSpeedAndAngle;
    private final Map<Speed, Bearing> beatAngles;
    private final Map<Speed, Bearing> gybeAngles;


    public ORCCertificateImpl(String sailnumber, String boatclass, Distance length,
            Map<Speed, Map<Bearing, Duration>> timeAllowancesPerTrueWindSpeedAndAngle, Map<Speed, Bearing> beatAngles,
            Map<Speed, Bearing> gybeAngles) {
        super();
        this.sailnumber = sailnumber;
        this.boatclass = boatclass;
        this.lengthOverAll = length;
        this.timeAllowancesPerTrueWindSpeedAndAngle = timeAllowancesPerTrueWindSpeedAndAngle;
        this.beatAngles = beatAngles;
        this.gybeAngles = gybeAngles;
    }

    @Override
    public ORCPerformanceCurve getPerformanceCurve(ORCPerformanceCurveCourse course) {
        return new ORCPerformanceCurveImpl(timeAllowancesPerTrueWindSpeedAndAngle, beatAngles, gybeAngles, course);
    }

    @Override
    public double getGPH() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Map<Speed, Duration> getWindwardLeewardAllowances() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Speed, Duration> getCircularRandomAllowances() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Speed, Duration> getLongDistanceAllowances() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Speed, Duration> getNonSpinnakerAllowances() {
        // TODO Auto-generated method stub
        return null;
    }
}
