package com.sap.sailing.gwt.ui.shared.dispatch;

import java.util.Date;
import java.util.Locale;

import com.sap.sailing.server.RacingEventService;


public interface DispatchContext {

    RacingEventService getRacingEventService();

    String getClientLocaleName();

    Locale getClientLocale();

    Date getCurrentClientTime();
}
