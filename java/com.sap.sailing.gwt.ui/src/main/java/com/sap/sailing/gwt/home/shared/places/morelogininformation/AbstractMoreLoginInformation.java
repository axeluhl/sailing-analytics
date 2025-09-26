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

    protected <T extends AbstractMoreLoginInformation>
    AbstractMoreLoginInformation(UiBinder<Widget, T> uiBinder,
                                 Runnable registerCallback) {
      @SuppressWarnings("unchecked")
      T owner = (T) this;                        // safe: 'this' is actually a T at runtime
      initWidget(uiBinder.createAndBindUi(owner));
      DOM.sinkEvents(registerControl, Event.ONCLICK);
      Event.setEventListener(registerControl, event -> registerCallback.run());
    }

    @Override
    public final void setRegisterControlVisible(boolean visible) {
        UIObject.setVisible(registerControl, visible);
    }

}
