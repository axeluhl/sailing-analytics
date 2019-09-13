package com.sap.sailing.domain.abstractlog.orc.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.orc.RegattaLogORCCertificateAssignmentEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sse.common.TimePoint;

public class RegattaLogORCCertificateAssignmentEventImpl extends BaseORCCertificateAssignmentEventImpl<RegattaLogEventVisitor> implements RegattaLogORCCertificateAssignmentEvent {
    private static final long serialVersionUID = -3186019736784868848L;
    
    public RegattaLogORCCertificateAssignmentEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable pId, ORCCertificate certificate, Boat boat) {
        super(createdAt, logicalTimePoint, author, pId, certificate, boat);
    }

    public RegattaLogORCCertificateAssignmentEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable pId, ORCCertificate certificate, Serializable boatId) {
        super(createdAt, logicalTimePoint, author, pId, certificate, boatId);
    }
    
    @Override
    public void accept(RegattaLogEventVisitor visitor) {
        visitor.visit(this);
    }
}
