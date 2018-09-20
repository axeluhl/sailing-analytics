package com.sap.sailing.gwt.ui.raceboard.tagging;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.raceboard.tagging.TagPanelResources.TagPanelStyle;

/**
 * A Dialog to show the URL of shared tags
 */
// TODO: adapt message instead of "URL:"
// TODO: preselect link
// TODO: add copy button for clipboard
// TODO: texfield should not be editable
public class TagSharedURLDialog extends DialogBox {

    private final TagPanelResources resources = TagPanelResources.INSTANCE;
    private final TagPanelStyle style = resources.style();

    /**
     * Creates a Dialog which shows the user the URL of the tag he wants to share
     * 
     * @param taggingPanel
     *            {@link TaggingPanel} which creates this {@link TagSharedURLDialog}.
     * @param url
     *            A URL which if called opens the {@link TaggingPanel} and selects the shared tag.
     */
    public TagSharedURLDialog(TaggingPanel taggingPanel, String url) {
        StringMessages stringMessages = taggingPanel.getStringMessages();

        Panel mainPanel = new FlowPanel();

        Label descriptionLabel = new Label();
        descriptionLabel.setText(stringMessages.tagSharedURL());
        descriptionLabel.setStyleName(style.tagSharedURLLabel());
        descriptionLabel.addStyleName("gwt-Label");

        TextBox urlTextBox = new TextBox();
        urlTextBox.setText(url);
        urlTextBox.setStyleName(style.tagSharedURLTextBox());
        urlTextBox.addStyleName("gwt-TextBox");

        Panel buttonPanel = new FlowPanel();
        buttonPanel.setStyleName(style.buttonsPanel());

        Button closeButton = new Button(stringMessages.close());
        closeButton.setStyleName(style.tagDialogButton());
        closeButton.addStyleName("gwt-Button");
        closeButton.addClickHandler(event -> {
            hide();
        });
        buttonPanel.add(closeButton);

        mainPanel.add(descriptionLabel);
        mainPanel.add(urlTextBox);
        mainPanel.add(buttonPanel);

        setGlassEnabled(true);
        setText(stringMessages.tagSharedDialog());
        addStyleName(style.tagSharedURLDialog());
        setWidget(mainPanel);
        center();
    }

}
