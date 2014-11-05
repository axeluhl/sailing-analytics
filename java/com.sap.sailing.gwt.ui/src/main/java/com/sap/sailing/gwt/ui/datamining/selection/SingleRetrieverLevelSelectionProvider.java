package com.sap.sailing.gwt.ui.datamining.selection;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.datamining.shared.impl.dto.LocalizedTypeDTO;

public class SingleRetrieverLevelSelectionProvider extends HorizontalPanel {

    private final LocalizedTypeDTO retrievedDataType;

    public SingleRetrieverLevelSelectionProvider(LocalizedTypeDTO retrievedDataType, StringMessages stringMessages) {
        this.retrievedDataType = retrievedDataType;
        
        add(new Label(retrievedDataType.getDisplayName()));
    }

}
