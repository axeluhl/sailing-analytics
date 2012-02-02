package com.sap.sailing.gwt.ui.shared.components;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class SimpleWelcomeWidget extends VerticalPanel implements WelcomeWidget {

    private StringMessages stringConstants;
    private Label welcomeHeader;
    private HTML welcomeText;

    public SimpleWelcomeWidget(String headerText, String welcomeText, HorizontalAlignmentConstant horizontalAlignment, StringMessages stringConstants) {
        super();
        this.stringConstants = stringConstants;
        
        setHorizontalAlignment(horizontalAlignment);
        welcomeHeader = new Label(this.stringConstants.welcomeToSailingAnalytics());
        add(welcomeHeader);
        this.welcomeText = new HTML(new SafeHtmlBuilder().appendEscapedLines(welcomeText).toSafeHtml());
        add(this.welcomeText);
    }

    @Override
    public void setWelcomeText(String welcomeText) {
        this.welcomeText = new HTML(new SafeHtmlBuilder().appendEscapedLines(welcomeText).toSafeHtml());
    }

    @Override
    public void setWelcomeHeaderText(String headerText) {
        welcomeHeader.setText(headerText);
    }

}
