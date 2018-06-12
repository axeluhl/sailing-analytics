package com.sap.sse.gwt.client.shared.controls;

import com.google.gwt.text.shared.AbstractRenderer;

public abstract class AbstractObjectRenderer<T> extends AbstractRenderer<T> {

    @Override
    public String render(T object) {
        if (object == null) {
            return "";
        }
        return convertObjectToString(object);
    }
    
    protected abstract String convertObjectToString(T nonNullObject);

}
