package com.sap.sailing.gwt.home.shared.partials.dialog;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.PopupPanel;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

/** Factory class to create generic dialogs. */
public final class DialogFactory {

    /** Create a generic dialog with a title, a message and two buttons. */
    public static PopupPanel createDialog(final String message, final String title,
            final boolean firstActionDestructive, final String firstButtonName, final String secondButtonName,
            final DialogCallback<Void> callback) {

        DialogResources.INSTANCE.css().ensureInjected();
        PopupPanel dialog = new PopupPanel();

        // create the dialog panel
        TwoOptionsDialogPanel dialogPanel = new TwoOptionsDialogPanel(message, title, callback, dialog);
        dialogPanel.addStyleName(DialogResources.INSTANCE.css().dialog());
        dialogPanel.setButtonLabels(firstButtonName, secondButtonName);
        if (firstActionDestructive) {
            dialogPanel.setFirstButtonDestructive();
        }

        dialog.setWidget(dialogPanel);
        dialog.addStyleName(DialogResources.INSTANCE.css().backgroundPanel());

        // close the dialog if the user clicks outside it
        dialog.addDomHandler(e -> {
            dialog.hide();
            callback.cancel();
        }, ClickEvent.getType());
        dialog.sinkEvents(Event.ONCLICK | Event.ONTOUCHEND);
        return dialog;
    }

}
