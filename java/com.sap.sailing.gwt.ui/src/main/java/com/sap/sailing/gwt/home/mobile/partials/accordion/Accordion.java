package com.sap.sailing.gwt.home.mobile.partials.accordion;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class Accordion extends Composite {

    private static AccordionUiBinder uiBinder = GWT.create(AccordionUiBinder.class);

    interface AccordionUiBinder extends UiBinder<Widget, Accordion> {
    }
    
    @UiField
    SimplePanel headerUi;
    
    @UiField
    SimplePanel footerUi;
    
    @UiField
    FlowPanel itemsUi;

    public Accordion() {
        AccordionResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

    @UiChild
    public void addHeader(Widget headerWidget) {
        headerUi.setWidget(headerWidget);
        itemsUi.setStyleName(AccordionResources.INSTANCE.css().withHeader(), headerWidget != null);
    }
    
    @UiChild
    public void addFooter(Widget footerWidget) {
        footerUi.setWidget(footerWidget);
        itemsUi.setStyleName(AccordionResources.INSTANCE.css().withFooter(), footerWidget != null);
    }
    
    @UiChild
    public void addItem(Widget item) {
        itemsUi.add(item);
    }
}
