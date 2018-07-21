package com.sap.sailing.gwt.home.shared.partials.bubble;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * {@link Widget} to be as content in a {@link Bubble} showing the given boat class information.
 */
public class BubbleContentBoatClass extends Composite {

    private static BubbleContentBoatClassUiBinder uiBinder = GWT.create(BubbleContentBoatClassUiBinder.class);

    interface BubbleContentBoatClassUiBinder extends UiBinder<Widget, BubbleContentBoatClass> {
    }

    @UiField
    DivElement boatClassNameUi;

    /**
     * Creates a new {@link BubbleContentBoatClass} instance showing the given boat class name.
     * 
     * @param boatClassName
     */
    public BubbleContentBoatClass(String boatClassName) {
        initWidget(uiBinder.createAndBindUi(this));
        boatClassNameUi.setInnerText(boatClassName);
    }
}
