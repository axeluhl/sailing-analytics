package com.sap.sse.security.shared.dto;

import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.impl.ObjectAnnotationImpl;

public class AccessControlListAnnotationDTO extends ObjectAnnotationImpl<AccessControlListDTO> {
    private static final long serialVersionUID = 4927964965965967418L;

    public AccessControlListAnnotationDTO(AccessControlListDTO annotation,
            QualifiedObjectIdentifier idOfAnnotatedObject,
            String displayNameOfAnnotatedObject) {
        super(annotation, idOfAnnotatedObject, displayNameOfAnnotatedObject);
    }
}
