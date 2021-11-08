package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONArray;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.maneuverdetection.TrackTimeInfo;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;

/**
 * 
 * @author Vladislav Chumak (D069712)
 * @see CompetitorTrackWithEstimationDataJsonSerializer
 */
public interface CompetitorTrackElementsJsonSerializer {

    JSONArray serialize(TrackedRace trackedRace, Competitor competitor, TimePoint from, TimePoint to,
            TrackTimeInfo trackTimeInfo);

}
