package com.sap.sailing.gwt.ui.shared.components;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SimpleWelcomeWidget extends VerticalPanel implements WelcomeWidget {

    private Label welcomeHeader;
    private HTML welcomeText;

    /**
     * Creates a VerticalPanel as welcome widget with a header and a welcome message
     * @param headerText The text of the header component
     * @param welcomeText The text of the welcome message
     * @param horizontalAlignment The alignment of the components in the VerticalPanel
     */
    public SimpleWelcomeWidget(String headerText, String welcomeText, HorizontalAlignmentConstant horizontalAlignment) {
        super();
        setHorizontalAlignment(horizontalAlignment);
        welcomeHeader = new Label(headerText);
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
