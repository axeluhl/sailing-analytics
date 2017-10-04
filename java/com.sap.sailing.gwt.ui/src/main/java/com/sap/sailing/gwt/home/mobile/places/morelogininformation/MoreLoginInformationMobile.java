package com.sap.sailing.gwt.home.mobile.places.morelogininformation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * Mobile page that shows the benefits of logging in on sapsailing.com.
 */
public class MoreLoginInformationMobile extends Composite {

    private static MoreLoginInformationUiBinder uiBinder = GWT.create(MoreLoginInformationUiBinder.class);

    interface MoreLoginInformationUiBinder extends UiBinder<Widget, MoreLoginInformationMobile> {
    }

    @UiField
    public Element register;
    
    public MoreLoginInformationMobile(Runnable onRegisterClick) {
        initWidget(uiBinder.createAndBindUi(this));
        DOM.sinkEvents(register, Event.ONCLICK);
        Event.setEventListener(register, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                onRegisterClick.run();
            }
        });
    }

}
