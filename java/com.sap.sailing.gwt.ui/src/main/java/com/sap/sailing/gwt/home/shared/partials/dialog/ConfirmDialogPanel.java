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
 * dialog panel for yes/no confirm dialog which contains a yes button, a no button and a message field with the message
 * in it.
 */
public class ConfirmDialogPanel extends Composite {

    private static ConfirmDialogPanelUiBinder uiBinder = GWT.create(ConfirmDialogPanelUiBinder.class);

    interface ConfirmDialogPanelUiBinder extends UiBinder<Widget, ConfirmDialogPanel> {
    }

    @UiField
    Button yesButton;

    @UiField
    Button noButton;

    @UiField
    DivElement messageField;

    private final DialogCallback<Void> callback;

    private final PopupPanel parent;

    public ConfirmDialogPanel() {
        this("", null, null);
    }

    public ConfirmDialogPanel(String message, DialogCallback<Void> callback, PopupPanel parent) {
        initWidget(uiBinder.createAndBindUi(this));
        this.callback = callback;
        this.parent = parent;
        messageField.setInnerText(message);
    }

    @UiHandler("yesButton")
    void onYesClick(ClickEvent e) {
        if (parent != null) {
            parent.hide();
            callback.ok(null);
        }
    }

    @UiHandler("noButton")
    void onNoClick(ClickEvent e) {
        if (parent != null) {
            parent.hide();
            callback.cancel();
        }
    }
}
