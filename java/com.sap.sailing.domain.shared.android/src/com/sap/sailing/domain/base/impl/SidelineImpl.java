package com.sap.sailing.domain.base.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.common.impl.NamedImpl;
import com.sap.sailing.domain.common.impl.Util;

public class SidelineImpl extends NamedImpl implements Sideline {
    private static final long serialVersionUID = -8145721464971358691L;
    private final List<ControlPoint> controlPoints;
    private static int idCounter = 1;
    private final int id;

    public SidelineImpl(String name, Iterable<ControlPoint> controlPoints) {
        super(name);
        this.controlPoints = new ArrayList<ControlPoint>();
        Util.addAll(controlPoints, this.controlPoints);
        id = idCounter++;
    }
    
    @Override
    public Iterable<ControlPoint> getControlPoints() {
        return controlPoints;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public Iterable<Mark> getMarks() {
        List<Mark> result = new ArrayList<Mark>();
        for(ControlPoint cp: controlPoints) {
            for(Mark mark: cp.getMarks()) {
                result.add(mark);
            }
        }
        return result;
    }

    @Override
    public Integer getId() {
        return id;
    }
}
