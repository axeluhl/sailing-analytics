package com.sap.sse.security.shared.impl;

import java.io.Serializable;

import com.sap.sse.security.shared.ObjectAnnotation;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;

public class ObjectAnnotationImpl<T extends Serializable> implements ObjectAnnotation<T> {
    private static final long serialVersionUID = -7213142753739732703L;
    
    private T annotation;
    private QualifiedObjectIdentifier idOfAnnotatedObject;
    private String displayNameOfAnnotatedObject;
    
    public ObjectAnnotationImpl(T annotation, QualifiedObjectIdentifier idOfAnnotatedObject, String displayNameOfAnnotatedObject) {
        super();
        this.annotation = annotation;
        this.idOfAnnotatedObject = idOfAnnotatedObject;
        this.displayNameOfAnnotatedObject = displayNameOfAnnotatedObject;
    }

    @Override
    public QualifiedObjectIdentifier getIdOfAnnotatedObject() {
        return idOfAnnotatedObject;
    }

    @Override
    public String getDisplayNameOfAnnotatedObject() {
        return displayNameOfAnnotatedObject;
    }

    @Override
    public T getAnnotation() {
        return annotation;
    }
    
    @Override
    public String toString() {
        return "Annotation " + annotation + " on object "
                + (getDisplayNameOfAnnotatedObject() == null ? "" : (getDisplayNameOfAnnotatedObject() + " "))
                + "with ID " + getIdOfAnnotatedObject();
    }
}
