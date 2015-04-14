package com.sap.sse.gwt.client.shared.components;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.Settings;

public interface LazyComponent<SettingsType extends Settings> extends Component<SettingsType> {
    Widget createWidget();
}
