package com.sap.sailing.domain.abstractlog.orc.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.AbstractLogEventImpl;
import com.sap.sailing.domain.abstractlog.orc.ORCCertificateAssignmentEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorAndBoat;
import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sse.common.TimePoint;

public class ORCCertificateAssignmentEventImpl extends AbstractLogEventImpl<ORCCertificate> implements ORCCertificateAssignmentEvent{

    private ORCCertificate certificate;
    private Competitor competitor;
    
    public ORCCertificateAssignmentEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable pId, ORCCertificate certificate, Competitor competitor) {
        super(createdAt, logicalTimePoint, author, pId);
        this.certificate = certificate;
        this.competitor = competitor;
    }

    private static final long serialVersionUID = -3186019736784868848L;

    @Override
    public void accept(RegattaLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ORCCertificate getCertificate() {
        return certificate;
    }

    @Override
    public void accept(ORCCertificate visitor) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Competitor getCompetitor() {
        // TODO Auto-generated method stub
        return null;
    }

}
