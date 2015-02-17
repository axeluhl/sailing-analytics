package com.sap.sse.gwt.theme.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.sap.sse.gwt.theme.client.resources.ThemeResources;
import com.sap.sse.gwt.theme.client.showcase.MainPage;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class ThemeEntryPoint implements EntryPoint {

    @Override
    public void onModuleLoad() {
        ThemeResources.INSTANCE.mediaCss().ensureInjected();
        ThemeResources.INSTANCE.mainCss().ensureInjected();

        RootLayoutPanel.get().add(new MainPage());
    }
}
