package com.sap.sailing.windestimation.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.domain.tracking.impl.WindWithConfidenceImpl;

import smile.sort.QuickSelect;

public class WindUtil {

    private WindUtil() {
    }

    public static List<WindWithConfidence<Void>> getWindFixesWithFixedConfidence(
            List<WindWithConfidence<Void>> windFixes, double fixedConfidence) {
        List<WindWithConfidence<Void>> result = new ArrayList<>();
        for (WindWithConfidence<Void> windWithConfidence : windFixes) {
            WindWithConfidence<Void> newWindWithConfidence = new WindWithConfidenceImpl<>(
                    windWithConfidence.getObject(), fixedConfidence, windWithConfidence.getRelativeTo(),
                    windWithConfidence.useSpeed());
            result.add(newWindWithConfidence);
        }
        return result;
    }

    public static List<WindWithConfidence<Void>> getWindFixesWithAveragedWindSpeed(
            List<WindWithConfidence<Void>> windFixes) {
        if (windFixes.size() <= 1) {
            return windFixes;
        }
        double[] windSpeedsInKnots = new double[windFixes.size()];
        int i = 0;
        int zerosCount = 0;
        for (WindWithConfidence<Void> windFix : windFixes) {
            double windSpeedInKnots = windFix.getObject().getKnots();
            if (windSpeedInKnots > 0) {
                windSpeedsInKnots[i++] = windFix.getObject().getKnots();
            } else {
                zerosCount++;
            }
        }
        if (zerosCount == windSpeedsInKnots.length) {
            return windFixes;
        }
        if (zerosCount > 0) {
            windSpeedsInKnots = Arrays.copyOfRange(windSpeedsInKnots, 0, windSpeedsInKnots.length - zerosCount);
        }
        double avgWindSpeedInKnots = windSpeedsInKnots.length == 1 ? windSpeedsInKnots[0]
                : QuickSelect.median(windSpeedsInKnots);
        List<WindWithConfidence<Void>> result = new ArrayList<>();
        for (WindWithConfidence<Void> windFix : windFixes) {
            Wind wind = windFix.getObject();
            WindWithConfidence<Void> newWindFix = new WindWithConfidenceImpl<Void>(
                    new WindImpl(wind.getPosition(), wind.getTimePoint(),
                            new KnotSpeedWithBearingImpl(avgWindSpeedInKnots, wind.getBearing())),
                    windFix.getConfidence(), windFix.getRelativeTo(), avgWindSpeedInKnots > 0);
            result.add(newWindFix);
        }
        return result;
    }

}
