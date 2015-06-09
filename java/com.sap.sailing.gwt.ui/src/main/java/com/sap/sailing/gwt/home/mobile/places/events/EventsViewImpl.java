package com.sap.sailing.gwt.home.mobile.places.events;

import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class EventsViewImpl extends Composite implements EventsView {
    private static StartPageViewUiBinder uiBinder = GWT.create(StartPageViewUiBinder.class);

    interface StartPageViewUiBinder extends UiBinder<Widget, EventsViewImpl> {
    }

    private Presenter currentPresenter;

    @UiField
    protected Anchor theEventLinkUi;

    public EventsViewImpl(Presenter presenter) {
        this.currentPresenter = presenter;
        initWidget(uiBinder.createAndBindUi(this));

    }

    @UiHandler("theEventLinkUi")
    public void gotoEvents(ClickEvent e) {
        UUID eventId = null;
        currentPresenter.gotoTheEvent(eventId);
    }
    
}
