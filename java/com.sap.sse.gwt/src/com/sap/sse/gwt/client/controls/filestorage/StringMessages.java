package com.sap.sse.gwt.client.controls.filestorage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;

@DefaultLocale("en")
public interface StringMessages extends com.sap.sse.gwt.client.StringMessages {
    public static StringMessages INSTANCE = GWT.create(StringMessages.class);
    
    String active();
    String setAsActive();
    String couldNotTestProperties();
    String couldNotSetProperties();
    String couldNotSetActiveService();
    String couldNotLoadActiveService();
    String couldNotLoadAvailableServices();
    String refresh();
    String name();
    String value();
    String description();
    String error();
}
