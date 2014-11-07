//
//  BatterySavingController.swift
//  SAPTracker
//
//  Created by computing on 07/11/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

/* Needs to be sublass of NSObject for receiving NSNotifications */
class BatteryManager: NSObject {

    struct NotificationType {
        static let batterySavingChanged = "battery_saving_changed"
    }

    /* NSUserDefaultsKey */
    private let BatterySavingDefaultsKey = "BatterySaving"
    
    /* Reference to device needed for reading battery level */
    private let device = UIDevice.currentDevice()
    
    /* Minimum battery level for sending data is 20% */
    private let minBatteryLevel: Float = 0.2
    
    /* Singleton */
    class var sharedManager: BatteryManager {
        struct Singleton {
            static let sharedBatteryManager = BatteryManager()
        }
        return Singleton.sharedBatteryManager
    }
    
    /* Register for battery events */
    override init() {
        super.init()
        
        // register for battery events
        device.batteryMonitoringEnabled = true;
        NSNotificationCenter.defaultCenter().addObserver(self, selector:"batteryChanged", name:UIDeviceBatteryLevelDidChangeNotification, object:device);
        NSNotificationCenter.defaultCenter().addObserver(self, selector:"batteryChanged", name:UIDeviceBatteryStateDidChangeNotification, object:device);
    }
    
    /* User preference for saving battery */
    var batterySavingPreference: Bool {
        get {
            let preferences = NSUserDefaults.standardUserDefaults()
            return preferences.boolForKey(BatterySavingDefaultsKey)
        }
        set {
            let preferences = NSUserDefaults.standardUserDefaults()
            preferences.setBool(newValue, forKey: BatterySavingDefaultsKey)
            preferences.synchronize()
            batteryChanged()
        }
    }

    /* Is battery saving on? */
    var batterySaving: Bool {
        get {
            return batterySavingPreference || device.batteryLevel < minBatteryLevel && (device.batteryState == UIDeviceBatteryState.Unplugged || device.batteryState == UIDeviceBatteryState.Unknown)
        }
    }
    
    /* Called when battery level or state changes or when user forces battery saving mode. */
    func batteryChanged() {
        let notification = NSNotification(name: NotificationType.batterySavingChanged, object: self,  userInfo: ["batterySaving": batterySaving])
        NSNotificationQueue.defaultQueue().enqueueNotification(notification, postingStyle: NSPostingStyle.PostASAP)
    }

}