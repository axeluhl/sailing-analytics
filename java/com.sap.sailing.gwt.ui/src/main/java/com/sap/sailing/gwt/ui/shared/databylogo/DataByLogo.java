package com.sap.sailing.gwt.ui.shared.databylogo;

import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.TrackingConnectorInfoDTO;

public class DataByLogo extends Widget {

    private static final String TRAC_TRAC = "TracTrac";
    private static final String TRAC_TRAC_DEFAULT_URL = "https://www.tractrac.com/";
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

    public void setUp(Set<TrackingConnectorInfoDTO> trackingConnectorInfos, boolean colorIfPossible,
            boolean enforceTextColor) {
        TrackingConnectorInfoDTO mostProminentConnectorInfo = selectMostProminentConnectorInfo(trackingConnectorInfos);
        if (mostProminentConnectorInfo == null) {
            this.setVisible(false);
        } else {
            setUpForConnectorType(colorIfPossible, enforceTextColor, mostProminentConnectorInfo);
        }
    }

    private TrackingConnectorInfoDTO selectMostProminentConnectorInfo(
            Set<TrackingConnectorInfoDTO> trackingConnectorInfos) {
        TrackingConnectorInfoDTO potentialConnectorInfo = null;
        if (trackingConnectorInfos != null && !trackingConnectorInfos.isEmpty()) {
            for (TrackingConnectorInfoDTO trackingConnectorInfo : trackingConnectorInfos) {
                // This logic currently only supports TracTrac as ConnectorInfo
                if (TRAC_TRAC.equals(trackingConnectorInfo.getTrackedBy())) {
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
        if (TRAC_TRAC.equals(trackingConnectorInfo.getTrackedBy())) {
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
        String webUrl = trackingConnectorInfo.getWebUrl();
        if (webUrl == null || "".equals(trackingConnectorInfo.getWebUrl())) {
            dataByContainer.setHref(getConnectorDefaultUrl(trackingConnectorInfo.getTrackedBy()));
        } else {
            dataByContainer.setHref(webUrl);
        }
    }

    private String getConnectorDefaultUrl(String trackedBy) {
        switch (trackedBy) {
        case TRAC_TRAC:
            return TRAC_TRAC_DEFAULT_URL;
        default:
            return null;
        }
    }
}
