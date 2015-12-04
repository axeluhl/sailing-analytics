package com.sap.sailing.gwt.ui.client.shared.perspective;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.Component;

/**
 * A perspective is a composition of UI components into a view/page
 * The perspective itself is also an component and can have settings
 * @author c5163874
 *
 */
public interface Perspective<SettingsType extends Settings> extends Component<SettingsType>  {
    Iterable<Component<?>> getComponents();
    
    String getPerspectiveName();
}
