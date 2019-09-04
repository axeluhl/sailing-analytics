package com.sap.sailing.domain.common.orc.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.impl.NauticalMileDistance;
import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
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

    /**
     * Equals the percentages of accounted allowances at a given speed for the Coastal / Long Distance Performance Curve
     * metric. Details can be found on: https://orc.org/index.asp?id=32. TWA keys of 0deg represent upwind, TWA keys of
     * 180deg represent downwind.
     */
    private static final Bearing UPWIND_TWA = new DegreeBearingImpl(0);
    private static final Bearing DOWNWIND_TWA = new DegreeBearingImpl(180);
    private static final Map<Speed, Map<Bearing, Double>> perCentOfAllowancesForLongDistancePC;
    static {
        Map<Speed, Map<Bearing, Double>> result = new HashMap<>();
        Map<Bearing, Double> map6kt = new HashMap<>();
        Map<Bearing, Double> map8kt = new HashMap<>();
        Map<Bearing, Double> map10kt = new HashMap<>();
        Map<Bearing, Double> map12kt = new HashMap<>();
        Map<Bearing, Double> map14kt = new HashMap<>();
        Map<Bearing, Double> map16kt = new HashMap<>();
        Map<Bearing, Double> map20kt = new HashMap<>();

        map6kt.put(UPWIND_TWA, 0.45); // representing upwind
        map6kt.put(ALLOWANCES_TRUE_WIND_ANGLES[1], 0.0);
        map6kt.put(ALLOWANCES_TRUE_WIND_ANGLES[3], 0.0);
        map6kt.put(ALLOWANCES_TRUE_WIND_ANGLES[5], 0.0);
        map6kt.put(ALLOWANCES_TRUE_WIND_ANGLES[7], 0.0);
        map6kt.put(DOWNWIND_TWA, 0.55); // representing downwind

        map8kt.put(UPWIND_TWA, 0.40);
        map8kt.put(ALLOWANCES_TRUE_WIND_ANGLES[1], 0.05);
        map8kt.put(ALLOWANCES_TRUE_WIND_ANGLES[3], 0.05);
        map8kt.put(ALLOWANCES_TRUE_WIND_ANGLES[5], 0.05);
        map8kt.put(ALLOWANCES_TRUE_WIND_ANGLES[7], 0.05);
        map8kt.put(DOWNWIND_TWA, 0.40);

        map10kt.put(UPWIND_TWA, 0.35);
        map10kt.put(ALLOWANCES_TRUE_WIND_ANGLES[1], 0.10);
        map10kt.put(ALLOWANCES_TRUE_WIND_ANGLES[3], 0.075);
        map10kt.put(ALLOWANCES_TRUE_WIND_ANGLES[5], 0.10);
        map10kt.put(ALLOWANCES_TRUE_WIND_ANGLES[7], 0.10);
        map10kt.put(DOWNWIND_TWA, 0.275);

        map12kt.put(UPWIND_TWA, 0.30);
        map12kt.put(ALLOWANCES_TRUE_WIND_ANGLES[1], 0.15);
        map12kt.put(ALLOWANCES_TRUE_WIND_ANGLES[3], 0.10);
        map12kt.put(ALLOWANCES_TRUE_WIND_ANGLES[5], 0.15);
        map12kt.put(ALLOWANCES_TRUE_WIND_ANGLES[7], 0.15);
        map12kt.put(DOWNWIND_TWA, 0.15);

        map14kt.put(UPWIND_TWA, 0.25);
        map14kt.put(ALLOWANCES_TRUE_WIND_ANGLES[1], 0.175);
        map14kt.put(ALLOWANCES_TRUE_WIND_ANGLES[3], 0.125);
        map14kt.put(ALLOWANCES_TRUE_WIND_ANGLES[5], 0.175);
        map14kt.put(ALLOWANCES_TRUE_WIND_ANGLES[7], 0.15);
        map14kt.put(DOWNWIND_TWA, 0.125);

        map16kt.put(UPWIND_TWA, 0.20);
        map16kt.put(ALLOWANCES_TRUE_WIND_ANGLES[1], 0.20);
        map16kt.put(ALLOWANCES_TRUE_WIND_ANGLES[3], 0.15);
        map16kt.put(ALLOWANCES_TRUE_WIND_ANGLES[5], 0.20);
        map16kt.put(ALLOWANCES_TRUE_WIND_ANGLES[7], 0.15);
        map16kt.put(DOWNWIND_TWA, 0.10);

        map20kt.put(UPWIND_TWA, 0.10);
        map20kt.put(ALLOWANCES_TRUE_WIND_ANGLES[1], 0.25);
        map20kt.put(ALLOWANCES_TRUE_WIND_ANGLES[3], 0.20);
        map20kt.put(ALLOWANCES_TRUE_WIND_ANGLES[5], 0.25);
        map20kt.put(ALLOWANCES_TRUE_WIND_ANGLES[7], 0.10);
        map20kt.put(DOWNWIND_TWA, 0.10);

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
    
    private final Map<Speed, Speed> circularRandomSpeedPredictionsPerTrueWindSpeed;
    
    private final Map<Speed, Speed> nonSpinnakerSpeedPredictionsPerTrueWindSpeed;

    // TODO Comment on Constructor
    public ORCCertificateImpl(String sailnumber, String boatclass, Distance length, Duration gph,
            Double cdl, Map<Speed, Map<Bearing, Speed>> velocityPredictionsPerTrueWindSpeedAndAngle,
            Map<Speed, Bearing> beatAngles, Map<Speed, Speed> beatVMGPredictionPerTrueWindSpeed,
            Map<Speed, Duration> beatAllowancePerTrueWindSpeed, Map<Speed, Bearing> runAngles,
            Map<Speed, Speed> runVMGPredictionPerTrueWindSpeed, Map<Speed, Duration> runAllowancePerTrueWindSpeed,
            Map<Speed, Speed> circularRandomSpeedPredictionsPerTrueWindSpeed,
            Map<Speed, Speed> nonSpinnakerSpeedPredictionsPerTrueWindSpeed) {
        this.sailnumber = sailnumber;
        this.boatclass = boatclass;
        this.lengthOverAll = length;
        this.gph = gph;
        this.cdl = cdl;
        this.velocityPredictionPerTrueWindSpeedAndAngle = Collections
                .unmodifiableMap(velocityPredictionsPerTrueWindSpeedAndAngle);
        this.beatAngles = Collections.unmodifiableMap(beatAngles);
        this.beatVMGPredictionPerTrueWindSpeed = Collections.unmodifiableMap(beatVMGPredictionPerTrueWindSpeed);
        this.beatAllowancePerTrueWindSpeed = Collections.unmodifiableMap(beatAllowancePerTrueWindSpeed);
        this.runAngles = Collections.unmodifiableMap(runAngles);
        this.runVMGPredictionPerTrueWindSpeed = Collections.unmodifiableMap(runVMGPredictionPerTrueWindSpeed);
        this.runAllowancePerTrueWindSpeed = Collections.unmodifiableMap(runAllowancePerTrueWindSpeed);
        this.circularRandomSpeedPredictionsPerTrueWindSpeed = Collections.unmodifiableMap(circularRandomSpeedPredictionsPerTrueWindSpeed);
        this.nonSpinnakerSpeedPredictionsPerTrueWindSpeed = Collections.unmodifiableMap(nonSpinnakerSpeedPredictionsPerTrueWindSpeed);
    }

    @Override
    public double getGPH() {
        return gph.asSeconds();
    }

    @Override
    public Map<Speed, Speed> getWindwardLeewardSpeedPrediction() {
        final Map<Speed, Speed> result = new HashMap<>();
        for (final Speed tws : ALLOWANCES_TRUE_WIND_SPEEDS) {
            // gets the Allowance for Beat&Run at the given TWS divided by 2
            Duration allowance = beatVMGPredictionPerTrueWindSpeed.get(tws).getDuration(NAUTICAL_MILE)
                    .plus(runVMGPredictionPerTrueWindSpeed.get(tws).getDuration(NAUTICAL_MILE)).divide(2);
            result.put(tws, NAUTICAL_MILE.inTime(allowance));
        }
        return result;
    }

    @Override
    public Map<Speed, Speed> getCircularRandomSpeedPredictions() {
        return circularRandomSpeedPredictionsPerTrueWindSpeed;
    }

    @Override
    public Map<Speed, Speed> getLongDistanceSpeedPredictions() {
        final Map<Speed, Speed> result = new HashMap<>();
        for (final Entry<Speed, Map<Bearing, Double>> twsEntry : perCentOfAllowancesForLongDistancePC.entrySet()) {
            Duration allowance = Duration.NULL;
            for (Entry<Bearing, Double> twaEntry : twsEntry.getValue().entrySet()) {
                if (twaEntry.getKey().equals(UPWIND_TWA)) {
                    allowance = allowance.plus(beatVMGPredictionPerTrueWindSpeed.get(twsEntry.getKey())
                            .getDuration(NAUTICAL_MILE).times(twaEntry.getValue()));
                } else if (twaEntry.getKey().equals(DOWNWIND_TWA)) {
                    allowance = allowance.plus(runVMGPredictionPerTrueWindSpeed.get(twsEntry.getKey())
                            .getDuration(NAUTICAL_MILE).times(twaEntry.getValue()));
                } else {
                    allowance = allowance.plus(velocityPredictionPerTrueWindSpeedAndAngle.get(twsEntry.getKey())
                            .get(twaEntry.getKey()).getDuration(NAUTICAL_MILE).times(twaEntry.getValue()));
                }
            }
            result.put(twsEntry.getKey(), NAUTICAL_MILE.inTime(allowance));
        }
        return result;
    }

    @Override
    public Map<Speed, Speed> getNonSpinnakerSpeedPredictions() {
        return nonSpinnakerSpeedPredictionsPerTrueWindSpeed;
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
}
