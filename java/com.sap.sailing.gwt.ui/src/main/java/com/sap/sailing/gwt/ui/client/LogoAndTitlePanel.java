package com.sap.sailing.gwt.ui.client;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class LogoAndTitlePanel extends FlowPanel {

    protected final Label titleLabel; 
    public LogoAndTitlePanel(String title, StringMessages stringConstants) {
        Anchor sapLogo = new Anchor(new SafeHtmlBuilder().appendHtmlConstant(
                "<img class=\"linkNoBorder\" src=\"/gwt/images/sap_66_transparent.png\"/>").toSafeHtml());
        sapLogo.setHref("http://www.sap.com");
        sapLogo.addStyleName("sapLogo");
        this.add(sapLogo);
        
        FlowPanel labelPanel = new FlowPanel();
        Label sailingAnalyticsLabel = new Label(stringConstants.sapSailingAnalytics());
        labelPanel.add(sailingAnalyticsLabel);
        labelPanel.addStyleName("sailingAnalyticsLabelPanel");
        sailingAnalyticsLabel.addStyleName("sailingAnalyticsLabel boldLabel");
        this.add(labelPanel);
        
        titleLabel = new Label(title);
        titleLabel.addStyleName("titleLabel");
        this.add(titleLabel);
    }

}
