package com.sap.sailing.gwt.ui.client;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class LogoAndTitlePanel extends FlowPanel {
    protected Label titleLabel; 
    protected Label subTitleLabel;

    public LogoAndTitlePanel(StringMessages stringConstants, boolean isSmallWidth) {
        this(null, null, stringConstants, isSmallWidth);
    }

    public LogoAndTitlePanel(String title, StringMessages stringConstants, boolean isSmallWidth) {
        this(title, null, stringConstants, isSmallWidth);
    }

    public LogoAndTitlePanel(String title, String subTitle, StringMessages stringConstants, boolean isSmallWidth) {
        Anchor sapLogo = new Anchor(new SafeHtmlBuilder().appendHtmlConstant(
                "<img class=\"linkNoBorder\" src=\"/gwt/images/sap_66_transparent.png\"/>").toSafeHtml());
        sapLogo.setHref("http://www.sap.com");
        sapLogo.addStyleName("sapLogo");
        this.add(sapLogo);
        if (!isSmallWidth) {
            FlowPanel sailingAnalyticsLabelPanel = new FlowPanel();
            Label sailingAnalyticsLabel = new Label(stringConstants.sapSailingAnalytics());
            sailingAnalyticsLabelPanel.add(sailingAnalyticsLabel);
            sailingAnalyticsLabelPanel.addStyleName("sailingAnalyticsLabelPanel");
            sailingAnalyticsLabel.addStyleName("sailingAnalyticsLabel boldLabel");
            this.add(sailingAnalyticsLabelPanel);
        }
        if (!isSmallWidth && title != null) {
            FlowPanel titleLabelWrapper = new FlowPanel();
            titleLabelWrapper.addStyleName("titleLabelWrapper");
            titleLabel = new Label(title);
            titleLabel.addStyleName("titleLabel");
            titleLabelWrapper.add(titleLabel);
            this.add(titleLabelWrapper);
        }
        if (subTitle != null) {
            subTitleLabel = new Label(subTitle);
            FlowPanel subTitleLabelWrapper = new FlowPanel();
            if (isSmallWidth) {
                subTitleLabelWrapper.addStyleName("titleLabelWrapper");
                subTitleLabel.addStyleName("titleLabelRight");
            } else {
                subTitleLabelWrapper.addStyleName("subTitleLabelWrapper");
                subTitleLabel.addStyleName("subTitleLabel");
            }
            subTitleLabelWrapper.add(subTitleLabel);
            this.add(subTitleLabelWrapper);
        }
    }

    public String getTitle() {
        return titleLabel != null ? titleLabel.getText() : null;
    }

    public void setTitle(String title) {
        if(titleLabel != null) {
            titleLabel.setText(title);
        }
    }

    public String getSubTitle() {
        return subTitleLabel != null ? subTitleLabel.getText() : null;
    }

    public void setSubTitle(String subTitle) {
        if(subTitleLabel != null) {
            subTitleLabel.setText(subTitle);
        }
    }   
}
