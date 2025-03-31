package com.sap.sailing.selenium.api.coursetemplate;

import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.JsonWrapper;

public class MarkRole extends JsonWrapper implements Comparable<MarkRole> {

    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_SHORT_NAME = "short_name";
    private static final String FIELD_ASSOCIATED_MARK_TEMPLATE_ID = "associatedMarkTemplateId";

    public MarkRole(JSONObject json) {
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
        return get(FIELD_SHORT_NAME);
    }
    
    public UUID getAssociatedMarkTemplateId() {
        final String uuid = get(FIELD_ASSOCIATED_MARK_TEMPLATE_ID);
        return uuid == null ? null : UUID.fromString(uuid);
    }

    @Override
    public int compareTo(MarkRole other) {
        if (other == null) {
            return -1;
        }
        return this.getId().compareTo(other.getId());
    }

}
