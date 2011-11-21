package com.sap.sailing.gwt.ui.client;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class LogoAndTitlePanel extends HorizontalPanel {

    public LogoAndTitlePanel(StringConstants stringConstants) {
        Anchor sapLogo = new Anchor(new SafeHtmlBuilder().appendHtmlConstant(
                "<img class=\"linkNoBorder\" src=\"/images/sap_66_transparent.png\"/>").toSafeHtml());
        sapLogo.setHref("http://www.sap.com");
        this.add(sapLogo);
        Label sailingAnalyticsLabel = new Label(stringConstants.sapSailingAnalytics());
        HorizontalPanel labelPanel = new HorizontalPanel();
        labelPanel.add(sailingAnalyticsLabel);
        labelPanel.setSpacing(10);
        this.add(labelPanel);
        sailingAnalyticsLabel.addStyleName("boldLabel");
    }

}
