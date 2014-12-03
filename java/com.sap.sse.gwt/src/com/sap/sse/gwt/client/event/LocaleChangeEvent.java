package com.sap.sse.gwt.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class LocaleChangeEvent extends GwtEvent<LocaleChangeEventHandler> {
    public static Type<LocaleChangeEventHandler> TYPE = new Type<LocaleChangeEventHandler>();
 
    private final String newLocaleID;

    public LocaleChangeEvent(final String newLocaleID) {
        this.newLocaleID = newLocaleID;
    }

    public String getNewLocaleID() {
        return newLocaleID;
    }

    @Override
    public Type<LocaleChangeEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(final LocaleChangeEventHandler handler) {
        handler.onLocaleChange(this);
    }
}
