package com.sap.sailing.server.gateway.deserialization.impl;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.UUID;
import java.util.logging.Logger;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatFactory;
import com.sap.sailing.domain.base.CompetitorFactory;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.domain.base.impl.DynamicTeam;
import com.sap.sailing.domain.common.tracking.impl.CompetitorJsonConstants;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorJsonSerializer;
import com.sap.sse.common.Color;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.RGBColor;

public class CompetitorWithBoatRefJsonDeserializer implements JsonDeserializer<CompetitorWithBoat> {
    protected final CompetitorFactory competitorFactory;
    protected final BoatFactory boatFactory;
    protected final JsonDeserializer<DynamicTeam> teamJsonDeserializer;
    private static final Logger logger = Logger.getLogger(CompetitorWithBoatRefJsonDeserializer.class.getName());

    public static CompetitorWithBoatRefJsonDeserializer create(SharedDomainFactory baseDomainFactory) {
        return new CompetitorWithBoatRefJsonDeserializer(baseDomainFactory, baseDomainFactory, new TeamJsonDeserializer(new PersonJsonDeserializer(
                new NationalityJsonDeserializer(baseDomainFactory))));
    }

    public CompetitorWithBoatRefJsonDeserializer(CompetitorFactory competitorFactory, BoatFactory boatFactory) {
        this(competitorFactory, boatFactory, null);
    }

    public CompetitorWithBoatRefJsonDeserializer(CompetitorFactory competitorFactory, BoatFactory boatFactory, JsonDeserializer<DynamicTeam> teamJsonDeserializer) {
        this.competitorFactory = competitorFactory;
        this.boatFactory = boatFactory;
        this.teamJsonDeserializer = teamJsonDeserializer;
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
                competitorId = Helpers.tryUuidConversion(competitorId);
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
            if (teamJsonDeserializer != null && object.get(CompetitorJsonConstants.FIELD_TEAM) != null) {
                team = teamJsonDeserializer.deserialize(Helpers.getNestedObjectSafe(object,
                        CompetitorJsonConstants.FIELD_TEAM));
            }
            final Double timeOnTimeFactor = (Double) object.get(CompetitorJsonConstants.FIELD_TIME_ON_TIME_FACTOR);
            final Double timeOnDistanceAllowanceInSecondsPerNauticalMile = (Double) object
                    .get(CompetitorJsonConstants.FIELD_TIME_ON_DISTANCE_ALLOWANCE_IN_SECONDS_PER_NAUTICAL_MILE);

            // if we find a boat identifier we try to find the boat with the boatfactory
            Serializable boatId = (Serializable) object.get(CompetitorJsonConstants.FIELD_BOAT_ID);
            Class<?> boatIdClass = Class.forName((String) object.get(CompetitorJsonConstants.FIELD_BOAT_ID_TYPE));
            if (Number.class.isAssignableFrom(boatIdClass)) {
                Constructor<?> constructorFromString = boatIdClass.getConstructor(String.class);
                boatId = (Serializable) constructorFromString.newInstance(boatId.toString());
            } else if (UUID.class.isAssignableFrom(idClass)) {
                boatId = Helpers.tryUuidConversion(boatId);
            }

            final Boat existingBoat = boatId != null ? boatFactory.getExistingBoatById(boatId) : null;

            CompetitorWithBoat competitorWithBoat = competitorFactory.getOrCreateCompetitorWithBoat(competitorId, name, shortName, displayColor, email,
                    flagImageURI, team, timeOnTimeFactor,
                    timeOnDistanceAllowanceInSecondsPerNauticalMile == null ? null : 
                        new MillisecondsDurationImpl((long) (timeOnDistanceAllowanceInSecondsPerNauticalMile*1000)), searchTag, (DynamicBoat) existingBoat);
            
            return competitorWithBoat;
        } catch (Exception e) {
            throw new JsonDeserializationException(e);
        }
    }
}
