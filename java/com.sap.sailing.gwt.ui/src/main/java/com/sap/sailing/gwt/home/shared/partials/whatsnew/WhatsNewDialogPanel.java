package com.sap.sailing.gwt.home.shared.partials.whatsnew;

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
public class WhatsNewDialogPanel extends Composite {

    private static ConfirmDialogPanelUiBinder uiBinder = GWT.create(ConfirmDialogPanelUiBinder.class);

    interface ConfirmDialogPanelUiBinder extends UiBinder<Widget, WhatsNewDialogPanel> {
    }

    @UiField
    Button showChangelogButtonUi;

    @UiField
    Button cancelButtonUi;

    @UiField
    DivElement messageField;

    private final DialogCallback<Void> callback;

    private final PopupPanel parent;

    public WhatsNewDialogPanel() {
        this("", null, null);
    }

    public WhatsNewDialogPanel(String message, DialogCallback<Void> callback, PopupPanel parent) {
        initWidget(uiBinder.createAndBindUi(this));
        this.callback = callback;
        this.parent = parent;
        messageField.setInnerText(message);
    }

    @UiHandler("showChangelogButtonUi")
    void onYesClick(ClickEvent e) {
        if (parent != null) {
            parent.hide();
            callback.ok(null);
        }
    }

    @UiHandler("cancelButtonUi")
    void onNoClick(ClickEvent e) {
        if (parent != null) {
            parent.hide();
            callback.cancel();
        }
    }
}
