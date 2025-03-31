package com.sap.sailing.gwt.home.shared.partials.dialog;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface DialogResources extends ClientBundle {

    public static final DialogResources INSTANCE = GWT.create(DialogResources.class);

    @Source("Dialog.gss")
    DialogCss css();

    public interface DialogCss extends CssResource {

        String dialog();

        String message();

        String destructiveButton();

        String buttonPanel();

        String backgroundPanel();

        String title();

        String buttonAdjustments();
    }
}
