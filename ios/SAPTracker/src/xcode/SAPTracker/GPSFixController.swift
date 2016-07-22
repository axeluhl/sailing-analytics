//
//  GPSFixController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 21.07.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import UIKit

class GPSFixController: NSObject {

    struct NotificationType {
        static let ModeChanged = "RegattaController.ModeChanged"
    }

    struct UserInfo {
        static let Mode = "Mode"
    }
    
    enum Mode: String {
        case BatterySaving
        case Error
        case Online
        case Offline
        var description: String {
            switch self {
            case .BatterySaving: return NSLocalizedString("Battery Saving", comment: "")
            case .Error: return NSLocalizedString("Error", comment: "")
            case .Offline: return NSLocalizedString("Offline", comment: "")
            case .Online: return NSLocalizedString("Online", comment: "")
            }
        }
    }
    
    let regatta: Regatta
    
    init(regatta: Regatta) {
        self.regatta = regatta
    }
    
    // MARK: - Methods
    
    func sendGPSFixes(completion: () -> Void) {
        guard let gpsFixes = regatta.gpsFixes as? Set<GPSFix> else {
            log("Can't get GPS fixes")
            completion()
            return
        }
        guard gpsFixes.count > 0 else {
            log("No GPS fixes available")
            completion()
            return
        }
        sendGPSFixes(gpsFixes, completion: completion)
    }

    func sendGPSFixes(gpsFixes: Set<GPSFix>, completion: () -> Void) {
        log("\(gpsFixes.count) GPS fixes will be sent")
        requestManager.postGPSFixes(gpsFixes,
                                    success: { (operation, responseObject) in self.sendGPSFixesSuccess(gpsFixes, completion: completion) },
                                    failure: { (operation, error) in self.sendGPSFixesFailure(error, completion: completion) }
        )
    }
    
    private func sendGPSFixesSuccess(gpsFixes: Set<GPSFix>, completion: () -> Void) {
        dispatch_async(dispatch_get_main_queue(), {
            self.log("Sending \(gpsFixes.count) GPS fixes was successful")
            self.postModeChangedNotification(BatteryManager.sharedManager.batterySaving ? .BatterySaving : .Online)
            CoreDataManager.sharedManager.deleteObjects(gpsFixes)
            CoreDataManager.sharedManager.saveContext()
            self.log("\(gpsFixes.count) GPS fixes deleted")
            completion()
        })
    }
    
    private func sendGPSFixesFailure(error: AnyObject, completion: () -> Void) {
        dispatch_async(dispatch_get_main_queue(), {
            self.log("Sending GPS fixes failed for reason: \(error)")
            self.postModeChangedNotification(AFNetworkReachabilityManager.sharedManager().reachable ? .Error : .Offline)
            completion()
        })
    }
    
    // MARK: - Notifications
    
    private func postModeChangedNotification(mode: Mode) {
        let userInfo = [UserInfo.Mode: mode.rawValue]
        let notification = NSNotification(name: NotificationType.ModeChanged, object: self, userInfo: userInfo)
        NSNotificationQueue.defaultQueue().enqueueNotification(notification, postingStyle: NSPostingStyle.PostASAP)
    }
    
    // MARK: - Properties
    
    lazy var requestManager: RequestManager = {
        let requestManager = RequestManager(baseURLString: self.regatta.serverURL)
        return requestManager
    }()
    
    // MARK: - Helper
    
    private func log(message: String) {
        print("[GPSFixController] \(message)")
    }
    
}
