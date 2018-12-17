package com.sap.sse.security.shared;

import com.sap.sse.common.Named;

public interface WithQualifiedObjectIdentifier extends Named {
    QualifiedObjectIdentifier getIdentifier();
    TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier(String... params);
    HasPermissions getType();
}
