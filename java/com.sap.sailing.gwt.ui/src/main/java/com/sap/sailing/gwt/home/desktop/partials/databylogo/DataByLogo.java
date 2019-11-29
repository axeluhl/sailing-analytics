package com.sap.sailing.gwt.home.desktop.partials.databylogo;

import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO;
import com.sap.sailing.gwt.home.communication.eventview.TrackingConnectorInfoDTO;

public class DataByLogo extends Composite {

    private static SharingButtonsUiBinder uiBinder = GWT.create(SharingButtonsUiBinder.class);

    interface SharingButtonsUiBinder extends UiBinder<Widget, DataByLogo> {
    }

    @UiField
    AnchorElement dataByContainer;
    @UiField
    Image logo;

    public DataByLogo() {
        DataByLogoResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void setUp(EventViewDTO event) {
        Set<TrackingConnectorInfoDTO> trackingConnectorInfos = event.getTrackingConnectorInfos();
        boolean istrackedByTracTrac = false;
        for (TrackingConnectorInfoDTO trackingConnectorInfo : trackingConnectorInfos) {
            if ("TracTrac".equals(trackingConnectorInfo.getTrackedBy())) {
                istrackedByTracTrac = true;
                logo.setUrl(DataByLogoResources.INSTANCE.tractrac().getSafeUri());
                String webUrl = trackingConnectorInfo.getWebUrl();
                if (webUrl == null || "".equals(trackingConnectorInfo.getWebUrl())) {
                    dataByContainer.setHref("https://www.tractrac.com/");
                }else {
                    dataByContainer.setHref(webUrl);
                }
            }
        }
        if (!istrackedByTracTrac) {
            this.removeFromParent();
        }
    }
}
