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
    
    fileprivate static let preferences = UserDefaults.standard
    
    // MARK: - CodeConventionRead
    
    class var codeConventionRead: Bool {
        get {
            return preferences.bool(forKey: PreferenceKey.CodeConventionRead)
        }
        set(value) {
            preferences.set(value, forKey:PreferenceKey.CodeConventionRead)
            preferences.synchronize()
        }
    }
    
    // MARK: - NewCheckInURL
    
    class var newCheckInURL: String? {
        get {
            return preferences.string(forKey: PreferenceKey.NewCheckInURL)
        }
        set(value) {
            preferences.set(value, forKey: PreferenceKey.NewCheckInURL)
            preferences.synchronize()

            // Send notification
            var userInfo = [String: AnyObject]()
            userInfo[UserInfo.CheckInURL] = value as AnyObject?
            let notification = Notification(name: Notification.Name(rawValue: NotificationType.NewCheckInURLChanged), object: self, userInfo: userInfo)
            NotificationQueue.default.enqueue(notification, postingStyle: NotificationQueue.PostingStyle.asap)
        }
    }
    
    // MARK: - TermsAccepted
    
    class var termsAccepted: Bool {
        get {
            return preferences.bool(forKey: PreferenceKey.TermsAccepted)
        }
        set(value) {
            preferences.set(value, forKey:PreferenceKey.TermsAccepted)
            preferences.synchronize()
        }
    }
    
    // MARK: - UUID

    static fileprivate(set) var uuid: String? = {
        let uuid = preferences.string(forKey: PreferenceKey.UUID) ?? NSUUID().uuidString.lowercased()
        preferences.set(uuid, forKey: PreferenceKey.UUID)
        preferences.synchronize()
        return uuid
    }()
    
    // MARK: - BatterySaving
    
    class var batterySaving: Bool {
        get {
            return preferences.bool(forKey: PreferenceKey.BatterySaving)
        }
        set(value) {
            preferences.set(value, forKey: PreferenceKey.BatterySaving)
            preferences.synchronize()
            
            // Send notification
            let userInfo = [UserInfo.BatterySaving: value]
            let notification = Notification(name: Notification.Name(rawValue: NotificationType.BatterySavingChanged), object: self, userInfo: userInfo)
            NotificationQueue.default.enqueue(notification, postingStyle: NotificationQueue.PostingStyle.asap)
        }
    }
    
}
