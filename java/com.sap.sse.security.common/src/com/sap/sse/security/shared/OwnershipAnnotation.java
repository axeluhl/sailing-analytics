package com.sap.sse.security.shared;

import com.sap.sse.security.shared.impl.ObjectAnnotationImpl;

public class OwnershipAnnotation extends ObjectAnnotationImpl<Ownership> {
    private static final long serialVersionUID = 7242600656125139931L;

    public OwnershipAnnotation(Ownership annotation, QualifiedObjectIdentifier idOfAnnotatedObject, String displayNameOfAnnotatedObject) {
        super(annotation, idOfAnnotatedObject, displayNameOfAnnotatedObject);
    }
}
