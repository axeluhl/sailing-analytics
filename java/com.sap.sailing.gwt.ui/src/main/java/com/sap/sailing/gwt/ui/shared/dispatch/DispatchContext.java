package com.sap.sailing.gwt.ui.shared.dispatch;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.server.RacingEventService;

@GwtIncompatible
public interface DispatchContext {
    RacingEventService getRacingEventService();
}
