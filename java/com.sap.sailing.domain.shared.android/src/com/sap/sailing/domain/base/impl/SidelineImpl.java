package com.sap.sailing.domain.base.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.NamedImpl;

public class SidelineImpl extends NamedImpl implements Sideline {
    private static final long serialVersionUID = -8145721464971358691L;
    private final List<Mark> marks;
    private static int idCounter = 1;
    private final int id;

    public SidelineImpl(String name, Iterable<Mark> marks) {
        super(name);
        List<Mark> myMarks = new ArrayList<Mark>();
        Util.addAll(marks, myMarks);
        this.marks = Collections.unmodifiableList(myMarks);
        id = idCounter++;
    }
    
    @Override
    public String toString() {
        return getName();
    }

    @Override
    public Iterable<Mark> getMarks() {
        return marks;
    }

    @Override
    public Integer getId() {
        return id;
    }
}
