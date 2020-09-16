package com.sap.sailing.gwt.ui.shared.databylogo;

import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.tractracadapter.TracTracAdapter;
import com.sap.sailing.gwt.ui.shared.TrackingConnectorInfoDTO;
import com.sap.sse.gwt.shared.ClientConfiguration;
import com.sap.sse.gwt.shared.DebugConstants;

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
        if (!ClientConfiguration.getInstance().isBrandingActive()) {
            dataByContainer.getStyle().setDisplay(Display.NONE);
        }
        dataByContainer.setAttribute(DebugConstants.DEBUG_ID_ATTRIBUTE, "dataByContainer");
    }

    public void setUp(Set<TrackingConnectorInfoDTO> trackingConnectorInfos, boolean colorIfPossible,
            boolean enforceTextColor) {
        if (ClientConfiguration.getInstance().isBrandingActive()) {
            TrackingConnectorInfoDTO mostProminentConnectorInfo = selectMostProminentConnectorInfo(
                    trackingConnectorInfos);
            if (mostProminentConnectorInfo == null) {
                this.setVisible(false);
            } else {
                setUpForConnectorType(colorIfPossible, enforceTextColor, mostProminentConnectorInfo);
            }
        }
    }

    private TrackingConnectorInfoDTO selectMostProminentConnectorInfo(
            Set<TrackingConnectorInfoDTO> trackingConnectorInfos) {
        TrackingConnectorInfoDTO potentialConnectorInfo = null;
        if (trackingConnectorInfos != null && !trackingConnectorInfos.isEmpty()) {
            for (TrackingConnectorInfoDTO trackingConnectorInfo : trackingConnectorInfos) {
                // This logic currently only supports TracTrac as ConnectorInfo
                if (trackingConnectorInfo.getTrackingConnectorName().equals(TracTracAdapter.NAME)) {
                    potentialConnectorInfo = trackingConnectorInfo;
                    String webUrl = trackingConnectorInfo.getWebUrl();
                    if (webUrl != null && !webUrl.isEmpty()) {
                        break;
                    }
                }
            }
        }
        return potentialConnectorInfo;
    }

    private void setUpForConnectorType(boolean colorIfPossible, boolean enforceTextColor,
            TrackingConnectorInfoDTO trackingConnectorInfo) {
        if (trackingConnectorInfo.getTrackingConnectorName().equals(TracTracAdapter.NAME)) {
            setUpTracTracLogo(colorIfPossible, enforceTextColor);
        }
        setUrl(trackingConnectorInfo);
    }

    private void setUpTracTracLogo(boolean colorIfPossible, boolean enforceTextColor) {
        final DataByLogoResources resources = DataByLogoResources.INSTANCE;

        final DataResource imageToSet = colorIfPossible ? resources.tractracColor() : resources.tractracWhite();
        logo.setSrc(imageToSet.getSafeUri().asString());

        if (enforceTextColor) {
            this.addStyleName(colorIfPossible ? resources.css().databylogo_black_text()
                    : resources.css().databylogo_white_text());
        }
    }

    private void setUrl(TrackingConnectorInfoDTO trackingConnectorInfo) {
        String webUrl = trackingConnectorInfo.getWebUrl() == null
                ? trackingConnectorInfo.getTrackingConnectorDefaultUrl()
                : trackingConnectorInfo.getWebUrl();
        if(webUrl != null) {
            dataByContainer.setHref(webUrl);
        }
    }
}
