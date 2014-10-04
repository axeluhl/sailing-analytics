package com.sap.sailing.racecommittee.app.services.sending;

import com.sap.sailing.android.shared.services.sending.MessagePersistenceManager;
import com.sap.sailing.android.shared.services.sending.MessageSendingService;

public class EventSendingService extends MessageSendingService {

    @Override
    protected MessagePersistenceManager getPersistenceManager() {
        return new EventPersistenceManager(this);
    }

}
