package com.sap.sailing.gwt.home.desktop.partials.raceviewerlaunchpad;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.UIObject;

class RaceviewerLaunchPadItem extends UIObject {

    private static RaceviewerLaunchPadItemUiBinder uiBinder = GWT.create(RaceviewerLaunchPadItemUiBinder.class);

    interface RaceviewerLaunchPadItemUiBinder extends UiBinder<AnchorElement, RaceviewerLaunchPadItem> {
    }

    @UiField DivElement itemLabelUi;
    @UiField DivElement itemIconContainerUi;
    private final AnchorElement anchorUi;
    
    RaceviewerLaunchPadItem(String label, String icon, String raceViewerUrl) {
        setElement(anchorUi = uiBinder.createAndBindUi(this));
        anchorUi.setHref(raceViewerUrl);
        itemLabelUi.setInnerText(label);
        itemIconContainerUi.setInnerHTML(icon);
    }

}
