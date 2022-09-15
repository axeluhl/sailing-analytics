package com.sap.sailing.domain.markpassinghash.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.markpassingcalculation.MarkPassingCalculator;
import com.sap.sailing.domain.markpassinghash.MarkPassingHashFingerprint;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;

public class MarkPassingHashFingerprintImpl implements MarkPassingHashFingerprint {
    private final int calculatorVersion;
    private final int competitorHash;
    private final TimePoint startOfTracking;
    private final TimePoint startTimeReceived;
    private final TimePoint endOfTracking;
    private final TimePoint startTimeFromRaceLog;
    private final TimePoint finishTimeFromRaceLog;
    private final int waypointsHash;
    private final int numberOfGPSFixes;
    private final int gpsFixesHash;

    private static enum JSON_FIELDS {
        COMPETITOR_HASH, START_OF_TRACKING_AS_MILLIS, END_OF_TRACKING_AS_MILLIS, START_TIME_RECEIVED_AS_MILLIS, START_TIME_FROM_RACE_LOG_AS_MILLIS,
        FINISH_TIME_FROM_RACE_LOG_AS_MILLIS, WAYPOINTS_HASH, NUMBEROFGPSFIXES, GPSFIXES_HASH, RACE_ID, CALCULATOR_VERSION
    };

    public MarkPassingHashFingerprintImpl(TrackedRace trackedRace) {
        this.calculatorVersion = getCalculatorVersion(trackedRace);
        this.competitorHash = calculateHashForCompetitors(trackedRace);
        this.startOfTracking = trackedRace.getStartOfTracking();
        this.endOfTracking = trackedRace.getEndOfTracking();
        this.startTimeReceived = trackedRace.getStartTimeReceived();
        final Pair<TimePoint, TimePoint> startAndFinishedTimeFromRaceLogs = trackedRace.getStartAndFinishedTimeFromRaceLogs();
        this.startTimeFromRaceLog = startAndFinishedTimeFromRaceLogs==null?null:startAndFinishedTimeFromRaceLogs.getA();
        this.finishTimeFromRaceLog = startAndFinishedTimeFromRaceLogs==null?null:startAndFinishedTimeFromRaceLogs.getB();
        this.waypointsHash = calculateHashForWaypoints(trackedRace);
        this.numberOfGPSFixes = calculateHashForNumberOfGPSFixes(trackedRace);
        this.gpsFixesHash = calculateHashForGPSFixes(trackedRace);
    }

    public MarkPassingHashFingerprintImpl(JSONObject json) {
        this.calculatorVersion = ((Number) json.get(JSON_FIELDS.CALCULATOR_VERSION.toString())).intValue();
        this.competitorHash = ((Number) json.get(JSON_FIELDS.COMPETITOR_HASH.toString())).intValue();
        final Number startOfTrackingAsNumber = (Number) json.get(JSON_FIELDS.START_OF_TRACKING_AS_MILLIS.toString());
        this.startOfTracking = startOfTrackingAsNumber==null?null:TimePoint.of(startOfTrackingAsNumber.longValue());
        final Number endOfTrackingAsNumber = (Number) json.get(JSON_FIELDS.END_OF_TRACKING_AS_MILLIS.toString());
        this.endOfTracking = endOfTrackingAsNumber==null?null:TimePoint.of(endOfTrackingAsNumber.longValue());
        final Number startTimeReceivedAsNumber = (Number) json.get(JSON_FIELDS.START_TIME_RECEIVED_AS_MILLIS.toString());
        this.startTimeReceived = startTimeReceivedAsNumber==null?null:TimePoint.of(startTimeReceivedAsNumber.longValue());
        final Number startTimeFromRaceLogAsNumber = (Number) json.get(JSON_FIELDS.START_TIME_FROM_RACE_LOG_AS_MILLIS.toString());
        this.startTimeFromRaceLog = startTimeFromRaceLogAsNumber==null?null:TimePoint.of(startTimeFromRaceLogAsNumber.longValue());
        final Number finishTimeFromRaceLogAsNumber = (Number) json.get(JSON_FIELDS.FINISH_TIME_FROM_RACE_LOG_AS_MILLIS.toString());
        this.finishTimeFromRaceLog = finishTimeFromRaceLogAsNumber==null?null:TimePoint.of(finishTimeFromRaceLogAsNumber.longValue());
        this.waypointsHash = ((Number) json.get(JSON_FIELDS.WAYPOINTS_HASH.toString())).intValue();
        this.numberOfGPSFixes = ((Number) json.get(JSON_FIELDS.NUMBEROFGPSFIXES.toString())).intValue();
        this.gpsFixesHash = ((Number) json.get(JSON_FIELDS.GPSFIXES_HASH.toString())).intValue();
    }

