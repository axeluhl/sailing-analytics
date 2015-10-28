package com.sap.sailing.gwt.home.desktop.places.contact;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class ContactActivity extends AbstractActivity {

    public ContactActivity(ContactPlace place, ContactClientFactory clientFactory) {
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        panel.setWidget(new ContactView());
    }

}
