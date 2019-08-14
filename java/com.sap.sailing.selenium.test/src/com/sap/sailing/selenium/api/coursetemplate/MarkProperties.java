package com.sap.sailing.selenium.api.coursetemplate;

import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.selenium.api.core.JsonWrapper;

public class MarkProperties extends JsonWrapper {

    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_SHORTNAME = "shortName";
    private static final String FIELD_HAS_DEVICEUUID = "hasDeviceUuid";
    private static final String FIELD_COLOR = "color";
    private static final String FIELD_SHAPE = "shape";
    private static final String FIELD_PATTERN = "pattern";
    private static final String FIELD_MARKTYPE = "markType";

    public MarkProperties(JSONObject json) {
        super(json);
    }

    public UUID getId() {
        final String uuid = get(FIELD_ID);
        return uuid != null ? UUID.fromString(uuid) : null;
    }

    public String getName() {
        return get(FIELD_NAME);
    }

    public String getShortName() {
        return get(FIELD_SHORTNAME);
    }

    public String getColor() {
        return get(FIELD_COLOR);
    }

    public String getShape() {
        return get(FIELD_SHAPE);
    }

    public String getPattern() {
        return get(FIELD_PATTERN);
    }

    public MarkType getMarkType() {
        String markType = get(FIELD_MARKTYPE);
        return markType != null ? MarkType.valueOf(markType) : null;
    }

    public Boolean hasDevice() {
        return get(FIELD_HAS_DEVICEUUID);
    }
}
