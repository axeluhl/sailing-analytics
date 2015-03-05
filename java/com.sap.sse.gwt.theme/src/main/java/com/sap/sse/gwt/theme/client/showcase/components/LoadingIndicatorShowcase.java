package com.sap.sse.gwt.theme.client.showcase.components;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.theme.client.component.loadingindicator.LoadingIndicator;

public class LoadingIndicatorShowcase extends Composite {

    private static LoadingIndicatorShowcaseUiBinder uiBinder = GWT.create(LoadingIndicatorShowcaseUiBinder.class);

    interface LoadingIndicatorShowcaseUiBinder extends UiBinder<Widget, LoadingIndicatorShowcase> {
    }

    @UiField
    LoadingIndicator loadingIndicator;

    public LoadingIndicatorShowcase() {
        initWidget(uiBinder.createAndBindUi(this));
    }
}
