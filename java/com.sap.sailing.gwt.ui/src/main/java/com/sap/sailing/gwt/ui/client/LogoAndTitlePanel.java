package com.sap.sailing.gwt.ui.client;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class LogoAndTitlePanel extends FlowPanel {
    protected Label titleLabel; 
    protected Label subTitleLabel;

    public LogoAndTitlePanel(StringMessages stringConstants) {
        this(null, null, stringConstants);
    }

    public LogoAndTitlePanel(String title, StringMessages stringConstants) {
        this(title, null, stringConstants);
    }

    public LogoAndTitlePanel(String title, String subTitle, StringMessages stringConstants) {
        Anchor sapLogo = new Anchor(new SafeHtmlBuilder().appendHtmlConstant(
                "<img class=\"linkNoBorder\" src=\"/gwt/images/sap_66_transparent.png\"/>").toSafeHtml());
        sapLogo.setHref("http://www.sap.com");
        sapLogo.addStyleName("sapLogo");
        this.add(sapLogo);

        Anchor pioLogo = new Anchor(new SafeHtmlBuilder().appendHtmlConstant(
                "<img class=\"linkNoBorder\" src=\"/gwt/images/pio_logo_42.png\"/>").toSafeHtml());
        pioLogo.setHref("http://www.sap.com/services/portfolio/predictive-analytics");
        pioLogo.addStyleName("pioLogo");
        this.add(pioLogo);

        FlowPanel sailingAnalyticsLabelPanel = new FlowPanel();
        Label sailingAnalyticsLabel = new Label(stringConstants.sapSailingAnalytics());
        sailingAnalyticsLabelPanel.add(sailingAnalyticsLabel);
        sailingAnalyticsLabelPanel.addStyleName("sailingAnalyticsLabelPanel");
        sailingAnalyticsLabel.addStyleName("sailingAnalyticsLabel boldLabel");
        this.add(sailingAnalyticsLabelPanel);
        
        if (title != null) {
            FlowPanel subTitleLabelWrapper = new FlowPanel();
            subTitleLabelWrapper.addStyleName("titleLabelWrapper");
            titleLabel = new Label(title);
            titleLabel.addStyleName("titleLabel");
            subTitleLabelWrapper.add(titleLabel);
            this.add(subTitleLabelWrapper);
        }
        
        if (subTitle != null) {
            FlowPanel subTitleLabelWrapper = new FlowPanel();
            subTitleLabelWrapper.addStyleName("subTitleLabelWrapper");
            subTitleLabel = new Label(subTitle);
            subTitleLabel.addStyleName("subTitleLabel");
            subTitleLabelWrapper.add(subTitleLabel);
            this.add(subTitleLabelWrapper);
        }
    }
    
}
