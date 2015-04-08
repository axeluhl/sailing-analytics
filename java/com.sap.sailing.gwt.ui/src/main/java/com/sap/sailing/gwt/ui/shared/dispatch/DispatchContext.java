package com.sap.sailing.gwt.ui.shared.dispatch;

import com.google.gwt.thirdparty.guava.common.annotations.GwtCompatible;
import com.sap.sailing.server.RacingEventService;

@GwtCompatible
public interface DispatchContext {

    RacingEventService getRacingEventService();
}
