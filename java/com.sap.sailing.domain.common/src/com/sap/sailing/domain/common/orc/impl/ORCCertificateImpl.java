package com.sap.sailing.domain.common.orc.impl;

import java.util.Collections;
import java.util.Map;

import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.impl.NauticalMileDistance;
import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.DegreeBearingImpl;

//TODO Finish this comment.
/**
 * For a {@link Competitor} https://orc.org/index.asp?id=23
 * 
 * @author Daniel Lisunkin (i505543)
 *
 */

public class ORCCertificateImpl implements ORCCertificate {

    private static final long serialVersionUID = 8725162998514202782L;
    
    private final String idConsistingOfNatAuthCertNoAndBIN;

    /**
     * Equals the column heading of the allowances table of an ORC certificate. The speeds are set by the offshore
     * racing congress. The speeds occur in the array in ascending order.
     * 
     * There are references in the persistance module. If the values change, there will be an adjustment needed
     * in {@link MongoObjectFactoryImpl.speedToKnotsString}.
     */
    public static final Speed[] ALLOWANCES_TRUE_WIND_SPEEDS = { new KnotSpeedImpl(6), new KnotSpeedImpl(8),
            new KnotSpeedImpl(10), new KnotSpeedImpl(12), new KnotSpeedImpl(14), new KnotSpeedImpl(16),
            new KnotSpeedImpl(20) };

    /**
     * Equals the line heading of the allowances table of an ORC certificate. The true wind angles are set by the
     * offshore racing congress. The angles occur in the array in ascending order.
     * 
     * There are references in the persistance module. If the values change, there will be an adjustment needed
     * in {@link MongoObjectFactoryImpl.bearingToDegreeString}.
     */
    public static final Bearing[] ALLOWANCES_TRUE_WIND_ANGLES = { new DegreeBearingImpl(52), new DegreeBearingImpl(60),
            new DegreeBearingImpl(75), new DegreeBearingImpl(90), new DegreeBearingImpl(110),
            new DegreeBearingImpl(120), new DegreeBearingImpl(135), new DegreeBearingImpl(150) };

    public final static Distance NAUTICAL_MILE = new NauticalMileDistance(1);
    
    public final static double PI = 3.14159265358979;

    private final String sailnumber;
    private final String boatName;
    private final String boatclass;
    private final Distance lengthOverAll;
    private final Duration gph;
    private final Double cdl;
    private final TimePoint issueDate;

    // TODO add meaningful Javadoc
    private final Map<Speed, Map<Bearing, Speed>> velocityPredictionPerTrueWindSpeedAndAngle;

    /**
     * The beat angles for the true wind speeds; key set is equal to that of
     * {@link #velocityPredictionPerTrueWindSpeedAndAngle}.
     */
    private final Map<Speed, Bearing> beatAngles;
    
    /**
     * The VMG the boat represented by this certificate sails at when sailing upwind, meaning
     * {@code Math.abs(TWA) <= beatAngles.get(TWS)}.
     */
    private final Map<Speed, Speed> beatVMGPredictionPerTrueWindSpeed;

    /**
     * The duration in seconds per nautical mile the boat represented by this certificate is allowed to sail at 100%
     * performance and a given windspeed, when sailing upwind. Key set is equal to that of
     * {@link #velocityPredictionPerTrueWindSpeedAndAngle}.
     */
    private final Map<Speed, Duration> beatAllowancePerTrueWindSpeed;

    /**
     * The run angles for the true wind speeds; note that the gybing angle is then {@code 180-2*runAngle}. The key set
     * is equal to that of {@link #durationPerNauticalMileAtTrueWindAngleAndSpeed}. In the certificates this is referred
     * to as "gybe angles" but this seems inconsistent as it really describes the TWAs sailed at on a downwind leg, not
     * the gybe angles.
     */
    private final Map<Speed, Bearing> runAngles;

    /**
     * The VMG the boat represented by this certificate sails at when sailing upwind, meaning
     * {@code Math.abs(TWA) <= runAngles.get(TWS)}.
     */
    private final Map<Speed, Speed> runVMGPredictionPerTrueWindSpeed;
    
    /**
     * The duration in seconds per nautical mile the boat represented by this certificate is allowed to sail at 100%
     * performance and a given windspeed, when sailing downwind. Key set is equal to that of
     * {@link #velocityPredictionPerTrueWindSpeedAndAngle}.
     */
    private final Map<Speed, Duration> runAllowancePerTrueWindSpeed;
    
    private final Map<Speed, Speed> windwardLeewardSpeedPredictionPerTrueWindSpeed;
    
    private final Map<Speed, Speed> longDistanceSpeedPredictionPerTrueWindSpeed;
    
    private final Map<Speed, Speed> circularRandomSpeedPredictionPerTrueWindSpeed;
    
    private final Map<Speed, Speed> nonSpinnakerSpeedPredictionPerTrueWindSpeed;

