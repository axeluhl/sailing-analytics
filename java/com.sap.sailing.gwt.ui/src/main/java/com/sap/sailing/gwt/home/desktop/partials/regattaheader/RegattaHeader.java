package com.sap.sailing.gwt.home.desktop.partials.regattaheader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;

public class RegattaHeader extends Composite {

    private static RegattaHeaderUiBinder uiBinder = GWT.create(RegattaHeaderUiBinder.class);

    interface RegattaHeaderUiBinder extends UiBinder<Widget, RegattaHeader> {
    }
    
    @UiField AnchorElement headerBodyUi;
    @UiField AnchorElement headerArrowUi;

    public RegattaHeader(RegattaMetadataDTO regattaMetadata, boolean showStateMarker) {
        initWidget(uiBinder.createAndBindUi(this));
        headerBodyUi.appendChild(new RegattaHeaderBody(regattaMetadata, showStateMarker).getElement());
    }
    
    public void setRegattaNavigation(PlaceNavigation<?> placeNavigation) {
        headerArrowUi.getStyle().clearDisplay();
        placeNavigation.configureAnchorElement(headerBodyUi);
        placeNavigation.configureAnchorElement(headerArrowUi);
    }
    
}
