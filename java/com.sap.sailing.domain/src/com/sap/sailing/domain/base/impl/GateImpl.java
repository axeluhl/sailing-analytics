package com.sap.sailing.domain.base.impl;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sailing.domain.base.SingleMark;
import com.sap.sailing.domain.base.Gate;

public class GateImpl implements Gate {
    private static final long serialVersionUID = 2807354812133070574L;
    private final SingleMark left;
    private final SingleMark right;
    private final String name;
    
    public GateImpl(SingleMark left, SingleMark right, String name) {
        super();
        this.left = left;
        this.right = right;
        this.name = name;
    }

    @Override
    public SingleMark getLeft() {
        return left;
    }

    @Override
    public SingleMark getRight() {
        return right;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public Iterable<SingleMark> getMarks() {
        Collection<SingleMark> result = new ArrayList<SingleMark>(2);
        result.add(getLeft());
        result.add(getRight());
        return result;
    }
}
