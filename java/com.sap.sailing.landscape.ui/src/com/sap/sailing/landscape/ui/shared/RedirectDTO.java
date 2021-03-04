package com.sap.sailing.landscape.ui.shared;

import java.util.Optional;

import com.google.gwt.user.client.rpc.IsSerializable;

public interface RedirectDTO extends IsSerializable {
    String getPath();

    default Optional<String> getQuery() {
        return Optional.empty();
    }
}
