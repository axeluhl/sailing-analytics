package com.sap.sailing.domain.base.impl;

import java.io.Serializable;
import java.util.UUID;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SailorProfile;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sse.common.IsManagedByCache;
import com.sap.sse.common.impl.NamedImpl;

/** used in {@link com.sap.sailing.server.impl.preferences.model.SailorProfilePreference} to store sailor profiles */
public class SailorProfileImpl extends NamedImpl implements SailorProfile {
    private static final long serialVersionUID = -2714015903187029053L;

    private UUID uuid;
    private Iterable<Competitor> competitors;

    public SailorProfileImpl(String name, UUID uuid, Iterable<Competitor> competitors) {
        this(name);
        this.uuid = uuid;
        this.competitors = competitors;
    }

    public SailorProfileImpl(String name) {
        super(name);
    }

    @Override
    public Serializable getId() {
        return uuid;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public IsManagedByCache<SharedDomainFactory> resolve(SharedDomainFactory domainFactory) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterable<Competitor> getCompetitors() {
        return competitors;
    }

}
