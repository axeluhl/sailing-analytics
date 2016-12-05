package com.sap.sailing.gwt.home.desktop.partials.regattaheader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public class RegattaHeaderLegendPopup extends PopupPanel {

    private static RegattaHeaderLegendPopupUiBinder uiBinder = GWT.create(RegattaHeaderLegendPopupUiBinder.class);

    interface RegattaHeaderLegendPopupUiBinder extends UiBinder<Widget, RegattaHeaderLegendPopup> {
    }
    public RegattaHeaderLegendPopup(final AnchorElement parent) {
        super(true);
        setWidget(uiBinder.createAndBindUi(this));
    }
}
