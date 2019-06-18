package com.sap.sailing.domain.orc.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.orc.ORCCertificate;
import com.sap.sailing.domain.orc.ORCPerformanceCurve;
import com.sap.sailing.domain.orc.ORCPerformanceCurveCourse;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.common.impl.DegreeBearingImpl;

//TODO Finish this comment.
/**
 * For a {@link Competitor} 
 * https://orc.org/index.asp?id=23
 * 
 * @author Daniel Lisunkin (i505543)
 *
 */

public class ORCCertificateImpl implements ORCCertificate {

    /**
     * Equals the column heading of the allowances table of an ORC certificate. The speeds are set by the offshore racing congress.
     */
    public static final Speed[] ALLOWANCES_TRUE_WIND_SPEEDS = {new KnotSpeedImpl( 6),
                                                               new KnotSpeedImpl( 8),
                                                               new KnotSpeedImpl(10),
                                                               new KnotSpeedImpl(12),
                                                               new KnotSpeedImpl(14),
                                                               new KnotSpeedImpl(16),
                                                               new KnotSpeedImpl(20) };
    
    /**
     * Equals the line heading of the allowances table of an ORC certificate. The true wind angles are set by the offshore racing congress.
     */
    public static final Bearing[] ALLOWANCES_TRUE_WIND_ANGLES = {new DegreeBearingImpl( 52),
                                                                 new DegreeBearingImpl( 60),
                                                                 new DegreeBearingImpl( 75),
                                                                 new DegreeBearingImpl( 90),
                                                                 new DegreeBearingImpl(110),
                                                                 new DegreeBearingImpl(120),
                                                                 new DegreeBearingImpl(135),
                                                                 new DegreeBearingImpl(150) };
    
