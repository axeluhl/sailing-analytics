package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.core.client.GWT;
import com.sap.sse.gwt.client.controls.dropdown.DropdownResources;

public interface RaceboardDropdownResources extends DropdownResources {
    static final RaceboardDropdownResources INSTANCE = GWT.create(RaceboardDropdownResources.class);

    @Source("RaceboardDropdown.gss")
    @Override
    LocalCss css();
    
    public interface LocalCss extends DropdownResources.LocalCss {
        String compactHeader();
    }
}
