package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.components.Component;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialogComponent;

public class SimpleComponent implements Component<SimpleComponentSettings> {

    private String componentName;
    
    private final Label label;
    public SimpleComponent(String componentName) {
        this.componentName = componentName;
        
        label = new Label("Ich bin eine Label component");
    }
    
    @Override
    public String getLocalizedShortName() {
        return componentName;
    }

    @Override
    public Widget getEntryWidget() {
        return label;
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public SettingsDialogComponent<SimpleComponentSettings> getSettingsDialogComponent() {
        return null;
    }

    @Override
    public void updateSettings(SimpleComponentSettings newSettings) {
    }

}
