package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.CompetitorResult.MergeState;
import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningEvent;
import com.sap.sailing.domain.abstractlog.race.impl.CompetitorResultImpl;
import com.sap.sailing.domain.abstractlog.race.impl.CompetitorResultsImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.DynamicCompetitor;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogFinishPositioningConfirmedEventSerializer;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.util.impl.UUIDHelper;

public abstract class RaceLogFinishPositioningEventDeserializer extends BaseRaceLogEventDeserializer {
    
    public RaceLogFinishPositioningEventDeserializer(JsonDeserializer<DynamicCompetitor> competitorDeserializer) {
        super(competitorDeserializer);
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint createdAt, AbstractLogEventAuthor author, TimePoint timePoint, int passId, List<Competitor> competitors)
            throws JsonDeserializationException {
        JSONArray jsonPositionedCompetitors = Helpers.getNestedArraySafe(object, RaceLogFinishPositioningConfirmedEventSerializer.FIELD_POSITIONED_COMPETITORS);
        CompetitorResults positionedCompetitors = deserializePositionedCompetitors(jsonPositionedCompetitors);
        return createRaceLogFinishPositioningEvent(id, createdAt, author, timePoint, passId, positionedCompetitors);
    }

    protected abstract RaceLogFinishPositioningEvent createRaceLogFinishPositioningEvent(Serializable id,
            TimePoint createdAt, AbstractLogEventAuthor author, TimePoint timePoint, int passId,
            CompetitorResults positionedCompetitors);
    
    private CompetitorResults deserializePositionedCompetitors(JSONArray jsonPositionedCompetitors) throws JsonDeserializationException {
        CompetitorResults positionedCompetitors = new CompetitorResultsImpl();
        int rankCounter = 1;
        for (Object object : jsonPositionedCompetitors) {
            JSONObject jsonPositionedCompetitor = Helpers.toJSONObjectSafe(object);
            Serializable competitorId = (Serializable) jsonPositionedCompetitor.get(RaceLogFinishPositioningConfirmedEventSerializer.FIELD_COMPETITOR_ID);
            competitorId = UUIDHelper.tryUuidConversion(competitorId);
            final String competitorDisplayName = (String) jsonPositionedCompetitor.get(RaceLogFinishPositioningConfirmedEventSerializer.FIELD_COMPETITOR_NAME);
            final String maxPointsReasonName = (String) jsonPositionedCompetitor.get(RaceLogFinishPositioningConfirmedEventSerializer.FIELD_SCORE_CORRECTIONS_MAX_POINTS_REASON);
            final MaxPointsReason maxPointsReason = MaxPointsReason.valueOf(maxPointsReasonName);
            final Number rank = (Number) jsonPositionedCompetitor.get(RaceLogFinishPositioningConfirmedEventSerializer.FIELD_RANK);
            final Double score = (Double) jsonPositionedCompetitor.get(RaceLogFinishPositioningConfirmedEventSerializer.FIELD_SCORE);
            final Long finishingTimePointAsMillis = (Long) jsonPositionedCompetitor.get(RaceLogFinishPositioningConfirmedEventSerializer.FIELD_FINISHING_TIME_POINT_AS_MILLIS);
            final TimePoint finishingTime = finishingTimePointAsMillis == null ? null : new MillisecondsTimePoint(finishingTimePointAsMillis);
            final String comment = (String) jsonPositionedCompetitor.get(RaceLogFinishPositioningConfirmedEventSerializer.FIELD_COMMENT);
            final String mergeStateAsString = (String) jsonPositionedCompetitor.get(RaceLogFinishPositioningConfirmedEventSerializer.FIELD_MERGE_STATE);
            final MergeState mergeState;
            if (mergeStateAsString == null) {
                mergeState = MergeState.OK;
            } else {
                mergeState = MergeState.valueOf(mergeStateAsString);
            }
            CompetitorResultImpl positionedCompetitor = new CompetitorResultImpl(
                    competitorId, competitorDisplayName, rank == null ? rankCounter : rank.intValue(), maxPointsReason, score, finishingTime, comment, mergeState);
            rankCounter++;
            positionedCompetitors.add(positionedCompetitor);
        }
        return positionedCompetitors;
    }

}
