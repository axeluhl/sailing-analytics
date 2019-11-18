package com.sap.sailing.domain.coursetemplate.impl;

import java.util.UUID;

import com.sap.sailing.domain.coursetemplate.MarkRole;
import com.sap.sse.common.impl.NamedWithUUIDImpl;

public class MarkRoleImpl extends NamedWithUUIDImpl implements MarkRole {

    private static final long serialVersionUID = 3661478108460413196L;

    public MarkRoleImpl(UUID id, String name) {
        super(name, id);
    }
}
