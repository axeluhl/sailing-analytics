package com.sap.sailing.gwt.home.client.app.event;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventView extends Composite implements EventPagePresenter.MyView {
    private static EventViewUiBinder uiBinder = GWT.create(EventViewUiBinder.class);

    interface EventViewUiBinder extends UiBinder<Widget, EventView> {
    }

    private EventDTO event;

    @UiField
    HeadingElement title;

    public EventView() {
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

    @Override
    public void setEvent(EventDTO event) {
        this.event = event;
        setTitle(event.getName());
    }

    @Override
    public void setTitle(String title) {
        this.title.setInnerHTML(title);
    }

}
