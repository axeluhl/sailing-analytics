package com.sap.sailing.gwt.ui.shared;

import java.io.Serializable;

import com.sap.sailing.domain.common.dto.PersonDTO;
import com.sap.sse.common.CountryCode;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;

public class SerializationDummy implements Serializable {
    private static final long serialVersionUID = -7013318977093101994L;
    PersonDTO personDTO;
    CountryCode countryCode;
    TypeRelativeObjectIdentifier typeRelativeObjectIdentifier;
}
