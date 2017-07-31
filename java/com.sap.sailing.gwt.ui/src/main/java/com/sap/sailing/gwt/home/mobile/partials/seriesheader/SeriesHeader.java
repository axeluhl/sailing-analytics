package com.sap.sailing.gwt.home.mobile.partials.seriesheader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.event.EventMetadataDTO;
import com.sap.sailing.gwt.home.communication.fakeseries.EventSeriesViewDTO;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.utils.LabelTypeUtil;
import com.sap.sailing.gwt.home.shared.utils.LogoUtil;

public class SeriesHeader extends Composite {
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Widget, SeriesHeader> {
    }
    
    @UiField SpanElement eventNameUi;
    @UiField DivElement eventStateUi;
    @UiField AnchorElement eventLogoUi;
    @UiField DivElement locationsUi;

    public SeriesHeader(EventSeriesViewDTO event) {
        this(event, null);
    }
    
    public SeriesHeader(EventSeriesViewDTO event, PlaceNavigation<?> logoNavigation) {
        SeriesHeaderResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        setUiFieldValues(event, logoNavigation);
    }

    private void setUiFieldValues(EventSeriesViewDTO event, PlaceNavigation<?> logoNavigation) {
        LogoUtil.setEventLogo(eventLogoUi, event);
        if(logoNavigation != null) {
            logoNavigation.configureAnchorElement(eventLogoUi);
        }
        eventNameUi.setInnerText(event.getDisplayName());
        LabelTypeUtil.renderLabelType(eventStateUi, event.getState().getStateMarker());
        StringBuilder locationsBuilder = new StringBuilder();
        boolean first = true;
        for (EventMetadataDTO eventOfSeries : event.getEventsAscending()) {
            if(!first) {
                locationsBuilder.append(", ");
            }
            locationsBuilder.append(eventOfSeries.getLocation());
            first = false;
        }
        locationsUi.setInnerText(locationsBuilder.toString());
    }
}
