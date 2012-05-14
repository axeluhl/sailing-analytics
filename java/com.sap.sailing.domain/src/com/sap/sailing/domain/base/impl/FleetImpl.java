package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.common.impl.NamedImpl;

public class FleetImpl extends NamedImpl implements Fleet {
    private static final long serialVersionUID = 7560417723293278246L;

    public FleetImpl(String name) {
        super(name);
    }
}
