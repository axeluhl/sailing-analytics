//
//  SessionController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 05.07.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import UIKit

enum SessionControllerError: Error {
    case checkInDataIsIncomplete
}

class SessionController: NSObject {
    
    weak var checkIn: CheckIn!
    weak var coreDataManager: CoreDataManager!
    
    var sendingBackgroundTask: UIBackgroundTaskIdentifier = UIBackgroundTaskInvalid
    var sendingDate: Date = Date()
    
    fileprivate (set) var isTracking: Bool = false
    
    init(checkIn: CheckIn, coreDataManager: CoreDataManager) {
        self.checkIn = checkIn
        self.coreDataManager = coreDataManager
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
            let gpsFix = self.coreDataManager.newGPSFix(checkIn: self.checkIn)
            gpsFix.updateWithLocationData(locationData: locationData)
            self.coreDataManager.saveContext()
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
    
    func update(
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error) -> Void) throws
    {
        guard let checkInData = CheckInData(checkIn: checkIn) else {
            throw SessionControllerError.checkInDataIsIncomplete
        }
        let checkInDataCollector = CheckInDataCollector(checkInData: checkInData)
        checkInDataCollector.collect(checkInData: checkInData, success: { [weak self] (checkInData) in
            self?.updateSuccess(checkInData: checkInData, success: success)
        }) { (error) in
            failure(error)
        }
    }
    
    fileprivate func updateSuccess(checkInData: CheckInData, success: () -> Void) {
        checkIn.event.updateWithEventData(eventData: checkInData.eventData)
        checkIn.leaderboard.updateWithLeaderboardData(leaderboardData: checkInData.leaderboardData)
        checkIn.updateWithCheckInData(checkInData: checkInData)
        coreDataManager.saveContext()
        success()
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
        checkInRequestManager.postCheckOut(
            checkIn,
            success: { () in completion(true) },
            failure: { (error) in completion(false) }
        )
    }
    
    // MARK: - Properties
    
    lazy var gpsFixController: GPSFixController = {
        return GPSFixController(checkIn: self.checkIn, coreDataManager: self.coreDataManager)
    }()
    
    lazy var checkInRequestManager: CheckInRequestManager = {
        return CheckInRequestManager(baseURLString: self.checkIn.serverURL)
    }()
    
}
