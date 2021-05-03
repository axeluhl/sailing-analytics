package com.sap.sailing.gwt.ui.client.media;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.DataResource.MimeType;

public interface NewMediaDialogResources extends ClientBundle {
    public static final NewMediaDialogResources INSTANCE = GWT.create(NewMediaDialogResources.class);

    interface NewMediaDialogStyle extends CssResource {
        @ClassName("textfield-size")
        String textfieldSizeClass();

        @ClassName("datepicker-styling")
        String datePickerClass();

        @ClassName("button-size")
        String buttonSizeClass();

        @ClassName("field-group")
        String fieldGroup();
        
        @ClassName("resetButton")
        String resetButtonClass();
        
        @ClassName("startTime-textbox")
        String startTimeTextboxClass();
    }

    @Source("NewMediaDialogResources.gss")
    NewMediaDialogStyle css();

    @Source("refresh.svg")
    @MimeType("image/svg+xml")
    DataResource reset();
}
