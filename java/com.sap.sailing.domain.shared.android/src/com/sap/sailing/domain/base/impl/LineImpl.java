package com.sap.sailing.domain.base.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import com.sap.sailing.domain.base.Line;
import com.sap.sailing.domain.base.Mark;

public class LineImpl implements Line {


    private static final long serialVersionUID = -2771299397766434232L;
    private final Mark left;
    private final Mark right;
    private final String name;
    private final Serializable id;
    
    /**
     * @param name also used as ID for the mark; if you have a better ID, use {@link GateImpl(Serializable, Mark, Mark, String)} instead.
     */
    public LineImpl(Mark left, Mark right, String name) {
        this(/* ID */ name, left, right, name);
    }
    public LineImpl(Serializable id, Mark left, Mark right, String name) {
        this.id = id;
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

    @Override
    public Serializable getId() {
        return id;
    }

    @Override
    public boolean isLine() {
       
        return true;
    }

    @Override
    public boolean hasTwoMarks() {
        // TODO Auto-generated method stub
        return true;
    }

}
