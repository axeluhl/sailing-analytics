package com.sap.sse.security;

public interface ActionWithResult<T> {

    T run() throws Exception;

}
