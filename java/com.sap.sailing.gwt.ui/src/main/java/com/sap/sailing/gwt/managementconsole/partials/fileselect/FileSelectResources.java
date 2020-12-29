package com.sap.sailing.gwt.managementconsole.partials.fileselect;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.sap.sailing.gwt.managementconsole.resources.ManagementConsoleResources;
import com.sap.sse.security.ui.authentication.generic.resource.AuthenticationResources;

public interface FileSelectResources extends AuthenticationResources {

    FileSelectResources INSTANCE = GWT.create(FileSelectResources.class);

    @Source({ ManagementConsoleResources.COLORS, "FileSelect.gss" })
    Style style();

    public interface Style extends CssResource {

        @ClassName("file-select")
        String fileSelect();

        String filename();

        String control();
    }
}
