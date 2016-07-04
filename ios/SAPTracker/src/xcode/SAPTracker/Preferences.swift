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
        static let BatterySaving = "BatterySaving"
        static let LastCheckInURLString = "lastCheckInURLString"
        static let AcceptedTerms = "acceptedTerms"
        static let UUID = "udid"
    }
    
    private static let preferences = NSUserDefaults.standardUserDefaults()
    
    // MARK: - LastCheckInData
    
    class var lastCheckInURLString: String? {
        get {
            return preferences.stringForKey(PreferenceKey.LastCheckInURLString)
        }
        set(value) {
            preferences.setObject(value, forKey: PreferenceKey.LastCheckInURLString)
            preferences.synchronize()
        }
    }
    
    // MARK: - AcceptedTerms
    
    class var acceptedTerms: Bool {
        get {
            return preferences.boolForKey(PreferenceKey.AcceptedTerms)
        }
        set(value) {
            preferences.setBool(value, forKey:PreferenceKey.AcceptedTerms)
            preferences.synchronize()
        }
    }
    
    // MARK: - UUID

    static private(set) var uuid: String? = {
        let uuid = preferences.stringForKey(PreferenceKey.UUID) ?? NSUUID().UUIDString.lowercaseString
        preferences.setObject(uuid, forKey: PreferenceKey.UUID)
        preferences.synchronize()
        return uuid
    }()
    
    // MARK: - BatterySaving
    
    class var batterySaving: Bool {
        get {
            return preferences.boolForKey(PreferenceKey.BatterySaving)
        }
        set(value) {
            preferences.setBool(value, forKey: PreferenceKey.BatterySaving)
            preferences.synchronize()
        }
    }
    
}
