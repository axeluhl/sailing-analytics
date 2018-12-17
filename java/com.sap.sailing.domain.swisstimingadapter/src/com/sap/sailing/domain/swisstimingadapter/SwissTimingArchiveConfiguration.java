package com.sap.sailing.domain.swisstimingadapter;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;

public interface SwissTimingArchiveConfiguration extends WithQualifiedObjectIdentifier {

    String getJsonURL();

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

    @Override
    default TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier(String... params) {
        return getTypeRelativeObjectIdentifier(getJsonURL());
    }

    public static TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier(SwissTimingArchiveConfiguration config) {
        return new TypeRelativeObjectIdentifier(config.getJsonURL());
    }

    public static TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier(String jsonUrl) {
        return new TypeRelativeObjectIdentifier(jsonUrl);
    }
}
