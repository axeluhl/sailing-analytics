package com.sap.sse.gwt.theme.client.showcase;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class ComponentShowCase extends Composite {

    private static ComponentShowCaseUiBinder uiBinder = GWT.create(ComponentShowCaseUiBinder.class);

    interface ComponentShowCaseUiBinder extends UiBinder<Widget, ComponentShowCase> {
    }

    @UiField 
    DivElement componentNameUi;

    @UiField
    SimplePanel componentDescriptionUi;
    
    @UiField
    SimplePanel componentPanelUi;

    private String name;

    public ComponentShowCase() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void setName(String name) {
        this.name = name;
        componentNameUi.setInnerText(name);
    }

    @UiChild
    public void addDescription(Widget description) {
        componentDescriptionUi.setWidget(description);
    }

    @UiChild
    public void addComponent(Widget w) {
        componentPanelUi.setWidget(w);
    }

    public String getName() {
        return this.name;
    }

}
