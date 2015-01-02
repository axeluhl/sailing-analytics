package com.sap.sailing.domain.common.racelog.tracking;

import java.io.IOException;
import java.io.Serializable;

public class TransformationException extends IOException implements Serializable {
    private static final long serialVersionUID = -7795777645371428971L;

    public TransformationException() {
    }

    public TransformationException(Class<?> from, Class<?> to, String at, Throwable e) {
        super("Couldn't transform "+from.getName()+" into "+to.getName()+" at "+at, e);
    }

    public TransformationException(Class<?> from, Class<?> to, String at) {
        this(from, to, at, null);
    }

    public TransformationException(String message, Throwable e) {
        super(message, e);
    }

    public TransformationException(Throwable e) {
        super(e);
    }

    public TransformationException(String message) {
        super(message);
    }
}
