package com.sap.sailing.gwt.managementconsole.places.event.media;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.managementconsole.partials.fileselect.FileSelect;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventMediaViewImpl extends Composite implements EventMediaView {

    interface EventMediaViewUiBinder extends UiBinder<Widget, EventMediaViewImpl> {
    }

    private static EventMediaViewUiBinder uiBinder = GWT.create(EventMediaViewUiBinder.class);

    @UiField
    EventMediaResources local_res;

    @UiField
    Element eventName;

    @UiField
    FileSelect fileSelect;

    public EventMediaViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        local_res.style().ensureInjected();
    }

    @Override
    public void setPresenter(final Presenter presenter) {

    }

    @Override
    public void setEvent(final EventDTO event) {
        this.eventName.setInnerText(event.getName());
    }

}