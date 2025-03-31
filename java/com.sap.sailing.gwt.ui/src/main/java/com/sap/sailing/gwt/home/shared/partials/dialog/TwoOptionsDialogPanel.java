package com.sap.sailing.gwt.home.shared.partials.dialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

/**
 * Dialog panel for yes/no confirm dialog which contains a yes button, a no button and a message field with the message
 * in it.
 */
public class TwoOptionsDialogPanel extends Composite {

    private static ConfirmDialogPanelUiBinder uiBinder = GWT.create(ConfirmDialogPanelUiBinder.class);

    interface ConfirmDialogPanelUiBinder extends UiBinder<Widget, TwoOptionsDialogPanel> {
    }

    @UiField
    Button firstButton;

    @UiField
    Button secondButton;

    @UiField
    DivElement messageField;

    @UiField
    DivElement titleField;

    private final DialogCallback<Void> callback;
    private final PopupPanel parent;

    TwoOptionsDialogPanel(String message, String title, DialogCallback<Void> callback, PopupPanel parent) {
        initWidget(uiBinder.createAndBindUi(this));
        this.callback = callback;
        this.parent = parent;
        this.messageField.setInnerText(message);
        this.titleField.setInnerText(title);
    }

    /** Sets the labels of the {@link #firstButton} and {@link #secondButton}. */
    void setButtonLabels(String firstButtonText, String secondButtonText) {
        firstButton.setText(firstButtonText);
        secondButton.setText(secondButtonText);
    }

    /** Changes the color of the {@link #firstButton} to red. */
    void setFirstButtonDestructive() {
        firstButton.addStyleName(DialogResources.INSTANCE.css().destructiveButton());
    }

    @UiHandler("firstButton")
    void onYesClick(ClickEvent e) {
        if (parent != null) {
            parent.hide();
            callback.ok(null);
        }
    }

    @UiHandler("secondButton")
    void onNoClick(ClickEvent e) {
        if (parent != null) {
            parent.hide();
            callback.cancel();
        }
    }
}
