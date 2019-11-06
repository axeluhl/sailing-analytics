package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sse.common.Color;
import com.sap.sse.common.impl.NamedImpl;

public class CommonMarkPropertiesImpl extends NamedImpl implements CommonMarkProperties {
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

    public CommonMarkPropertiesImpl(final CommonMarkProperties commonMarkProperties) {
        this(commonMarkProperties.getName(), commonMarkProperties.getShortName(), commonMarkProperties.getColor(),
                commonMarkProperties.getShape(), commonMarkProperties.getPattern(), commonMarkProperties.getType());
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
