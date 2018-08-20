package com.sap.sse.gwt.client.player;

import java.util.Date;

public interface TimeListener {
    void timeChanged(Date newTime, Date oldTime);
}
