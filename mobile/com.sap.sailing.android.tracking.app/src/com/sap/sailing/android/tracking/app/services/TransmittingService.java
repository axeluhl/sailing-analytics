package com.sap.sailing.android.tracking.app.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class TransmittingService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
