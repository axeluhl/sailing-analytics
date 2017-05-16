package com.sap.sailing.gwt.ui.client.shared.panels;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

public class SimpleWelcomeWidget extends WelcomeWidget {

    protected FlowPanel mainPanel;
    private Label welcomeHeader;
    private HTML welcomeText;

    /**
     * Creates a VerticalPanel as welcome widget with a header and a welcome message
     * @param headerText The text of the header component
     * @param welcomeText The text of the welcome message
     */
    public SimpleWelcomeWidget(String headerText, String welcomeText) {
        super();
        setStyleName(STYLE_NAME_PREFIX + "WelcomePanelWrapper");
        mainPanel = new FlowPanel();
        mainPanel.setStyleName(STYLE_NAME_PREFIX + "WelcomePanel");
        add(mainPanel);
        
        welcomeHeader = new Label(headerText);
        welcomeHeader.setStyleName(STYLE_NAME_PREFIX + "Header");
        mainPanel.add(welcomeHeader);
        this.welcomeText = new HTML(new SafeHtmlBuilder().appendEscapedLines(welcomeText).toSafeHtml());
        this.welcomeText.setStyleName(STYLE_NAME_PREFIX + "WelcomeText");
        mainPanel.add(this.welcomeText);
        SimplePanel clearAfter = new SimplePanel();
        clearAfter.setStyleName("clearafter");
        add(clearAfter);
    }

    @Override
    public void setWelcomeText(String welcomeText) {
        this.welcomeText = new HTML(new SafeHtmlBuilder().appendEscapedLines(welcomeText).toSafeHtml());
        this.welcomeText.setStyleName(STYLE_NAME_PREFIX + "WelcomeText");
    }

    @Override
    public void setWelcomeHeaderText(String headerText) {
        welcomeHeader.setText(headerText);
    }

}
