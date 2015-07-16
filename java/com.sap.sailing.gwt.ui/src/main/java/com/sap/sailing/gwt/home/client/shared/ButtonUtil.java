package com.sap.sailing.gwt.home.client.shared;

import com.google.gwt.dom.client.Element;
import com.sap.sailing.gwt.common.client.SharedResources;

public final class ButtonUtil {
    private ButtonUtil() {
    }
    
    public static void applyButtonStyle(Element buttonElement, ButtonType buttonType) {
        buttonElement.removeClassName(SharedResources.INSTANCE.mainCss().buttonprimary());
        buttonElement.removeClassName(SharedResources.INSTANCE.mainCss().buttonred());
        if(buttonType == ButtonType.PRIMARY) {
            buttonElement.addClassName(SharedResources.INSTANCE.mainCss().buttonprimary());
        }
        if(buttonType == ButtonType.RED) {
            buttonElement.addClassName(SharedResources.INSTANCE.mainCss().buttonred());
        }
    }
}
