package com.sap.sailing.domain.swisstimingadapter.impl;

import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.Speed;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.swisstimingadapter.Fix;
import com.sap.sailing.domain.swisstimingadapter.TrackerType;

public class FixImpl implements Fix {
    private final String boatID;
    private final TrackerType trackerType;
    private final long ageOfDataInMilliseconds;
    private final Position position;
    private final SpeedWithBearing speed;
    private final int nextMarkIndex;
    private final int rank;
    private final Speed averageSpeedOverGroundPerLeg;
    private final Speed velocityMadeGood;
    private final Distance distanceToLeader;
    private final Distance distanceToNextMark;

    
    public FixImpl(String boatID, TrackerType trackerType, long ageOfDataInMilliseconds, Position position,
            SpeedWithBearing speed, int nextMarkIndex, int rank, Speed averageSpeedOverGroundPerLeg,
            Speed velocityMadeGood, Distance distanceToLeader, Distance distanceToNextMark) {
        super();
        this.boatID = boatID;
        this.trackerType = trackerType;
        this.ageOfDataInMilliseconds = ageOfDataInMilliseconds;
        this.position = position;
        this.speed = speed;
        this.nextMarkIndex = nextMarkIndex;
        this.rank = rank;
        this.averageSpeedOverGroundPerLeg = averageSpeedOverGroundPerLeg;
        this.velocityMadeGood = velocityMadeGood;
        this.distanceToLeader = distanceToLeader;
        this.distanceToNextMark = distanceToNextMark;
    }

    @Override
    public String getBoatID() {
        return boatID;
    }

    @Override
    public TrackerType getTrackerType() {
        return trackerType;
    }

    @Override
    public long getAgeOfDataInMilliseconds() {
        return ageOfDataInMilliseconds;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public SpeedWithBearing getSpeed() {
        return speed;
    }

    @Override
    public int getNextMarkIndex() {
        return nextMarkIndex;
    }

    @Override
    public int getRank() {
        return rank;
    }

    @Override
    public Speed getVelocityMadeGood() {
        return velocityMadeGood;
    }

    @Override
    public Distance getDistanceToLeader() {
        return distanceToLeader;
    }

    @Override
    public Distance getDistanceToNextMark() {
        return distanceToNextMark;
    }

    @Override
    public Speed getAverageSpeedOverGroundPerLeg() {
        return averageSpeedOverGroundPerLeg;
    }

}
