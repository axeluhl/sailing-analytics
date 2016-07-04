//
//  SendGPSFixesController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 29.06.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import UIKit

class SendGPSFixesController: NSObject {

    private struct SendingInterval {
        static let Normal: NSTimeInterval = 3
        static let BatterySaving: NSTimeInterval = 30
    }
    
    private let checkIn: CheckIn!
    private let requestManager: RequestManager!

    private var isRunning: Bool = false
    
    init(checkIn: CheckIn) {
        self.checkIn = checkIn
        requestManager = RequestManager(baseURLString: checkIn.serverURL)
        super.init()
    }
    
    deinit {
        unsubscribeFromNotifications()
    }
    
    // MARK: - Notifications
    
    private func subscribeForNotifications() {
        NSNotificationCenter.defaultCenter().addObserver(self,
                                                         selector:#selector(locationManagerUpdated(_:)),
                                                         name:LocationManager.NotificationType.LocationManagerUpdated,
                                                         object: nil)
    }
    
    private func unsubscribeFromNotifications() {
        NSNotificationCenter.defaultCenter().removeObserver(self)
    }
    
    func locationManagerUpdated(notification: NSNotification) {
        dispatch_async(dispatch_get_main_queue(), {
            if let userInfo = notification.userInfo {
                if userInfo[LocationManager.UserInfo.IsValid] as? Bool ?? false {
                    let gpsFix = CoreDataManager.sharedManager.newGPSFix(self.checkIn)
                    gpsFix.course = userInfo[LocationManager.UserInfo.Course] as! Double
                    gpsFix.latitude = userInfo[LocationManager.UserInfo.Latitude] as! Double
                    gpsFix.longitude = userInfo[LocationManager.UserInfo.Longitude] as! Double
                    gpsFix.speed = userInfo[LocationManager.UserInfo.Speed] as! Double
                    gpsFix.timestamp = round(userInfo[LocationManager.UserInfo.Timestamp] as! Double * 1000)
                    CoreDataManager.sharedManager.saveContext()
                }
            }
        })
    }
    
    // MARK: - Methods
    
    func startSending() {
        isRunning = true
        sendGPSFixes()
    }
    
    func stopSending() {
        isRunning = false
    }
    
    func sendGPSFixes() {
        var gpsFixes = [GPSFix]()
        if checkIn.gpsFixes != nil {
            checkIn.gpsFixes!.forEach { (gpsFix) in
                gpsFixes.append(gpsFix as! GPSFix)
            }
        }
        if gpsFixes.count > 0 {
            log("\(gpsFixes.count) GPS fixes will be sent")
            requestManager.postGPSFixes(gpsFixes,
                                        success: { (operation, responseObject) in self.sendGPSFixesSuccess(gpsFixes) },
                                        failure: { (operation, error) in self.sendGPSFixesFailure(error) })
        } else {
            log("No GPS fixes available")
            sendGPSFixesFinished()
        }
    }
    
    private func sendGPSFixesSuccess(gpsFixes: [GPSFix]!) {
        print(NSThread.isMainThread)
        dispatch_async(dispatch_get_main_queue(), {
            self.log("sending \(gpsFixes.count) GPS fixes was successful")
            CoreDataManager.sharedManager.deleteGPSFixes(gpsFixes)
            CoreDataManager.sharedManager.saveContext()
            self.sendGPSFixesFinished()
        })
    }
    
    private func sendGPSFixesFailure(error: AnyObject) {
        print(NSThread.isMainThread)
        dispatch_async(dispatch_get_main_queue(), {
            self.log("sending GPS fixes failed for reason: \(error)")
            self.sendGPSFixesFinished()
        })
    }
    
    private func sendGPSFixesFinished() {
        if isRunning {
            let delay = sendingPeriod
            log("sending next GPS fixes in \(delay) seconds")
            performSelector(#selector(SendGPSFixesController.sendGPSFixes), withObject: nil, afterDelay: delay)
        } else {
            log("sending GPS fixes stopped")
        }
    }
    
    // MARK: - Helper
    
    private var sendingPeriod: NSTimeInterval {
        get {
            return BatteryManager.sharedManager.batterySaving ? SendingInterval.BatterySaving : SendingInterval.Normal
        }
    }
    
    private func log(message: String) {
        print("[GPSFixesController] \(message)")
    }
    
}
