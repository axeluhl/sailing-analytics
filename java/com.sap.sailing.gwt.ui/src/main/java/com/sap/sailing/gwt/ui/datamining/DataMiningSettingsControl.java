package com.sap.sailing.gwt.ui.datamining;

import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.CompositeSettings;

public interface DataMiningSettingsControl extends Component<CompositeSettings> {

    void addSettingsComponent(Component<?> component);
    void removeSettingsComponent(Component<?> component);

}
