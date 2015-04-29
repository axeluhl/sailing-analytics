package com.sap.sse.gwt.client.panels;

import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.user.client.ui.FlowPanel;

public class HorizontalFlowPanel extends FlowPanel {
    
    public HorizontalFlowPanel() {
        super();
        getElement().getStyle().setFloat(Float.LEFT);
    }
    
    public HorizontalFlowPanel(String tag) {
        super(tag);
        getElement().getStyle().setFloat(Float.LEFT);
    }

}
