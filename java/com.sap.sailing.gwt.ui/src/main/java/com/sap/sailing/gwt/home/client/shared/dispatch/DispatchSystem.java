package com.sap.sailing.gwt.home.client.shared.dispatch;

import java.util.Date;

public interface DispatchSystem extends DispatchAsync {
    Date getCurrentServerTime();
}
