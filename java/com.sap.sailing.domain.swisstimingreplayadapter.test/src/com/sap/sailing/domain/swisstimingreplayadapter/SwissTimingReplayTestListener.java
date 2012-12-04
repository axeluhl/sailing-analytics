package com.sap.sailing.domain.swisstimingreplayadapter;

import java.util.Date;

public class SwissTimingReplayTestListener implements SwissTimingReplayListener {
    
    private long referenceTimestamp;
    private int referenceLatitude;
    private int referenceLongitude;
    
    @Override
    public void rsc_cid(String text) {
        System.out.println("rsc_cid - text: " + text);
        
    }
    
    @Override
    public void referenceTimestamp(long referenceTimestamp) {
        this.referenceTimestamp = referenceTimestamp;
        System.out.println("referenceTimestamp - referenceTimestamp: " + new Date(this.referenceTimestamp));
    }
    
    @Override
    public void referenceLocation(int latitude, int longitude) {
        this.referenceLatitude = latitude;
        this.referenceLongitude = longitude;
        System.out.println("referenceLocation - latitude: " + (double) this.referenceLatitude / 1E7 + ", longitude: " + (double) this.referenceLongitude / 1E7); 
        
    }
    
    @Override
    public void keyFrameIndexPosition(int keyFrameIndexPosition) {
        System.out.println("keyFrameIndexPosition - keyFrameIndexPosition: " + keyFrameIndexPosition);
        
    }
    
    @Override
    public void keyFrameIndex(int keyFrameIndex) {
        System.out.println("keyFrameIndex - keyFrameIndex:" + keyFrameIndex); 
        
    }
    
    @Override
    public void competitorsCount(short competitorsCount) {
        System.out.println("competitorsCount - competitorsCount: " + competitorsCount);
        
    }
    
    @Override
    public void competitor(int hashValue, String nation, String sailNumber, String name, Status status,
            BoatType boatType, short cRank_Bracket, short cnPoints_x10_Bracket, short ctPoints_x10_Winner) {
        System.out.println("competitor - hashValue: " + hashValue + ", nation: " + nation + ", sailNumber: " + sailNumber + ", name: " + name + ", status: " + status + ", boatType: " + boatType + ", cRank_Bracket: " + cRank_Bracket + ", cnPoints_x10_Bracket: " + cnPoints_x10_Bracket + ", ctPoints_x10_Winner: " + ctPoints_x10_Winner);
        
    }

    @Override
    public void mark(MarkType markType, String identifier, byte index, String id1, String id2, short windSpeed,
            short windDirection) {
        System.out.println("mark - markType: " + markType + ", identifier: " + identifier + ", index: " + index + ", id1: " + id1 + ", id2: " + id2 + ", windSpeed: " + windSpeed + ", windDirection: " + windDirection);
        
    }

    @Override
    public void frame(byte cid, int raceTime, int startTime, int estimatedStartTime, Status status,
            short distanceToNextMark, Weather weather, short humidity, short temperature, String messageText,
            byte cFlag, byte rFlag, byte duration, short nm) {
        System.out.println("frame - cid: " + cid + ", raceTime: " + raceTime + ", startTime: " + startTime + ", estimatedStartTime: " + estimatedStartTime + ", status: " + status + ", distanceToNextMark: " + distanceToNextMark + ", weather: " + weather + ", humidity: " + humidity + ", temperature: " + temperature + ", messageText: " + messageText + ", cFlag: " + cFlag + ", rFlag: " + rFlag + ", duration: " + duration + ", nm: " + nm);
        
    }

    @Override
    public void ranking(int hashValue, short rank, short rankIndex, short racePoints, Status status,
            short finishRank, short finishRankIndex, int gap, int raceTime) {
        System.out.println("ranking - hashValue: " + hashValue + ", rank: " + rank + ", rankIndex: " + rankIndex + ", racePoints: " + racePoints + ", status: " + status + ", finishRank: " + finishRank + ", finishRankIndex: " + finishRankIndex + ", gap: " + gap + ", raceTime: " + raceTime);
        
    }

    @Override
    public void rankingsCount(short entriesCount) {
        System.out.println("rankingsCount - entriesCount: " + entriesCount);
        
    }

    @Override
    public void rankingMark(short marksRank, short marksRankIndex, int marksGap, int marksRaceTime) {
        System.out.println("rankingMark - marksRank: " + marksRank + ", marksRankIndex: " + marksRankIndex + ", marksGap: " + marksGap + ", marksRaceTime: " + marksRaceTime);
        
    }

    @Override
    public void trackersCount(short trackersCount) {
        System.out.println("trackersCount - trackersCount: " + trackersCount);
        
    }

    @Override
    public void trackers(int hashValue, int latitude, int longitude, short cog, short sog, short average_sog,
            short vmg, Status status, short rank, short dtl, short dtnm, short nm, short pRank, short ptPoints,
            short pnPoints) {
        latitude = referenceLatitude + latitude;
        longitude = referenceLongitude + longitude;
        System.out.println("trackers - hashValue: " + hashValue + ", latitude: " + (double) latitude / 1E7 + ", longitude: " + (double) longitude / 1E7 + ", cog: " + cog + ", sog: " + sog + ", average_sog: " + average_sog + ", vmg: " + vmg + ", status: " + status + ", rank: " + rank + ", dtl: " + dtl + ", dtnm: " + dtnm + ", nm: " + nm + ", pRank: " + pRank + ", ptPoints: " + ptPoints + ", pnPoints: " + pnPoints);
        
    }

    @Override
    public void illegalState(String message) {
        throw new IllegalStateException(message);
    }

    @Override
    public void unknownMessageIdentificationCode(byte messageIdentificationCode) {
        throw new IllegalArgumentException("Unknown message identification code: " +  messageIdentificationCode);
    }

    @Override
    public void eot() {
        System.out.println("EOT");
    }

}
