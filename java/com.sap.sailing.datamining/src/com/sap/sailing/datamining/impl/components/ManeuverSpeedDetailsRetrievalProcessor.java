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
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sse.common.TimePoint;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;

public class ManeuverSpeedDetailsRetrievalProcessor extends AbstractRetrievalProcessor<HasManeuverContext, HasManeuverSpeedDetailsContext> {

    private final ManeuverSpeedDetailsSettings settings;

    public ManeuverSpeedDetailsRetrievalProcessor(ExecutorService executor, Collection<Processor<HasManeuverSpeedDetailsContext, ?>> resultReceivers, ManeuverSpeedDetailsSettings settings, int retrievalLevel) {
        super(HasManeuverContext.class, HasManeuverSpeedDetailsContext.class, executor, resultReceivers, retrievalLevel);
        this.settings = settings;
    }

    @Override
    protected Iterable<HasManeuverSpeedDetailsContext> retrieveData(HasManeuverContext element) {
        Collection<HasManeuverSpeedDetailsContext> maneuverSpeedDetails = new ArrayList<>();
        Maneuver maneuver = element.getManeuver();
        TrackedLegOfCompetitor trackedLegOfCompetitor = element.getTrackedLegOfCompetitorContext().getTrackedLegOfCompetitor();
        Competitor competitor = element.getTrackedLegOfCompetitorContext().getCompetitor();
        GPSFixTrack<Competitor,GPSFixMoving> competitorTrack = element.getTrackedLegOfCompetitorContext().getTrackedLegContext().getTrackedRaceContext().getTrackedRace().getTrack(competitor);
        Wind wind = trackedLegOfCompetitor.getTrackedLeg().getTrackedRace().getWind(maneuver.getPosition(), maneuver.getTimePoint());
        if(wind != null) {
            competitorTrack.lockForRead();
            try {
                if (trackedLegOfCompetitor.getStartTime() != null && trackedLegOfCompetitor.getFinishTime() != null && maneuver.getTimePointBefore().until(maneuver.getTimePointAfter()).asMillis() >= 500) {
                    SpeedPerTWAExtraction speedPerTWAExtraction = extractSpeedPerTWA(competitorTrack, wind, maneuver.getTimePointBefore(), maneuver.getTimePointAfter());
                    ManeuverSpeedDetailsWithContext maneuverSpeedDetailsContext = new ManeuverSpeedDetailsWithContext(element, speedPerTWAExtraction.getToSide(), speedPerTWAExtraction.getSpeedPerTWA(), speedPerTWAExtraction.getEnteringTWA(), speedPerTWAExtraction.getExitingTWA(), settings);
                    maneuverSpeedDetails.add(maneuverSpeedDetailsContext);
                }
            } finally {
                competitorTrack.unlockAfterRead();
            }
        }
        
        return maneuverSpeedDetails;
    }


