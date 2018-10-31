package com.sap.sailing.gwt.home.shared.partials.dialog;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.PopupPanel;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

/** factory class for confirm dialog with yes/no button */
public final class ConfirmDialogFactory {

    /**
     * creates a yes/no {@link ConfirmDialogPanel} with {@link #message}, which calls {@link DialogCallback#ok(Object)}
     * or {@link DialogCallback#cancel()}
     */
    public static void showConfirmDialog(final String message, final String title,
            final DialogCallback<Void> callback) {
        DialogResources.INSTANCE.css().ensureInjected();
        PopupPanel dialog = new PopupPanel();
        ConfirmDialogPanel confirmDialogPanel = new ConfirmDialogPanel(message, title, callback, dialog);
        dialog.setWidget(confirmDialogPanel);
        confirmDialogPanel.addStyleName(DialogResources.INSTANCE.css().dialog());
        dialog.addStyleName(DialogResources.INSTANCE.css().backgroundPanel());
        dialog.show();

        dialog.addDomHandler(e -> {
            dialog.hide();
            callback.cancel();
        }, ClickEvent.getType());
        dialog.sinkEvents(Event.ONCLICK | Event.ONTOUCHEND);
    }

}
