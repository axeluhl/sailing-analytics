package com.sap.sailing.gwt.home.client.shared.placeholder;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class InfoPlaceholder extends Composite {

    interface InfoPlaceholderUiBinder extends UiBinder<Widget, InfoPlaceholder> {
    }

    private static InfoPlaceholderUiBinder uiBinder = GWT.create(InfoPlaceholderUiBinder.class);

    @UiField
    DivElement content;

    public InfoPlaceholder(String text) {
        InfoPlaceholderResources.INSTANCE.css().ensureInjected();

        initWidget(uiBinder.createAndBindUi(this));
        this.setHeight(Window.getClientHeight() + "px");

        content.setInnerText(text);
    }
}
