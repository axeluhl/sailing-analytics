package com.sap.sse.gwt.client.shared.components;

import com.sap.sse.common.settings.Settings;

public interface ComponentConstructorArgs<C extends Component<S>, S extends Settings> {
    C createComponent(S settings);
}
