package com.sap.sailing.domain.common;

import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;

public interface RegattaAndRaceIdentifier extends RegattaIdentifier, RaceIdentifier, WithQualifiedObjectIdentifier {

    TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier();
}
