package com.sap.sailing.domain.abstractlog.orc.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.AbstractLogEventImpl;
import com.sap.sailing.domain.abstractlog.orc.ORCCertificateAssignmentEvent;
import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sse.common.TimePoint;

public class ORCCertificateAssignmentEventImpl extends AbstractLogEventImpl<ORCCertificate> implements ORCCertificateAssignmentEvent{

    public ORCCertificateAssignmentEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable pId) {
        super(createdAt, logicalTimePoint, author, pId);
    }

    private static final long serialVersionUID = -3186019736784868848L;

    @Override
    public void accept(ORCCertificate visitor) {
        // TODO Auto-generated method stub
        
    }

}
