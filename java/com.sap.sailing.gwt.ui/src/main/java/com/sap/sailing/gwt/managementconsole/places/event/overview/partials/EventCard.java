package com.sap.sailing.gwt.managementconsole.places.event.overview.partials;

import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.managementconsole.places.event.overview.EventOverviewResources;

public class EventCard extends Composite {
    
    Logger logger = Logger.getLogger(this.getClass().getName());

    @UiField
    EventOverviewResources local_res;

    @UiField
    DivElement card;
    
    @UiField
    HeadingElement title;
    
    @UiField
    HeadingElement subTitle;
    
    @UiField
    DivElement eventState1;
    
    @UiField
    DivElement eventState2;
    
    interface EventCardUiBinder extends UiBinder<Widget, EventCard> {
    }
    
    private static EventCardUiBinder uiBinder = GWT.create(EventCardUiBinder.class);
    
    public EventCard(String title, String subTitle, boolean featured, String imageUrl) {
        initWidget(uiBinder.createAndBindUi(this));
        local_res.style().ensureInjected();
        
        this.title.setInnerSafeHtml(SafeHtmlUtils.fromString(title));
        this.subTitle.setInnerSafeHtml(SafeHtmlUtils.fromString(subTitle));
        if (featured) {
            this.eventState1.getStyle().setVisibility(Visibility.VISIBLE);
            this.eventState2.getStyle().setVisibility(Visibility.VISIBLE);
        } else {
            this.eventState1.getStyle().setVisibility(Visibility.HIDDEN);
            this.eventState2.getStyle().setVisibility(Visibility.HIDDEN);
        }
        if (imageUrl != null) {
            this.card.getStyle().setBackgroundImage("url(' " + imageUrl + "')");
            this.card.addClassName(local_res.style().customTeaser());
        }
    }

}
