package com.sap.sailing.gwt.managementconsole.partials.inputs.listofstrings;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface ListOfStringsInputResources extends ClientBundle {

    ListOfStringsInputResources INSTANCE = GWT.create(ListOfStringsInputResources.class);

    @Source({ "ListOfStringsInput.gss" })
    Style style();

    public interface Style extends CssResource {
        @ClassName("delete-wrapper")
        String deleteWrapper();
        @ClassName("anchor-wrapper")
        String anchorWrapper();
        @ClassName("text-input-wrapper")
        String textInputWrapper();
        @ClassName("text-input")
        String textInput();
    }
}
