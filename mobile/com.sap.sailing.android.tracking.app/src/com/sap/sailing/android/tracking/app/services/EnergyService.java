package com.sap.sailing.android.tracking.app.services;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class EnergyService extends IntentService {
    
    StringBuffer stringBuffer;
    
    public EnergyService() {
        super("EnergyService");
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        stringBuffer = new StringBuffer();
    };
    
    @SuppressLint({ "InlinedApi", "NewApi" })
    @Override
    protected void onHandleIntent(Intent intent) {
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        String buffer = "";
        
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        // Error checking that probably isn't needed but I added just in case.
        if(level == -1 || scale == -1) {
            buffer += 0.0f;
        } else {
            buffer += ((float)level / (float)scale)+";";
        }
        
        buffer += batteryIntent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)+";";
        
        if (android.os.Build.VERSION.SDK_INT>20){
            BatteryManager bm = (BatteryManager)this.getSystemService(Context.BATTERY_SERVICE);
            long energyCounter  = bm.getLongProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER);
        }

    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
