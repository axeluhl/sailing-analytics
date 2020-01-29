package com.sap.sailing.domain.common.orc.impl;

import java.util.Collection;
import java.util.Map;

import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;

public class ORCCertificateCustomBinsImpl extends ORCCertificateImpl {
    private static final long serialVersionUID = 6151992286427539248L;
    
    private final Speed[] customAllowancesTrueWindSpeeds;
    private final Bearing[] customAllowancesTrueWindAngle;
    
    public ORCCertificateCustomBinsImpl(Collection<Speed> customTrueWindSpeed, Collection<Bearing> customTrueWindAngle,
            String idConsistingOfNatAuthCertNoAndBIN, String sailnumber, String boatName, String boatClassName,
            Distance length, Duration gph, Double cdl, TimePoint issueDate,
            Map<Speed, Map<Bearing, Speed>> velocityPredictionsPerTrueWindSpeedAndAngle, Map<Speed, Bearing> beatAngles,
            Map<Speed, Speed> beatVMGPredictionPerTrueWindSpeed, Map<Speed, Duration> beatAllowancePerTrueWindSpeed,
            Map<Speed, Bearing> runAngles, Map<Speed, Speed> runVMGPredictionPerTrueWindSpeed,
            Map<Speed, Duration> runAllowancePerTrueWindSpeed,
            Map<Speed, Speed> windwardLeewardSpeedPredictionsPerTrueWindSpeed,
            Map<Speed, Speed> longDistanceSpeedPredictionsPerTrueWindSpeed,
            Map<Speed, Speed> circularRandomSpeedPredictionsPerTrueWindSpeed,
            Map<Speed, Speed> nonSpinnakerSpeedPredictionsPerTrueWindSpeed) {
        super(idConsistingOfNatAuthCertNoAndBIN, sailnumber, boatName, boatClassName, length, gph, cdl, issueDate,
                velocityPredictionsPerTrueWindSpeedAndAngle, beatAngles, beatVMGPredictionPerTrueWindSpeed,
                beatAllowancePerTrueWindSpeed, runAngles, runVMGPredictionPerTrueWindSpeed,
                runAllowancePerTrueWindSpeed, windwardLeewardSpeedPredictionsPerTrueWindSpeed,
                longDistanceSpeedPredictionsPerTrueWindSpeed, circularRandomSpeedPredictionsPerTrueWindSpeed,
                nonSpinnakerSpeedPredictionsPerTrueWindSpeed);
        this.customAllowancesTrueWindAngle = new Bearing[customTrueWindAngle.size()];
        this.customAllowancesTrueWindSpeeds = new Speed[customTrueWindSpeed.size()];
        customTrueWindAngle.toArray(customAllowancesTrueWindAngle);
        customTrueWindSpeed.toArray(customAllowancesTrueWindSpeeds);
    }
    
    @Override
    public Bearing[] getTrueWindAngle() {
        return this.customAllowancesTrueWindAngle;
    }
    
    @Override
    public Speed[] getTrueWindSpeed() {
        return this.customAllowancesTrueWindSpeeds;
    }

}
