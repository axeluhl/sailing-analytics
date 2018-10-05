package com.sap.sailing.domain.base;

import com.sap.sse.security.shared.QualifiedObjectIdentifier;

public interface WithQualifiedObjectIdentifier {

    QualifiedObjectIdentifier getQualifiedObjectIdentifier();

    String getSecurityDisplayName();

}
