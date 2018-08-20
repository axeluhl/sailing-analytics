package com.sap.sailing.gwt.home.mobile.partials.eventheader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.BRElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.utils.EventDatesFormatterUtil;
import com.sap.sailing.gwt.home.shared.utils.LabelTypeUtil;
import com.sap.sailing.gwt.home.shared.utils.LogoUtil;

public class EventHeader extends Composite {
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Widget, EventHeader> {
    }
    
    @UiField SpanElement eventNameUi;
    @UiField BRElement nameSeparatorUi;
    @UiField SpanElement eventRegattaUi;
    @UiField DivElement eventStateUi;
    @UiField AnchorElement eventLogoUi;
    @UiField DivElement eventDateUi;
    @UiField DivElement eventLocationUi;

    public EventHeader(EventViewDTO event, String optionalRegattaDisplayName, PlaceNavigation<?> logoNavigation) {
        EventHeaderResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        setUiFieldValues(event, optionalRegattaDisplayName, logoNavigation);
    }

    private void setUiFieldValues(EventViewDTO event, String optionalRegattaDisplayName, PlaceNavigation<?> logoNavigation) {
        eventNameUi.setInnerText(event.getDisplayName());
        if(optionalRegattaDisplayName != null && !optionalRegattaDisplayName.isEmpty()) {
            eventRegattaUi.setInnerText(optionalRegattaDisplayName);
        } else {
            nameSeparatorUi.removeFromParent();
        }
        LabelTypeUtil.renderLabelType(eventStateUi, event.getState().getStateMarker());
        LogoUtil.setEventLogo(eventLogoUi, event);
        if(logoNavigation != null) {
            logoNavigation.configureAnchorElement(eventLogoUi);
        }
        eventDateUi.setInnerText(EventDatesFormatterUtil.formatDateRangeWithYear(event.getStartDate(), event.getEndDate()));
        eventLocationUi.setInnerText(event.getLocationAndVenueAndCountry());
    }
}
