//
//  RegattaController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 05.07.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import UIKit

class RegattaController: NSObject {
    
    private struct SendingInterval {
        static let Normal: NSTimeInterval = 3
        static let BatterySaving: NSTimeInterval = 30
    }
    
    let regatta: Regatta
    
    var sendingBackgroundTask: UIBackgroundTaskIdentifier = UIBackgroundTaskInvalid
    var sendingDate: NSDate = NSDate()
    
    private (set) var isTracking: Bool = false
    
    init(regatta: Regatta) {
        self.regatta = regatta
        super.init()
        subscribeForNotifications()
    }
    
    deinit {
        unsubscribeFromNotifications()
    }
    
    // MARK: - Notifications
    
    private func subscribeForNotifications() {
        NSNotificationCenter.defaultCenter().addObserver(self,
                                                         selector:#selector(locationManagerUpdated(_:)),
                                                         name:LocationManager.NotificationType.Updated,
                                                         object: nil
        )
        NSNotificationCenter.defaultCenter().addObserver(self,
                                                         selector:#selector(batterySavingChanged(_:)),
                                                         name:Preferences.NotificationType.BatterySavingChanged,
                                                         object: nil
        )
    }
    
    private func unsubscribeFromNotifications() {
        NSNotificationCenter.defaultCenter().removeObserver(self)
    }
    
    func locationManagerUpdated(notification: NSNotification) {
        dispatch_async(dispatch_get_main_queue(), {
            guard self.isTracking else { return }
            guard let locationData = notification.userInfo?[LocationManager.UserInfo.LocationData] as? LocationData else { return }
            guard locationData.isValid else { return }
            let gpsFix = CoreDataManager.sharedManager.newGPSFix(self.regatta)
            gpsFix.updateWithLocationData(locationData)
            CoreDataManager.sharedManager.saveContext()
            if self.sendingDate.compare(NSDate()) == .OrderedAscending {
                self.sendingDate = NSDate().dateByAddingTimeInterval(self.sendingPeriod)
                self.beginGPSFixSendingInBackgroundTask()
            }
        })
    }
    
    func batterySavingChanged(notification: NSNotification) {
        dispatch_async(dispatch_get_main_queue(), {
            guard let batterySaving = notification.userInfo?[Preferences.UserInfo.BatterySaving] as? Bool else { return }
            if !batterySaving {
                self.sendingDate = NSDate() // Send next GPS fixes soon as possible
            }
        })
    }
    
    // MARK: - Background Task
    
    private func beginGPSFixSendingInBackgroundTask() {
        sendingBackgroundTask = UIApplication.sharedApplication().beginBackgroundTaskWithName("Send GPS Fixes", expirationHandler: {
            self.endGPSFixSendingInBackgroundTask()
        })
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), {
            self.gpsFixController.sendSlice({ (withSuccess) in
                self.endGPSFixSendingInBackgroundTask()
            })
        })
    }
    
    private func endGPSFixSendingInBackgroundTask() {
        UIApplication.sharedApplication().endBackgroundTask(sendingBackgroundTask)
        sendingBackgroundTask = UIBackgroundTaskInvalid
    }
    
    // MARK: - Update
    
    func update() {
        updateDidStart()
        let regattaData = RegattaData(serverURL: regatta.serverURL,
                                      eventID: regatta.event.eventID,
                                      leaderboardName: regatta.leaderboard.name,
                                      competitorID: regatta.competitor.competitorID
        )
        requestManager.getRegattaData(regattaData,
                                      success: { (regattaData) in self.updateSuccess(regattaData) },
                                      failure: { (title, error) in self.updateFailure() }
        )
    }
    
    func updateSuccess(regattaData: RegattaData) {
        regatta.event.updateWithEventData(regattaData.eventData)
        regatta.leaderboard.updateWithLeaderboardData(regattaData.leaderboardData)
        regatta.competitor.updateWithCompetitorData(regattaData.competitorData)
        CoreDataManager.sharedManager.saveContext()
        updateDidFinish()
    }
    
    func updateFailure() {
        updateDidFinish()
    }
    
    func updateDidStart() {
        SVProgressHUD.show()
    }
    
    func updateDidFinish() {
        SVProgressHUD.popActivity()
    }
    
    // MARK: - Methods
    
    func startTracking() throws {
        isTracking = false
        try LocationManager.sharedManager.startTracking()
        isTracking = true
    }
    
    func stopTracking() {
        isTracking = false
        LocationManager.sharedManager.stopTracking()
    }
    
    // MARK: - Properties
    
    lazy var gpsFixController: GPSFixController = {
        let gpsFixController = GPSFixController(regatta: self.regatta)
        return gpsFixController
    }()
    
    lazy var requestManager: RequestManager = {
        let requestManager = RequestManager(baseURLString: self.regatta.serverURL)
        return requestManager
    }()
    
    // MARK: - Helper
    
    var sendingPeriod: NSTimeInterval { get { return BatteryManager.sharedManager.batterySaving ? SendingInterval.BatterySaving : SendingInterval.Normal } }
    
}
