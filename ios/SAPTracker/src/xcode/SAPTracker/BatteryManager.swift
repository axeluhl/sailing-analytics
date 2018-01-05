//
//  BatteryManager.swift
//  SAPTracker
//
//  Created by Raimund Wege on 04.07.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import Foundation

class BatteryManager: NSObject {
    
    struct BatteryLevel {
        static let Min: Float = 0.2
    }
    
    struct SendingInterval {
        static let Normal: TimeInterval = 3
        static let BatterySaving: TimeInterval = 30
    }
    
    fileprivate let device: UIDevice!
    
    class var sharedManager: BatteryManager {
        struct Singleton {
            static let sharedManager = BatteryManager()
        }
        return Singleton.sharedManager
    }
    
    override init() {
        device = UIDevice.current
        super.init()
        setup()
    }
    
    // MARK: - Setup
    
    fileprivate func setup() {
        setupDevice()
    }
    
    fileprivate func setupDevice() {
        device.isBatteryMonitoringEnabled = true
    }
    
    // MARK: - Properties
    
    var batterySaving: Bool { get { return Preferences.batterySaving || forceBatterySaving() } }
        
    var sendingPeriod: TimeInterval { get { return batterySaving ? SendingInterval.BatterySaving : SendingInterval.Normal } }
    
    // MARK: - Helper
    
    fileprivate func forceBatterySaving() -> Bool {
        return device.batteryLevel < BatteryLevel.Min && (device.batteryState == .unplugged || device.batteryState == .unknown)
    }
    
 }
