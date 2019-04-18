package com.sap.sailing.domain.swisstimingadapter;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;

public interface SwissTimingArchiveConfiguration extends WithQualifiedObjectIdentifier {

    String getJsonURL();

    String getCreatorName();

    @Override
    default String getName() {
        return getJsonURL();
    }

    @Override
    default QualifiedObjectIdentifier getIdentifier() {
        return getType().getQualifiedObjectIdentifier(getTypeRelativeObjectIdentifier());
    }

    @Override
    default HasPermissions getType() {
        return SecuredDomainType.SWISS_TIMING_ARCHIVE_ACCOUNT;
    }

    default TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier() {
        return getTypeRelativeObjectIdentifier(getJsonURL(), getCreatorName());
    }

    public static TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier(String jsonUrl, String username) {
        return username == null ? new TypeRelativeObjectIdentifier(jsonUrl)
                : new TypeRelativeObjectIdentifier(jsonUrl, username);
    }
}
