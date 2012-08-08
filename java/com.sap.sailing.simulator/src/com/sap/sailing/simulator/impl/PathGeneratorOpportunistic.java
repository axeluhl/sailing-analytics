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
import com.sap.sailing.simulator.PathGenerator;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.TimedPositionWithSpeed;
import com.sap.sailing.simulator.WindFieldGenerator;

public class PathGeneratorOpportunistic implements PathGenerator {

    private static Logger logger = Logger.getLogger("com.sap.sailing");
    SimulationParameters simulationParameters;
    int maxLeft;
    int maxRight;
    boolean startLeft;
    
    public PathGeneratorOpportunistic(SimulationParameters params) {
        simulationParameters = params;
    }

    @Override
    public void setSimulationParameters(SimulationParameters params) {
        simulationParameters = params;
    }

    @Override
    public SimulationParameters getSimulationParameters() {
        return simulationParameters;
    }

    public void setEvaluationParameters(int maxLeftVal, int maxRightVal, boolean startLeftVal) {
        this.maxLeft = maxLeftVal;        
        this.maxRight = maxRightVal;
        this.startLeft = startLeftVal;
    }

    @Override
    public Path getPath() {

        WindFieldGenerator wf = simulationParameters.getWindField();
        PolarDiagram pd = simulationParameters.getBoatPolarDiagram();
        Position start = simulationParameters.getCourse().get(0);
        Position end = simulationParameters.getCourse().get(1);
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

        TimePoint leftTime;
        TimePoint rightTime;

        Wind wndStart = wf.getWind(new TimedPositionWithSpeedImpl(startTime, start, null));
        logger.fine("wndStart speed:" + wndStart.getKnots() + " angle:" + wndStart.getBearing().getDegrees());
        pd.setWind(wndStart);
        Bearing bearStart = currentPosition.getBearingGreatCircle(end);
        SpeedWithBearing spdStart = pd.getSpeedAtBearing(bearStart);
        lst.add(new TimedPositionWithSpeedImpl(startTime, start, spdStart));
        long timeStep = wf.getTimeStep().asMillis();
        logger.info("Time step :" + timeStep);
        // while there is more than 5% of the total distance to the finish

        SpeedWithBearing slft = null;
        SpeedWithBearing srght = null;
        while (currentPosition.getDistance(end).compareTo(start.getDistance(end).scale(0.05)) > 0) {

            // TimePoint nextTime = new MillisecondsTimePoint(currentTime.asMillis() + 30000);

            long nextTimeVal = currentTime.asMillis() + timeStep;// + 30000;
            TimePoint nextTime = new MillisecondsTimePoint(nextTimeVal);

            Wind cWind = wf.getWind(new TimedPositionWithSpeedImpl(currentTime, currentPosition, null));
            logger.fine("cWind speed:" + cWind.getKnots() + " angle:" + cWind.getBearing().getDegrees());
            // System.out.println("Start WindBear: " + (cWind.getBearing().getDegrees() - bearStart.getDegrees()));
            pd.setWind(cWind);

            // get wind of direction
            Bearing wLft = pd.optimalDirectionsUpwind()[0];
            Bearing wRght = pd.optimalDirectionsUpwind()[1];
            // Bearing wDirect = currentPosition.getBearingGreatCircle(end);

            // SpeedWithBearing sWDirect = pd.getSpeedAtBearing(wDirect);
            SpeedWithBearing sWLft = pd.getSpeedAtBearing(wLft);
            SpeedWithBearing sWRght = pd.getSpeedAtBearing(wRght);
            logger.fine("left boat speed:" + sWLft.getKnots() + " angle:" + sWLft.getBearing().getDegrees()
                    + "  right boat speed:" + sWRght.getKnots() + " angle:" + sWRght.getBearing().getDegrees());

            TimePoint wTime = new MillisecondsTimePoint(currentTime.asMillis() + windpred);
            // Position pWDirect = sWDirect.travelTo(currentPosition, currentTime, wTime);
            Position pWLft = sWLft.travelTo(currentPosition, currentTime, wTime);
            Position pWRght = sWRght.travelTo(currentPosition, currentTime, wTime);

            logger.fine("current Pos:" + currentPosition.getLatDeg() + "," + currentPosition.getLngDeg());
            logger.fine("left    Pos:" + pWLft.getLatDeg() + "," + pWLft.getLngDeg());
            logger.fine("right   Pos:" + pWRght.getLatDeg() + "," + pWRght.getLngDeg());

            // calculate next step
            // Wind dWind = wf.getWind(new TimedPositionWithSpeedImpl(currentTime, pWDirect, null));
            // logger.fine("dWind speed:" + dWind.getKnots() + " angle:" + dWind.getBearing().getDegrees());
            // pd.setWind(dWind);
            // Bearing direct = currentPosition.getBearingGreatCircle(end);
            // SpeedWithBearing sdirect = pd.getSpeedAtBearing(direct);

            Wind lWind = wf.getWind(new TimedPositionWithSpeedImpl(currentTime, pWLft, null));
            logger.fine("lWind speed:" + lWind.getKnots() + " angle:" + lWind.getBearing().getDegrees());
            // System.out.println("Left WindBear: " + (lWind.getBearing().getDegrees() - bearStart.getDegrees()));
            pd.setWind(lWind);
            Bearing lft = pd.optimalDirectionsUpwind()[0];
            slft = pd.getSpeedAtBearing(lft);

            Wind rWind = wf.getWind(new TimedPositionWithSpeedImpl(currentTime, pWRght, null));
            logger.fine("rWind speed:" + rWind.getKnots() + " angle:" + rWind.getBearing().getDegrees());
            // System.out.println("Right WindBear: " + (rWind.getBearing().getDegrees() - bearStart.getDegrees()));
            pd.setWind(rWind);
            Bearing rght = pd.optimalDirectionsUpwind()[1];
            srght = pd.getSpeedAtBearing(rght);

            // System.out.println("Bearings : " + (lft.getDegrees() - bearStart.getDegrees()) + " "
            // + (rght.getDegrees() - bearStart.getDegrees()));

            logger.fine("left boat speed:" + slft.getKnots() + " angle:" + slft.getBearing().getDegrees()
                    + "  right boat speed:" + srght.getKnots() + " angle:" + srght.getBearing().getDegrees());

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
                if (((lDistCM <= rDistCM) && (!allLeft || (stepsLeft < maxLeft)))
                        || (allRight && (stepsRight >= maxRight))) {
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

        // approach end
        double bearingToEnd;
        SpeedWithBearing bearingBoat;
        if (prevDirection == 1) {
            bearingToEnd = currentPosition.getBearingGreatCircle(end).getDegrees()
                    - pd.optimalDirectionsUpwind()[1].getDegrees();
            bearingBoat = slft;
        } else {
            bearingToEnd = pd.optimalDirectionsUpwind()[0].getDegrees()
                    - currentPosition.getBearingGreatCircle(end).getDegrees();
            bearingBoat = srght;
        }
        bearingToEnd = (bearingToEnd + 360 + 180) % 360 - 180;
        // System.out.println("b2End:" + bearingToEnd);
        double bearingToEndSign = Math.signum(bearingToEnd);
        Position tackPosition = currentPosition;
        Position tackPositionNew = currentPosition;
        TimePoint tackTime = currentTime;
        TimePoint tackTimeNext = currentTime;
        long searchTime = 500;
        while (Math.signum(bearingToEnd) == bearingToEndSign) {
            tackTime = tackTimeNext;
            tackPosition = tackPositionNew;
            if (bearingToEnd < 0) {
                tackTimeNext = new MillisecondsTimePoint(tackTime.asMillis() + searchTime);
                tackPositionNew = bearingBoat.travelTo(tackPosition, tackTime, tackTimeNext);
            } else {
                tackTimeNext = new MillisecondsTimePoint(tackTime.asMillis() - searchTime);
                tackPositionNew = bearingBoat.travelTo(tackPosition, tackTime, tackTimeNext);
            }
            if (prevDirection == 1) {
                bearingToEnd = tackPositionNew.getBearingGreatCircle(end).getDegrees()
                        - pd.optimalDirectionsUpwind()[1].getDegrees();
                // System.out.println("bEnd :" + tackPosition.getBearingGreatCircle(end).getDegrees());
                // System.out.println("bBoat:" + pd.optimalDirectionsUpwind()[1].getDegrees());
            } else {
                bearingToEnd = pd.optimalDirectionsUpwind()[0].getDegrees()
                        - tackPositionNew.getBearingGreatCircle(end).getDegrees();
                // System.out.println("bEnd :" + tackPosition.getBearingGreatCircle(end).getDegrees());
                // System.out.println("bBoat:" + pd.optimalDirectionsUpwind()[0].getDegrees());
            }
            bearingToEnd = (bearingToEnd + 360 + 180) % 360 - 180;
            // System.out.println("b2End:" + bearingToEnd);
        }

        // scan more detailed between tackPosition and tackPositionNew
        boolean endIteration = false;
        while ((Math.abs(bearingToEnd) > 0.1) && (!endIteration)) {
            TimePoint tackTimeMid = new MillisecondsTimePoint((tackTime.asMillis() + tackTimeNext.asMillis()) / 2);
            if (tackTimeMid.asMillis() == tackTime.asMillis()) {
                endIteration = true;
            }
            Position tackPositionMid = bearingBoat.travelTo(tackPosition, tackTime, tackTimeMid);
            if (prevDirection == 1) {
                bearingToEnd = tackPositionMid.getBearingGreatCircle(end).getDegrees()
                        - pd.optimalDirectionsUpwind()[1].getDegrees();
                // System.out.println("bEnd :" + tackPositionMid.getBearingGreatCircle(end).getDegrees());
                // System.out.println("bBoat:" + pd.optimalDirectionsUpwind()[1].getDegrees());
            } else {
                bearingToEnd = pd.optimalDirectionsUpwind()[0].getDegrees()
                        - tackPositionMid.getBearingGreatCircle(end).getDegrees();
                // System.out.println("bEnd :" + tackPositionMid.getBearingGreatCircle(end).getDegrees());
                // System.out.println("bBoat:" + pd.optimalDirectionsUpwind()[0].getDegrees());
            }
            bearingToEnd = (bearingToEnd + 360 + 180) % 360 - 180;
            // System.out.println("fine b2End:" + bearingToEnd);
            if (Math.signum(bearingToEnd) == bearingToEndSign) {
                tackPosition = tackPositionMid;
                tackTime = tackTimeMid;
            } else {
                tackPositionNew = tackPositionMid;
                tackTimeNext = tackTimeMid;
            }
        }

        TimePoint tackTimeOpt = new MillisecondsTimePoint((tackTime.asMillis() + tackTimeNext.asMillis()) / 2);
        Position tackPositionOpt = bearingBoat.travelTo(tackPosition, tackTime, tackTimeOpt);

        // System.out.println("tack time:"+tackPosition.)
        lst.remove(lst.size() - 1);
        lst.add(new TimedPositionWithSpeedImpl(tackTimeOpt, tackPositionOpt, null));

        currentPosition = tackPosition;
        currentTime = tackTime;

        Wind wndEnd = wf.getWind(new TimedPositionWithSpeedImpl(currentTime, currentPosition, null));
        // System.out.println("wndEnd speed:" + wndEnd.getKnots() + " angle:" + wndEnd.getBearing().getDegrees());
        pd.setWind(wndEnd);
        Bearing bearEnd = currentPosition.getBearingGreatCircle(end);
        // System.out.println("bearEnd angle:" + bearEnd.getDegrees());
        SpeedWithBearing spdEnd = pd.getSpeedAtBearing(bearEnd);

        long nextTimeVal = currentTime.asMillis()
                + Math.round(currentPosition.getDistance(end).getMeters() / spdEnd.getMetersPerSecond()) * 1000;// 30000;
        // System.out.println("time:" + currentTime.asMillis() + " " + currentPosition.getDistance(end).getMeters()
        // + " speed:" + spdEnd.getMetersPerSecond());
        // System.out.println("next:" + nextTimeVal);
        TimePoint nextTime = new MillisecondsTimePoint(nextTimeVal + turnloss);

        lst.add(new TimedPositionWithSpeedImpl(nextTime, end, null));

        return new PathImpl(lst, wf);
        
    }

    @Override
    public List<TimedPositionWithSpeed> getPathEvenTimed(long millisecondsStep) {

        Path path = this.getPath();
        return path.getEvenTimedPoints(millisecondsStep);
        
    }
 
}