    /**
     * Equals the percentages of accounted allowances at a given speed for the Coastal / Long Distance Performance Curve metric.
     * Details can be found on: https://orc.org/index.asp?id=32
     */
    public static final Map<Speed, Map<Bearing, Double>> perCentOfAllowancesForLongDistancePC;
    static {
        Map<Speed, Map<Bearing, Double>> result = new HashMap<>();
        Map<Bearing, Double> map6kt = new HashMap<>();
        Map<Bearing, Double> map8kt = new HashMap<>();
        Map<Bearing, Double> map10kt = new HashMap<>();
        Map<Bearing, Double> map12kt = new HashMap<>();
        Map<Bearing, Double> map14kt = new HashMap<>();
        Map<Bearing, Double> map16kt = new HashMap<>();
        Map<Bearing, Double> map20kt = new HashMap<>();
        
        map6kt.put(new DegreeBearingImpl(0)      , 0.45);
        map6kt.put(ALLOWANCES_TRUE_WIND_ANGLES[1], 0.0);
        map6kt.put(ALLOWANCES_TRUE_WIND_ANGLES[3], 0.0);
        map6kt.put(ALLOWANCES_TRUE_WIND_ANGLES[5], 0.0);
        map6kt.put(ALLOWANCES_TRUE_WIND_ANGLES[7], 0.0);
        map6kt.put(new DegreeBearingImpl(180)    , 0.55);
        
        map8kt.put(new DegreeBearingImpl(0)      , 0.40);
        map8kt.put(ALLOWANCES_TRUE_WIND_ANGLES[1], 0.05);
        map8kt.put(ALLOWANCES_TRUE_WIND_ANGLES[3], 0.05);
        map8kt.put(ALLOWANCES_TRUE_WIND_ANGLES[5], 0.05);
        map8kt.put(ALLOWANCES_TRUE_WIND_ANGLES[7], 0.05);
        map8kt.put(new DegreeBearingImpl(180)    , 0.40);
        
        map10kt.put(new DegreeBearingImpl(0)      , 0.35);
        map10kt.put(ALLOWANCES_TRUE_WIND_ANGLES[1], 0.10);
        map10kt.put(ALLOWANCES_TRUE_WIND_ANGLES[3], 0.075);
        map10kt.put(ALLOWANCES_TRUE_WIND_ANGLES[5], 0.10);
        map10kt.put(ALLOWANCES_TRUE_WIND_ANGLES[7], 0.10);
        map10kt.put(new DegreeBearingImpl(180)    , 0.275);
        
        map12kt.put(new DegreeBearingImpl(0)      , 0.30);
        map12kt.put(ALLOWANCES_TRUE_WIND_ANGLES[1], 0.15);
        map12kt.put(ALLOWANCES_TRUE_WIND_ANGLES[3], 0.10);
        map12kt.put(ALLOWANCES_TRUE_WIND_ANGLES[5], 0.15);
        map12kt.put(ALLOWANCES_TRUE_WIND_ANGLES[7], 0.15);
        map12kt.put(new DegreeBearingImpl(180)    , 0.15);
        
        map14kt.put(new DegreeBearingImpl(0)      , 0.25);
        map14kt.put(ALLOWANCES_TRUE_WIND_ANGLES[1], 0.175);
        map14kt.put(ALLOWANCES_TRUE_WIND_ANGLES[3], 0.125);
        map14kt.put(ALLOWANCES_TRUE_WIND_ANGLES[5], 0.175);
        map14kt.put(ALLOWANCES_TRUE_WIND_ANGLES[7], 0.15);
        map14kt.put(new DegreeBearingImpl(180)    , 0.125);
        
        map16kt.put(new DegreeBearingImpl(0)      , 0.20);
        map16kt.put(ALLOWANCES_TRUE_WIND_ANGLES[1], 0.20);
        map16kt.put(ALLOWANCES_TRUE_WIND_ANGLES[3], 0.15);
        map16kt.put(ALLOWANCES_TRUE_WIND_ANGLES[5], 0.20);
        map16kt.put(ALLOWANCES_TRUE_WIND_ANGLES[7], 0.15);
        map16kt.put(new DegreeBearingImpl(180)    , 0.10);
        
        map20kt.put(new DegreeBearingImpl(0)      , 0.10);
        map20kt.put(ALLOWANCES_TRUE_WIND_ANGLES[1], 0.25);
        map20kt.put(ALLOWANCES_TRUE_WIND_ANGLES[3], 0.20);
        map20kt.put(ALLOWANCES_TRUE_WIND_ANGLES[5], 0.25);
        map20kt.put(ALLOWANCES_TRUE_WIND_ANGLES[7], 0.10);
        map20kt.put(new DegreeBearingImpl(180)    , 0.10);
        
        result.put(ALLOWANCES_TRUE_WIND_SPEEDS[0], Collections.unmodifiableMap(map6kt));
        result.put(ALLOWANCES_TRUE_WIND_SPEEDS[1], Collections.unmodifiableMap(map8kt));
        result.put(ALLOWANCES_TRUE_WIND_SPEEDS[2], Collections.unmodifiableMap(map10kt));
        result.put(ALLOWANCES_TRUE_WIND_SPEEDS[3], Collections.unmodifiableMap(map12kt));
        result.put(ALLOWANCES_TRUE_WIND_SPEEDS[4], Collections.unmodifiableMap(map14kt));
        result.put(ALLOWANCES_TRUE_WIND_SPEEDS[5], Collections.unmodifiableMap(map16kt));
        result.put(ALLOWANCES_TRUE_WIND_SPEEDS[6], Collections.unmodifiableMap(map20kt));
        perCentOfAllowancesForLongDistancePC = Collections.unmodifiableMap(result);
    }
    
    /**
     * 
     */
    private final String sailnumber;
    private final String boatclass;
    private final Distance lengthOverAll;
    private final Duration gph;
    private final Double cdl;
    private final Map<Speed, Map<Bearing, Duration>> timeAllowancesPerTrueWindSpeedAndAngle;
    private final Map<Speed, Bearing> beatAngles;
    private final Map<Speed, Bearing> gybeAngles;

