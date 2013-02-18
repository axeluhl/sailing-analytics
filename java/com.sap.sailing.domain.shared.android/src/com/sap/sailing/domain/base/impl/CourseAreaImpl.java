package com.sap.sailing.domain.base.impl;

import java.io.Serializable;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.common.impl.NamedImpl;

public class CourseAreaImpl extends NamedImpl implements CourseArea {
    private static final long serialVersionUID = 5912385360170509150L;
    
    private final Serializable id;

    public CourseAreaImpl(String name, Serializable id) {
        super(name);
        this.id = id;
    }

	@Override
	public Serializable getId() {
		return id;
	}
}
