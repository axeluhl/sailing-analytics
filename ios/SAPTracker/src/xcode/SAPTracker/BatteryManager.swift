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
        static let Normal: NSTimeInterval = 3
        static let BatterySaving: NSTimeInterval = 30
    }
    
    private let device: UIDevice!
    
    class var sharedManager: BatteryManager {
        struct Singleton {
            static let sharedManager = BatteryManager()
        }
        return Singleton.sharedManager
    }
    
    override init() {
        device = UIDevice.currentDevice()
        super.init()
        setup()
    }
    
    // MARK: - Setup
    
    private func setup() {
        setupDevice()
    }
    
    private func setupDevice() {
        device.batteryMonitoringEnabled = true
    }
    
    // MARK: - Properties
    
    var batterySaving: Bool { get { return Preferences.batterySaving || forceBatterySaving() } }
        
    var sendingPeriod: NSTimeInterval { get { return batterySaving ? SendingInterval.BatterySaving : SendingInterval.Normal } }
    
    // MARK: - Helper
    
    private func forceBatterySaving() -> Bool {
        return device.batteryLevel < BatteryLevel.Min && (device.batteryState == .Unplugged || device.batteryState == .Unknown)
    }
    
 }
