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
        static let acceptedTerms = "acceptedTerms"
        static let uuid = "udid"
    }
    
    private static let preferences = NSUserDefaults.standardUserDefaults()
    
    // MARK: - LastCheckInData
    
    class func setLastCheckInURLString(urlString: String?) {
        self.preferences.setObject(urlString, forKey: PreferenceKey.lastCheckInURLString)
        self.preferences.synchronize()
    }
    
    class func lastCheckInURLString() -> String? {
        return self.preferences.stringForKey(PreferenceKey.lastCheckInURLString)
    }
    
    // MARK: - AcceptedTerms
    
    class func acceptedTerms() -> Bool {
        return self.preferences.boolForKey(PreferenceKey.acceptedTerms)
    }
    
    class func setAcceptedTerms(value: Bool) {
        self.preferences.setBool(value, forKey:PreferenceKey.acceptedTerms)
        self.preferences.synchronize()
    }
    
    // MARK: - UUID
    
    private static var UUID: String?
    class func uuid() -> String {
        if UUID == nil {
            UUID = self.preferences.stringForKey(PreferenceKey.uuid) ?? NSUUID().UUIDString.lowercaseString
            preferences.setObject(UUID, forKey: PreferenceKey.uuid)
            preferences.synchronize()
        }
        return UUID!
    }
    
}
