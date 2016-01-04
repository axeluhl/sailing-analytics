package com.sap.sse.gwt.client.shared.components;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.AbstractSettings;

public interface LazyComponent<SettingsType extends AbstractSettings> extends Component<SettingsType> {
    Widget createWidget();
}
