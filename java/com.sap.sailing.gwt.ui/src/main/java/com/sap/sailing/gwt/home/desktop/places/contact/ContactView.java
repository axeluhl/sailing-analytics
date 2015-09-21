package com.sap.sailing.gwt.home.desktop.places.contact;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class ContactView extends Composite {
    private static ContactViewUiBinder uiBinder = GWT.create(ContactViewUiBinder.class);

    interface ContactViewUiBinder extends UiBinder<Widget, ContactView> {
    }

    public ContactView() {
        super();
        initWidget(uiBinder.createAndBindUi(this));
    }
}
