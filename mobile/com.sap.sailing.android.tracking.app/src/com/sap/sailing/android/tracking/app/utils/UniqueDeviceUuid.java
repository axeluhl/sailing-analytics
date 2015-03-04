package com.sap.sailing.android.tracking.app.utils;

import java.util.UUID;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * I'm picking a device id based on this reply on Stack Overflow: http://stackoverflow.com/a/2853253/3589281
 * @author lukas
 *
 */
public class UniqueDeviceUuid {

	public static String getUniqueId(Context context)
	{
		 AppPreferences prefs = new AppPreferences(context);
        /*
		 String existingDeviceIdentifier = prefs.getDeviceIdentifier();
		 
		 if (existingDeviceIdentifier != null && existingDeviceIdentifier.length() > 0)
		 {
			 return existingDeviceIdentifier;
		 }
		 */
		
		 final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

		 final String tmDevice, tmSerial, androidId;
		 tmDevice = "" + tm.getDeviceId();
		 tmSerial = "" + tm.getSimSerialNumber();
		 androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

		 UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
		 String deviceId = deviceUuid.toString();
		 
		 prefs.setDeviceIdentifier(deviceId);
		
		 return deviceId;
	}
}
