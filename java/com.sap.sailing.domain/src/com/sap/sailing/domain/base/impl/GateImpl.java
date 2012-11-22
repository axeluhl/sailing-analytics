package com.sap.sailing.domain.base.impl;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Gate;

public class GateImpl implements Gate {
    private static final long serialVersionUID = 2807354812133070574L;
    private final Mark left;
    private final Mark right;
    private final String name;
    
    public GateImpl(Mark left, Mark right, String name) {
        super();
        this.left = left;
        this.right = right;
        this.name = name;
    }

    @Override
    public Mark getLeft() {
        return left;
    }

    @Override
    public Mark getRight() {
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
    public Iterable<Mark> getMarks() {
        Collection<Mark> result = new ArrayList<Mark>(2);
        result.add(getLeft());
        result.add(getRight());
        return result;
    }
}
