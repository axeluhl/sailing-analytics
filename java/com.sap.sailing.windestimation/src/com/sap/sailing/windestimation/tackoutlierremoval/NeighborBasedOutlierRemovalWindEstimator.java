package com.sap.sailing.windestimation.tackoutlierremoval;

import java.util.List;

import com.sap.sailing.windestimation.classifier.maneuver.ManeuverWithEstimatedType;
import com.sap.sailing.windestimation.windinference.TwdFromManeuverCalculator;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Util.Pair;

public class NeighborBasedOutlierRemovalWindEstimator extends AbstractOutlierRemovalWindEstimator {

    public NeighborBasedOutlierRemovalWindEstimator(TwdFromManeuverCalculator twdCalculator) {
        super(twdCalculator);
    }

    @Override
    protected OutlierAnalysisResult analyzeOutlier(List<Pair<Bearing, ManeuverWithEstimatedType>> twdsWithManeuvers) {
        OutlierAnalysisResult outlierAnalysisResult = new OutlierAnalysisResult();
        Bearing previousTwd = null;
        Bearing currentTwd = null;
        Bearing firstNonOutlierTwd = null;
        Bearing firstNearlyNonOutlierTwd = null;
        for (Pair<Bearing, ManeuverWithEstimatedType> twdWithManeuver : twdsWithManeuvers) {
            Bearing nextTwd = twdWithManeuver.getA();
            if (currentTwd != null && Math
                    .abs(currentTwd.getDifferenceTo(nextTwd).getDegrees()) <= MAX_DEVIATON_FROM_AVG_WIND_COURSE) {
                if (firstNearlyNonOutlierTwd == null) {
                    firstNearlyNonOutlierTwd = currentTwd;
                }
                if (previousTwd != null && Math.abs(
                        currentTwd.getDifferenceTo(previousTwd).getDegrees()) <= MAX_DEVIATON_FROM_AVG_WIND_COURSE) {
                    firstNonOutlierTwd = currentTwd;
                    break;
                }
            }
            previousTwd = currentTwd;
            currentTwd = nextTwd;
        }

        Bearing lastNonOutlierTwd = firstNonOutlierTwd == null ? firstNearlyNonOutlierTwd : firstNonOutlierTwd;
        if (lastNonOutlierTwd == null) {
            return outlierAnalysisResult;
        }
        for (Pair<Bearing, ManeuverWithEstimatedType> twdWithManeuver : twdsWithManeuvers) {
            currentTwd = twdWithManeuver.getA();
            ManeuverWithEstimatedType maneuver = twdWithManeuver.getB();
            if (Math.abs(
                    currentTwd.getDifferenceTo(lastNonOutlierTwd).getDegrees()) <= MAX_DEVIATON_FROM_AVG_WIND_COURSE) {
                outlierAnalysisResult.addIncludedManeuver(maneuver);
                lastNonOutlierTwd = currentTwd;
            } else {
                outlierAnalysisResult.addExcludedManeuver(maneuver);
            }
        }
        return outlierAnalysisResult;
    }

}
