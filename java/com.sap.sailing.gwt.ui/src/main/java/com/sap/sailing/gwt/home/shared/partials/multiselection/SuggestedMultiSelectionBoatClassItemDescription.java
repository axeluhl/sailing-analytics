package com.sap.sailing.gwt.home.shared.partials.multiselection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.gwt.common.client.BoatClassImageResolver;

public class SuggestedMultiSelectionBoatClassItemDescription extends Widget {

    private static LocalUiBinder uiBinder = GWT.create(LocalUiBinder.class);

    interface LocalUiBinder extends UiBinder<Element, SuggestedMultiSelectionBoatClassItemDescription> {
    }
    
    @UiField DivElement imageUi;
    @UiField SpanElement nameUi;
    
    public SuggestedMultiSelectionBoatClassItemDescription(BoatClassDTO boatClass) {
        setElement(uiBinder.createAndBindUi(this));
        imageUi.getStyle().setBackgroundImage("url('" + BoatClassImageResolver.getBoatClassIconResource(
                boatClass.getName()).getSafeUri().asString() + "')");
        nameUi.setInnerText(boatClass.getName());
    }
    
}
