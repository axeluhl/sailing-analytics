//
//  DeviceUDIDManager.swift
//  SAPTracker
//
//  Created by computing on 22/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

struct DeviceUDIDManager {
    
    private static let preferences = NSUserDefaults.standardUserDefaults()
    private static var staticUdid: String?
    
    static var UDID: String {
        get {
            if (staticUdid == nil) {
                if preferences.objectForKey("udid") == nil {
                    staticUdid = NSUUID().UUIDString.lowercaseString
                    preferences.setObject(staticUdid, forKey: "udid")
                    preferences.synchronize()
                } else {
                    staticUdid = preferences.objectForKey("udid") as? String
                }
            }
            return staticUdid!
        }
    }
    
}
