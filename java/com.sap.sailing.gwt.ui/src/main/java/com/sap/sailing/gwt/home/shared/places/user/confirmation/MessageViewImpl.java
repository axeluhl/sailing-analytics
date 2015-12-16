package com.sap.sailing.gwt.home.shared.places.user.confirmation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.mvp.ErrorView;

public class MessageViewImpl extends Composite implements ErrorView {
    private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

    interface WidgetUiBinder extends UiBinder<Widget, MessageViewImpl> {
    }

    @UiField
    protected DivElement messageTitleUi;
    @UiField
    protected Element messageUi;

    
    public MessageViewImpl(String messageTitle, String message) {
        initWidget(uiBinder.createAndBindUi(this));
        messageTitleUi.setInnerText(messageTitle);
        messageUi.setInnerText(message);

    }
}
