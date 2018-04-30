package com.sap.sailing.gwt.home.shared.partials.windfinder;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.UIObject;

class WindfinderLaunchPadItem extends UIObject {

    private static WindfinderLaunchPadItemUiBinder uiBinder = GWT.create(WindfinderLaunchPadItemUiBinder.class);

    interface WindfinderLaunchPadItemUiBinder extends UiBinder<AnchorElement, WindfinderLaunchPadItem> {
    }

    @UiField WindfinderResources local_res;
    @UiField DivElement itemLabelUi, itemIconUi;
    private final AnchorElement anchorUi;
    
    WindfinderLaunchPadItem(String label, String raceViewerUrl) {
        setElement(anchorUi = uiBinder.createAndBindUi(this));
        anchorUi.setHref(raceViewerUrl);
        itemLabelUi.setInnerText(label);
        itemIconUi.setInnerHTML(local_res.windfinderLogo().getText());
    }

}
