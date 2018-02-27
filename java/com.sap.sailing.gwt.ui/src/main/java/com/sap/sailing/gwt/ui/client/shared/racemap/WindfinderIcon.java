package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class WindfinderIcon extends Widget {

    private static WindfinderIconUiBinder uiBinder = GWT.create(WindfinderIconUiBinder.class);

    interface WindfinderIconUiBinder extends UiBinder<Element, WindfinderIcon> {
    }
    
    @UiField AnchorElement anchorUi;
    @UiField DivElement iconContainerUi, textContainerUi;

    public WindfinderIcon(final RaceMapImageManager raceMapImageManager, final StringMessages stringMessages) {
        setElement(uiBinder.createAndBindUi(this));
        iconContainerUi.setInnerHTML(raceMapImageManager.getWindFinderLogo().getText());
        textContainerUi.setInnerHTML(stringMessages.windFinderWeatherData());
    }

    public void setHref(String href) {
        this.anchorUi.setHref(href);
    }

}
