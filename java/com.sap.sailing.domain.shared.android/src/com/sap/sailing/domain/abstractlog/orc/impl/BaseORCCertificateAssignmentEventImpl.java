package com.sap.sailing.domain.abstractlog.orc.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.AbstractLogEventImpl;
import com.sap.sailing.domain.abstractlog.orc.ORCCertificateAssignmentEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sse.common.TimePoint;

public abstract class BaseORCCertificateAssignmentEventImpl<VisitorT> extends AbstractLogEventImpl<VisitorT> implements ORCCertificateAssignmentEvent<VisitorT> {
    private static final long serialVersionUID = -3186019736784868848L;

    private ORCCertificate certificate;
    private Serializable competitorID;

    public BaseORCCertificateAssignmentEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable pId, ORCCertificate certificate, Competitor competitor) {
        super(createdAt, logicalTimePoint, author, pId);
        this.certificate = certificate;
        this.competitorID = competitor.getId();
    }

    public BaseORCCertificateAssignmentEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable pId, ORCCertificate certificate, Serializable competitorID) {
        super(createdAt, logicalTimePoint, author, pId);
        this.certificate = certificate;
        this.competitorID = competitorID;
    }

    @Override
    public ORCCertificate getCertificate() {
        return certificate;
    }

    @Override
    public Serializable getCompetitorID() {
        return competitorID;
    }
}
