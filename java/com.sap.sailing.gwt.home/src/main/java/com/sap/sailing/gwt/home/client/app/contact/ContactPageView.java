package com.sap.sailing.gwt.home.client.app.contact;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class ContactPageView extends Composite implements ContactPagePresenter.MyView {
    private static ContactPageViewUiBinder uiBinder = GWT.create(ContactPageViewUiBinder.class);

    interface ContactPageViewUiBinder extends UiBinder<Widget, ContactPageView> {
    }

    public ContactPageView() {
        super();

        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void addToSlot(Object slot, IsWidget content) {
    }

    @Override
    public void removeFromSlot(Object slot, IsWidget content) {
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
    }
}

