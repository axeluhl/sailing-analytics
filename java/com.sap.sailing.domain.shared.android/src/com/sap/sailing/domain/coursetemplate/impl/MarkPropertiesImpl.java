package com.sap.sailing.domain.coursetemplate.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkRole;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.domain.coursetemplate.Positioning;
import com.sap.sse.common.Color;
import com.sap.sse.common.TimePoint;

public class MarkPropertiesImpl extends CommonMarkPropertiesImpl implements MarkProperties {
    private static final long serialVersionUID = -5588202720707030502L;
    private Positioning positioningInformation;
    private Iterable<String> tags;
    private UUID id;

    private final Map<MarkTemplate, TimePoint> lastUsedTemplate = new HashMap<>();
    private final Map<MarkRole, TimePoint> lastUsedRole = new HashMap<>();

    public MarkPropertiesImpl(String name, String shortName, Color color, String shape, String pattern, MarkType type) {
        this(UUID.randomUUID(), name, shortName, color, shape, pattern, type);
    }

    public MarkPropertiesImpl(UUID id, String name, String shortName, Color color, String shape, String pattern,
            MarkType type) {
        super(name, shortName, color, shape, pattern, type);
        this.id = id;
    }

    public MarkPropertiesImpl(UUID id, final CommonMarkProperties commonMarkProperties) {
        super(commonMarkProperties);
        this.id = id;
    }
    
    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public void setShape(String shape) {
        this.shape = shape;
    }

    @Override
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public void setType(MarkType type) {
        this.type = type;
    }

    @Override
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    @Override
    public Positioning getPositioningInformation() {
        return positioningInformation;
    }

    @Override
    public void setPositioningInformation(Positioning positioningInformation) {
        this.positioningInformation = positioningInformation;
    }

    @Override
    public Iterable<String> getTags() {
        return this.tags;
    }

    public void setTags(Iterable<String> tags) {
        this.tags = tags;
    }

    @Override
    public Map<MarkTemplate, TimePoint> getLastUsedTemplate() {
        return lastUsedTemplate;
    }

    @Override
    public Map<MarkRole, TimePoint> getLastUsedRole() {
        return lastUsedRole;
    }

    public void setLastUsedRole(Map<MarkRole, TimePoint> lastUsedRole) {
        this.lastUsedRole.clear();
        this.lastUsedRole.putAll(lastUsedRole);
    }

    public void setLastUsedTemplate(Map<MarkTemplate, TimePoint> lastUsedTemplate) {
        this.lastUsedTemplate.clear();
        this.lastUsedTemplate.putAll(lastUsedTemplate);
    }

}
