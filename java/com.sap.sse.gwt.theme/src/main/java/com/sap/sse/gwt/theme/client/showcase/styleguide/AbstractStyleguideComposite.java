package com.sap.sse.gwt.theme.client.showcase.styleguide;

import com.google.gwt.user.client.ui.Composite;
import com.sap.sse.gwt.theme.client.resources.ThemeResources;
import com.sap.sse.gwt.theme.client.showcase.ShowCaseResources;

public class AbstractStyleguideComposite extends Composite {
    public AbstractStyleguideComposite() {
        ThemeResources.INSTANCE.mediaCss().ensureInjected();
        ThemeResources.INSTANCE.mainCss().ensureInjected();
        ShowCaseResources.INSTANCE.css().ensureInjected();
    }
}
