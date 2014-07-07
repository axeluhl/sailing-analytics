package com.sap.sailing.gwt.home.client.shared.stage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.shared.EventDatesFormatterUtil;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class Stage extends Composite {

    @SuppressWarnings("unused")
    private EventDTO featuredEvent;
    
    @UiField SpanElement subtitle;
    @UiField SpanElement title;
    @UiField SpanElement bandTitle;
    @UiField SpanElement bandSubtitle;
    @UiField SpanElement bandAction;

    interface StageUiBinder extends UiBinder<Widget, Stage> {
    }
    
    private static StageUiBinder uiBinder = GWT.create(StageUiBinder.class);

    public Stage() {
        StageResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void setFeaturedEvent(EventDTO featuredEvent) {
        this.featuredEvent = featuredEvent;
        
        title.setInnerText(featuredEvent.getName());
        subtitle.setInnerText(featuredEvent.venue.getName());

        bandTitle.setInnerText(EventDatesFormatterUtil.formatDateRangeWithYear(featuredEvent.startDate, featuredEvent.endDate));
        bandSubtitle.setInnerText("[Race 6]");
        bandAction.setInnerText("Next Race");
    }
    
}
