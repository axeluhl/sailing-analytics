package com.sap.sailing.domain.tractracadapter;

import com.maptrack.client.io.TypeController;

public interface Receiver {
    void stop();
    Iterable<TypeController> getTypeControllers();
}
