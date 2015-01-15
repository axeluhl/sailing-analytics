package com.sap.sse.gwt.client.mvp.example;

import com.google.gwt.core.client.GWT;
import com.sap.sse.gwt.client.StringMessages;

/**
 * String messages for the MVP sample app
 * @author Frank
 *
 */
public interface TextMessages extends StringMessages {
    public static final TextMessages INSTANCE = GWT.create(TextMessages.class);

}
