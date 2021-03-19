package com.sap.sailing.gwt.ui.client.media;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface NewMediaDialogResources extends ClientBundle {
    public static final NewMediaDialogResources INSTANCE = GWT.create(NewMediaDialogResources.class);

    interface NewMediaDialogStyle extends CssResource {
        @ClassName("textfield-size")
        String textfieldSizeClass();

        @ClassName("datepicker-styling")
        String datePickerClass();
        
        @ClassName("mobile-augmentation")
        String mobileAugmentations();
    }

    @Source("NewMediaDialogResources.gss")
    NewMediaDialogStyle css();
}
