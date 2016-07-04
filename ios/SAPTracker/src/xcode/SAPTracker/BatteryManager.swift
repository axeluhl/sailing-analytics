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
        setupDevice()
    }
    
    // MARK: - Setup
    
    private func setupDevice() {
        device.batteryMonitoringEnabled = true
    }
    
    // MARK: - Properties
    
    var batterySaving: Bool { get { return Preferences.batterySaving || forceBatterySaving() } }
    
    // MARK: - Helper
    
    private func forceBatterySaving() -> Bool {
        return device.batteryLevel < BatteryLevel.Min && (device.batteryState == .Unplugged || device.batteryState == .Unknown)
    }
    
 }
