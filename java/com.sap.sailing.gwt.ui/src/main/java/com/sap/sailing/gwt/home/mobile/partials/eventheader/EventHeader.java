package com.sap.sailing.gwt.home.mobile.partials.eventheader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BRElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.shared.EventDatesFormatterUtil;
import com.sap.sailing.gwt.home.client.shared.LabelTypeUtil;
import com.sap.sailing.gwt.home.shared.utils.LogoUtil;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;

public class EventHeader extends Composite {
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Widget, EventHeader> {
    }
    
    @UiField SpanElement eventNameUi;
    @UiField BRElement nameSeparatorUi;
    @UiField SpanElement eventRegattaUi;
    @UiField DivElement eventStateUi;
    @UiField DivElement eventLogoUi;
    @UiField DivElement eventDateUi;
    @UiField DivElement eventLocationUi;

    public EventHeader(EventViewDTO event) {
        this(event, null);
    }
    
    public EventHeader(EventViewDTO event, String optionalRegattaDisplayName) {
        EventHeaderResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        setUiFieldValues(event, optionalRegattaDisplayName);
    }

    private void setUiFieldValues(EventViewDTO event, String optionalRegattaDisplayName) {
        eventNameUi.setInnerText(event.getDisplayName());
        if(optionalRegattaDisplayName != null && !optionalRegattaDisplayName.isEmpty()) {
            eventRegattaUi.setInnerText(optionalRegattaDisplayName);
        } else {
            nameSeparatorUi.removeFromParent();
        }
        LabelTypeUtil.renderLabelType(eventStateUi, event.getState().getStateMarker());
        LogoUtil.setEventLogo(eventLogoUi, event);
        eventDateUi.setInnerText(EventDatesFormatterUtil.formatDateRangeWithYear(event.getStartDate(), event.getEndDate()));
        eventLocationUi.setInnerText(event.getLocationAndVenueAndCountry());
    }
}
