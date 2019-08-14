package com.sap.sailing.domain.coursetemplate.impl;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.coursetemplate.ControlPointTemplate;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.NamedImpl;

public class ControlPointTemplateImpl extends NamedImpl implements ControlPointTemplate {
    private static final long serialVersionUID = 5150679935881533985L;
    private final Iterable<MarkTemplate> marks;
    
    public ControlPointTemplateImpl(String name, Iterable<MarkTemplate> marks) {
        super(name);
        final Set<MarkTemplate> theMarks = new HashSet<>();
        Util.addAll(marks, theMarks);
        this.marks = theMarks;
    }

    @Override
    public Iterable<MarkTemplate> getMarks() {
        return marks;
    }

}
