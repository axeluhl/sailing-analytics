package com.sap.sailing.gwt.ui.client.shared.charts;

import java.util.function.Consumer;

public interface HasAvailabilityCheck {

    void checkBackendAvailability(Consumer<Boolean> callback);
}
