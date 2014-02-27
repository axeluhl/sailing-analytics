package com.sap.sailing.domain.common.racelog.tracking;

import java.io.IOException;

public class TransformationException extends IOException {
    private static final long serialVersionUID = -7795777645371428971L;

    public TransformationException() {
    }

    public TransformationException(Class<?> from, Class<?> to, String at, Throwable e) {
        super(String.format("Couldn't transform %s into %s at %s", from.getName(), to.getName(), at), e);
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
