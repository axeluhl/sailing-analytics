//
//  Preferences.swift
//  SAPTracker
//
//  Created by Raimund Wege on 26.05.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import UIKit

class Preferences: NSObject {

    struct PreferenceKey {
        static let lastCheckInURLString = "lastCheckInURLString"
    }
    
    private static let preferences = NSUserDefaults.standardUserDefaults()
    
    // MARK: - LastCheckInData
    
    class func setLastCheckInURLString(urlString: String?) {
        preferences.setObject(urlString, forKey: PreferenceKey.lastCheckInURLString)
    }
    
    class func lastCheckInURLString() -> String? {
        return preferences.stringForKey(PreferenceKey.lastCheckInURLString)
    }
    
}
