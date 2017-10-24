package com.sap.sailing.gwt.home.desktop.places.morelogininformation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * Desktop page that shows the benefits of logging in on sapsailing.com.
 */
public class MoreLoginInformation extends Composite {

    private static MoreLoginInformationUiBinder uiBinder = GWT.create(MoreLoginInformationUiBinder.class);

    interface MoreLoginInformationUiBinder extends UiBinder<Widget, MoreLoginInformation> {
    }

    @UiField
    Element registerControl;
    
    public MoreLoginInformation(Runnable onRegisterClick) {
        initWidget(uiBinder.createAndBindUi(this));
        DOM.sinkEvents(registerControl, Event.ONCLICK);
        Event.setEventListener(registerControl, event -> onRegisterClick.run());
    }

}
