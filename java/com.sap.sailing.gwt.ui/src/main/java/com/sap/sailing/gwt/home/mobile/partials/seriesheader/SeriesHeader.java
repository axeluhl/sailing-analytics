package com.sap.sailing.gwt.home.mobile.partials.seriesheader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.shared.LabelTypeUtil;
import com.sap.sailing.gwt.home.shared.utils.LogoUtil;
import com.sap.sailing.gwt.ui.shared.fakeseries.EventSeriesViewDTO;
import com.sap.sailing.gwt.ui.shared.general.EventMetadataDTO;

public class SeriesHeader extends Composite {
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Widget, SeriesHeader> {
    }
    
    @UiField SpanElement eventNameUi;
    @UiField DivElement eventStateUi;
    @UiField DivElement eventLogoUi;
    @UiField DivElement locationsUi;

    public SeriesHeader(EventSeriesViewDTO event) {
        SeriesHeaderResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        setUiFieldValues(event);
    }

    private void setUiFieldValues(EventSeriesViewDTO event) {
        LogoUtil.setEventLogo(eventLogoUi, event);
        eventNameUi.setInnerText(event.getDisplayName());
        LabelTypeUtil.renderLabelType(eventStateUi, event.getState().getStateMarker());
        StringBuilder locationsBuilder = new StringBuilder();
        boolean first = true;
        for (EventMetadataDTO eventOfSeries : event.getEvents()) {
            if(!first) {
                locationsBuilder.append(", ");
            }
            locationsBuilder.append(eventOfSeries.getLocation());
            first = false;
        }
        locationsUi.setInnerText(locationsBuilder.toString());
    }
}
