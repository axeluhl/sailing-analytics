package com.sap.sailing.domain.abstractlog.orc;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sailing.domain.abstractlog.BaseLogAnalyzer;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.common.orc.ORCCertificate;

public class BaseORCCertificateAssignmentFinder<LogT extends AbstractLog<EventT, VisitorT>, VisitorT, EventT extends AbstractLogEvent<VisitorT>>
extends BaseLogAnalyzer<LogT, EventT, VisitorT, Map<Boat, ORCCertificate>> {
    private static final Logger logger = Logger.getLogger(BaseORCCertificateAssignmentFinder.class.getName());
    private final Map<Serializable, Boat> boatsById;

    public BaseORCCertificateAssignmentFinder(LogT log, Map<Serializable, Boat> boatsById) {
        super(log);
        this.boatsById = boatsById;
    }

    @Override
    protected Map<Boat, ORCCertificate> performAnalysis() {
        final Map<Boat, ORCCertificate> result = new HashMap<>();
        for (final ORCCertificateAssignmentEvent<VisitorT> e : new BaseORCCertificateAssignmentAnalyzer<>(log).analyze().values()) {
            final Boat boat = boatsById.get(e.getBoatId());
            if (boat != null) {
                final ORCCertificate certificate = e.getCertificate();
                result.put(boat, certificate);
            } else {
                logger.warning(
                        "Unable to find boat with ID " + e.getBoatId() + " for which an ORC certificate with sail number "
                                + e.getCertificate() + " is defined. Certificate is ignored.");
            }
        }
        return result;
    }
}
