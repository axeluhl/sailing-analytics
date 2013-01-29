package com.sap.sailing.gwt.ui.shared.panels;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.client.URLFactory;

public class BreadcrumbPanel extends FlowPanel {

    interface AnchorTemplates extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<a href=\"{0}\">{1}</a>")
        SafeHtml anchor(String url, String displayName);
    }
    
    public static final AnchorTemplates ANCHORTEMPLATE = GWT.create(AnchorTemplates.class);
    public static final String STYLE_NAME_PREFIX = "breadcrumbPanel-";

    /**
     * Creates a new BreadcrumbPanel with the <code>breadcrumbLinks</code> as active links and
     * <code>actualBreadcrumb</code> as Label
     * 
     * @param breadcrumbLinksData
     *            The data of the breadcrumbs which will be displayed as links. Part A of the pair is the URL and part B
     *            the displayed text. The URL will be encoded here so that in particular parentheses are escaped
     *            so that they don't cause problems when the URLs are inserted unquoted in a JavaScript url(...)
     *            parameter.
     * @param actualBreadcrumbName
     *            The text as Label. Should be the name of the component in which the BreadcrumbPanel is displayed.
     */
    public BreadcrumbPanel(Iterable<Pair<String, String>> breadcrumbLinksData, String actualBreadcrumbName) {
        super();
        //Adding the active breadcrumbs
        for (Pair<String, String> breadcrumbData : breadcrumbLinksData) {
            HTML breadcrumb = new HTML(ANCHORTEMPLATE.anchor(URLFactory.INSTANCE.encode(breadcrumbData.getA()),
                    breadcrumbData.getB()));
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
        Label actualBreadcrumb = new Label(actualBreadcrumbName);
        actualBreadcrumb.addStyleName(STYLE_NAME_PREFIX + "InactiveBreadcrumb");
        actualBreadcrumb.getElement().getStyle().setProperty("clear", "right");
        actualBreadcrumb.getElement().getStyle().setPadding(5, Style.Unit.PX);
        add(actualBreadcrumb);
    }
    
}
