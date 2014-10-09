package com.sap.sse.security.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

public interface StringMessages extends Messages {
    public static final StringMessages INSTANCE = GWT.create(StringMessages.class);

    String name();

    String password();

    String loggedIn();
}
