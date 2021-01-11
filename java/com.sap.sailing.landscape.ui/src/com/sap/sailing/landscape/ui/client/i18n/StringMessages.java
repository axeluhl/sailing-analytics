package com.sap.sailing.landscape.ui.client.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;

@DefaultLocale("en")
public interface StringMessages extends com.sap.sse.gwt.client.StringMessages,
com.sap.sse.security.ui.client.i18n.StringMessages,
com.sap.sse.gwt.adminconsole.StringMessages {
    public static final StringMessages INSTANCE = GWT.create(StringMessages.class);

    String region();
    String images();
    String imageType();
    String upgrade();
    String replicaSet();
    String awsCredentials();
    String awsAccessKey();
    String awsSecret();
}
