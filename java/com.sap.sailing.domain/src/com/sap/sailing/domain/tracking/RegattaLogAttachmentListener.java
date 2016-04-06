package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;

public interface RegattaLogAttachmentListener {

    void regattaLogAttached(RegattaLog regattaLog);

}
