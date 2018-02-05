package com.sap.sse.security.shared;

import com.sap.sse.security.shared.impl.ObjectAnnotationImpl;

public class AccessControlListAnnotation extends ObjectAnnotationImpl<AccessControlList> {
    private static final long serialVersionUID = 4927964965965967418L;

    public AccessControlListAnnotation(AccessControlList annotation, String idOfAnnotatedObjectAsString,
            String displayNameOfAnnotatedObject) {
        super(annotation, idOfAnnotatedObjectAsString, displayNameOfAnnotatedObject);
    }
}
