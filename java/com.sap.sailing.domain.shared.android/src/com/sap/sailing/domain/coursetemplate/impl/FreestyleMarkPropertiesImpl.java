package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sailing.domain.coursetemplate.FreestyleMarkProperties;
import com.sap.sse.common.Color;

public class FreestyleMarkPropertiesImpl extends CommonMarkPropertiesImpl implements FreestyleMarkProperties {
    private static final long serialVersionUID = -925045903410189747L;
    private Iterable<String> tags;

    public FreestyleMarkPropertiesImpl(String name, String shortName, Color color, String shape, String pattern, MarkType type, Iterable<String> tags) {
        super(name, shortName, color, shape, pattern, type);
        this.tags = tags;
    }

    public FreestyleMarkPropertiesImpl(final CommonMarkProperties commonMarkProperties, Iterable<String> tags) {
        this(commonMarkProperties.getName(), commonMarkProperties.getShortName(), commonMarkProperties.getColor(),
                commonMarkProperties.getShape(), commonMarkProperties.getPattern(), commonMarkProperties.getType(), tags);
    }

    @Override
    public Iterable<String> getTags() {
        return this.tags;
    }
    
    @Override
    public void setTags(Iterable<String> tags) {
        this.tags = tags;
    }
}
