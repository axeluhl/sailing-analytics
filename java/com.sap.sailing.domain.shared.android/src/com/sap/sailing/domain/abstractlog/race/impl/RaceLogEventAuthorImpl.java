package com.sap.sailing.domain.abstractlog.race.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLogEventAuthor;
import com.sap.sse.common.impl.NamedImpl;

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
        /** as '0' has a higher priority than '1' the compare method ranks a '0' priority higher than a '1' priority */
        Integer result = new Integer(getPriority()).compareTo(other.getPriority());
        return -result;
    }
    
    @Override
    public String toString() {
        return super.toString()+" with priority "+getPriority();
    }
}
