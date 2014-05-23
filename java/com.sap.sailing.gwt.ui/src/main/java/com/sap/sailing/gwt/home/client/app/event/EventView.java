package com.sap.sailing.gwt.home.client.app.event;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventView extends Composite {
    private static EventViewUiBinder uiBinder = GWT.create(EventViewUiBinder.class);

    interface EventViewUiBinder extends UiBinder<Widget, EventView> {
    }

    private final EventDTO event;

    @UiField
    HeadingElement title;

    public EventView(EventDTO event) {
        super();
        this.event = event;
        setTitle(getEvent().getName());
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    private EventDTO getEvent() {
        return event;
    }

    @Override
    public void setTitle(String title) {
        this.title.setInnerHTML(title);
    }

}
