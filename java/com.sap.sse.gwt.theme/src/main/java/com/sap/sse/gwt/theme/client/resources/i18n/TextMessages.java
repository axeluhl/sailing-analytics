package com.sap.sse.gwt.theme.client.resources.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

public interface TextMessages extends Messages {
    public static final TextMessages INSTANCE = GWT.create(TextMessages.class);

    String chooseALanguage();
}
