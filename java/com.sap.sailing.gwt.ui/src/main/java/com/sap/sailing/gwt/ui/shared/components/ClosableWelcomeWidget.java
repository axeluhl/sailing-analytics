package com.sap.sailing.gwt.ui.shared.components;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class ClosableWelcomeWidget extends VerticalPanel implements WelcomeWidget {

    private StringMessages stringConstants;
    private Button closeButton;
    private Label welcomeHeader;
    private HTML welcomeText;
    private boolean closable;

    public ClosableWelcomeWidget(boolean closable, HorizontalAlignmentConstant horizontalAlignment, StringMessages stringConstants) {
        this(closable, "", horizontalAlignment, stringConstants);
    }
    
    public ClosableWelcomeWidget(boolean closable, String welcomeText, HorizontalAlignmentConstant horizontalAlignment,
            StringMessages stringConstants) {
        super();
        this.stringConstants = stringConstants;
        this.closable = closable;
        
        closeButton = new Button(stringConstants.close());
        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ClosableWelcomeWidget.this.setVisible(false);
            }
        });
        closeButton.setVisible(this.closable);
        setHorizontalAlignment(ALIGN_RIGHT);
        add(closeButton);
        
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
    
    public boolean isClosable() {
        return closable;
    }
    
    public void setIsClosable(boolean isClosable) {
        closable = isClosable;
        closeButton.setVisible(closable);
    }

}
