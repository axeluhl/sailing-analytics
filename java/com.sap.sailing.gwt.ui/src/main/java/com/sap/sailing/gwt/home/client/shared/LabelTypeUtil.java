package com.sap.sailing.gwt.home.client.shared;

import com.google.gwt.dom.client.Element;
import com.sap.sailing.gwt.ui.shared.general.LabelType;

public final class LabelTypeUtil {
    private LabelTypeUtil() {
    }

    public static void renderLabelType(Element labelElement, LabelType labelType) {
        if(labelType == null || labelType.getLabelType() == null || labelType.getLabel() == null) {
            labelElement.removeFromParent();
            return;
        }
        labelElement.setInnerText(labelType.getLabel());
        labelElement.setAttribute("data-labeltype", labelType.getLabelType());
    }
}
