package com.sap.sse.security.shared.impl;

import java.io.Serializable;

import com.sap.sse.security.shared.ObjectAnnotation;

public class ObjectAnnotationImpl<T extends Serializable> implements ObjectAnnotation<T> {
    private static final long serialVersionUID = -7213142753739732703L;
    
    private final T annotation;
    private final String idOfAnnotatedObjectAsString;
    private final String displayNameOfAnnotatedObject;
    
    public ObjectAnnotationImpl(T annotation, String idOfAnnotatedObjectAsString, String displayNameOfAnnotatedObject) {
        super();
        this.annotation = annotation;
        this.idOfAnnotatedObjectAsString = idOfAnnotatedObjectAsString;
        this.displayNameOfAnnotatedObject = displayNameOfAnnotatedObject;
    }

    @Override
    public String getIdOfAnnotatedObjectAsString() {
        return idOfAnnotatedObjectAsString;
    }

    @Override
    public String getDisplayNameOfAnnotatedObject() {
        return displayNameOfAnnotatedObject;
    }

    @Override
    public T getAnnotation() {
        return annotation;
    }
}