    public ORCCertificateImpl(String idConsistingOfNatAuthCertNoAndBIN,
            String sailnumber, String boatName, String boatclass,
            Distance length, Duration gph,
            Double cdl, TimePoint issueDate,
            Map<Speed, Map<Bearing, Speed>> velocityPredictionsPerTrueWindSpeedAndAngle, 
            Map<Speed, Bearing> beatAngles, Map<Speed, Speed> beatVMGPredictionPerTrueWindSpeed,
            Map<Speed, Duration> beatAllowancePerTrueWindSpeed, Map<Speed, Bearing> runAngles,
            Map<Speed, Speed> runVMGPredictionPerTrueWindSpeed,
            Map<Speed, Duration> runAllowancePerTrueWindSpeed,
            Map<Speed, Speed> windwardLeewardSpeedPredictionsPerTrueWindSpeed,
            Map<Speed, Speed> longDistanceSpeedPredictionsPerTrueWindSpeed,
            Map<Speed, Speed> circularRandomSpeedPredictionsPerTrueWindSpeed,
            Map<Speed, Speed> nonSpinnakerSpeedPredictionsPerTrueWindSpeed) {
        this.idConsistingOfNatAuthCertNoAndBIN = idConsistingOfNatAuthCertNoAndBIN;
        this.sailnumber = sailnumber;
        this.boatName = boatName;
        this.boatclass = boatclass;
        this.lengthOverAll = length;
        this.gph = gph;
        this.cdl = cdl;
        this.issueDate = issueDate;
        this.velocityPredictionPerTrueWindSpeedAndAngle = Collections
                .unmodifiableMap(velocityPredictionsPerTrueWindSpeedAndAngle);
        this.beatAngles = Collections.unmodifiableMap(beatAngles);
        this.beatVMGPredictionPerTrueWindSpeed = Collections.unmodifiableMap(beatVMGPredictionPerTrueWindSpeed);
        this.beatAllowancePerTrueWindSpeed = Collections.unmodifiableMap(beatAllowancePerTrueWindSpeed);
        this.runAngles = Collections.unmodifiableMap(runAngles);
        this.runVMGPredictionPerTrueWindSpeed = Collections.unmodifiableMap(runVMGPredictionPerTrueWindSpeed);
        this.runAllowancePerTrueWindSpeed = Collections.unmodifiableMap(runAllowancePerTrueWindSpeed);
        this.windwardLeewardSpeedPredictionPerTrueWindSpeed = Collections.unmodifiableMap(windwardLeewardSpeedPredictionsPerTrueWindSpeed);
        this.longDistanceSpeedPredictionPerTrueWindSpeed = Collections.unmodifiableMap(longDistanceSpeedPredictionsPerTrueWindSpeed);
        this.circularRandomSpeedPredictionPerTrueWindSpeed = Collections.unmodifiableMap(circularRandomSpeedPredictionsPerTrueWindSpeed);
        this.nonSpinnakerSpeedPredictionPerTrueWindSpeed = Collections.unmodifiableMap(nonSpinnakerSpeedPredictionsPerTrueWindSpeed);
    }

    @Override
    public String getId() {
        return idConsistingOfNatAuthCertNoAndBIN;
    }

    @Override
    public String getBoatName() {
        return boatName;
    }

    @Override
    public double getGPH() {
        return gph.asSeconds();
    }

    // Please do not try to calculate these combined allowances and instead use the provided values from the certificate.
    // The values from the certificate are calculated with more accurate base data (via the velocity prediction program)
    // and rounded afterwards. Those values aren't provided to the public, so there is no possibility to get the same
    // level of accuracy.
    @Override
    public Map<Speed, Speed> getWindwardLeewardSpeedPrediction() {
        return windwardLeewardSpeedPredictionPerTrueWindSpeed;
    }

    @Override
    public Map<Speed, Speed> getCircularRandomSpeedPredictions() {
        return circularRandomSpeedPredictionPerTrueWindSpeed;
    }

    @Override
    public Map<Speed, Speed> getLongDistanceSpeedPredictions() {
        return longDistanceSpeedPredictionPerTrueWindSpeed;
    }

    @Override
    public Map<Speed, Speed> getNonSpinnakerSpeedPredictions() {
        return nonSpinnakerSpeedPredictionPerTrueWindSpeed;
    }

    @Override
    public String getSailnumber() {
        return sailnumber;
    }

    @Override
    public String getBoatclass() {
        return boatclass;
    }

    @Override
    public Distance getLengthOverAll() {
        return lengthOverAll;
    }

    @Override
    public double getCDL() {
        return cdl;
    }

    @Override
    public Map<Speed, Bearing> getBeatAngles() {
        return beatAngles;
    }

    @Override
    public Map<Speed, Bearing> getRunAngles() {
        return runAngles;
    }

    @Override
    public Map<Speed, Duration> getBeatAllowances() {
        return beatAllowancePerTrueWindSpeed;
    }

    @Override
    public Map<Speed, Duration> getRunAllowances() {
        return runAllowancePerTrueWindSpeed;
    }

    @Override
    public Map<Speed, Map<Bearing, Speed>> getVelocityPredictionPerTrueWindSpeedAndAngle() {
        return velocityPredictionPerTrueWindSpeedAndAngle;
    }

    @Override
    public Map<Speed, Speed> getBeatVMGPredictions() {
        return beatVMGPredictionPerTrueWindSpeed;
    }

    @Override
    public Map<Speed, Speed> getRunVMGPredictions() {
        return runVMGPredictionPerTrueWindSpeed;
    }
    
    @Override
    public TimePoint getIssueDate() {
        return issueDate;
    }
    
    @Override
    public String toString() {
        return "ID \""+getId()+"\" for "+getSailnumber()+" / "+getBoatName() + " - Issued on: " + getIssueDate().asDate()+" with GPH "+
                Util.padPositiveValue(getGPH(), 1, 1, /* round */ true);
    }
}
