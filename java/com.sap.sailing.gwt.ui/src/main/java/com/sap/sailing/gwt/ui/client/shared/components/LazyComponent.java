package com.sap.sailing.gwt.ui.client.shared.components;

import com.google.gwt.user.client.ui.Widget;

public interface LazyComponent<SettingsType> extends Component<SettingsType> {
    Widget createWidget();
}