    @Override
    public JSONObject toJson() {
        JSONObject result = new JSONObject();
        result.put(JSON_FIELDS.CALCULATOR_VERSION, calculatorVersion);
        result.put(JSON_FIELDS.COMPETITOR_HASH, competitorHash);
        result.put(JSON_FIELDS.START_OF_TRACKING_AS_MILLIS, startOfTracking==null?null:startOfTracking.asMillis());
        result.put(JSON_FIELDS.END_OF_TRACKING_AS_MILLIS, endOfTracking==null?null:endOfTracking.asMillis());
        result.put(JSON_FIELDS.START_TIME_RECEIVED_AS_MILLIS, startTimeReceived==null?null:startTimeReceived.asMillis());
        result.put(JSON_FIELDS.START_TIME_FROM_RACE_LOG_AS_MILLIS, startTimeFromRaceLog==null?null:startTimeFromRaceLog.asMillis());
        result.put(JSON_FIELDS.FINISH_TIME_FROM_RACE_LOG_AS_MILLIS, finishTimeFromRaceLog==null?null:finishTimeFromRaceLog.asMillis());
        result.put(JSON_FIELDS.WAYPOINTS_HASH, waypointsHash);
        result.put(JSON_FIELDS.NUMBEROFGPSFIXES, numberOfGPSFixes);
        result.put(JSON_FIELDS.GPSFIXES_HASH, gpsFixesHash);
        return result;
    }

    @Override
    public boolean matches(TrackedRace trackedRace) {
        final boolean result;
        if (!Util.equalsWithNull(calculatorVersion, getCalculatorVersion(trackedRace))) {
            result = false;
        } else if (!Util.equalsWithNull(startOfTracking, trackedRace.getStartOfTracking())) {
            result = false;
        } else if (!Util.equalsWithNull(endOfTracking, trackedRace.getEndOfTracking())) {
            result = false;
        } else if (!Util.equalsWithNull(startTimeReceived, trackedRace.getStartTimeReceived())) {
            result = false;
        } else {
            final Pair<TimePoint, TimePoint> startAndFinishedTimeFromRaceLogs = trackedRace.getStartAndFinishedTimeFromRaceLogs();
            if (!Util.equalsWithNull(startTimeFromRaceLog, startAndFinishedTimeFromRaceLogs==null?null:startAndFinishedTimeFromRaceLogs.getA())) {
                result = false;
            } else if (!Util.equalsWithNull(finishTimeFromRaceLog, startAndFinishedTimeFromRaceLogs==null?null:startAndFinishedTimeFromRaceLogs.getB())) {
                result = false;
            } else if (waypointsHash != calculateHashForWaypoints(trackedRace)) {
                result = false;
            } else if (competitorHash != calculateHashForCompetitors(trackedRace)) {
                result = false;
            } else if (numberOfGPSFixes != calculateHashForNumberOfGPSFixes(trackedRace)) {
                result = false;
            } else if (gpsFixesHash != calculateHashForGPSFixes(trackedRace)) {
                result = false;
            } else {
                result = true;
            }
        }
        return result;
    }

    private int getCalculatorVersion(TrackedRace trackedRace) {
        MarkPassingCalculator calculator = new MarkPassingCalculator((DynamicTrackedRace) trackedRace, false, false);
        int result = calculator.getCalculatorVersion();
        return result;
    }
    
    private int calculateHashForCompetitors(TrackedRace trackedRace) {
        int hashForCompetitors = 1023;
        for (Competitor c : trackedRace.getRace().getCompetitors()) {
            hashForCompetitors = hashForCompetitors ^ c.getId().hashCode();
        }
        return hashForCompetitors;
    }

    private int calculateHashForNumberOfGPSFixes(TrackedRace trackedRace) {
        int count = 0;
        for (Mark m : trackedRace.getMarks()) {
            count += trackedRace.getTrack(m).size();
        }
        for (final Competitor competitor : trackedRace.getRace().getCompetitors()) {
            count += trackedRace.getTrack(competitor).size();
        }
        return count;
    }

    private int calculateHashForGPSFixes(TrackedRace trackedRace) {
        int res = 511;
        for (Mark m : trackedRace.getMarks()) {
            final GPSFixTrack<Mark, GPSFix> markTrack = trackedRace.getTrack(m);
            markTrack.lockForRead();
            try {
                for (GPSFix gf : markTrack.getFixes()) {
                    res = res ^ gf.getTimePoint().hashCode();
                    res = res ^ gf.getPosition().hashCode();
                }
            } finally {
                markTrack.unlockAfterRead();
            }
        }
        for (final Competitor competitor : trackedRace.getRace().getCompetitors()) {
            final GPSFixTrack<Competitor, GPSFixMoving> competitorTrack = trackedRace.getTrack(competitor);
            competitorTrack.lockForRead();
            try {
                for (GPSFixMoving gfm : competitorTrack.getFixes()) {
                    res = res ^ gfm.getTimePoint().hashCode();
                    res = res ^ gfm.getPosition().hashCode();
                    res = res ^ gfm.getSpeed().getBearing().hashCode();
                    res = res ^ Double.hashCode(gfm.getSpeed().getKnots());
                }
            } finally {
                competitorTrack.unlockAfterRead();
            }
        }
        return res;
    }

    private int calculateHashForWaypoints(TrackedRace trackedRace) {
        Iterable<Waypoint> waypoints = trackedRace.getRace().getCourse().getWaypoints();
        int res = 0;
        for (Waypoint p : waypoints) {
            Iterable<Mark> marks = p.getMarks();
            for (Mark m : marks) {
                res = res ^ m.getId().hashCode();
            }
            res = res ^ p.getPassingInstructions().hashCode();
            res = (res << 5) - res; // we want to detect changes in order
        }
        return res;
    }
}
