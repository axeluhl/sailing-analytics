package com.sap.sse.gwt.theme.client.component.loadingindicator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class LoadingIndicator extends Composite {
    private static LoadingIndicatorUiBinder uiBinder = GWT.create(LoadingIndicatorUiBinder.class);

    interface LoadingIndicatorUiBinder extends UiBinder<Widget, LoadingIndicator> {
    }
    
    public LoadingIndicator() {
        LoadingIndicatorResources.INSTANCE.css().ensureInjected();
        
        initWidget(uiBinder.createAndBindUi(this));
    }
}
