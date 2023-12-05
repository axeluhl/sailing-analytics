package com.sap.sailing.datamining.shared;

import com.sap.sailing.domain.common.tracking.BravoFix;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.common.settings.SerializableSettings;

public class TackTypeSegmentsDataMiningSettings extends SerializableSettings {
    private static final long serialVersionUID = 3239182650750480678L;
    private final Duration minimumFoilingSegmentDuration;
    private final Duration minimumDurationBetweenAdjacentFoilingSegments;
    private final Speed minimumSpeedForFoiling;
    private final Speed maximumSpeedNotFoiling;
    private final Distance minimumRideHeight;

    public TackTypeSegmentsDataMiningSettings(Duration minimumFoilingSegmentDuration, Duration minimumDurationBetweenAdjacentFoilingSegments,
            Speed minimumSpeedForFoiling, Speed maximumSpeedNotFoiling, Distance minimumRideHeight) {
        super();
        this.minimumFoilingSegmentDuration = minimumFoilingSegmentDuration;
        this.minimumDurationBetweenAdjacentFoilingSegments = minimumDurationBetweenAdjacentFoilingSegments;
        this.minimumSpeedForFoiling = minimumSpeedForFoiling;
        this.maximumSpeedNotFoiling = maximumSpeedNotFoiling;
        this.minimumRideHeight = minimumRideHeight;
    }

    public Duration getMinimumTackTypeSegmentDuration() {
        return minimumFoilingSegmentDuration;
    }
    public Duration getMinimumDurationBetweenAdjacentTackTypeSegments() {
        return minimumDurationBetweenAdjacentFoilingSegments;
    }
    public Speed getMinimumSpeedForTackType() {
        return minimumSpeedForFoiling;
    }
    public Speed getMaximumSpeedNotTackType() {
        return maximumSpeedNotFoiling;
    }
    public Distance getMinimumRideHeight() {
        return minimumRideHeight;
    }

    public static TackTypeSegmentsDataMiningSettings createDefaultSettings() {
        return new TackTypeSegmentsDataMiningSettings(
                /* minimumFoilingSegmentDuration */ null,
                /* minimumDurationBetweenAdjacentFoilingSegments */ null,
                /* minimumSpeedForFoiling */ null,
                /* maximumSpeedNotFoiling */ null, /* minimumRideHeight */ BravoFix.MIN_FOILING_HEIGHT_THRESHOLD);
    }

}
