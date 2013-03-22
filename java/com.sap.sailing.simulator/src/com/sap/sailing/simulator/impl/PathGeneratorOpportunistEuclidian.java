package com.sap.sailing.simulator.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.TimedPositionWithSpeed;
import com.sap.sailing.simulator.windfield.WindFieldGenerator;

public class PathGeneratorOpportunistEuclidian extends PathGeneratorBase {

    private static final Logger LOGGER = Logger.getLogger("com.sap.sailing.simulator");

    private static final double MINIMUM_DISTANCE_METERS = 20.0;

    private int maxLeft = 0;
    private int maxRight = 0;
    private boolean startLeft = false;

    public PathGeneratorOpportunistEuclidian(SimulationParameters parameters) {
        this.parameters = parameters;
    }

    public void setEvaluationParameters(int maxLeftVal, int maxRightVal, boolean startLeftVal) {
        this.maxLeft = maxLeftVal;
        this.maxRight = maxRightVal;
        this.startLeft = startLeftVal;
    }

    @Override
    public Path getPathLeg(int selectedRaceIndex, int selectedCompetitorIndex, int selectedLegIndex) {
        return null;
    }
    
    @Override
    public Path getPath() {

        WindFieldGenerator wf = this.parameters.getWindField();
        PolarDiagram pd = this.parameters.getBoatPolarDiagram();
        Position start = this.parameters.getCourse().get(0);
        Position end = this.parameters.getCourse().get(1);
        TimePoint startTime = wf.getStartTime();// new MillisecondsTimePoint(0);
        List<TimedPositionWithSpeed> lst = new ArrayList<TimedPositionWithSpeed>();

        Position currentPosition = start;
        TimePoint currentTime = startTime;

        int stepsLeft = 0;
        int stepsRight = 0;
        boolean allLeft = true;
        boolean allRight = true;

        int prevDirection = -1;
        long turnloss = pd.getTurnLoss(); // 4000; // time lost when doing a turn
        long windpred = 1000; // time used to predict wind, i.e. hypothetical sailors prediction
        double fracFinishPhase = 0.05;

        TimePoint leftTime;
        TimePoint rightTime;

        Wind wndStart = wf.getWind(new TimedPositionWithSpeedImpl(startTime, start, null));

        LOGGER.fine("wndStart speed:" + wndStart.getKnots() + " angle:" + wndStart.getBearing().getDegrees());

        pd.setWind(wndStart);
        Bearing bearStart = currentPosition.getBearingGreatCircle(end);
        SpeedWithBearing spdStart = pd.getSpeedAtBearing(bearStart);
        lst.add(new TimedPositionWithSpeedImpl(startTime, start, spdStart));
        long timeStep = wf.getTimeStep().asMillis();

        LOGGER.info("Time step :" + timeStep);

        // while there is more than 5% of the total distance to the finish

        //
        // StrategicPhase: start & intermediate course until close to target
        //
        SpeedWithBearing slft = null;
        SpeedWithBearing srght = null;
        while (currentPosition.getDistance(end).compareTo(start.getDistance(end).scale(fracFinishPhase)) > 0) {

            // TimePoint nextTime = new MillisecondsTimePoint(currentTime.asMillis() + 30000);

            long nextTimeVal = currentTime.asMillis() + timeStep;// + 30000;
            TimePoint nextTime = new MillisecondsTimePoint(nextTimeVal);

            Wind cWind = wf.getWind(new TimedPositionWithSpeedImpl(currentTime, currentPosition, null));

            LOGGER.fine("cWind speed:" + cWind.getKnots() + " angle:" + cWind.getBearing().getDegrees());

            // System.out.println("Start WindBear: " + (cWind.getBearing().getDegrees() - bearStart.getDegrees()));
            pd.setWind(cWind);

            // get wind of direction
            Bearing wLft = pd.optimalDirectionsUpwind()[0];
            Bearing wRght = pd.optimalDirectionsUpwind()[1];
            // Bearing wDirect = currentPosition.getBearingGreatCircle(end);

            // SpeedWithBearing sWDirect = pd.getSpeedAtBearing(wDirect);
            SpeedWithBearing sWLft = pd.getSpeedAtBearing(wLft);
            SpeedWithBearing sWRght = pd.getSpeedAtBearing(wRght);

            LOGGER.fine("left boat speed:" + sWLft.getKnots() + " angle:" + sWLft.getBearing().getDegrees() + "  right boat speed:"
                    + sWRght.getKnots() + " angle:" + sWRght.getBearing().getDegrees());

            TimePoint wTime = new MillisecondsTimePoint(currentTime.asMillis() + windpred);
            // Position pWDirect = sWDirect.travelTo(currentPosition, currentTime, wTime);
            Position pWLft = sWLft.travelTo(currentPosition, currentTime, wTime);
            Position pWRght = sWRght.travelTo(currentPosition, currentTime, wTime);

            LOGGER.fine("current Pos:" + currentPosition.getLatDeg() + "," + currentPosition.getLngDeg());
            LOGGER.fine("left    Pos:" + pWLft.getLatDeg() + "," + pWLft.getLngDeg());
            LOGGER.fine("right   Pos:" + pWRght.getLatDeg() + "," + pWRght.getLngDeg());

            // calculate next step
            // Wind dWind = wf.getWind(new TimedPositionWithSpeedImpl(currentTime, pWDirect, null));
            // logger.fine("dWind speed:" + dWind.getKnots() + " angle:" + dWind.getBearing().getDegrees());
            // pd.setWind(dWind);
            // Bearing direct = currentPosition.getBearingGreatCircle(end);
            // SpeedWithBearing sdirect = pd.getSpeedAtBearing(direct);

            Wind lWind = wf.getWind(new TimedPositionWithSpeedImpl(currentTime, pWLft, null));

            LOGGER.fine("lWind speed:" + lWind.getKnots() + " angle:" + lWind.getBearing().getDegrees());
            // System.out.println("Left WindBear: " + (lWind.getBearing().getDegrees() - bearStart.getDegrees()));
            pd.setWind(lWind);
            Bearing lft = pd.optimalDirectionsUpwind()[0];
            slft = pd.getSpeedAtBearing(lft);

            Wind rWind = wf.getWind(new TimedPositionWithSpeedImpl(currentTime, pWRght, null));

            LOGGER.fine("rWind speed:" + rWind.getKnots() + " angle:" + rWind.getBearing().getDegrees());
            // System.out.println("Right WindBear: " + (rWind.getBearing().getDegrees() - bearStart.getDegrees()));
            pd.setWind(rWind);
            Bearing rght = pd.optimalDirectionsUpwind()[1];
            srght = pd.getSpeedAtBearing(rght);

            // System.out.println("Bearings : " + (lft.getDegrees() - bearStart.getDegrees()) + " "
            // + (rght.getDegrees() - bearStart.getDegrees()));

            LOGGER.fine("left boat speed:" + slft.getKnots() + " angle:" + slft.getBearing().getDegrees() + "  right boat speed:"
                    + srght.getKnots() + " angle:" + srght.getBearing().getDegrees());

            /*
             * if (prevDirection == 0) { directTime = new MillisecondsTimePoint(nextTimeVal); leftTime = new
             * MillisecondsTimePoint(nextTimeVal - turnloss); rightTime = new MillisecondsTimePoint(nextTimeVal -
             * turnloss); } else
             */
            if (prevDirection == 1) {
                leftTime = new MillisecondsTimePoint(nextTimeVal);
                rightTime = new MillisecondsTimePoint(nextTimeVal - turnloss);
            } else if (prevDirection == 2) {
                leftTime = new MillisecondsTimePoint(nextTimeVal - turnloss);
                rightTime = new MillisecondsTimePoint(nextTimeVal);
            } else {
                leftTime = new MillisecondsTimePoint(nextTimeVal);
                rightTime = new MillisecondsTimePoint(nextTimeVal);
            }

            // Position pdirect = sdirect.travelTo(currentPosition, currentTime, directTime);
            Position plft = slft.travelTo(currentPosition, currentTime, leftTime);
            Position prght = srght.travelTo(currentPosition, currentTime, rightTime);

            // System.out.println("RelBearLeft : " + (slft.getBearing().getDegrees() - bearStart.getDegrees()));
            // System.out.println("RelBearRight: " + (srght.getBearing().getDegrees() - bearStart.getDegrees()));

            // Distance ddirect = pdirect.getDistance(end);
            Distance dlft = plft.getDistance(end);
            Distance drght = prght.getDistance(end);

            double lDistCM = Math.round(dlft.getMeters() * 1000.) / 1000.;
            double rDistCM = Math.round(drght.getMeters() * 1000.) / 1000.;

            /*
             * if (ddirect.compareTo(dlft) <= 0 && ddirect.compareTo(drght) <= 0) { lst.add(new
             * TimedPositionWithSpeedImpl(nextTime, pdirect, sdirect)); currentPosition = pdirect; prevDirection = 0; }
             */

            if (prevDirection == -1) {

                if (startLeft) {
                    lst.add(new TimedPositionWithSpeedImpl(nextTime, plft, slft));
                    currentPosition = plft;
                    if (prevDirection == 2) {
                        allLeft = false;
                    } else {
                        stepsLeft++;
                    }
                    prevDirection = 1;
                } else {
                    lst.add(new TimedPositionWithSpeedImpl(nextTime, prght, srght));
                    currentPosition = prght;
                    if (prevDirection == 1) {
                        allRight = false;
                    } else {
                        stepsRight++;
                    }
                    prevDirection = 2;
                }

            } else {
                // System.out.println("Distance Left - Right: "+lDistCM+" - "+ rDistCM);
                if (((lDistCM <= rDistCM) && (!allLeft || (stepsLeft < maxLeft))) || (allRight && (stepsRight >= maxRight))) {
                    lst.add(new TimedPositionWithSpeedImpl(nextTime, plft, slft));
                    currentPosition = plft;
                    if (prevDirection == 2) {
                        allLeft = false;
                    } else {
                        stepsLeft++;
                    }
                    prevDirection = 1;
                } else {
                    // if (((drght.compareTo(dlft) < 0)&&(stepsRight < maxRight))||(stepsLeft >= maxLeft)) {
                    lst.add(new TimedPositionWithSpeedImpl(nextTime, prght, srght));
                    currentPosition = prght;
                    if (prevDirection == 1) {
                        allRight = false;
                    } else {
                        stepsRight++;
                    }
                    prevDirection = 2;
                }
            }

            currentTime = nextTime;

        }

        Position first = currentPosition;
        Position second = this.parameters.getCourse().get(1);

        if (first.getDistance(second).getMeters() > MINIMUM_DISTANCE_METERS) {

            //
            // FinishPhase: get 1-turners to finalize course
            //

            PathGenerator1Turner gen1Turner = new PathGenerator1Turner(this.parameters);
            TimePoint leftGoingTime;
            TimePoint rightGoingTime;
            if (prevDirection == 1) {
                leftGoingTime = currentTime;
                rightGoingTime = new MillisecondsTimePoint(currentTime.asMillis() + turnloss);
            } else {
                leftGoingTime = new MillisecondsTimePoint(currentTime.asMillis() + turnloss);
                rightGoingTime = currentTime;
            }

            long oneTurnerTimeStep = wf.getTimeStep().asMillis() / 15;

            gen1Turner.setEvaluationParameters(true, currentPosition, leftGoingTime, oneTurnerTimeStep, 300);
            Path leftPath = gen1Turner.getPath();

            gen1Turner.setEvaluationParameters(false, currentPosition, rightGoingTime, oneTurnerTimeStep, 300);
            Path rightPath = gen1Turner.getPath();

            if ((leftPath.getPathPoints() != null) && (rightPath.getPathPoints() != null)) {
                if (leftPath.getPathPoints().get(leftPath.getPathPoints().size() - 1).getTimePoint().asMillis() <= rightPath.getPathPoints()
                        .get(rightPath.getPathPoints().size() - 1).getTimePoint().asMillis()) {
                    lst.addAll(leftPath.getPathPoints());
                } else {
                    lst.addAll(rightPath.getPathPoints());
                }
            } else if (leftPath.getPathPoints() != null) {
                lst.addAll(leftPath.getPathPoints());
            } else if (rightPath.getPathPoints() != null) {
                lst.addAll(rightPath.getPathPoints());
            }
        }

        return new PathImpl(lst, wf);

    }
}
