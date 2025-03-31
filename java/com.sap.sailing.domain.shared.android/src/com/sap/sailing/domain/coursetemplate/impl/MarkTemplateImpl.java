package com.sap.sailing.domain.coursetemplate.impl;

import java.util.UUID;

import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sse.common.Color;
import com.sap.sse.common.impl.NamedWithUUIDImpl;

public class MarkTemplateImpl extends NamedWithUUIDImpl implements MarkTemplate {
    private static final long serialVersionUID = 620307743421809258L;
    private final String shortName;
    private final Color color;
    private final String shape;
    private final String pattern;
    private final MarkType type;
    
    public MarkTemplateImpl(String name, String shortName, Color color, String shape, String pattern, MarkType type) {
        super(name);
        this.shortName = shortName;
        this.color = color;
        this.shape = shape;
        this.pattern = pattern;
        this.type = type;
    }
    
    public MarkTemplateImpl(UUID id, String name, String shortName, Color color, String shape, String pattern, MarkType type) {
        super(name, id);
        this.shortName = shortName;
        this.color = color;
        this.shape = shape;
        this.pattern = pattern;
        this.type = type;
    }
    
    public MarkTemplateImpl(UUID id, CommonMarkProperties commonMarkProperties) {
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