    private SpeedPerTWAExtraction extractSpeedPerTWA(GPSFixTrack<Competitor, GPSFixMoving> competitorTrack, Wind wind,
            TimePoint timePointBefore, TimePoint timePointAfter) {
        long maneuverDuration = timePointBefore.until(timePointAfter).asMillis();
        long stepMillis = maneuverDuration < 200 * 5 ? maneuverDuration / 5 : 200;
        if(stepMillis == 0) {
            stepMillis = 1;
        }
        
        int starboardSideVotes = 0;
        int portsideVotes = 0;
        
        int totalSteps = (int) (maneuverDuration / stepMillis) + 1;
        
        double[] twas = new double[totalSteps];
        double[] speeds = new double[totalSteps];
        for(int step = 0; step < totalSteps; ++step) {
            TimePoint t = timePointBefore.plus(stepMillis * step);
            SpeedWithBearing speedWithBearing = competitorTrack.getEstimatedSpeed(t);
            double twa =  wind.getFrom().getDifferenceTo(speedWithBearing.getBearing()).getDegrees();
            if(twa < 0) {
                twa += 360;
            }
            double speed = speedWithBearing.getKnots();
            if(step != 0) {
                if(twa > twas[step - 1]) {
                    ++starboardSideVotes;
                } else if(twa < twas[step - 1]) {
                    ++portsideVotes;
                }
            }
            twas[step] = twa;
            speeds[step] = speed;
        }
        
        double firstTWA = twas[0];
        double lastTWA = twas[twas.length-1];
        
        NauticalSide toSide;
        if(Math.abs(starboardSideVotes - portsideVotes) > 1) {
            toSide = starboardSideVotes > portsideVotes ? NauticalSide.STARBOARD : NauticalSide.PORT;
        } else {
            toSide = ManeuverSpeedDetailsUtils.determineNauticalSideByClosestAngleDistance(firstTWA, lastTWA);
        }
        
        Function<Integer, Integer> forNextTWA = ManeuverSpeedDetailsUtils.getNextTWAFunctionForManeuverDirection(toSide, settings);
        
        double[] speedPerTWA = new double[360];
        int previousRoundedTWA = -1;
        double previousSpeed = 0;
        
        int enteringTWA = -1;
        for(int i = 0; i < totalSteps; ++i) {
            int roundedTWA = (int) Math.round(twas[i]) % 360;
            
            if(settings.isNormalizeManeuverDirection()) {
                if(toSide != settings.getNormalizedManeuverDirection()) {
                    roundedTWA = (360 - roundedTWA) % 360;
                }
            }
            double speed = speeds[i];
            //neglect speed differences between TWA resolution < 1 Deg
            if(speedPerTWA[roundedTWA] == 0) {
                speedPerTWA[roundedTWA] = speeds[i];
            } else {
                speedPerTWA[roundedTWA] = (speedPerTWA[roundedTWA] + speeds[i]) / 2;
            }
            if(i != 0) {
                double diffWithPreviousTWA = Math.abs(previousRoundedTWA - roundedTWA);
                NauticalSide currentSide = ManeuverSpeedDetailsUtils.determineNauticalSideByClosestAngleDistance(previousRoundedTWA, roundedTWA);
                if(diffWithPreviousTWA > 1 && currentSide == toSide) {
                    double diffWithPreviousSpeed = speed - previousSpeed;
                    //fill the TWA gaps with linear approximation
                    for(int step = 1, fillingTWA = forNextTWA.apply(previousRoundedTWA); fillingTWA != roundedTWA; fillingTWA = forNextTWA.apply(fillingTWA), ++step) {
                        if(speedPerTWA[fillingTWA] == 0) {
                            speedPerTWA[fillingTWA] = previousSpeed + diffWithPreviousSpeed * step / diffWithPreviousTWA;
                        }
                    }
                }
            } else {
                enteringTWA = roundedTWA;
            }
            previousRoundedTWA = roundedTWA;
            previousSpeed = speed;
        }
        int exitingTWA = previousRoundedTWA == -1 ? enteringTWA : previousRoundedTWA;
        
        return new SpeedPerTWAExtraction(speedPerTWA, toSide, enteringTWA, exitingTWA);
    }
    
    private static class SpeedPerTWAExtraction {
        
        private final double[] speedPerTWA;
        private final NauticalSide toSide;
        private final int enteringTWA;
        private final int exitingTWA;
        
        public SpeedPerTWAExtraction(double[] speedPerTWA, NauticalSide toSide, int enteringTWA, int exitingTWA) {
            this.speedPerTWA = speedPerTWA;
            this.toSide = toSide;
            this.enteringTWA = enteringTWA;
            this.exitingTWA = exitingTWA;
        }
        public double[] getSpeedPerTWA() {
            return speedPerTWA;
        }
        public NauticalSide getToSide() {
            return toSide;
        }
        public int getEnteringTWA() {
            return enteringTWA;
        }
        public int getExitingTWA() {
            return exitingTWA;
        }
    }
    
}
