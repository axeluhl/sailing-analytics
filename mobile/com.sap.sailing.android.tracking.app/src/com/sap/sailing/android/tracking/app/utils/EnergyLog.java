package com.sap.sailing.android.tracking.app.utils;

import java.util.Calendar;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class EnergyLog {
    
    private StringBuffer stringBuffer;
    
    public EnergyLog(){
        stringBuffer = new StringBuffer();
    }
    
    public void addEntry(float accuracy, int satelites, float batteryPercentage, int health,
            int voltage){
        String str = "";
        
        str += Calendar.getInstance().toString()+";";
        str += accuracy+";";
        str += satelites+";";
        str += batteryPercentage+";";
        str += health+";";
        str += (voltage/100000)+";";
        
        str += "\n";
        stringBuffer.append(str);
    }
    
    public void writeLog(){
        
    }
    
}
