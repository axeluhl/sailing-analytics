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
    
    struct GPSFixSending {
        // FIXME: - Change me back to 1000
        static let SliceSize = 10
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
    
    // MARK: - Send All
    
    func sendAll(completion: (withSuccess: Bool) -> Void) {
        sendAll({
            completion(withSuccess: true)
        }) { (error, gpsFixesLeft) in
            completion(withSuccess: false)
        }
    }
    
    private func sendAll(success: () -> Void, failure: (error: AnyObject, gpsFixesLeft: Set<GPSFix>) -> Void) {
        sendAll(regatta.gpsFixes as? Set<GPSFix> ?? [], success: success, failure: failure)
    }
    
    private func sendAll(gpsFixesLeft: Set<GPSFix>,
                         success: () -> Void,
                         failure: (error: AnyObject, gpsFixesLeft: Set<GPSFix>) -> Void)
    {
        guard gpsFixesLeft.count > 0 else { success(); return }
        sendSlice(gpsFixesLeft, success: { (gpsFixesLeft) in
            self.sendAll(gpsFixesLeft, success: success, failure: failure)
        }) { (error, gpsFixesLeft) in
            failure(error: error, gpsFixesLeft: gpsFixesLeft)
        }
    }
    
    // MARK: - Send Slice
    
    func sendSlice(completion: (withSuccess: Bool) -> Void) {
        sendSlice({ (gpsFixesLeft) in
            completion(withSuccess: true)
        }) { (error, gpsFixesLeft) in
            completion(withSuccess: false)
        }
    }
    
    private func sendSlice(success: (gpsFixesLeft: Set<GPSFix>) -> Void,
                           failure: (error: AnyObject?, gpsFixesLeft: Set<GPSFix>?) -> Void)
    {
        sendSlice(regatta.gpsFixes as? Set<GPSFix> ?? [], success: success, failure: failure)
    }
    
    private func sendSlice(gpsFixes: Set<GPSFix>,
                           success: (gpsFixesLeft: Set<GPSFix>) -> Void,
                           failure: (error: AnyObject, gpsFixesLeft: Set<GPSFix>) -> Void)
    {
        guard gpsFixes.count > 0 else { success(gpsFixesLeft: []); return }
        let slicedGPSFixes = sliceGPSFixes(gpsFixes)
        let gpsFixesLeft = gpsFixes.subtract(slicedGPSFixes)
        sendSlice(slicedGPSFixes,
                  gpsFixesLeft: gpsFixesLeft,
                  success: success,
                  failure: failure)
    }
    
    private func sendSlice(gpsFixes: Array<GPSFix>,
                           gpsFixesLeft: Set<GPSFix>,
                           success: (gpsFixesLeft: Set<GPSFix>) -> Void,
                           failure: (error: AnyObject, gpsFixesLeft: Set<GPSFix>) -> Void)
    {
        log("\(gpsFixes.count) GPS fixes will be sent and \(gpsFixesLeft.count) will be left")
        requestManager.postGPSFixes(gpsFixes,
                                    success: { (operation, responseObject) in self.sendSliceSuccess(gpsFixes, gpsFixesLeft: gpsFixesLeft, success: success) },
                                    failure: { (operation, error) in self.sendSliceFailure(error, gpsFixesLeft: gpsFixesLeft, failure: failure) }
        )
    }
    
    private func sendSliceSuccess(gpsFixes: Array<GPSFix>,
                                  gpsFixesLeft: Set<GPSFix>,
                                  success: (gpsFixesLeft: Set<GPSFix>) -> Void)
    {
        dispatch_async(dispatch_get_main_queue(), {
            self.log("Sending \(gpsFixes.count) GPS fixes was successful")
            self.postModeChangedNotification(BatteryManager.sharedManager.batterySaving ? .BatterySaving : .Online)
            CoreDataManager.sharedManager.deleteObjects(gpsFixes)
            CoreDataManager.sharedManager.saveContext()
            self.log("\(gpsFixes.count) GPS fixes deleted")
            success(gpsFixesLeft: gpsFixesLeft)
        })
    }
    
    private func sendSliceFailure(error: AnyObject,
                                  gpsFixesLeft: Set<GPSFix>,
                                  failure: (error: AnyObject, gpsFixesLeft: Set<GPSFix>) -> Void)
    {
        dispatch_async(dispatch_get_main_queue(), {
            self.log("Sending GPS fixes failed for reason: \(error.description)")
            self.postModeChangedNotification(AFNetworkReachabilityManager.sharedManager().reachable ? .Error : .Offline)
            failure(error: error, gpsFixesLeft: gpsFixesLeft)
        })
    }
    
    // MARK: - Notifications
    
    private func postModeChangedNotification(mode: Mode) {
        let userInfo = [UserInfo.Mode: mode.rawValue]
        let notification = NSNotification(name: NotificationType.ModeChanged, object: self, userInfo: userInfo)
        NSNotificationQueue.defaultQueue().enqueueNotification(notification, postingStyle: NSPostingStyle.PostASAP)
    }
    
    // MARK: - Properties
    
    private lazy var requestManager: RequestManager = {
        let requestManager = RequestManager(baseURLString: self.regatta.serverURL)
        return requestManager
    }()
    
    // MARK: - Helper
    
    private func log(message: String) {
        print("[GPSFixController] \(message)")
    }
    
    private func sliceGPSFixes(gpsFixes: Set<GPSFix>) -> Array<GPSFix> {
        return Array(gpsFixes.sort { (gpsFix1, gpsFix2) -> Bool in gpsFix1.timestamp < gpsFix2.timestamp }.prefix(GPSFixSending.SliceSize))
    }
    
}
