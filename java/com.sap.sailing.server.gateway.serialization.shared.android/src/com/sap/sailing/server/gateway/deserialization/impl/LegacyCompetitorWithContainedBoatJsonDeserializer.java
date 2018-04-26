package com.sap.sailing.server.gateway.deserialization.impl;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.UUID;
import java.util.logging.Logger;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.CompetitorFactory;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.impl.CompetitorWithBoatImpl;
import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.domain.base.impl.DynamicTeam;
import com.sap.sailing.domain.common.tracking.impl.CompetitorJsonConstants;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorJsonSerializer;
import com.sap.sse.common.Color;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.RGBColor;
import com.sap.sse.util.impl.UUIDHelper;

public class LegacyCompetitorWithContainedBoatJsonDeserializer implements JsonDeserializer<CompetitorWithBoat> {
    protected final CompetitorFactory competitorWithBoatFactory;
    protected final JsonDeserializer<DynamicTeam> teamJsonDeserializer;
    protected final JsonDeserializer<DynamicBoat> boatJsonDeserializer;
    private static final Logger logger = Logger.getLogger(LegacyCompetitorWithContainedBoatJsonDeserializer.class.getName());

    public static LegacyCompetitorWithContainedBoatJsonDeserializer create(SharedDomainFactory baseDomainFactory) {
        return new LegacyCompetitorWithContainedBoatJsonDeserializer(baseDomainFactory, new TeamJsonDeserializer(new PersonJsonDeserializer(
                new NationalityJsonDeserializer(baseDomainFactory))), new LegacyBoatJsonDeserializer(baseDomainFactory, new BoatClassJsonDeserializer(baseDomainFactory)));
    }

    public LegacyCompetitorWithContainedBoatJsonDeserializer(CompetitorFactory competitorWithBoatFactory) {
        this(competitorWithBoatFactory, null, /* boatDeserializer */ null);
    }

    public LegacyCompetitorWithContainedBoatJsonDeserializer(CompetitorFactory competitorWithBoatFactory, JsonDeserializer<DynamicTeam> teamJsonDeserializer, JsonDeserializer<DynamicBoat> boatDeserializer) {
        this.competitorWithBoatFactory = competitorWithBoatFactory;
        this.teamJsonDeserializer = teamJsonDeserializer;
        this.boatJsonDeserializer = boatDeserializer;
    }

    @Override
    public CompetitorWithBoat deserialize(JSONObject object) throws JsonDeserializationException {
        Serializable competitorId = (Serializable) object.get(CompetitorJsonSerializer.FIELD_ID);
        try {
            Class<?> idClass = Class.forName((String) object.get(CompetitorJsonConstants.FIELD_ID_TYPE));
            if (Number.class.isAssignableFrom(idClass)) {
                Constructor<?> constructorFromString = idClass.getConstructor(String.class);
                competitorId = (Serializable) constructorFromString.newInstance(competitorId.toString());
            } else if (UUID.class.isAssignableFrom(idClass)) {
                competitorId = UUIDHelper.tryUuidConversion(competitorId);
            }
            String name = (String) object.get(CompetitorJsonConstants.FIELD_NAME);
            String shortName = (String) object.get(CompetitorJsonConstants.FIELD_SHORT_NAME);
            String displayColorAsString = (String) object.get(CompetitorJsonConstants.FIELD_DISPLAY_COLOR);
            String email = (String) object.get(CompetitorJsonConstants.FIELD_EMAIL);
            String searchTag = (String) object.get(CompetitorJsonConstants.FIELD_SEARCHTAG);
            
            URI flagImageURI = null;
            String flagImageURIAsString = (String) object.get(CompetitorJsonConstants.FIELD_FLAG_IMAGE_URI);
            if (flagImageURIAsString != null) {
                try {
                    flagImageURI = URI.create(flagImageURIAsString);
                } catch (IllegalArgumentException e) {
                    logger.warning("Illegal flag image URI " + e.getMessage());
                }
            }

            final Color displayColor;
            if (displayColorAsString == null || displayColorAsString.isEmpty()) {
                displayColor = null;
            } else {
                displayColor = new RGBColor(displayColorAsString);
            }
            DynamicTeam team = null;
            DynamicBoat boat = null;
            if (teamJsonDeserializer != null && object.get(CompetitorJsonConstants.FIELD_TEAM) != null) {
                team = teamJsonDeserializer.deserialize(Helpers.getNestedObjectSafe(object,
                        CompetitorJsonConstants.FIELD_TEAM));
            }
            if (boatJsonDeserializer != null && object.get(CompetitorJsonConstants.FIELD_BOAT) != null) {
                boat = boatJsonDeserializer.deserialize(Helpers.getNestedObjectSafe(object,
                        CompetitorJsonConstants.FIELD_BOAT));
            }
            final Double timeOnTimeFactor = (Double) object.get(CompetitorJsonConstants.FIELD_TIME_ON_TIME_FACTOR);
            final Double timeOnDistanceAllowanceInSecondsPerNauticalMile = (Double) object
                    .get(CompetitorJsonConstants.FIELD_TIME_ON_DISTANCE_ALLOWANCE_IN_SECONDS_PER_NAUTICAL_MILE);
            CompetitorWithBoat competitorWithBoat = new CompetitorWithBoatImpl(competitorId, name, shortName, displayColor, email,
                    flagImageURI, team, timeOnTimeFactor,
                    timeOnDistanceAllowanceInSecondsPerNauticalMile == null ? null : 
                        new MillisecondsDurationImpl((long) (timeOnDistanceAllowanceInSecondsPerNauticalMile*1000)), searchTag, boat);
            
            return competitorWithBoat;
        } catch (Exception e) {
            throw new JsonDeserializationException(e);
        }
    }
}
