package com.sap.sailing.gwt.ui.client;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class LogoAndTitlePanel extends FlowPanel {

    public LogoAndTitlePanel(StringConstants stringConstants) {
        Anchor sapLogo = new Anchor(new SafeHtmlBuilder().appendHtmlConstant(
                "<img class=\"linkNoBorder\" src=\"/images/sap_66_transparent.png\"/>").toSafeHtml());
        sapLogo.setHref("http://www.sap.com");
        sapLogo.addStyleName("sapLogo");
        this.add(sapLogo);
        
        Label sailingAnalyticsLabel = new Label(stringConstants.sapSailingAnalytics());
        FlowPanel labelPanel = new FlowPanel();
        labelPanel.add(sailingAnalyticsLabel);
        this.add(labelPanel);
        sailingAnalyticsLabel.addStyleName("sailingAnalyticsLabel boldLabel");
    }

}
