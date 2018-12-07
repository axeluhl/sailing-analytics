package com.sap.sailing.domain.igtimiadapter;

import com.sap.sailing.domain.igtimiadapter.security.IgtimiSecuredDomainType;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;

/**
 * Represents the authorization of a {@link Client} to access the data of some {@link #getUser}.
 */
public interface Account extends WithQualifiedObjectIdentifier {
    User getUser();

    @Override
    default HasPermissions getType() {
        return IgtimiSecuredDomainType.IGTIMI_ACCOUNT;
    }

    @Override
    default String getName() {
        return getUser().getFirstName() + " " + getUser().getSurname();
    }

    @Override
    default QualifiedObjectIdentifier getIdentifier() {
        return getType().getQualifiedObjectIdentifier(this);
    }
}
