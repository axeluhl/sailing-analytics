package com.sap.sailing.gwt.home.mobile.partials.simpleinfoblock;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;

public class SimpleInfoBlock extends Composite {
    private static SimpleInfoBlockUiBinder uiBinder = GWT.create(SimpleInfoBlockUiBinder.class);

    interface SimpleInfoBlockUiBinder extends UiBinder<Widget, SimpleInfoBlock> {
    }

    @UiField
    protected DivElement descriptionUi;
    @UiField
    protected AnchorElement actionUi;

    public SimpleInfoBlock() {
        initWidget(uiBinder.createAndBindUi(this));
        getElement().getStyle().setDisplay(Display.NONE);
    }

    public void setDescription(SafeHtml description) {
        descriptionUi.getStyle().setDisplay(Display.BLOCK);
        getElement().getStyle().clearDisplay();
        descriptionUi.setInnerSafeHtml(description);
    }

    public void setAction(String label, String url) {
        actionUi.getStyle().setDisplay(Display.BLOCK);
        getElement().getStyle().clearDisplay();
        actionUi.setInnerText(label);
        actionUi.setHref(UriUtils.fromString(url));
    }
    
    public void setAction(String label, PlaceNavigation<?> placeNavigation) {
        actionUi.getStyle().setDisplay(Display.BLOCK);
        getElement().getStyle().clearDisplay();
        actionUi.setInnerText(label);
        placeNavigation.configureAnchorElement(actionUi);
    }

}
