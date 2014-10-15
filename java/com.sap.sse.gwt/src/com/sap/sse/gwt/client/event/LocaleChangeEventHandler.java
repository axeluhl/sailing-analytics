package com.sap.sse.gwt.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface LocaleChangeEventHandler extends EventHandler {
    /**
     * @param localeChangeEvent
     */
    void onLocaleChange(LocaleChangeEvent event);
}
