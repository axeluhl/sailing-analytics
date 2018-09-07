package com.sap.sailing.server.gateway.deserialization.impl;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.UUID;
import java.util.logging.Logger;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.CompetitorFactory;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.domain.base.impl.DynamicCompetitor;
import com.sap.sailing.domain.base.impl.DynamicTeam;
import com.sap.sailing.domain.common.tracking.impl.CompetitorJsonConstants;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorJsonSerializer;
import com.sap.sse.common.Color;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.RGBColor;
import com.sap.sse.util.impl.UUIDHelper;

public class CompetitorJsonDeserializer implements JsonDeserializer<DynamicCompetitor> {
    private final CompetitorFactory competitorWithBoatFactory;
    private final JsonDeserializer<DynamicTeam> teamJsonDeserializer;
    private final BoatJsonDeserializer boatJsonDeserializer;
    private static final Logger logger = Logger.getLogger(CompetitorJsonDeserializer.class.getName());

    public static CompetitorJsonDeserializer create(SharedDomainFactory baseDomainFactory) {
        return new CompetitorJsonDeserializer(baseDomainFactory, new TeamJsonDeserializer(new PersonJsonDeserializer(
                new NationalityJsonDeserializer(baseDomainFactory))), new BoatJsonDeserializer(baseDomainFactory, new BoatClassJsonDeserializer(baseDomainFactory)));
    }

    public CompetitorJsonDeserializer(CompetitorFactory competitorWithBoatFactory) {
        this(competitorWithBoatFactory, null, /* boatDeserializer */ null);
    }

    public CompetitorJsonDeserializer(CompetitorFactory competitorWithBoatFactory, JsonDeserializer<DynamicTeam> teamJsonDeserializer, BoatJsonDeserializer boatDeserializer) {
        this.competitorWithBoatFactory = competitorWithBoatFactory;
        this.teamJsonDeserializer = teamJsonDeserializer;
        this.boatJsonDeserializer = boatDeserializer;
    }

    @Override
    public DynamicCompetitor deserialize(JSONObject object) throws JsonDeserializationException {
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
            if (teamJsonDeserializer != null && object.get(CompetitorJsonConstants.FIELD_TEAM) != null) {
                team = teamJsonDeserializer.deserialize(Helpers.getNestedObjectSafe(object,
                        CompetitorJsonConstants.FIELD_TEAM));
            }
            final DynamicBoat boat = getBoat(object, /* default ID */ competitorId);
            final Double timeOnTimeFactor = ((Number) object.get(CompetitorJsonConstants.FIELD_TIME_ON_TIME_FACTOR)).doubleValue();
            final Double timeOnDistanceAllowanceInSecondsPerNauticalMile = ((Number) object
                    .get(CompetitorJsonConstants.FIELD_TIME_ON_DISTANCE_ALLOWANCE_IN_SECONDS_PER_NAUTICAL_MILE)).doubleValue();
            final DynamicCompetitor result;
            if (boat == null) {
                result = competitorWithBoatFactory.getOrCreateCompetitor(competitorId, name, shortName, displayColor, email,
                        flagImageURI, team, timeOnTimeFactor,
                        timeOnDistanceAllowanceInSecondsPerNauticalMile == null ? null : 
                            new MillisecondsDurationImpl((long) (timeOnDistanceAllowanceInSecondsPerNauticalMile*1000)), searchTag);
            } else {
                result = competitorWithBoatFactory.getOrCreateCompetitorWithBoat(competitorId, name, shortName, displayColor, email,
                        flagImageURI, team, timeOnTimeFactor,
                        timeOnDistanceAllowanceInSecondsPerNauticalMile == null ? null : 
                            new MillisecondsDurationImpl((long) (timeOnDistanceAllowanceInSecondsPerNauticalMile*1000)), searchTag, boat);
            }
            return result;
        } catch (Exception e) {
            throw new JsonDeserializationException(e);
        }
    }

    /**
     * Looks for the {@link CompetitorJsonConstants#FIELD_BOAT} field and if present and there is a
     * {@link #boatJsonDeserializer} set, the boat is deserialized from that field and returned; otherwise, {@code null}
     * is returned.
     * 
     * @param defaultId
     *            in case no boat ID can be read, e.g., from older competitor/boat representations, a default boat ID
     *            can be passed, such as the boat owning competitor's ID
     */
    protected DynamicBoat getBoat(JSONObject object, Serializable defaultId) throws ClassNotFoundException, NoSuchMethodException,
            InstantiationException, IllegalAccessException, InvocationTargetException, JsonDeserializationException {
        final DynamicBoat boat;
        if (boatJsonDeserializer != null && object.get(CompetitorJsonConstants.FIELD_BOAT) != null) {
            boat = boatJsonDeserializer.deserialize(Helpers.getNestedObjectSafe(object, CompetitorJsonConstants.FIELD_BOAT), defaultId);
        } else {
            boat = null;
        }
        return boat;
    }
}
