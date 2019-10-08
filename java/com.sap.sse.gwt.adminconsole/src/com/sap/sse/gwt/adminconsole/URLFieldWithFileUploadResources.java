package com.sap.sse.gwt.adminconsole;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface URLFieldWithFileUploadResources extends ClientBundle {
    public static final URLFieldWithFileUploadResources INSTANCE = GWT.create(URLFieldWithFileUploadResources.class);

    interface URLFieldWithFileUploadStyle extends CssResource {
        /**
         * Inlines elements so that they move into the next line if space is tight.
         */
        @ClassName("inline")
        String inlineClass();

        @ClassName("spacing")
        String spaceDirectChildrenClass();
    }

    @Source("URLFieldWithFileUpload.gss")
    URLFieldWithFileUploadStyle urlFieldWithFileUploadStyle();
}
