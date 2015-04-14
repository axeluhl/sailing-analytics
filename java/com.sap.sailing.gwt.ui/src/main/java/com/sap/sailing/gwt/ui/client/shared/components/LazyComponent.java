package com.sap.sailing.gwt.ui.client.shared.components;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.settings.Settings;

public interface LazyComponent<SettingsType extends Settings> extends Component<SettingsType> {
    Widget createWidget();
}
