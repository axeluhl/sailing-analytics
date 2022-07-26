package com.sap.sailing.gwt.ui.datamining;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.common.client.help.HelpButtonResources;

public interface DataMiningHelpButtonResources extends HelpButtonResources {
    static HelpButtonResources INSTANCE = GWT.create(DataMiningHelpButtonResources.class);

    @Source("DataMiningHelpButton.gss")
    @Override
    Style style();
}
