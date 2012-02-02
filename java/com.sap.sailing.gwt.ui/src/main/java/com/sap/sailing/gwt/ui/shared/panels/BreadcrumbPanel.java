package com.sap.sailing.gwt.ui.shared.panels;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

public class BreadcrumbPanel extends FlowPanel {
    
    public static final String STYLE_NAME_PREFIX = "breadcrumbPanel-";

    public BreadcrumbPanel(Iterable<HTML> breadcrumbLinks, HTML actualBreadcrumb) {
        super();
        //Adding the active breadcrumbs
        for (HTML breadcrumb : breadcrumbLinks) {
            breadcrumb.addStyleName(STYLE_NAME_PREFIX + "ActiveBreadcrumb");
            breadcrumb.getElement().getStyle().setFloat(Style.Float.LEFT);
            breadcrumb.getElement().getStyle().setPadding(5, Style.Unit.PX);
            add(breadcrumb);
            
            Label nextArrow = new Label(">");
            nextArrow.setStyleName(STYLE_NAME_PREFIX + "NextArrow");
            nextArrow.getElement().getStyle().setFloat(Style.Float.LEFT);
            nextArrow.getElement().getStyle().setPadding(5, Style.Unit.PX);
            add(nextArrow);
        }
        //Adding the actual breadcrumb
        actualBreadcrumb.addStyleName(STYLE_NAME_PREFIX + "InactiveBreadcrumb");
        actualBreadcrumb.getElement().getStyle().setFloat(Style.Float.LEFT);
        actualBreadcrumb.getElement().getStyle().setPadding(5, Style.Unit.PX);
        add(actualBreadcrumb);
    }
    
}
