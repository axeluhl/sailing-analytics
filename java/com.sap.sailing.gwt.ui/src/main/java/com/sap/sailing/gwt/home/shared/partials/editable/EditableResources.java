package com.sap.sailing.gwt.home.shared.partials.editable;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public interface EditableResources extends ClientBundle {

    public static final EditableResources INSTANCE = GWT.create(EditableResources.class);

    @Source("com/sap/sailing/gwt/ui/client/images/editable/edit_pencil.png")
    ImageResource editPencil();

    @Source("com/sap/sailing/gwt/ui/client/images/editable/save.png")
    ImageResource save();

    @Source("EditableCss.gss")
    EditableCss css();

    public interface EditableCss extends CssResource {
        String inlineEditButtonStyle();

        String inlineEditLabel();

        String inlineElement();

        String editableSuggestionEditButton();

        String listItem();

        String listItemBorder();

        String listItemOffset();
    }
}
