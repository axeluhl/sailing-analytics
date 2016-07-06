//
//  RegattaController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 05.07.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import UIKit

@objc protocol RegattaControllerDelegate {
    
    func showRegattaAlert(regattaController: RegattaController, alertController: UIAlertController)
    
    optional func regattaControllerDidUpdate(regattaController: RegattaController)
    
//    optional func checkInDidStart(checkInController: CheckInController)
//    optional func checkInDidEnd(checkInController: CheckInController, withSuccess succeed: Bool)
    
}

class RegattaController: NSObject {
    
    struct NotificationType {
        static let ModeChanged = "RegattaController.ModeChanged"
    }
    
    struct UserInfo {
        static let Mode = "Mode"
    }
    
    enum Mode: String {
        case BatterySaving = "Battery Saving"
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
    
    private (set) var isTracking: Bool = false

    init(regatta: Regatta) {
        self.regatta = regatta
        super.init()
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
        })
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
    
    func startTracking() -> Bool {
        isTracking = false
        do {
            try LocationManager.sharedManager.startTracking()
            isTracking = true
        } catch let error as LocationManager.LocationManagerError {
            let alertController = UIAlertController(title: error.description, message: nil, preferredStyle: .Alert)
            let cancelTitle = NSLocalizedString("Cancel", comment: "")
            let cancelAction = UIAlertAction(title: cancelTitle, style: .Cancel, handler: nil)
            alertController.addAction(cancelAction)
            delegate?.showRegattaAlert(self, alertController: alertController)
        } catch {
            print("Unknown error")
        }
        
        
        isTracking = true
        sendGPSFixes()
        return isTracking
    }
    
    func stopTracking() {
        isTracking = false
    }
    
    func sendGPSFixes() {
        var gpsFixes = [GPSFix]()
        if regatta.gpsFixes != nil {
            regatta.gpsFixes!.forEach { (gpsFix) in gpsFixes.append(gpsFix as! GPSFix) }
        }
        if gpsFixes.count > 0 {
            print("\(gpsFixes.count) GPS fixes will be sent")
            requestManager.postGPSFixes(gpsFixes,
                                        success: { (operation, responseObject) in self.sendGPSFixesSuccess(gpsFixes) },
                                        failure: { (operation, error) in self.sendGPSFixesFailure(error) })
        } else {
            print("No GPS fixes available")
            sendGPSFixesFinished()
        }
    }
    
    private func sendGPSFixesSuccess(gpsFixes: [GPSFix]!) {
        print(NSThread.isMainThread)
        dispatch_async(dispatch_get_main_queue(), {
            print("sending \(gpsFixes.count) GPS fixes was successful")
            CoreDataManager.sharedManager.deleteGPSFixes(gpsFixes)
            CoreDataManager.sharedManager.saveContext()
            self.sendGPSFixesFinished()
        })
    }
    
    private func sendGPSFixesFailure(error: AnyObject) {
        print(NSThread.isMainThread)
        dispatch_async(dispatch_get_main_queue(), {
            print("sending GPS fixes failed for reason: \(error)")
            self.sendGPSFixesFinished()
        })
    }
    
    private func sendGPSFixesFinished() {
        if isTracking {
            let delay = sendingPeriod
            print("sending next GPS fixes in \(delay) seconds")
            performSelector(#selector(RegattaController.sendGPSFixes), withObject: nil, afterDelay: delay)
        } else {
            print("sending GPS fixes stopped")
        }
    }
    
    // MARK: - Helper
    
    var sendingPeriod: NSTimeInterval { get { return BatteryManager.sharedManager.batterySaving ? SendingInterval.BatterySaving : SendingInterval.Normal } }
    
    // MARK: - Properties
    
    lazy var requestManager: RequestManager = {
        let requestManager = RequestManager(baseURLString: self.regatta.serverURL)
        return requestManager
    }()
    
}
