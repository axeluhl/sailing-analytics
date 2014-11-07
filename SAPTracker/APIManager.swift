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
    
    /* Base url of all requests */
    private var baseUrlString: String?
    
    /* AFNetworking operation manager */
    private var manager: AFHTTPRequestOperationManager?
    
    /* Date of last GPS position upload */
    var lastSync: NSDate?
    
    /* Number of seconds between syncs */
    private var syncPeriod: NSTimeInterval = 3
    
    var loop: NSTimer?
    
    var backgroundTaskIdentifier: UIBackgroundTaskIdentifier?
    
    /* REST Paths */
    /* Map device to competitor */
    private let postDeviceMapping = "/sailingserver/rc/racelog"
    
    /* Send location to server */
    private let postGPSFixPath = "/tracking/recordFixesFlatJson"
    
    /* Singleton */
    class var sharedManager: APIManager {
        struct Singleton {
            static let sharedAPIManager = APIManager()
        }
        return Singleton.sharedAPIManager
    }
    override init() {
        super.init()
        
        // TODO: move to initManager
        uploadData()
    }

    /* Sets base URL for all requests. Request and response are JSON. Starts reachability listener. Starts timer for uploading data. */
    func initManager(baseUrlString: String) {
        if self.baseUrlString == baseUrlString {
            return
        }
        
        self.baseUrlString = baseUrlString;
        
        manager = AFHTTPRequestOperationManager(baseURL: NSURL(string: baseUrlString)) // baseUrl needed for checking reachability
        
        // encode body as JSON
        manager!.requestSerializer = AFJSONRequestSerializer() as AFHTTPRequestSerializer
        manager!.responseSerializer = AFCompoundResponseSerializer() as AFHTTPResponseSerializer
        
        // set up reachability
        let operationQueue = manager!.operationQueue
        manager!.reachabilityManager.setReachabilityStatusChangeBlock({(AFNetworkReachabilityStatus status) -> Void in
            switch (status) {
            case AFNetworkReachabilityStatus.ReachableViaWWAN, AFNetworkReachabilityStatus.ReachableViaWiFi:
                operationQueue?.suspended = false;
                break;
            default:
                operationQueue?.suspended = true;
                
            }
        })
        manager!.reachabilityManager.startMonitoring()
        
        // TODO: only one operation at a time?
        //manager!.operationQueue.maxConcurrentOperationCount = 0
        
    }
    
    /* Send a device to competitor mapping. */
    func postDeviceMapping(qrcodeData: QRCodeData!, success: (AFHTTPRequestOperation!, AnyObject!) -> Void, failure: (AFHTTPRequestOperation!, AnyObject!) -> Void) {
        
        // resource path contains query parameters, note QR code data is already URL encoded
        var urlString = baseUrlString! + postDeviceMapping
        urlString += "?leaderboard=\(qrcodeData.leaderBoard!)"
        urlString += "&raceColumn=\(qrcodeData.raceColumn!)"
        urlString += "&fleet=\(qrcodeData.fleet!)"
        urlString += "&clientuuid=\(qrcodeData.competitor!)"
        
        var body = DeviceCompetitorMappingEvent(deviceId: DeviceUDIDManager.UDID, competitor: qrcodeData.competitor!, from: qrcodeData.from!, to: qrcodeData.to!).dictionary()
        
        manager!.POST(urlString, parameters: body, success: success, failure: failure)
    }
    
    /* Send GPS location to server. Delete row from cache. */
    func postGPSFix(gpsFix: GPSFix!, success: (AFHTTPRequestOperation!, AnyObject!) -> Void, failure: (AFHTTPRequestOperation!, AnyObject!) -> Void) {
        var urlString = baseUrlString! + postGPSFixPath
        manager!.POST(urlString, parameters: gpsFix.dictionary(), success: { (AFHTTPRequestOperation operation, AnyObject responseObject) -> Void in
            // delete GPS fix from database
            DataManager.sharedManager.managedObjectContext!.deleteObject(gpsFix)
            }, failure: { (AFHTTPRequestOperation operation, NSError error) -> Void in
                NSLog("failure")
        })
    }
    
    /* See if any rows need to be uploaded. Schedule timer again. */
    func uploadData() {
        NSLog("uploadData")
        let loop = NSTimer.scheduledTimerWithTimeInterval(syncPeriod++, target:self, selector:"uploadData", userInfo:nil, repeats:false);
        NSRunLoop.currentRunLoop().addTimer(loop, forMode:NSRunLoopCommonModes);
    }
}