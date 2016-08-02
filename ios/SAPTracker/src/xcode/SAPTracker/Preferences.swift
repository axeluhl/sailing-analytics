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
        static let BatterySavingChanged = "BatterySavingChanged"
        static let NewCheckInURLChanged = "NewCheckInURLChanged"
    }
    
    struct UserInfo {
        static let BatterySaving = "BatterySaving"
        static let CheckInURL = "CheckInURL"
    }
    
    struct PreferenceKey {
        static let CodeConventionRead = "CodeConventionRead"
        static let BatterySaving = "BatterySaving"
        static let NewCheckInURL = "NewCheckInURL"
        static let TermsAccepted = "TermsAccepted"
        static let UUID = "udid"
    }
    
    private static let preferences = NSUserDefaults.standardUserDefaults()
    
    // MARK: - CodeConventionRead
    
    class var codeConventionRead: Bool {
        get {
            return preferences.boolForKey(PreferenceKey.CodeConventionRead)
        }
        set(value) {
            preferences.setBool(value, forKey:PreferenceKey.CodeConventionRead)
            preferences.synchronize()
        }
    }
    
    // MARK: - NewCheckInURL
    
    class var newCheckInURL: String? {
        get {
            return preferences.stringForKey(PreferenceKey.NewCheckInURL)
        }
        set(value) {
            preferences.setObject(value, forKey: PreferenceKey.NewCheckInURL)
            preferences.synchronize()

            // Send notification
            var userInfo = [String: AnyObject]()
            userInfo[UserInfo.CheckInURL] = value
            let notification = NSNotification(name: NotificationType.NewCheckInURLChanged, object: self, userInfo: userInfo)
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
            
            // Send notification
            let userInfo = [UserInfo.BatterySaving: value]
            let notification = NSNotification(name: NotificationType.BatterySavingChanged, object: self, userInfo: userInfo)
            NSNotificationQueue.defaultQueue().enqueueNotification(notification, postingStyle: NSPostingStyle.PostASAP)
        }
    }
    
}
