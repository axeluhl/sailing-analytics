package com.sap.sse.gwt.theme.client.component.recentevent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class RecentEvent extends Composite {

    @UiField 
    SpanElement eventNameUi;
    
    @UiField
    SpanElement venueNameUi;
    
    @UiField
    SpanElement eventDateUi;
    
    @UiField
    Anchor eventOverviewUi;
    
    @UiField
    DivElement eventImageContainerUi;
    
    @UiField
    DivElement isLiveUi;

    interface RecentEventUiBinder extends UiBinder<Widget, RecentEvent> {
    }

    private static RecentEventUiBinder uiBinder = GWT.create(RecentEventUiBinder.class);
    private Command eventClickAction;

    public RecentEvent(RecentEventData data) {

        this.eventClickAction = data.getCommand();
        RecentEventResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));

        eventNameUi.setInnerText(data.getEventName());
        venueNameUi.setInnerText(data.getVenue());
        eventDateUi.setInnerText(data.getEventStart().toString().substring(10) + " "
                + data.getEventEnd().toString().substring(10));

        final StringBuilder thumbnailUrlBuilder = new StringBuilder("url('").append(data.getEventImageUrl()).append(
                "')");
        GWT.log(thumbnailUrlBuilder.toString());
        eventImageContainerUi.getStyle().setBackgroundImage(thumbnailUrlBuilder.toString());
        if (data.isLive())
            isLiveUi.getStyle().setDisplay(Display.BLOCK);
        else
            isLiveUi.getStyle().setDisplay(Display.NONE);
    }

    @UiHandler("eventOverviewUi")
    public void goToEventOverview(ClickEvent e) {
        eventClickAction.execute();
        e.preventDefault();
    }

}
