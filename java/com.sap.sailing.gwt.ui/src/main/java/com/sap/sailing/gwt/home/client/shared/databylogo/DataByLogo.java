package com.sap.sailing.gwt.home.client.shared.databylogo;

import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO;
import com.sap.sailing.gwt.home.communication.eventview.TrackingConnectorInfoDTO;

public class DataByLogo extends Widget {

    private static DataByLogoUiBinder uiBinder = GWT.create(DataByLogoUiBinder.class);

    interface DataByLogoUiBinder extends UiBinder<Element, DataByLogo> {
    }

    @UiField
    AnchorElement dataByContainer;
    @UiField
    ImageElement logo;

    public DataByLogo() {
        DataByLogoResources.INSTANCE.css().ensureInjected();
        setElement(uiBinder.createAndBindUi(this));
    }

    public void setUp(EventViewDTO event) {
        Set<TrackingConnectorInfoDTO> trackingConnectorInfos = event.getTrackingConnectorInfos();
        boolean istrackedByTracTrac = false;
        for (TrackingConnectorInfoDTO trackingConnectorInfo : trackingConnectorInfos) {
            if ("TracTrac".equals(trackingConnectorInfo.getTrackedBy())) {
                istrackedByTracTrac = true;
                logo.setSrc(DataByLogoResources.INSTANCE.tractrac().getSafeUri().asString());
                String webUrl = trackingConnectorInfo.getWebUrl();
                if (webUrl == null || "".equals(trackingConnectorInfo.getWebUrl())) {
                    dataByContainer.setHref("https://www.tractrac.com/");
                }else {
                    dataByContainer.setHref(webUrl);
                }
            }
        }
        if (!istrackedByTracTrac) {
            this.setVisible(false);
        }
    }
}
