//
//  RegattaController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 05.07.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import UIKit

@objc protocol RegattaControllerDelegate {
    
    //   optional func regattaControllerDidStartTracking(sender: RegattaController)
    //   optional func regattaControllerDidStopTracking(sender: RegattaController)
    
}

class RegattaController: NSObject {
    
    struct NotificationType {
        static let ModeChanged = "RegattaController.ModeChanged"
    }
    
    struct UserInfo {
        static let Mode = "Mode"
    }
    
    enum Mode: String {
        case BatterySaving = "BatterySaving"
        case Error = "Error"
        case Online = "Online"
        case Offline = "Offline"
        var description: String {
            switch self {
            case .BatterySaving: return NSLocalizedString("Battery Saving", comment: "")
            case .Error: return NSLocalizedString("Error", comment: "")
            case .Offline: return NSLocalizedString("Offline", comment: "")
            case .Online: return NSLocalizedString("Online", comment: "")
            }
        }
    }
    
    private struct SendingInterval {
        static let Normal: NSTimeInterval = 3
        static let BatterySaving: NSTimeInterval = 30
    }
    
    let regatta: Regatta
    
    var delegate: RegattaControllerDelegate?
    var sendingBackgroundTask: UIBackgroundTaskIdentifier = UIBackgroundTaskInvalid
    var sendingDate: NSDate = NSDate()
    
    private (set) var isTracking: Bool = false
    
    init(regatta: Regatta) {
        self.regatta = regatta
        super.init()
        subscribeForNotifications()
    }
    
    // MARK: - Notifications
    
    private func subscribeForNotifications() {
        NSNotificationCenter.defaultCenter().addObserver(self,
                                                         selector:#selector(locationManagerUpdated(_:)),
                                                         name:LocationManager.NotificationType.Updated,
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
            self.beginGPSFixSendingInBackgroundTask()
        })
    }
    
    // MARK: - Background Task
    
    private func beginGPSFixSendingInBackgroundTask() {
        if sendingDate.compare(NSDate()) == .OrderedAscending {
            sendingBackgroundTask = UIApplication.sharedApplication().beginBackgroundTaskWithName("Send GPS Fixes", expirationHandler: {
                self.endGPSFixSendingInBackgroundTask()
            })
            dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), {
                self.sendingDate = NSDate().dateByAddingTimeInterval(self.sendingPeriod)
                self.gpsFixController.sendGPSFixes({
                    self.endGPSFixSendingInBackgroundTask()
                })
            })
        }
    }
    
    private func endGPSFixSendingInBackgroundTask() {
        UIApplication.sharedApplication().endBackgroundTask(sendingBackgroundTask)
        sendingBackgroundTask = UIBackgroundTaskInvalid;
    }
    
    // MARK: - Update
    
    func update() {
        SVProgressHUD.show()
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
        SVProgressHUD.popActivity()
        updateFinished()
    }
    
    func updateFailure() {
        updateFinished()
    }
    
    func updateFinished() {
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
