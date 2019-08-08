package com.sap.sailing.domain.coursetemplate;

import java.util.UUID;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.coursetemplate.impl.MarkPropertiesImpl;
import com.sap.sse.common.Color;

public class MarkPropertiesBuilder {

    private final UUID id;
    private final String name;
    private final String shortName;
    private final Color color;
    private final String shape;
    private final String pattern;
    private final MarkType type;
    private Iterable<String> tags;
    private DeviceIdentifier deviceId;
    private Position position;

    public MarkPropertiesBuilder(UUID id, String name, String shortName, Color color, String shape, String pattern,
            MarkType type) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.color = color;
        this.shape = shape;
        this.pattern = pattern;
        this.type = type;
    }

    public MarkPropertiesBuilder withTags(Iterable<String> tags) {
        this.tags = tags;
        return this;
    }

    public MarkPropertiesBuilder withDeviceId(DeviceIdentifier deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public MarkPropertiesBuilder withFixedPosition(Position position) {
        this.position = position;
        return this;
    }

    public MarkProperties build() {
        MarkPropertiesImpl impl = new MarkPropertiesImpl(id, name, shortName, color, shape, pattern, type);
        if (tags != null) {
            impl.setTags(tags);
        }

        if (deviceId != null) {
            impl.setTrackingDeviceIdentifier(deviceId);
        }

        if (position != null) {
            impl.setFixedPosition(position);
        }
        return impl;
    }

}
