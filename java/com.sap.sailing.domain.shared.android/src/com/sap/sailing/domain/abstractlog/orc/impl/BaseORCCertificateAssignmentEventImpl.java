package com.sap.sailing.domain.abstractlog.orc.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.AbstractLogEventImpl;
import com.sap.sailing.domain.abstractlog.orc.ORCCertificateAssignmentEvent;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sse.common.TimePoint;

public abstract class BaseORCCertificateAssignmentEventImpl<VisitorT> extends AbstractLogEventImpl<VisitorT> implements ORCCertificateAssignmentEvent<VisitorT> {
    private static final long serialVersionUID = -3186019736784868848L;

    private ORCCertificate certificate;
    private Serializable boatId;

    public BaseORCCertificateAssignmentEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable pId, ORCCertificate certificate, Boat boat) {
        super(createdAt, logicalTimePoint, author, pId);
        this.certificate = certificate;
        this.boatId = boat.getId();
    }

    public BaseORCCertificateAssignmentEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable pId, ORCCertificate certificate, Serializable boatId) {
        super(createdAt, logicalTimePoint, author, pId);
        this.certificate = certificate;
        this.boatId = boatId;
    }

    @Override
    public ORCCertificate getCertificate() {
        return certificate;
    }

    @Override
    public Serializable getBoatId() {
        return boatId;
    }

    @Override
    public String getShortInfo() {
        return "Certificate "+getCertificate()+" for boat with ID "+getBoatId();
    }
}
