//
//  APIManager.swift
//  SAPTracker
//
//  Created by computing on 24/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation
import CoreData

class APIManager: NSObject {
    
    struct NotificationType {
        static let networkAvailabilityChanged = "networkAvailabilityChanged"
    }
    
    private struct SyncPeriod {
        /* Normal rate of upload is every 3s */
        static let Normal: NSTimeInterval = 3
        
        /* To save battery, upload rate is lowered to every 30s */
        static let BatterySaving: NSTimeInterval = 30
    }
    
    /* Constants */
    struct Constants {
        /* Max number of fix objects to be sent per POST request. */
        static let maxSendGPSFix = 100
    }
    
    /* Server URL */
    private var serverUrlString: String?
    
    /* Base url of all requests, contains serverUrlString */
    private let baseUrlString = "/api/v1"
    
    /* AFNetworking operation manager */
    private var manager: AFHTTPRequestOperationManager?
    
    /* Number of seconds between syncs */
    private var syncPeriod: NSTimeInterval {
        get {
            return BatteryManager.sharedManager.batterySaving ? SyncPeriod.BatterySaving : SyncPeriod.Normal
        }
    }
    
    var networkAvailable: Bool {
        get {
            if manager == nil {
                return false
            }
            return !manager!.operationQueue.suspended
        }
    }
    
    /* Singleton */
    class var sharedManager: APIManager {
        struct Singleton {
            static let sharedAPIManager = APIManager()
        }
        return Singleton.sharedAPIManager
    }
    
    /* Sets base URL for all requests. Request and response are JSON. Starts reachability listener. Starts timer for uploading data. */
    func initManager(serverUrlString: String) {
        if self.serverUrlString == serverUrlString {
            return
        }
        
        manager = AFHTTPRequestOperationManager(baseURL: NSURL(string: serverUrlString)) // baseUrl needed for checking reachability
        
        // encode/decode body as JSON
        manager!.requestSerializer = AFJSONRequestSerializer() as AFHTTPRequestSerializer
        manager!.responseSerializer = AFJSONResponseSerializer() as AFHTTPResponseSerializer
        
        // set up reachability
        let operationQueue = manager!.operationQueue
        manager!.reachabilityManager.setReachabilityStatusChangeBlock({(AFNetworkReachabilityStatus status) -> Void in
            switch (status) {
            case AFNetworkReachabilityStatus.ReachableViaWWAN, AFNetworkReachabilityStatus.ReachableViaWiFi:
                operationQueue?.suspended = false
                break
            default:
                operationQueue?.suspended = true
            }
            
            // send notification (e.g. to tracker view controller)
            let notification = NSNotification(name: NotificationType.networkAvailabilityChanged, object: self)
            NSNotificationQueue.defaultQueue().enqueueNotification(notification, postingStyle: NSPostingStyle.PostASAP)
        })
        manager!.reachabilityManager.startMonitoring()
        
        // TODO: only one operation at a time?
        //manager!.operationQueue.maxConcurrentOperationCount = 0
        
        // start timer
        timer()
    }
    
    // MARK: - REST API
    
    /* Get event */
    func getEvent(eventId: String!, success: (AFHTTPRequestOperation!, AnyObject!) -> Void, failure: (AFHTTPRequestOperation!, AnyObject!) -> Void) {
        let urlString = baseUrlString + "/events/\(eventId)"
        manager!.GET(urlString, parameters: nil, success: success, failure: failure)
    }
    
    /* Get leader board */
    func getLeaderBoard(leaderBoardName: String!, success: (AFHTTPRequestOperation!, AnyObject!) -> Void, failure: (AFHTTPRequestOperation!, AnyObject!) -> Void) {
        let urlString = baseUrlString + "/leaderboards/\(leaderBoardName)"
        manager!.GET(urlString, parameters: nil, success: success, failure: failure)
    }
    
    /* Get competitor */
    func getCompetitor(competitorId: String!, success: (AFHTTPRequestOperation!, AnyObject!) -> Void, failure: (AFHTTPRequestOperation!, AnyObject!) -> Void) {
        let urlString = baseUrlString + "/competitors/\(competitorId)"
        manager!.GET(urlString, parameters: nil, success: success, failure: failure)
    }
    
    /* Map a device to competitor. */
    func checkIn(leaderBoardName: String!, competitorId: String!, deviceUuid: String!, pushId: String!, fromMillis: Int!, success: (AFHTTPRequestOperation!, AnyObject!) -> Void, failure: (AFHTTPRequestOperation!, AnyObject!) -> Void) {
        
        let urlString = baseUrlString + "/leaderboards/\(leaderBoardName)/device_mappings/start"
        
        var body = [String: AnyObject]()
        body["competitorId"] = competitorId
        body["deviceUuid"] = deviceUuid
        body["pushId"] = pushId
        body["fromMillis"] = fromMillis
        
        manager!.POST(urlString, parameters: body, success: success, failure: failure)
    }
    
    /* Disconnect a device from competitor. */
    func checkOut(leaderBoardName: String!, competitorId: String!, deviceUuid: String!, toMillis: Int!, success:(AFHTTPRequestOperation!, AnyObject!) -> Void, failure: (AFHTTPRequestOperation!, AnyObject!) -> Void) {
        
        let urlString = baseUrlString + "/leaderboards/\(leaderBoardName)/device_mappings/end"
        
        var body = [String: AnyObject]()
        body["competitorId"] = competitorId
        body["deviceUuid"] = deviceUuid
        body["toMillis"] = toMillis
        
        manager!.POST(urlString, parameters: body, success: success, failure: failure)
    }
    
    /* Send GPS location to server. Delete row from cache. */
    private func postGPSFixes(deviceUuid: String!, gpsFixes: [GPSFix]!) {
        if gpsFixes.count == 0 {
            return
        }
        
        let urlString = baseUrlString + "/gps_fixes"
        
        var body = [String: AnyObject]()
        body["deviceUuid"] = deviceUuid
        var array: [[String: AnyObject]] = []
        for gpsFix in gpsFixes {
            array.append(gpsFix.dictionary())
        }
        body["fixes"] = array
        
        manager!.POST(urlString, parameters: array, success: { (AFHTTPRequestOperation operation, AnyObject responseObject) -> Void in
            // delete GPS fixes from database
            for gpsFix in gpsFixes {
                println("sent GPS fixes")
                DataManager.sharedManager.managedObjectContext!.deleteObject(gpsFix)
            }
            }, failure: { (AFHTTPRequestOperation operation, NSError error) -> Void in
                println("error sending GPS fixes")
        })
    }
    
    // MARK: - Timer
    
    /* See if any rows need to be uploaded. Schedule timer again. */
    func timer() {
        let loop = NSTimer.scheduledTimerWithTimeInterval(syncPeriod, target:self, selector:"timer", userInfo:nil, repeats:false)
        NSRunLoop.currentRunLoop().addTimer(loop, forMode:NSRunLoopCommonModes)
        
        if (manager != nil && !manager!.operationQueue.suspended) {
            let deviceUuid = DeviceUDIDManager.UDID
            let lastestGPSFixes = DataManager.sharedManager.latestLocations()
            if lastestGPSFixes.count > 0 {
                postGPSFixes(deviceUuid, gpsFixes: lastestGPSFixes)
            }
        }
    }

}