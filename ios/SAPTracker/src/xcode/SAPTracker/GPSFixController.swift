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
        static let ModeChanged = "GPSFixController.ModeChanged"
        static let SentGPSFixes = "GPSFixController.SentGPSFixes"
    }
    
    struct UserInfo {
        static let Mode = "Mode"
        static let Sent = "Sent"
        struct SentKey {
            static let Count = "Count"
        }
    }
    
    struct GPSFixSending {
        static let SliceSize = 1000
    }
    
    enum Mode: String {
        case BatterySaving
        case Error
        case None
        case Offline
        case Online
        var description: String {
            switch self {
            case .BatterySaving: return Translation.GPSFixController.Mode.BatterySaving.String
            case .Error: return Translation.GPSFixController.Mode.Error.String
            case .None: return Translation.GPSFixController.Mode.None.String
            case .Offline: return Translation.GPSFixController.Mode.Offline.String
            case .Online: return Translation.GPSFixController.Mode.Online.String
            }
        }
    }
    
    weak var checkIn: CheckIn!
    weak var coreDataManager: CoreDataManager!
    
    init(checkIn: CheckIn, coreDataManager: CoreDataManager) {
        self.checkIn = checkIn
        self.coreDataManager = coreDataManager
    }
    
    // MARK: - Send All
    
    func sendAll(completion: @escaping (_ withSuccess: Bool) -> Void) {
        sendAll(success: {
            completion(true)
        }) { (error, gpsFixesLeft) in
            completion(false)
        }
    }
    
    fileprivate func sendAll(success: @escaping () -> Void, failure: @escaping (_ error: Error, _ gpsFixesLeft: Set<GPSFix>) -> Void) {
        sendAll(gpsFixesLeft: checkIn.gpsFixes as? Set<GPSFix> ?? [], success: success, failure: failure)
    }
    
    fileprivate func sendAll(
        gpsFixesLeft: Set<GPSFix>,
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error, _ gpsFixesLeft: Set<GPSFix>) -> Void)
    {
        guard gpsFixesLeft.count > 0 else { success(); return }
        sendSlice(gpsFixes: gpsFixesLeft, success: { (gpsFixesLeft) in
            self.sendAll(gpsFixesLeft: gpsFixesLeft, success: success, failure: failure)
        }) { (error, gpsFixesLeft) in
            failure(error, gpsFixesLeft)
        }
    }
    
    // MARK: - Send Slice
    
    func sendSlice(completion: @escaping (_ withSuccess: Bool) -> Void) {
        sendSlice(success: { (gpsFixesLeft) in
            completion(true)
        }) { (error, gpsFixesLeft) in
            completion(false)
        }
    }
    
    fileprivate func sendSlice(
        success: @escaping (_ gpsFixesLeft: Set<GPSFix>) -> Void,
        failure: @escaping (_ error: Error, _ gpsFixesLeft: Set<GPSFix>?) -> Void)
    {
        sendSlice(gpsFixes: checkIn.gpsFixes as? Set<GPSFix> ?? [], success: success, failure: failure)
    }
    
    fileprivate func sendSlice(
        gpsFixes: Set<GPSFix>,
        success: @escaping (_ gpsFixesLeft: Set<GPSFix>) -> Void,
        failure: @escaping (_ error: Error, _ gpsFixesLeft: Set<GPSFix>) -> Void)
    {
        guard gpsFixes.count > 0 else { success([]); return }
        let slicedGPSFixes = sliceGPSFixes(gpsFixes: gpsFixes)
        let gpsFixesLeft = gpsFixes.subtracting(slicedGPSFixes)
        sendSlice(
            gpsFixes: slicedGPSFixes,
            gpsFixesLeft: gpsFixesLeft,
            success: success,
            failure: failure
        )
    }
    
    fileprivate func sendSlice(
        gpsFixes: Array<GPSFix>,
        gpsFixesLeft: Set<GPSFix>,
        success: @escaping (_ gpsFixesLeft: Set<GPSFix>) -> Void,
        failure: @escaping (_ error: Error, _ gpsFixesLeft: Set<GPSFix>) -> Void)
    {
        log(info: "\(gpsFixes.count) GPS fixes will be sent and \(gpsFixesLeft.count) will be left")
        checkInRequestManager.postGPSFixes(gpsFixes: gpsFixes, success: { () in
            self.sendSliceSuccess(gpsFixes: gpsFixes, gpsFixesLeft: gpsFixesLeft, success: success)
        }) { (error) in
            self.sendSliceFailure(error: error, gpsFixesLeft: gpsFixesLeft, failure: failure)
        }
    }
    
    fileprivate func sendSliceSuccess(
        gpsFixes: Array<GPSFix>,
        gpsFixesLeft: Set<GPSFix>,
        success: @escaping (_ gpsFixesLeft: Set<GPSFix>) -> Void)
    {
        DispatchQueue.main.async(execute: {
            self.log(info: "Sending \(gpsFixes.count) GPS fixes was successful")
            self.postModeChangedNotification(mode: BatteryManager.sharedManager.batterySaving ? .BatterySaving : .Online)
            self.postSentGPSFixes(count: gpsFixes.count)
            self.coreDataManager.deleteObjects(objects: gpsFixes)
            self.coreDataManager.saveContext()
            self.log(info: "\(gpsFixes.count) GPS fixes deleted")
            success(gpsFixesLeft)
        })
    }
    
    fileprivate func sendSliceFailure(
        error: Error,
        gpsFixesLeft: Set<GPSFix>,
        failure: @escaping (_ error: Error, _ gpsFixesLeft: Set<GPSFix>) -> Void)
    {
        DispatchQueue.main.async(execute: {
            self.log(info: "Sending GPS fixes failed: \(error.localizedDescription)")
            self.postModeChangedNotification(mode: AFNetworkReachabilityManager.shared().isReachable ? .Error : .Offline)
            failure(error, gpsFixesLeft)
        })
    }
    
    // MARK: - Notifications
    
    fileprivate func postModeChangedNotification(mode: Mode) {
        let userInfo = [UserInfo.Mode: mode.rawValue]
        let notification = Notification(name: Notification.Name(rawValue: NotificationType.ModeChanged), object: self, userInfo: userInfo)
        NotificationQueue.default.enqueue(notification, postingStyle: .asap)
    }

    fileprivate func postSentGPSFixes(count: Int) {
        let userInfo = [UserInfo.Sent: [UserInfo.SentKey.Count: count]]
        let notification = Notification(name: Notification.Name(rawValue: NotificationType.SentGPSFixes), object: self, userInfo: userInfo)
        NotificationQueue.default.enqueue(notification, postingStyle: .asap)
    }

    // MARK: - Properties
    
    fileprivate lazy var checkInRequestManager: CheckInRequestManager = {
        return CheckInRequestManager(baseURLString: self.checkIn.serverURL)
    }()
    
    // MARK: - Helper
    
    fileprivate func log(info: String) {
        logInfo(name: "\(self.description)", info: info)
    }
    
    fileprivate func sliceGPSFixes(gpsFixes: Set<GPSFix>) -> Array<GPSFix> {
        return Array(gpsFixes.sorted { (gpsFix1, gpsFix2) -> Bool in gpsFix1.timestamp < gpsFix2.timestamp }.prefix(GPSFixSending.SliceSize))
    }
    
}
