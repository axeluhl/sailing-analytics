package com.sap.sailing.domain.swisstimingadapter;

import java.io.IOException;
import java.net.UnknownHostException;

public interface SailMasterConnector {
    SailMasterMessage sendRequestAndGetResponse(String requestMessage) throws UnknownHostException, IOException;
}
