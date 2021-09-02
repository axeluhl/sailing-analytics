package com.sap.sse.gwt.client.dialog;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * Interface for styling {@link TaggingPanel} and its content.
 */
public interface DialogResources extends ClientBundle {
    static final DialogResources INSTANCE = GWT.create(DialogResources.class);

    @Source("dialog.gss")
    DialogStyle style();

    public interface DialogStyle extends CssResource {
        String infoText();
        String confirmationDialog();
        String confirmationDialogPanel();
        String buttonsPanel();
        String dialogButton();
    }
}
