package com.sap.sailing.gwt.home.shared.places.user.confirmation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class ConfirmationViewImpl extends Composite implements ConfirmationView {
    private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

    interface WidgetUiBinder extends UiBinder<Widget, ConfirmationViewImpl> {
    }

    @UiField
    protected DivElement messageTitleUi;
    @UiField
    protected Element messageUi;

    public ConfirmationViewImpl(String messageTitle, String message) {
        initWidget(uiBinder.createAndBindUi(this));
        messageTitleUi.setInnerText(messageTitle);
        messageUi.setInnerText(message);
    }

}