    //TODO Comment on Constructor
    /**
     * 
     * @param sailnumber
     * @param boatclass
     * @param length
     * @param gph
     * @param timeAllowancesPerTrueWindSpeedAndAngle
     * @param beatAngles
     * @param gybeAngles
     */
    public ORCCertificateImpl(String sailnumber, String boatclass, Distance length, Duration gph, Double cdl,
            Map<Speed, Map<Bearing, Duration>> timeAllowancesPerTrueWindSpeedAndAngle, Map<Speed, Bearing> beatAngles,
            Map<Speed, Bearing> gybeAngles) {
        super();
        this.sailnumber = sailnumber;
        this.boatclass = boatclass;
        this.lengthOverAll = length;
        this.gph = gph;
        this.cdl = cdl;
        this.timeAllowancesPerTrueWindSpeedAndAngle = Collections.unmodifiableMap(timeAllowancesPerTrueWindSpeedAndAngle);
        this.beatAngles = Collections.unmodifiableMap(beatAngles);
        this.gybeAngles = Collections.unmodifiableMap(gybeAngles);
    }

    @Override
    public ORCPerformanceCurve getPerformanceCurve(ORCPerformanceCurveCourse course) {
        return new ORCPerformanceCurveImpl(timeAllowancesPerTrueWindSpeedAndAngle, beatAngles, gybeAngles, course);
    }

    @Override
    public double getGPH() {
        return gph.asSeconds();
    }

    @Override
    public Map<Speed, Duration> getWindwardLeewardAllowances() {
        Map<Speed, Duration> result = new HashMap<>();
        for (Speed tws : ALLOWANCES_TRUE_WIND_SPEEDS) {
            //gets the Allowance for Beat&Run at the given TWS divided by 2
            Duration allowance = timeAllowancesPerTrueWindSpeedAndAngle.get(tws).get(beatAngles.get(tws))
                    .plus(timeAllowancesPerTrueWindSpeedAndAngle.get(tws).get(gybeAngles.get(tws))).divide(2);
            result.put(tws, allowance);
        }
        return result;
    }

    @Override
    public Map<Speed, Duration> getCircularRandomAllowances() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Speed, Duration> getLongDistanceAllowances() {
        Map<Speed, Duration> result = new HashMap<>();
        for (Entry<Speed, Map<Bearing, Double>> twsEntry : perCentOfAllowancesForLongDistancePC.entrySet()) {
            double allowanceInSec = 0.0;
            for (Entry<Bearing, Double> twaEntry : twsEntry.getValue().entrySet()) {
                if(twaEntry.getKey().equals(new DegreeBearingImpl(0))) {
                    allowanceInSec += twaEntry.getValue() * timeAllowancesPerTrueWindSpeedAndAngle.get(twsEntry.getKey()).get(beatAngles.get(twsEntry.getKey())).asSeconds();
                }
                else if (twaEntry.getKey().equals(new DegreeBearingImpl(180))) {
                    allowanceInSec += twaEntry.getValue() * timeAllowancesPerTrueWindSpeedAndAngle.get(twsEntry.getKey()).get(gybeAngles.get(twsEntry.getKey())).asSeconds();
                }
                else {
                    allowanceInSec += twaEntry.getValue() * timeAllowancesPerTrueWindSpeedAndAngle.get(twsEntry.getKey()).get(twaEntry.getKey()).asSeconds();
                }
            }
            result.put(twsEntry.getKey(), Duration.ONE_SECOND.times(allowanceInSec));
        }
        return result;
    }
    
    @Override
    public Map<Speed, Duration> getNonSpinnakerAllowances() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getSailnumber() {
        return sailnumber;
    }

    public String getBoatclass() {
        return boatclass;
    }

    public Distance getLengthOverAll() {
        return lengthOverAll;
    }

    @Override
    public double getCDL() {
        return cdl;
    }
}
