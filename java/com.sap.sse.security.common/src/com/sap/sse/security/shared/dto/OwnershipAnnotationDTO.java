package com.sap.sse.security.shared.dto;

import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.impl.ObjectAnnotationImpl;

public class OwnershipAnnotationDTO extends ObjectAnnotationImpl<OwnershipDTO> {
    private static final long serialVersionUID = 7242600656125139931L;

    public OwnershipAnnotationDTO(OwnershipDTO annotation, QualifiedObjectIdentifier idOfAnnotatedObject,
            String displayNameOfAnnotatedObject) {
        super(annotation, idOfAnnotatedObject, displayNameOfAnnotatedObject);
    }
}
