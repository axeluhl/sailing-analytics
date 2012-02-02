package com.sap.sailing.gwt.ui.shared.components;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class ClosableWelcomeWidget extends WelcomeWidget {

    private StringMessages stringConstants;
    private boolean closable;
    private FormPanel mainPanel;
    
    private Button closeButton;
    private Label welcomeHeader;
    private HTML welcomeText;
    
    /**
     * Creates a new closable welcome widget. It has the same components as the {@link SimpleWelcomeWidget} and a Button
     * at the top right corner to close the welcome widget (it sets the whole component visible = <code>false</code>).
     * 
     * @param closable Sets the visibility of the close Button
     * @param headerText The text of the header component
     * @param welcomeText The text of the component under the header (welcome component)
     */
    public ClosableWelcomeWidget(boolean closable, String headerText, String welcomeText, StringMessages stringConstants) {
        super();
        this.stringConstants = stringConstants;
        this.closable = closable;
        
        mainPanel = new FormPanel();
        mainPanel.setStyleName(STYLE_NAME_PREFIX + "WelcomePanel");
        add(mainPanel);
        
        closeButton = new Button(this.stringConstants.close());
        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ClosableWelcomeWidget.this.setVisible(false);
            }
        });
        closeButton.setVisible(this.closable);
        closeButton.setStyleName(STYLE_NAME_PREFIX + "CloseButton");
        mainPanel.add(closeButton);
        
        welcomeHeader = new Label(headerText);
        welcomeHeader.setStyleName(STYLE_NAME_PREFIX + "Header");
        mainPanel.add(welcomeHeader);
        this.welcomeText = new HTML(new SafeHtmlBuilder().appendEscapedLines(welcomeText).toSafeHtml());
        this.welcomeText.setStyleName(STYLE_NAME_PREFIX + "WelcomeText");
        mainPanel.add(this.welcomeText);
    }
    
    /**
     * @return <code>true</code> if the welcome widget is closable (close button is visible) right now, else returns <code>false</code>
     */
    public boolean isClosable() {
        return closable;
    }
    
    /**
     * Sets the visibility of the close button and consequently the ability of the welcome widget to close itself.
     */
    public void setIsClosable(boolean isClosable) {
        closable = isClosable;
        closeButton.setVisible(closable);
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
