package com.sap.sailing.server.gateway.serialization.coursedata.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class MarkJsonSerializer extends BaseControlPointJsonSerializer implements JsonSerializer<ControlPoint> {
    public static final String VALUE_CLASS = Mark.class.getSimpleName();

    public static final String FIELD_ID = "id";
    public static final String FIELD_COLOR = "color";
    public static final String FIELD_PATTERN = "pattern";
    public static final String FIELD_SHAPE = "shape";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_SHORT_NAME = "shortName";
    public static final String FIELD_ORIGINATING_MARK_TEMPLATE_ID = "originatingMarkTemplateId";
    public static final String FIELD_ORIGINATING_MARK_PROPERTIES_ID = "originatingMarkPropertiesId";

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }

    @Override
    public JSONObject serialize(ControlPoint object) {
        Mark mark = (Mark) object;
        JSONObject result = super.serialize(mark);
        result.put(FIELD_ID, mark.getId().toString());
        if (mark.getColor() != null) {
            result.put(FIELD_COLOR, mark.getColor().getAsHtml());
        }
        if (mark.getPattern() != null) {
            result.put(FIELD_PATTERN, mark.getPattern());
        }
        if (mark.getShape() != null) {
            result.put(FIELD_SHAPE, mark.getShape());
        }
        if (mark.getType() != null) {
            result.put(FIELD_TYPE, mark.getType().name());
        }
        if (mark.getShortName() != null) {
            result.put(FIELD_SHORT_NAME, mark.getShortName());
        }
        if (mark.getOriginatingMarkTemplateIdOrNull() != null) {
            result.put(FIELD_ORIGINATING_MARK_TEMPLATE_ID, mark.getOriginatingMarkTemplateIdOrNull().toString());
        }
        if (mark.getOriginatingMarkPropertiesIdOrNull() != null) {
            result.put(FIELD_ORIGINATING_MARK_PROPERTIES_ID, mark.getOriginatingMarkPropertiesIdOrNull().toString());
        }
        return result;
    }

}
