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
        static let TrainingEndpointChanged = "TrainingEndpointChanged"
    }
    
    struct UserInfo {
        static let BatterySaving = "BatterySaving"
        static let CheckInURL = "CheckInURL"
        static let TrainingEndpoint = "TrainingEndpoint"
    }
    
    struct PreferenceKey {
        static let ActiveTrainingRaceData = "ActiveTrainingRaceData"
        static let CodeConventionRead = "CodeConventionRead"
        static let BatterySaving = "BatterySaving"
        static let BoatClassName = "BoatClassName"
        static let NewCheckInURL = "NewCheckInURL"
        static let TrainingEndpoint = "TrainingEndpoint"
        static let TermsAccepted = "TermsAccepted"
        static let UUID = "udid"
    }
    
    fileprivate static let preferences = UserDefaults.standard
    
    // MARK: - ActiveTrainingRaceData
    
    class var activeTrainingRaceData: TrainingRaceData? {
        get {
            guard let data = preferences.data(forKey: PreferenceKey.ActiveTrainingRaceData) else { return nil }
            guard let trainingRaceData = NSKeyedUnarchiver.unarchiveObject(with: data) as? TrainingRaceData else { return nil }
            return trainingRaceData
        }
        set (value) {
            if let rootObject = value {
                let data = NSKeyedArchiver.archivedData(withRootObject: rootObject)
                preferences.set(data, forKey: PreferenceKey.ActiveTrainingRaceData)
            } else {
                preferences.set(nil, forKey: PreferenceKey.ActiveTrainingRaceData)
            }
            preferences.synchronize()
        }
    }
    
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

    // MARK: - BoatClassName

    class var boatClassName: String {
        get {
            return preferences.string(forKey: PreferenceKey.BoatClassName) ?? ""
        }
        set(value) {
            preferences.set(value, forKey: PreferenceKey.BoatClassName)
            preferences.synchronize()
        }
    }

    // MARK: - TrainingEndpoint

    class var trainingEndpoint: String {
        get {
            if let url = URL(string: preferences.string(forKey: PreferenceKey.TrainingEndpoint) ?? "") {
                return url.absoluteString
            } else {
                return Translation.Endpoint.Training.String
            }
        }
        set (value) {
            if let url = URL(string: value) {
                preferences.set(url.absoluteString, forKey: PreferenceKey.TrainingEndpoint)
            } else {
                preferences.set(Translation.Endpoint.Training.String, forKey: PreferenceKey.TrainingEndpoint)
            }
            preferences.synchronize()

            // Send notification
            var userInfo = [String: AnyObject]()
            userInfo[UserInfo.TrainingEndpoint] = value as AnyObject?
            let notification = Notification(name: Notification.Name(rawValue: NotificationType.TrainingEndpointChanged), object: self, userInfo: userInfo)
            NotificationQueue.default.enqueue(notification, postingStyle: NotificationQueue.PostingStyle.asap)
        }
    }

}
