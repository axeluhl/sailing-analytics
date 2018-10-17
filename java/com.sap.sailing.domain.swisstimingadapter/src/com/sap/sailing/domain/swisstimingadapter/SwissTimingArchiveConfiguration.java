package com.sap.sailing.domain.swisstimingadapter;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;

public interface SwissTimingArchiveConfiguration extends WithQualifiedObjectIdentifier {
    String getJsonUrl();

    @Override
    default String getName() {
        return getJsonUrl();
    }

    @Override
    default QualifiedObjectIdentifier getIdentifier() {
        return getType().getQualifiedObjectIdentifier(getJsonUrl());
    }

    @Override
    default HasPermissions getType() {
        return SecuredDomainType.SWISS_TIMING_ARCHIVE_ACCOUNT;
    }
}
