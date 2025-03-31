package com.sap.sailing.domain.abstractlog.orc;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sailing.domain.abstractlog.BaseLogAnalyzer;

public class BaseORCCertificateAssignmentAnalyzer<LogT extends AbstractLog<EventT, VisitorT>, VisitorT, EventT extends AbstractLogEvent<VisitorT>,
AssignmentEventT extends ORCCertificateAssignmentEvent<VisitorT>>
extends BaseLogAnalyzer<LogT, EventT, VisitorT, Map<Serializable, AssignmentEventT>> {
    public BaseORCCertificateAssignmentAnalyzer(LogT log) {
        super(log);
    }

    @Override
    protected Map<Serializable, AssignmentEventT> performAnalysis() {
        final Map<Serializable, AssignmentEventT> result = new HashMap<>();
        for (final EventT o : getLog().getUnrevokedEvents()) {
            if (o instanceof ORCCertificateAssignmentEvent) {
                @SuppressWarnings("unchecked")
                AssignmentEventT event = (AssignmentEventT) o;
                final Serializable boatId = event.getBoatId();
                result.put(boatId, event);
            }
        }
        return result;
    }
}
