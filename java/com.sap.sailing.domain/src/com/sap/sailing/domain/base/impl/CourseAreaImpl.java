package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.common.impl.NamedImpl;

public class CourseAreaImpl extends NamedImpl implements CourseArea {
    private static final long serialVersionUID = 5912385360170509150L;

    public CourseAreaImpl(String name) {
        super(name);
    }
}
