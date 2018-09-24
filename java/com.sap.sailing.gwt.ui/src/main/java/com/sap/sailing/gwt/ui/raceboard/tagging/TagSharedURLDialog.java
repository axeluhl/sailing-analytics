package com.sap.sailing.gwt.ui.raceboard.tagging;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.raceboard.tagging.TagPanelResources.TagPanelStyle;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;

/**
 * A dialog which is used to show the link to share a tag.
 */
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
    public TagSharedURLDialog(StringMessages stringMessages, String url) {
        Panel mainPanel = new FlowPanel();

        Label descriptionLabel = new Label();
        descriptionLabel.setText(stringMessages.tagSharedURL());
        descriptionLabel.setStyleName(style.tagSharedURLLabel());
        descriptionLabel.addStyleName("gwt-Label");

        TextBox urlTextBox = new TextBox();
        urlTextBox.setText(url);
        urlTextBox.setStyleName(style.tagSharedURLTextBox());
        urlTextBox.addStyleName("gwt-TextBox");
        urlTextBox.setReadOnly(true);

        Panel buttonPanel = new FlowPanel();
        buttonPanel.setStyleName(style.buttonsPanel());

        Button copyToClipBoardButton = new Button(stringMessages.tagCopyToClipBoard());
        copyToClipBoardButton.setStyleName(style.tagDialogButton());
        copyToClipBoardButton.addStyleName("gwt-Button");
        copyToClipBoardButton.addClickHandler(event -> {
            // select text to copy
            urlTextBox.setFocus(true);
            urlTextBox.selectAll();
            if (copyToClipboard()) {
                Notification.notify(stringMessages.tagCopiedLinkSuccessfully(), NotificationType.SUCCESS);
            } else {
                Notification.notify(stringMessages.tagCopiedLinkNotSuccessfully(), NotificationType.WARNING);
            }
        });

        Button closeButton = new Button(stringMessages.close());
        closeButton.setStyleName(style.tagDialogButton());
        closeButton.addStyleName("gwt-Button");
        closeButton.addClickHandler(event -> {
            hide();
        });
        buttonPanel.add(copyToClipBoardButton);
        buttonPanel.add(closeButton);

        mainPanel.add(descriptionLabel);
        mainPanel.add(urlTextBox);
        mainPanel.add(buttonPanel);

        setGlassEnabled(true);
        setText(stringMessages.tagSharedDialog());
        addStyleName(style.tagSharedURLDialog());
        setWidget(mainPanel);
        center();
        urlTextBox.selectAll();
        urlTextBox.setFocus(true);
    }

    /**
     * GWT does not support copying text to clipboard so native java script was added
     */
    private static native boolean copyToClipboard() /*-{
        return $doc.execCommand('copy');
    }-*/;
}