package com.sap.sailing.gwt.home.shared.utils;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.sap.sailing.gwt.home.communication.event.LabelType;

public final class LabelTypeUtil {
    private LabelTypeUtil() {
    }

    public static void renderLabelType(Element labelElement, LabelType labelType) {
        if(labelType == null || !labelType.isRendered()) {
            labelElement.removeFromParent();
            return;
        }
        renderLabelTypeInternal(labelElement, labelType);
    }
    
    public static void renderLabelTypeOrHide(Element labelElement, LabelType labelType) {
        if(labelType == null || !labelType.isRendered()) {
            labelElement.getStyle().setDisplay(Display.NONE);
            return;
        }
        labelElement.getStyle().clearDisplay();
        renderLabelTypeInternal(labelElement, labelType);
    }
    
    private static void renderLabelTypeInternal(Element labelElement, LabelType labelType) {
        labelElement.setInnerText(labelType.getLabel());
        labelElement.setAttribute("data-labeltype", labelType.getLabelType());
    }
}
