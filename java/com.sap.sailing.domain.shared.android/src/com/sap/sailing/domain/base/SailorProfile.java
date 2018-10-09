package com.sap.sailing.domain.base;

import java.util.UUID;

import com.sap.sailing.domain.base.impl.SailorProfileImpl;
import com.sap.sse.common.IsManagedByCache;
import com.sap.sse.common.NamedWithID;

/**
 * used in com.sap.sailing.server.impl.preferences.model.SailorProfilePreference to store sailor profiles, implemented
 * in {@link SailorProfileImpl} which stores a name, an identifier and a set of competitors.
 */
public interface SailorProfile extends NamedWithID, IsManagedByCache<SharedDomainFactory> {
    String getName();

    UUID getUuid();

    Iterable<Competitor> getCompetitors();
}
