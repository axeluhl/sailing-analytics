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

    public TwoOptionsDialogPanel() {
        this("", "", null, null);
    }

    public TwoOptionsDialogPanel(String message, String title, DialogCallback<Void> callback, PopupPanel parent) {
        initWidget(uiBinder.createAndBindUi(this));
        this.callback = callback;
        this.parent = parent;
        messageField.setInnerText(message);
        titleField.setInnerText(title);
    }

    public void setButtonLabels(String firstButtonText, String secondButtonText) {
        firstButton.setText(firstButtonText);
        secondButton.setText(secondButtonText);
    }

    public void setFirstButtonDestructive() {
        firstButton.addStyleName(DialogResources.INSTANCE.css().yesButton());
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
