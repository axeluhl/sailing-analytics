package com.sap.sailing.domain.abstractlog.impl;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sse.common.impl.NamedImpl;

public class LogEventAuthorImpl extends NamedImpl implements AbstractLogEventAuthor {
    private static final long serialVersionUID = -5602802911563685812L;
    private final int priority;
    
    public static AbstractLogEventAuthor createCompatibilityAuthor() {
        return new LogEventAuthorImpl(NAME_COMPATIBILITY, PRIORITY_COMPATIBILITY);
    }
    
    public LogEventAuthorImpl(String name, int priority) {
        super(name);
        this.priority = priority;
    }
    
    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public int compareTo(AbstractLogEventAuthor other) {
        /** as '0' has a higher priority than '1' the compare method ranks a '0' priority higher than a '1' priority */
        Integer result = Integer.compare(getPriority(), other.getPriority());
        return -result;
    }
    
    @Override
    public String toString() {
        return super.toString()+" with priority "+getPriority();
    }
}
