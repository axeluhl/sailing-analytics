package com.sap.sailing.gwt.home.shared.partials.placeholder;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class Placeholder extends Composite {

    interface PlaceholderUiBinder extends UiBinder<Widget, Placeholder> {
    }
    
    private static PlaceholderUiBinder uiBinder = GWT.create(PlaceholderUiBinder.class);

    public Placeholder() {
        PlaceholderResources.INSTANCE.css().ensureInjected();

        initWidget(uiBinder.createAndBindUi(this));
        
        this.setHeight(Window.getClientHeight() + "px");
    }
}
