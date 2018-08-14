package com.sap.sailing.domain.base;

import java.util.UUID;

import com.sap.sse.common.IsManagedByCache;
import com.sap.sse.common.NamedWithID;

public interface SailorProfile extends NamedWithID, IsManagedByCache<SharedDomainFactory> {
    String getName();

    UUID getUuid();

    Iterable<Competitor> getCompetitors();
}
