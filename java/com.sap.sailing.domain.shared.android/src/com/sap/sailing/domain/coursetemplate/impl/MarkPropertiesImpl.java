package com.sap.sailing.domain.coursetemplate.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sse.common.Color;
import com.sap.sse.common.TimePoint;

public class MarkPropertiesImpl extends CommonMarkPropertiesImpl implements MarkProperties {
    private static final long serialVersionUID = -5588202720707030502L;
    private DeviceIdentifier trackingDeviceIdentifier;
    private Position fixedPosition;
    private Iterable<String> tags;

    private final Map<MarkTemplate, TimePoint> lastUsedTemplate = new HashMap<>();
    private final Map<String, TimePoint> lastUsedRole = new HashMap<>();

    public MarkPropertiesImpl(String name, String shortName, Color color, String shape, String pattern, MarkType type) {
        super(name, shortName, color, shape, pattern, type);
    }

    public MarkPropertiesImpl(UUID id, String name, String shortName, Color color, String shape, String pattern,
            MarkType type) {
        super(id, name, shortName, color, shape, pattern, type);
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
    public DeviceIdentifier getTrackingDeviceIdentifier() {
        return trackingDeviceIdentifier;
    }

    @Override
    public void setTrackingDeviceIdentifier(DeviceIdentifier trackingDeviceIdentifier) {
        this.trackingDeviceIdentifier = trackingDeviceIdentifier;
    }

    @Override
    public Position getFixedPosition() {
        return fixedPosition;
    }

    @Override
    public void setFixedPosition(Position fixedPosition) {
        this.fixedPosition = fixedPosition;
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
    public Map<String, TimePoint> getLastUsedRole() {
        return lastUsedRole;
    }

    public void setLastUsedRole(Map<String, TimePoint> lastUsedRole) {
        this.lastUsedRole.clear();
        this.lastUsedRole.putAll(lastUsedRole);
    }

    public void setLastUsedTemplate(Map<MarkTemplate, TimePoint> lastUsedTemplate) {
        this.lastUsedTemplate.clear();
        this.lastUsedTemplate.putAll(lastUsedTemplate);
    }

}
