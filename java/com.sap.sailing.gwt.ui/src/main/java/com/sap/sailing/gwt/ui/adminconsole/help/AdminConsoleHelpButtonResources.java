package com.sap.sailing.gwt.ui.adminconsole.help;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.sap.sailing.gwt.common.client.help.HelpButtonResources;

public interface AdminConsoleHelpButtonResources extends HelpButtonResources, ClientBundle {

    static HelpButtonResources INSTANCE = GWT.create(AdminConsoleHelpButtonResources.class);

    @Override
    @Source("help.png")
    ImageResource icon();

    @Override
    @Source("help.gss")
    Style style();

}
