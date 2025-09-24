package com.sap.sailing.domain.maneuverhash.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.maneuverhash.ManeuverRaceFingerprint;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
//import com.sap.sailing.domain.markpassinghash.MarkPassingRaceFingerprintRegistry;
//import com.sap.sailing.domain.markpassinghash.impl.MarkPassingRaceFingerprintImpl;
//import com.sap.sailing.domain.markpassinghash.impl.MarkPassingRaceFingerprintImpl.JSON_FIELDS;
import com.sap.sailing.domain.markpassinghash.MarkPassingRaceFingerprint;
import com.sap.sailing.domain.markpassinghash.MarkPassingRaceFingerprintFactory;
//import com.sap.sailing.domain.markpassinghash.impl.MarkPassingRaceFingerprintImpl.JSON_FIELDS;

public class ManeuverRaceFingerprintImpl implements ManeuverRaceFingerprint {

    MarkPassingRaceFingerprintFactory factory = MarkPassingRaceFingerprintFactory.INSTANCE;
    
    private static enum JSON_FIELDS {
        COMPETITOR_HASH, START_OF_TRACKING_AS_MILLIS, END_OF_TRACKING_AS_MILLIS, NUMBEROFGPSFIXES, 
        GPSFIXES_HASH, RACE_ID, MARKPASSINGFINGERPRINT, START_TIME_RECEIVED_AS_MILLIS
    };

    private final int competitorHash;
    private final int numberOfGPSFixes;
    private final int gpsFixesHash;
    private final MarkPassingRaceFingerprint markPassingFingerprint;
    private final TimePoint startOfTracking;
    private final TimePoint startTimeReceived;
    private final TimePoint endOfTracking;
    
    
    public ManeuverRaceFingerprintImpl( TrackedRace trackedRace ) {
        this.competitorHash = calculateHashForCompetitors(trackedRace);
        this.numberOfGPSFixes = calculateHashForNumberOfGPSFixes(trackedRace);
        this.gpsFixesHash = calculateHashForGPSFixes(trackedRace);
        this.markPassingFingerprint = factory.createFingerprint(trackedRace);
        this.startOfTracking = trackedRace.getStartOfTracking();
        this.endOfTracking = trackedRace.getEndOfTracking();
        this.startTimeReceived = trackedRace.getStartTimeReceived();
    }
    
    
    public ManeuverRaceFingerprintImpl(JSONObject json) {
        this.competitorHash = ((Number) json.get(JSON_FIELDS.COMPETITOR_HASH.name())).intValue();
        this.numberOfGPSFixes = ((Number) json.get(JSON_FIELDS.NUMBEROFGPSFIXES.name())).intValue();
        this.gpsFixesHash = ((Number) json.get(JSON_FIELDS.GPSFIXES_HASH.name())).intValue();
        this.markPassingFingerprint = (MarkPassingRaceFingerprint) json.get(JSON_FIELDS.MARKPASSINGFINGERPRINT.name());
        this.startOfTracking = TimePoint.of((Long) json.get(JSON_FIELDS.START_OF_TRACKING_AS_MILLIS.name()));
        this.endOfTracking = TimePoint.of((Long) json.get(JSON_FIELDS.END_OF_TRACKING_AS_MILLIS.name()));
        this.startTimeReceived = TimePoint.of((Long) json.get(JSON_FIELDS.START_TIME_RECEIVED_AS_MILLIS.name()));
    }
    
    
    @Override
    public JSONObject toJson() {
        JSONObject result = new JSONObject();
        result.put(JSON_FIELDS.COMPETITOR_HASH.name(), competitorHash);
        result.put(JSON_FIELDS.START_OF_TRACKING_AS_MILLIS.name(),
                startOfTracking == null ? null : startOfTracking.asMillis());
        result.put(JSON_FIELDS.END_OF_TRACKING_AS_MILLIS.name(),
                endOfTracking == null ? null : endOfTracking.asMillis());
        result.put(JSON_FIELDS.START_TIME_RECEIVED_AS_MILLIS.name(),
                startTimeReceived == null ? null : startTimeReceived.asMillis());
        result.put(JSON_FIELDS.NUMBEROFGPSFIXES.name(), numberOfGPSFixes);
        result.put(JSON_FIELDS.GPSFIXES_HASH.name(), gpsFixesHash);
        result.put(JSON_FIELDS.MARKPASSINGFINGERPRINT.name(), markPassingFingerprint);        
        
        return result;
    }

    @Override
    public boolean matches(TrackedRace trackedRace) {
        if( !Util.equalsWithNull(startOfTracking, trackedRace.getStartOfTracking())) {
            return false;
        }else if (!Util.equalsWithNull(endOfTracking, trackedRace.getEndOfTracking())) {
            return false;
        } else if (!Util.equalsWithNull(startTimeReceived, trackedRace.getStartTimeReceived())) {
            return false;
        } else if (competitorHash != calculateHashForCompetitors(trackedRace) ) {
            return false;
        } else if (gpsFixesHash != calculateHashForGPSFixes(trackedRace)) {
            return false;
        } else if ( markPassingFingerprint != factory.createFingerprint(trackedRace)) {
            return false;
        } else if (numberOfGPSFixes != calculateHashForNumberOfGPSFixes(trackedRace)) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + competitorHash;
        result = prime * result + ((endOfTracking == null) ? 0 : endOfTracking.hashCode());
        result = prime * result + gpsFixesHash;
        result = prime * result + numberOfGPSFixes;
        result = prime * result + ((startOfTracking == null) ? 0 : startOfTracking.hashCode());
        result = prime * result + ((startTimeReceived == null) ? 0 : startTimeReceived.hashCode());
        result = prime * result + (markPassingFingerprint.hashCode()); // Test which method is called
        return result;
    }
    
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ManeuverRaceFingerprintImpl other = (ManeuverRaceFingerprintImpl) obj;
        if (competitorHash != other.competitorHash)
            return false;
        if (endOfTracking == null) {
            if (other.endOfTracking != null)
                return false;
        } else if (!endOfTracking.equals(other.endOfTracking))
            return false;  
        if (gpsFixesHash != other.gpsFixesHash)
            return false;
        if (numberOfGPSFixes != other.numberOfGPSFixes)
            return false;
        if (startOfTracking == null) {
            if (other.startOfTracking != null)
                return false;
        } else if (!startOfTracking.equals(other.startOfTracking))
            return false;  
        if (startTimeReceived == null) {
            if (other.startTimeReceived != null)
                return false;
        } else if (!startTimeReceived.equals(other.startTimeReceived))
            return false;
        if (markPassingFingerprint != other.markPassingFingerprint)
            return false;
        
       
        return true;
    }
    
    
    
    
    
    
    // Kopierte Methoden
    private int calculateHashForCompetitors(TrackedRace trackedRace) {
        int hashForCompetitors = 1023;
        for (Competitor c : trackedRace.getRace().getCompetitors()) {
            hashForCompetitors = hashForCompetitors ^ c.getId().hashCode();
        }
        return hashForCompetitors;
    } // Methode already implemteed in MarkPassingRaceFingerprintImpl, but set to private -> ask if its possible to set to public
    
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
                for (GPSFix gf : markTrack.getRawFixes()) {
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
                for (GPSFixMoving gfm : competitorTrack.getRawFixes()) {
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
    } // same Problem as explained above, Methods existing put private

}
