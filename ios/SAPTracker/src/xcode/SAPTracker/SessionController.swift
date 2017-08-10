//
//  SessionController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 05.07.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import UIKit

class SessionController: NSObject {
        
    let checkIn: CheckIn
    
    var sendingBackgroundTask: UIBackgroundTaskIdentifier = UIBackgroundTaskInvalid
    var sendingDate: Date = Date()
    
    fileprivate (set) var isTracking: Bool = false
    
    init(checkIn: CheckIn) {
        self.checkIn = checkIn
        super.init()
        subscribeForNotifications()
    }
    
    deinit {
        unsubscribeFromNotifications()
    }
    
    // MARK: - Notifications
    
    fileprivate func subscribeForNotifications() {
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(locationManagerUpdated(_:)),
            name: NSNotification.Name(rawValue: LocationManager.NotificationType.Updated),
            object: nil
        )
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(batterySavingChanged(_:)),
            name: NSNotification.Name(rawValue: Preferences.NotificationType.BatterySavingChanged),
            object: nil
        )
    }
    
    fileprivate func unsubscribeFromNotifications() {
        NotificationCenter.default.removeObserver(self)
    }
    
    @objc fileprivate func locationManagerUpdated(_ notification: Notification) {
        DispatchQueue.main.async(execute: {
            guard self.isTracking else { return }
            guard let locationData = notification.userInfo?[LocationManager.UserInfo.LocationData] as? LocationData else { return }
            guard locationData.isValid else { return }
            let gpsFix = CoreDataManager.sharedManager.newGPSFix(checkIn: self.checkIn)
            gpsFix.updateWithLocationData(locationData: locationData)
            CoreDataManager.sharedManager.saveContext()
            if self.sendingDate.compare(Date()) == .orderedAscending {
                self.sendingDate = Date().addingTimeInterval(BatteryManager.sharedManager.sendingPeriod)
                self.beginGPSFixSendingInBackgroundTask()
            }
        })
    }
    
    @objc fileprivate func batterySavingChanged(_ notification: Notification) {
        DispatchQueue.main.async(execute: {
            guard let batterySaving = notification.userInfo?[Preferences.UserInfo.BatterySaving] as? Bool else { return }
            if !batterySaving {
                self.sendingDate = Date() // Send next GPS fixes soon as possible
            }
        })
    }
    
    // MARK: - Background Task
    
    fileprivate func beginGPSFixSendingInBackgroundTask() {
        sendingBackgroundTask = UIApplication.shared.beginBackgroundTask(withName: "Send GPS Fixes", expirationHandler: {
            self.endGPSFixSendingInBackgroundTask()
        })
        DispatchQueue.global(qos: DispatchQoS.QoSClass.default).async(execute: {
            self.gpsFixController.sendSlice(completion: { (withSuccess) in
                self.endGPSFixSendingInBackgroundTask()
            })
        })
    }
    
    fileprivate func endGPSFixSendingInBackgroundTask() {
        UIApplication.shared.endBackgroundTask(sendingBackgroundTask)
        sendingBackgroundTask = UIBackgroundTaskInvalid
    }
    
    // MARK: - Update
    
    func update(completion: @escaping () -> Void) {
        guard let checkInData = CheckInData(checkIn: checkIn) else { updateFailure(completion: completion); return }
        requestManager.getCheckInData(
            checkInData: checkInData,
            success: { (checkInData) in self.updateSuccess(checkInData: checkInData, completion: completion) },
            failure: { (error) in self.updateFailure(completion: completion) }
        )
    }
    
    func updateSuccess(checkInData: CheckInData, completion: () -> Void) {
        checkIn.event.updateWithEventData(eventData: checkInData.eventData)
        checkIn.leaderboard.updateWithLeaderboardData(leaderboardData: checkInData.leaderboardData)
        checkIn.updateWithCheckInData(checkInData: checkInData)
        CoreDataManager.sharedManager.saveContext()
        completion()
    }
    
    func updateFailure(completion: () -> Void) {
        completion()
    }
    
    // MARK: - Tracking
    
    func startTracking() throws {
        isTracking = false
        try LocationManager.sharedManager.startTracking()
        isTracking = true
    }
    
    func stopTracking() {
        isTracking = false
        LocationManager.sharedManager.stopTracking()
    }
    
    // MARK: - CheckOut
    
    func checkOut(completion: @escaping (_ withSuccess: Bool) -> Void) {
        requestManager.postCheckOut(
            checkIn,
            success: { () in completion(true) },
            failure: { (error) in completion(false) }
        )
    }
    
    // MARK: - Properties
    
    lazy var gpsFixController: GPSFixController = {
        let gpsFixController = GPSFixController(checkIn: self.checkIn)
        return gpsFixController
    }()
    
    lazy var requestManager: RequestManager = {
        let requestManager = RequestManager(baseURLString: self.checkIn.serverURL)
        return requestManager
    }()
    
}
