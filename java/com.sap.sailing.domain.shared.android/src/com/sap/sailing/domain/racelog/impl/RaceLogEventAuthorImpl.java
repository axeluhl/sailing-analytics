package com.sap.sailing.domain.racelog.impl;

import com.sap.sailing.domain.common.impl.NamedImpl;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;

public class RaceLogEventAuthorImpl extends NamedImpl implements RaceLogEventAuthor {
    private static final long serialVersionUID = -5602802911563685812L;
    private final int priority;
    
    public static RaceLogEventAuthor createCompatibilityAuthor() {
        return new RaceLogEventAuthorImpl(NAME_COMPATIBILITY, PRIORITY_COMPATIBILITY);
    }
    
    public RaceLogEventAuthorImpl(String name, int priority) {
        super(name);
        this.priority = priority;
    }
    
    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public int compareTo(RaceLogEventAuthor other) {
        return new Integer(other.getPriority()).compareTo(getPriority());
    }
    
    @Override
    public String toString() {
        return super.toString()+" with priority "+getPriority();
    }
}
