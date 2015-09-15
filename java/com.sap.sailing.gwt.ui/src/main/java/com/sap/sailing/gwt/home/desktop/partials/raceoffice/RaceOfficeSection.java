package com.sap.sailing.gwt.home.desktop.partials.raceoffice;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;

public class RaceOfficeSection extends Widget {

    private static RaceOfficeSectionUiBinder uiBinder = GWT.create(RaceOfficeSectionUiBinder.class);

    interface RaceOfficeSectionUiBinder extends UiBinder<Element, RaceOfficeSection> {
    }
    
    @UiField RaceOfficeSectionResources local_res;
    @UiField DivElement linkContainerUi;

    public RaceOfficeSection() {
        setElement(uiBinder.createAndBindUi(this));
        local_res.css().ensureInjected();
    }
    
    public void addLink(String text, String url) {
        AnchorElement anchor = DOM.createAnchor().cast();
        anchor.setInnerText(text);
        anchor.setTarget("_blank");
        anchor.setHref(url);
        anchor.addClassName(local_res.css().raceoffice_link());
        linkContainerUi.appendChild(anchor);
    }

}
