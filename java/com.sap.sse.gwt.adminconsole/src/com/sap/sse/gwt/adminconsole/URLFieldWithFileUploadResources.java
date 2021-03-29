package com.sap.sse.gwt.adminconsole;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.DataResource.MimeType;

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
        
        @ClassName("file-input-class")
        String fileInputClass();
        
        @ClassName("uploadButton")
        String uploadButtonClass();

        @ClassName("uploadButton-loading")
        String uploadButtonLoadingClass();
        
        @ClassName("loading")
        String loadingClass();
        
        @ClassName("url-textbox")
        String urlTextboxClass();
    }

    @Source("URLFieldWithFileUpload.gss")
    URLFieldWithFileUploadStyle urlFieldWithFileUploadStyle();

    @Source("folder_white.svg")
    @MimeType("image/svg+xml")
    DataResource folderWhite();

    @Source("busy_indicator_circle.gif")
    @MimeType("image/gif")
    DataResource busyIndicator();
}
