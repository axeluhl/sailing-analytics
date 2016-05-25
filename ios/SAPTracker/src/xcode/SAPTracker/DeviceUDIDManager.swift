//
//  DeviceUDIDManager.swift
//  SAPTracker
//
//  Created by computing on 22/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

struct DeviceUDIDManager {
    
    struct Keys {
        static let UDID = "udid"
    }
    
    private static let preferences = NSUserDefaults.standardUserDefaults()
    private static var staticUDID: String?
    
    static var UDID: String {
        get {
            if (staticUDID == nil) {
                if preferences.objectForKey(Keys.UDID) == nil {
                    staticUDID = NSUUID().UUIDString.lowercaseString
                    preferences.setObject(staticUDID, forKey: Keys.UDID)
                    preferences.synchronize()
                } else {
                    staticUDID = preferences.objectForKey(Keys.UDID) as? String
                }
            }
            return staticUDID!
        }
    }
    
}
