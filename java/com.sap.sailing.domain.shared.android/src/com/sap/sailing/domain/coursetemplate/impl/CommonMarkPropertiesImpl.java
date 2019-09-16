package com.sap.sailing.domain.coursetemplate.impl;

import java.util.UUID;

import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sse.common.Color;
import com.sap.sse.common.impl.NamedWithUUIDImpl;

public abstract class CommonMarkPropertiesImpl extends NamedWithUUIDImpl implements CommonMarkProperties {
    private static final long serialVersionUID = 5599464471737104833L;
    protected String shortName;
    protected Color color;
    protected String shape;
    protected String pattern;
    protected MarkType type;
    
    public CommonMarkPropertiesImpl(String name, String shortName, Color color, String shape, String pattern, MarkType type) {
        super(name);
        this.shortName = shortName;
        this.color = color;
        this.shape = shape;
        this.pattern = pattern;
        this.type = type;
    }
    
    public CommonMarkPropertiesImpl(UUID id, String name, String shortName, Color color, String shape, String pattern, MarkType type) {
        super(name, id);
        this.shortName = shortName;
        this.color = color;
        this.shape = shape;
        this.pattern = pattern;
        this.type = type;
    }

    public CommonMarkPropertiesImpl(UUID id, final CommonMarkProperties commonMarkProperties) {
        super(commonMarkProperties.getName(), id);
        this.shortName = commonMarkProperties.getShortName();
        this.color = commonMarkProperties.getColor();
        this.shape = commonMarkProperties.getShape();
        this.pattern = commonMarkProperties.getPattern();
        this.type = commonMarkProperties.getType();
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public String getShape() {
        return shape;
    }

    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public MarkType getType() {
        return type;
    }

    @Override
    public String getShortName() {
        return shortName;
    }
}
