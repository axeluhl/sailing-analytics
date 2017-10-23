package com.sap.sailing.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import com.sap.sailing.datamining.data.HasManeuverContext;
import com.sap.sailing.datamining.data.HasManeuverSpeedDetailsContext;
import com.sap.sailing.datamining.impl.data.ManeuverSpeedDetailsWithContext;
import com.sap.sailing.datamining.shared.ManeuverSpeedDetailsSettings;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.tracking.SpeedWithBearingStep;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverSpeedDetailsRetrievalProcessor
        extends AbstractRetrievalProcessor<HasManeuverContext, HasManeuverSpeedDetailsContext> {

    private final ManeuverSpeedDetailsSettings settings;

    public ManeuverSpeedDetailsRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasManeuverSpeedDetailsContext, ?>> resultReceivers,
            ManeuverSpeedDetailsSettings settings, int retrievalLevel) {
        super(HasManeuverContext.class, HasManeuverSpeedDetailsContext.class, executor, resultReceivers,
                retrievalLevel);
        this.settings = settings;
    }

    @Override
    protected Iterable<HasManeuverSpeedDetailsContext> retrieveData(HasManeuverContext element) {
        Collection<HasManeuverSpeedDetailsContext> maneuverSpeedDetails = new ArrayList<>();
        Maneuver maneuver = element.getManeuver();
        TrackedLegOfCompetitor trackedLegOfCompetitor = element.getTrackedLegOfCompetitorContext()
                .getTrackedLegOfCompetitor();
        TrackedRace trackedRace = element.getTrackedLegOfCompetitorContext().getTrackedLegContext()
                .getTrackedRaceContext().getTrackedRace();
        Wind wind = trackedRace.getWind(maneuver.getPosition(), maneuver.getTimePoint());
        if (wind != null) {
            if (trackedLegOfCompetitor.getStartTime() != null && trackedLegOfCompetitor.getFinishTime() != null
                    && maneuver.getTimePointBefore().until(maneuver.getTimePointAfter()).asMillis() >= 500) {
                SpeedPerTWAExtraction speedPerTWAExtraction = extractSpeedPerTWA(trackedRace,
                        trackedLegOfCompetitor.getCompetitor(), wind, maneuver);
                ManeuverSpeedDetailsWithContext maneuverSpeedDetailsContext = new ManeuverSpeedDetailsWithContext(
                        element, speedPerTWAExtraction.getSpeedPerTWA(), speedPerTWAExtraction.getEnteringTWA(),
                        settings);
                maneuverSpeedDetails.add(maneuverSpeedDetailsContext);
            }
        }

        return maneuverSpeedDetails;
    }

    private SpeedPerTWAExtraction extractSpeedPerTWA(TrackedRace trackedRace, Competitor competitor, Wind wind,
            Maneuver maneuver) {
        long maneuverDuration = maneuver.getTimePointBefore().until(maneuver.getTimePointAfter()).asMillis();
        long stepMillis = maneuverDuration < 200 * 5 ? maneuverDuration / 5 : 200;
        if (stepMillis == 0) {
            stepMillis = 1;
        }

        final Iterable<SpeedWithBearingStep> maneuverBearingSteps = trackedRace.getTrack(competitor).getSpeedWithBearingSteps(
                maneuver.getTimePointBefore(), maneuver.getTimePointAfter(), new MillisecondsDurationImpl(stepMillis));

        NauticalSide maneuverDirection = maneuver.getDirectionChangeInDegrees() < 0 ? NauticalSide.PORT
                : NauticalSide.STARBOARD;
        double totalCourseChangeSignum = Math.signum(maneuver.getDirectionChangeInDegrees());
        Function<Integer, Integer> forNextTWA = ManeuverSpeedDetailsUtils
                .getNextTWAFunctionForManeuverDirection(maneuverDirection);

        double[] speedPerTWA = new double[360];
        int previousRoundedTWA = -1;
        double previousSpeed = 0;

        int enteringTWA = -1;

        for (SpeedWithBearingStep bearingStep : maneuverBearingSteps) {
            SpeedWithBearing speedWithBearing = bearingStep.getSpeedWithBearing();
            double twa = wind.getFrom().getDifferenceTo(speedWithBearing.getBearing()).getDegrees();
            if (twa < 0) {
                twa += 360;
            }
            double speed = speedWithBearing.getKnots();

            int roundedTWA = (int) Math.round(twa) % 360;

            if (enteringTWA == -1) {
                enteringTWA = roundedTWA;
            }

            if (Math.abs(Math.signum(bearingStep.getCourseChangeInDegrees()) - totalCourseChangeSignum) == 2) {
                continue;
            }

            // Neglect speed differences between TWA resolution < 1 Deg
            // First twa/speed tuple gets priority over next tuples due it its "speed change freshness" regarding the
            // whole twa sequence
            if (speedPerTWA[roundedTWA] == 0) {
                speedPerTWA[roundedTWA] = speed;
                // First bearing step supposed to have 0 as course change as
                // it does not have any previous steps with bearings to compute bearing difference.
                if (bearingStep.getCourseChangeInDegrees() != 0 && bearingStep.getCourseChangeInDegrees() < 40) {
                    int diffWithPreviousTWA = Math.abs(previousRoundedTWA - roundedTWA);
                    if (diffWithPreviousTWA > 1) {
                        double diffWithPreviousSpeed = speed - previousSpeed;
                        // fill the TWA gaps with linear approximation
                        for (int step = 1, fillingTWA = forNextTWA
                                .apply(previousRoundedTWA); fillingTWA != roundedTWA; fillingTWA = forNextTWA
                                        .apply(fillingTWA), ++step) {
                            if (speedPerTWA[fillingTWA] == 0) {
                                speedPerTWA[fillingTWA] = previousSpeed
                                        + diffWithPreviousSpeed * step / diffWithPreviousTWA;
                            }
                        }
                    }
                }
            } else {
                speedPerTWA[roundedTWA] = (speedPerTWA[roundedTWA] + speed) / 2;
            }
            previousRoundedTWA = roundedTWA;
            previousSpeed = speed;
        }

        return new SpeedPerTWAExtraction(speedPerTWA, enteringTWA);
    }

    private static class SpeedPerTWAExtraction {

        private final double[] speedPerTWA;
        private final int enteringTWA;

        public SpeedPerTWAExtraction(double[] speedPerTWA, int enteringTWA) {
            this.speedPerTWA = speedPerTWA;
            this.enteringTWA = enteringTWA;
        }

        public double[] getSpeedPerTWA() {
            return speedPerTWA;
        }

        public int getEnteringTWA() {
            return enteringTWA;
        }
    }

}
