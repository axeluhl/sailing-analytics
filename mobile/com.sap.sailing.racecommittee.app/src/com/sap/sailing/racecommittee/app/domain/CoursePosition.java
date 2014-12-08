package com.sap.sailing.racecommittee.app.domain;

import java.util.UUID;

import com.sap.sailing.domain.base.IsManagedBySharedDomainFactory;
import com.sap.sailing.domain.common.WithID;
import com.sap.sse.common.Named;

public interface CoursePosition extends Named, WithID, IsManagedBySharedDomainFactory {

   UUID getId();

}
