package com.sap.sailing.selenium.api.coursetemplate;

import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.JsonWrapper;

public class MarkConfiguration extends JsonWrapper {

    private static final String FIELD_ID = "id";
    private static final String FIELD_MARK_TEMPLATE_ID = "markTemplateId";
    private static final String FIELD_MARK_PROPERTIES_ID = "markPropertiesId";
    private static final String FIELD_ASSOCIATED_ROLE = "associatedRole";
    private static final String FIELD_FREESTYLE_PROPERTIES = "freestyleProperties";
    private static final String FIELD_MARK_ID = "markId";
    private static final String FIELD_POSITIONING = "positioning";

    public MarkConfiguration(final JSONObject json) {
        super(json);
    }

    public static MarkConfiguration createFreestyle(final UUID markTemplateId, final UUID markPropertiesId,
            final String associatedRole, final String name, final String shortName, final String color,
            final String shape, final String pattern, final String markType) {
        MarkConfiguration markConfiguration = new MarkConfiguration(new JSONObject());
        markConfiguration.getJson().put(FIELD_ID, UUID.randomUUID().toString());
        markConfiguration.getJson().put(FIELD_MARK_TEMPLATE_ID, markTemplateId.toString());
        markConfiguration.getJson().put(FIELD_MARK_PROPERTIES_ID, markPropertiesId.toString());
        markConfiguration.getJson().put(FIELD_ASSOCIATED_ROLE, associatedRole.toString());
        markConfiguration.getJson().put(FIELD_FREESTYLE_PROPERTIES, new MarkAppearance(name, shortName, color, shape, pattern, markType).getJson());
        return markConfiguration;
    }

    public static MarkConfiguration createMarkPropertiesBased(final UUID markPropertiesId,
            final String associatedRole) {
        MarkConfiguration markConfiguration = new MarkConfiguration(new JSONObject());
        markConfiguration.getJson().put(FIELD_ID, UUID.randomUUID().toString());
        markConfiguration.getJson().put(FIELD_MARK_PROPERTIES_ID, markPropertiesId.toString());
        markConfiguration.getJson().put(FIELD_ASSOCIATED_ROLE, associatedRole.toString());
        return markConfiguration;
    }

    public static MarkConfiguration createMarkBased(final UUID markId, final String associatedRole) {
        MarkConfiguration markConfiguration = new MarkConfiguration(new JSONObject());
        markConfiguration.getJson().put(FIELD_ID, UUID.randomUUID().toString());
        markConfiguration.getJson().put(FIELD_MARK_ID, markId.toString());
        markConfiguration.getJson().put(FIELD_ASSOCIATED_ROLE, associatedRole.toString());
        return markConfiguration;
    }

    public static MarkConfiguration createMarkTemplateBased(final UUID markTemplateId, final String associatedRole) {
        MarkConfiguration markConfiguration = new MarkConfiguration(new JSONObject());
        markConfiguration.getJson().put(FIELD_ID, UUID.randomUUID().toString());
        markConfiguration.getJson().put(FIELD_MARK_TEMPLATE_ID, markTemplateId.toString());
        markConfiguration.getJson().put(FIELD_ASSOCIATED_ROLE, associatedRole.toString());
        return markConfiguration;
    }

    public String getId() {
        return this.get(FIELD_ID);
    }
    
    public void setTrackingDeviceId(UUID deviceId) {
        getJson().put(FIELD_POSITIONING, new Positioning(deviceId).getJson());
    }
    
    public void setFixedPosition(double latDeg, double lngDeg) {
        getJson().put(FIELD_POSITIONING, new Positioning(latDeg, lngDeg).getJson());
    }
    
    public Positioning getPositioning() {
        final JSONObject positioningObject = (JSONObject) getJson().get(FIELD_POSITIONING);
        return positioningObject == null ? null : new Positioning(positioningObject);
    }
}
