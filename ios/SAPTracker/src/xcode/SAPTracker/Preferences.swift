//
//  Preferences.swift
//  SAPTracker
//
//  Created by Raimund Wege on 26.05.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import UIKit

class Preferences: NSObject {

    struct NotificationType {
        static let NewCheckInURLChanged = "NewCheckInURLStringChanged"
    }
    
    struct PreferenceKey {
        static let BatterySaving = "BatterySaving"
        static let NewCheckInURL = "NewCheckInURL"
        static let TermsAccepted = "TermsAccepted"
        static let UUID = "udid"
    }
    
    private static let preferences = NSUserDefaults.standardUserDefaults()
    
    // MARK: - NewCheckInURL
    
    class var newCheckInURL: String? {
        get {
            return preferences.stringForKey(PreferenceKey.NewCheckInURL)
        }
        set(value) {
            preferences.setObject(value, forKey: PreferenceKey.NewCheckInURL)
            preferences.synchronize()

            // Send notification
            let notification = NSNotification(name: NotificationType.NewCheckInURLChanged, object: self)
            NSNotificationQueue.defaultQueue().enqueueNotification(notification, postingStyle: NSPostingStyle.PostASAP)
        }
    }
    
    // MARK: - TermsAccepted
    
    class var termsAccepted: Bool {
        get {
            return preferences.boolForKey(PreferenceKey.TermsAccepted)
        }
        set(value) {
            preferences.setBool(value, forKey:PreferenceKey.TermsAccepted)
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
