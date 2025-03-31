package com.sap.sailing.gwt.home.shared.places.morelogininformation;

import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

public class AbstractMoreLoginInformation extends Composite implements MoreLoginInformationView {

    @UiField
    public Element registerControl;

    protected AbstractMoreLoginInformation(UiBinder<Widget, AbstractMoreLoginInformation> uiBinder,
            Runnable registerCallback) {
        initWidget(uiBinder.createAndBindUi(this));
        DOM.sinkEvents(registerControl, Event.ONCLICK);
        Event.setEventListener(registerControl, event -> registerCallback.run());
    }

    @Override
    public final void setRegisterControlVisible(boolean visible) {
        UIObject.setVisible(registerControl, visible);
    }

}
