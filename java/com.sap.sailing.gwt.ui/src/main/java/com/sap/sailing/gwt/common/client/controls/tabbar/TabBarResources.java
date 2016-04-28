package com.sap.sailing.gwt.common.client.controls.tabbar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface TabBarResources extends ClientBundle {
    public static final TabBarResources INSTANCE = GWT.create(TabBarResources.class);

    @Source("TabBar.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String navbar();
        String navbar_button();
        String navbar_buttonhidden();
        String navbar_buttonactive();
    }

}
